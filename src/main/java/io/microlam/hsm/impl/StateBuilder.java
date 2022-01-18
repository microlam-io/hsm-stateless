package io.microlam.hsm.impl;

import io.microlam.hsm.Action;
import io.microlam.hsm.EntryExitAction;
import io.microlam.hsm.Hsm;

public class StateBuilder<S, C> {

	BasicHsm<S, C> hsm;
	String name;
	String parent;
	String initialChild;
	
	Action<S, C> inner;
	EntryExitAction<S, C> entry, exit;
	
	public StateBuilder(BasicHsm<S, C> hsm) {
		this.hsm = hsm;
	}
	
	public StateBuilder<S, C> name(String name) {
		this.name = name;
		return this;
	}
	
	public StateBuilder<S, C> entry(EntryExitAction<S, C> entry) {
		this.entry = entry;
		return this;
	}

	public StateBuilder<S, C> exit(EntryExitAction<S, C> exit) {
		this.exit = exit;
		return this;
	}

	public StateBuilder<S, C> inner(Action<S, C> inner) {
		this.inner = inner;
		return this;
	}

	public StateBuilder<S, C> parent(String parent) {
		this.parent = parent;
		return this;
	}
	
	public StateBuilder<S, C> initialChild(String initialChild) {
		this.initialChild = initialChild;
		return this;
	}

	public StateBuilder<S, C> build() {
		hsm.registerState(new BasicState<S, C>(hsm, name, parent, initialChild, entry, exit, inner));
		return new StateBuilder<S, C>(hsm);
	}
	
	public Hsm<S, C> buildAndStop() {
		hsm.registerState(new BasicState<S, C>(hsm, name, parent,initialChild, entry, exit, inner));
		return hsm;
	}
}
