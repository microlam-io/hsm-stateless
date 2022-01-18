package io.microlam.hsm;

public interface State<S, C> extends Action<S, C> {

	Hsm<S, C> getHsm();
	
	State<S, C> initState(C context);

	String getName();
	
	State<S, C> getParentState();
	
	State<S, C> getChildInitialState();

	boolean isDescendentOf(String name);

	EntryExitAction<S, C> getEntryAction();
	
	Action<S, C> getInnerAction();

	EntryExitAction<S, C> getExitAction();

}
