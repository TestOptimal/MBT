package com.testoptimal.mscript.groovy;

public class ScriptRuntimeException extends Exception {
	private String scriptName;
	private String methodName;
	private int lineNum;
	private int colNum;

	public ScriptRuntimeException (String scriptName_p, String methodName_p, int lineNum_p, int colNum_p, String msg_p) {
		super (msg_p);
		this.scriptName = scriptName_p;
		this.methodName = methodName_p;
		this.lineNum = lineNum_p;
		this.colNum = colNum_p;
	}
	
	public int getLineNum() {
		return this.lineNum;
	}

	public int getColNum() {
		return this.colNum;
	}

	public String getScriptName() {
		return this.scriptName;
	}
	
	public String getMethodName() {
		return this.methodName;
	}
	
	public String toString() {
		return this.scriptName + "." + this.methodName + " (" + this.lineNum + "," + this.colNum + "): " + this.getMessage();
	}
}
