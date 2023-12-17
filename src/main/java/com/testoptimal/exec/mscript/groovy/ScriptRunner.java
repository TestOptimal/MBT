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

import com.testoptimal.server.config.Config;
import com.testoptimal.util.FileUtil;

import groovy.lang.Binding;
import groovy.lang.Script;
import groovy.util.GroovyScriptEngine;

public class ScriptRunner {
	private Binding binding = new Binding();
	private Script scriptExec;

	public ScriptRunner (String scriptName_p, String scriptText_p) throws Exception {
		String filePath = "work/" + FileUtil.cleanseFileName(scriptName_p) + ".gvy";
		FileUtil.writeToFile(Config.getRootPath() + filePath, scriptText_p);
		GroovyScriptEngine scriptEng = new GroovyScriptEngine("");
		
		this.scriptExec = scriptEng.createScript(filePath, this.binding).getClass().newInstance();
		scriptExec.setBinding(this.binding);
	}

	public void setVariable(String key_p, Object val_p) {
		this.binding.setProperty(key_p, val_p);
	}
	
	public Object getVariable (String key_p) {
		return this.binding.getVariable(key_p);
	}
	
	public Object callFunc (String fName_p) {
		return scriptExec.invokeMethod(fName_p, null);
	}
	
	public Binding getBinding() {
		return this.binding;
	}
	
	public Object evaluate (String script_p) {
		return this.scriptExec.evaluate(script_p);
	}
}
