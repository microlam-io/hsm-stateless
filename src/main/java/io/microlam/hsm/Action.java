package io.microlam.hsm;

public interface Action<S, C> {

	String process(Event<S> event, C context);
	
	
}
