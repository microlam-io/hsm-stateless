package io.microlam.hsm.impl;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.microlam.hsm.Action;
import io.microlam.hsm.EntryExitAction;
import io.microlam.hsm.Event;
import io.microlam.hsm.Hsm;
import io.microlam.hsm.State;

public class BasicState<S, C> implements State<S, C> {

	private static Logger LOGGER = LoggerFactory.getLogger(BasicState.class);

	protected final BasicHsm<S, C> hsm;
	protected final String name;
	protected final String parent;
	protected final String initialChild;
	
	protected final Action<S, C> inner;
	protected final EntryExitAction<S, C> entry, exit;
	
	protected Set<String> descendents;

	public BasicState(BasicHsm<S, C> hsm, String name, String parent, String initialChild, EntryExitAction<S, C> entry, EntryExitAction<S, C> exit, Action<S, C> inner) {
		this.hsm = hsm;
		this.name = name;
		this.parent = parent;
		this.initialChild = initialChild;
		this.entry = entry;
		this.exit = exit;
		this.inner = inner;
	}

	
	public boolean isDescendentOf(String name) {
		if (parent != null) {
			if (parent.equals(name)) {
				return true;
			}
			else {
				return hsm.resolveState(parent).isDescendentOf(name);
			}
		}
		else {
			return false;
		}
	}

	@Override
	public State<S, C> initState(C context) {
		return goToLeaf(hsm.resolveState(initialChild), context);
	}
	
	
	public State<S,C> goToLeaf(State<S,C> child, C context) {
		if (child.getEntryAction() != null) {
			child.getEntryAction().process(null, context);
		}
		if (child.getChildInitialState() != null) {
			return goToLeaf(child.getChildInitialState(), null, context);
		}
		return child;
	}	
	
	State<S,C> goToLeaf(State<S,C> child, Event<S> event, C context) {
		if (child.getEntryAction() != null) {
			child.getEntryAction().process(null, context);
		}
		if (child.getChildInitialState() != null) {
			return goToLeaf(child.getChildInitialState(), event, context);
		}
		return child;
	}
	
	State<S,C> goToState(State<S,C> from, State<S,C> to, Event<S> event, C context) {
		LOGGER.debug("Go to state [{}] from state [{}]", to.getName(), from.getName());
		if (from == to) {
			if (from.getExitAction() != null) {
				from.getExitAction().process(event, context);
			}
			if (from.getEntryAction() != null) {
				from.getEntryAction().process(event, context);
			}
			LOGGER.debug("Same state [{}] so exit and enter", from.getName());
		}
		else if (to.isDescendentOf(from.getName())) {
			goToDescendent(from, to, event, context);
			LOGGER.debug("Go to descendent state [{}] from state [{}]", to.getName(), from.getName());
		}
		else if (from.isDescendentOf(to.getName())) {
			goToAscendent(from, to, event, context);
			if (to.getEntryAction() != null) {
				to.getEntryAction().process(event, context);
			}
			LOGGER.debug("Go to ascendent state [{}] from state [{}]", to.getName(), from.getName());
		}
		else {
			State<S, C> commonAscendent = goToCommonAscendentOf(from, to, event, context);
			LOGGER.debug("Go to common ascendent state [{}] of states [{}] and [{}]", commonAscendent.getName(), from.getName(), to.getName());
			goToDescendent(from, to, event, context);
			LOGGER.debug("Go to descendent state [{}] from state [{}]", to.getName(), commonAscendent.getName());
		}
		return to;
	}

	
	State<S,C> goToDescendent(State<S,C> from, State<S,C> to, Event<S> event, C context) {
		if ((from != to) && (to.getParentState() != null)) {
			goToDescendent(from, to.getParentState(), event, context);
			if (to.getEntryAction() != null) {
				to.getEntryAction().process(event, context);
			}
		}
		return to;
	}

	
	State<S,C> goToAscendent(State<S,C> from, State<S,C> to, Event<S> event, C context) {
		if (from.getExitAction() != null) {
			from.getExitAction().process(event, context);
		}
		if ((from != to) && (from.getParentState() != null)) {
			return goToAscendent(from.getParentState(), to, event, context);
		}
		return to;
	}
	
	
	State<S,C> goUpToAscendent(State<S,C> from, State<S,C> to, Event<S> event, C context) {
		if (from.getExitAction() != null) {
			from.getExitAction().process(event, context);
		}
		if (from.getParentState() == to) {
			return from;
		}
		if ((from.getParentState() != to) && (from.getParentState() != null)) {
			return goUpToAscendent(from.getParentState(), to, event, context);
		}
		return null;
	}


	State<S,C> goToCommonAscendentOf(State<S,C> from, State<S,C> to, Event<S> event, C context) {
		if (to.isDescendentOf(from.getName())) {
			return from;
		}
		if (from.getExitAction() != null) {
			from.getExitAction().process(event, context);
		}
		if (from.getParentState() != null) {
			return goToCommonAscendentOf(from.getParentState(), to, event, context);
		}
		return null;
	}

	@Override
	public String process(Event<S> event, C context) {
		if (initialChild != null) {
			State<S,C> leaf = goToLeaf(hsm.resolveState(initialChild), event, context);
			LOGGER.debug("Go to leaf state [{}] from state [{}]", leaf.getName(), getName());
			return leaf.process(event, context);
		}
		String result = inner.process(event, context);
		if (result != null) {
			State<S,C> newState = goToState(this, hsm.resolveState(result), event, context);
			return newState.process(event, context);
		}
		else {
			SourceResult sourceResult = callParentsInnerForSourceResult(hsm.resolveState(parent), event, context);
			result = sourceResult.result;
			if (result != null) {
				LOGGER.debug("Exit up to Source State [{}]:", sourceResult.source.getName());
				goUpToAscendent(this, sourceResult.source, event, context);
				goToState(sourceResult.source, hsm.resolveState(result), event, context);
				if (hsm.resolveState(result).getChildInitialState() != null) {
					State<S,C> leaf = goToLeaf(hsm.resolveState(result).getChildInitialState(), event, context);
					LOGGER.debug("Go to leaf state [{}] from state [{}]", leaf.getName(), hsm.resolveState(result).getName());
					return leaf.getName();
				}
				return result;
			}
		}
		LOGGER.debug("Ignoring... and staying on State [{}]", getName());
		return getName();
	}

	
	private SourceResult callParentsInnerForSourceResult(State<S, C> parent, Event<S> event, C context) {
		SourceResult  sourceResult = null;
		String result = parent.getInnerAction().process(event, context);
		if (result != null) {
			sourceResult = new SourceResult(parent, result);
		}
		if ((result == null) && (parent.getParentState() != null)) {
			sourceResult = callParentsInnerForSourceResult(parent.getParentState(), event, context);
			result = sourceResult.result;
		}
		else if (result == null) {
			sourceResult = new SourceResult(parent, result);
		}
		return sourceResult;
	}

	
//	private String callParentsInner(State<S, C> parent, Event<S> event, C context) {
//		String result = parent.getInnerAction().process(event, context);
//		if ((result == null) && (parent.getParentState() != null)) {
//			result = callParentsInner(parent.getParentState(), event, context);
//		}
//		return result;
//	}

	@Override
	public Hsm<S, C> getHsm() {
		return hsm;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public State<S, C> getParentState() {
		return hsm.resolveState(parent);
	}

	@Override
	public State<S, C> getChildInitialState() {
		return hsm.resolveState(initialChild);
	}

	@Override
	public EntryExitAction<S, C> getEntryAction() {
		return entry;
	}

	@Override
	public Action<S, C> getInnerAction() {
		return inner;
	}

	@Override
	public EntryExitAction<S, C> getExitAction() {
		return exit;
	}

	
	class SourceResult {
		final State<S,C> source;
		final String result;
		
		public SourceResult(State<S, C> source, String result) {
			this.source = source;
			this.result = result;
		}
	}

	
}
