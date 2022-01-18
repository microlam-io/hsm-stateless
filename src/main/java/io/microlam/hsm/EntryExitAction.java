package io.microlam.hsm;

public interface EntryExitAction<S, C> {

	void process(Event<S> event, C context);
	
}
