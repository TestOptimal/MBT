/***********************************************************************************************
 * Copyright (c) 2009-2024 TestOptimal.com
 *
 * This file is part of TestOptimal MBT.
 *
 * TestOptimal MBT is free software: you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *
 * TestOptimal MBT is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See 
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with TestOptimal MBT. 
 * If not, see <https://www.gnu.org/licenses/>.
 ***********************************************************************************************/

package com.testoptimal.exec.mscript.groovy;

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
