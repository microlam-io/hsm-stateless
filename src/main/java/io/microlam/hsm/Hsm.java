package io.microlam.hsm;

public interface Hsm<S, C> extends State<S, C> {
	
	State<S, C> resolveState(String name);
	
}
