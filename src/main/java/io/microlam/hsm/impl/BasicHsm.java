package io.microlam.hsm.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.microlam.hsm.Action;
import io.microlam.hsm.EntryExitAction;
import io.microlam.hsm.Event;
import io.microlam.hsm.Hsm;
import io.microlam.hsm.State;

public class BasicHsm<S, C> implements Hsm<S, C> {

	private static Logger LOGGER = LoggerFactory.getLogger(BasicHsm.class);

	protected final String name;
	protected final String initialState;
	protected Map<String, State<S, C>> states = new HashMap<>();
	protected State<S, C> resolvedInitState;

	public BasicHsm(String name, String initialState) {
		this.name = name;
		this.initialState = initialState;
	}
	
	public void registerState(State<S, C> state) {
		states.put(state.getName(), state);
	}
	
	@Override
	public State<S, C> resolveState(String name) {
		return states.get(name);
	}

	@Override
	public Hsm<S, C> getHsm() {
		return this;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public State<S, C> getParentState() {
		return resolveInitState().getParentState();
	}

	@Override
	public State<S, C> getChildInitialState() {
		return resolveInitState().getChildInitialState();
	}

	@Override
	public EntryExitAction<S, C> getEntryAction() {
		return resolveInitState().getEntryAction();
	}

	@Override
	public Action<S, C> getInnerAction() {
		return resolveInitState().getInnerAction();
	}

	@Override
	public EntryExitAction<S, C> getExitAction() {
		return resolveInitState().getExitAction();
	}
	
	private State<S,C> goToLeaf(State<S,C> child, Event<S> event, C context) {
		if (child.getEntryAction() != null) {
			child.getEntryAction().process(null, context);
		}
		if (child.getChildInitialState() != null) {
			return goToLeaf(child.getChildInitialState(), event, context);
		}
		return child;
	}
	
	@Override
	public String process(Event<S> event, C context) {
		State<S,C> leaf = goToLeaf(resolveState(initialState), event, context);
		LOGGER.debug("Go to leaf state [{}] from state [{}]", leaf.getName(), getName());
		return leaf.process(event, context);
	}

	public StateBuilder<S, C> stateBuilder() {
		return new StateBuilder<>(this);
	}
	
	@Override
	public State<S, C> initState(C context) {
		return goToLeaf(resolveState(initialState), context);
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

	
	private State<S, C> resolveInitState() {
		State<S, C> currentState;
		State<S, C>  childState;
		currentState = resolveState(initialState);
		childState = currentState;
		while(childState != null) {
			childState = currentState.getChildInitialState();
			if (childState != null) {
				currentState = childState;
			}
		}
		return currentState;
	}


	@Override
	public boolean isDescendentOf(String name) {
		//return resolveState(initialState).isDescendentOf(name);
		return false;
	}


}