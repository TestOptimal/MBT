package com.testoptimal.server.model;

public class TestCmd {
	public static enum Type { MBT_START, MBT_END, STATE, TRANSITION, FAIL, ERROR, ABORT, TIMEOUT }
	public Type type;
	public String message1;
	public String message2;
	public TestCmd (Type type, String state, String trans) {
		this.type = type;
		this.message1 = state;
		this.message2 = trans;
	}
}