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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.testoptimal.exec.FSM.ModelMgr;
import com.testoptimal.exec.exception.MBTAbort;
import com.testoptimal.exec.exception.MBTException;
import com.testoptimal.exec.mscript.STEP;
import com.testoptimal.exec.mscript.StepMethod;
import com.testoptimal.scxml.ScxmlNode;
import com.testoptimal.util.FileUtil;

import groovy.lang.Binding;
import groovy.lang.Script;
import groovy.util.GroovyScriptEngine;

 
/**
 * one script engine for each model execution.
 * @author yxl01
 *
 */
public class GroovyEngine {
	private static Logger logger = LoggerFactory.getLogger(GroovyEngine.class);
	
	// keyed on modelName
	private Map<String, GroovyScriptEngine> scriptEngList = new java.util.HashMap<>();
	private Map<String, Binding> bindingList = new java.util.HashMap<>();
	private String threadName;
	private String scriptFolderPath;
	private String mainModelName;
	private ModelMgr modelMgr;
	private CompilerConfiguration conf;
	private boolean noOp = false;
	
	/**
	 * keyed model name.
	 */
	private Map<String, List<GroovyScriptExec>> gScriptExecMapList = new java.util.HashMap<>();
	private List<StepMethod> stepMethodList;
	private GroovyScriptExec mainScriptExec;
	
    public GroovyEngine (String threadName_p, ModelMgr modelMgr_p, String tempScriptFolder_p) {
    	this.mainModelName = modelMgr_p.getModelName();
    	this.modelMgr = modelMgr_p;
		this.threadName = threadName_p;
		this.scriptFolderPath = tempScriptFolder_p;
//    }
//    
//    public void init (ModelMgr modelMgr_p) throws IOException, ScriptRuntimeException {
//    	this.mainModelName = modelMgr_p.getModelName();
//        GroovyScriptEngine scriptEng = this.getScriptEngine(mainModelName);
        Binding binding = new Binding();
		bindingList.put(modelMgr_p.getModelName(), binding);
		for (ScxmlNode subScxml: modelMgr_p.getSubModelList()) {
			binding = new Binding();
			this.bindingList.put(subScxml.getModelName(), binding);
		}
    }
    
    public void setNoOp () {
    	this.noOp = true;
    }
    
    private GroovyScriptEngine getScriptEngine (String modelName_p) throws Exception {
        GroovyScriptEngine scriptEng = this.scriptEngList.get(modelName_p);
        if (scriptEng==null) {
        	String filePath = FileUtil.concatFilePath(this.scriptFolderPath, modelName_p);
        	scriptEng = new GroovyScriptEngine(filePath);
        	this.scriptEngList.put(modelName_p, scriptEng);
            this.conf = new CompilerConfiguration();
            this.conf.setParameters(true);
            scriptEng.setConfig(this.conf);
//			String s = "@ThreadInterrupt\r\n" + 
//					"import groovy.transform.ThreadInterrupt\r\n" + 
//					"\r\n" + 
//					"@TimedInterrupt(5)\r\n" + 
//					"import groovy.transform.TimedInterrupt\r\nSystem.out.println(\"*******xyz\")\r\nSystem.exit(1)";
//            FileUtil.writeToFile(filePath + "/SYS_SECURITY.gvy", s);
//			Script scriptExec = scriptEng.createScript("SYS_SECURITY.gvy", new Binding());
//			scriptExec.evaluate("SYS_SECURITY");
        }
        return scriptEng;
    }
    
    public void addSysProperty (String varName_p, Object varObj_p) {
    	this.bindingList.entrySet().stream().forEach(x -> x.getValue().setProperty(varName_p, varObj_p));
    }
    
    public void addGroovyScriptList (List<GroovyScript> list_p) throws Exception {
    	if (this.noOp) return;
    	
    	// load in reverse order so that TRIGGERS is loaded last
    	for (int i=list_p.size()-1; i>=0; i--) {
			this.addGroovyScript(list_p.get(i));
    	}
    }
    
    public void addGroovyScript (GroovyScript gScript_p) throws Exception {
    	if (this.noOp) return;

    	logger.info("loading groovy script " + gScript_p.getScriptName());
		String scriptName = gScript_p.getScriptName();
		if (scriptName.equalsIgnoreCase("TRIGGERS") && this.stepMethodList!=null && !this.stepMethodList.isEmpty()) {
			// resolve STEPs
			gScript_p = GroovyScript.resolveSteps (gScript_p, this.stepMethodList);
			gScript_p.saveScript(FileUtil.concatFilePath(this.scriptFolderPath, gScript_p.getModelName()));
		}
		
		String modelName = gScript_p.getModelName();
		Binding binding = this.bindingList.get(modelName);
//    	gScript_p.writeToFile(this.scriptFolderPath);
		GroovyScriptEngine scriptEng = this.getScriptEngine(modelName);
    	GroovyScriptExec gExec =  new GroovyScriptExec(scriptEng, gScript_p, binding);
    	if (this.mainScriptExec == null) {
    		this.mainScriptExec = gExec;
    	}
//    	binding.setProperty(scriptName, gExec.getScriptExec());
    	List<GroovyScriptExec> gExecList = this.gScriptExecMapList.get(modelName);
    	if (gExecList==null) {
    		gExecList = new java.util.ArrayList<>();
    		this.gScriptExecMapList.put(modelName, gExecList);
    	}
    	gExecList.add(gExec);
    	if (scriptName.equalsIgnoreCase("STEPS")) {
        	this.loadStepScript(gScript_p);
    	}
    }
    
    public boolean callTrigger (String modelName_p, String uid_p) throws MBTAbort, MBTException {
    	if (this.noOp) return false;

    	List<GroovyScriptExec> gExecList = this.gScriptExecMapList.get(modelName_p);
    	if (gExecList==null) return false;
    	for (GroovyScriptExec gExec: gExecList) {
    		try {
				if (gExec.runTrigger(uid_p)) {
					return true;
				}
    		}
    		catch (ScriptRuntimeException es) {
    			throw new MBTException(es.toString());
    		}
    		catch (Exception e) {
    			throw new MBTException(e.getMessage());
    		}
    	}
    	return false;
    }

    public Object evalExpr (String scriptName_p, String expr_p) throws Exception {
    	if (this.noOp) return null;

    	return this.mainScriptExec.evalExpr(scriptName_p, expr_p);
    }

    private void loadStepScript (GroovyScript gScript_p) throws ScriptRuntimeException {
        String scriptName = gScript_p.getScriptName();
        String modelName = gScript_p.getModelName();
//    	gScript_p.writeToFile(FileUtil.concatFilePath(this.scriptFolderPath, modelName));
    	try {
            Binding binding = this.bindingList.get(gScript_p.getModelName());
            Script script = this.getScriptEngine(modelName).createScript(gScript_p.getScriptFileName(), binding);
            //script = (Script) script.run();
//            if (binding!=null) {
//            	binding.setProperty("$" + scriptName, script);
//            }
            Method [] methods = script.getClass().getMethods();
            this.stepMethodList = new java.util.ArrayList<>();
            int count = 0;
            for (Method method: methods) {
               Annotation[] annotations = method.getAnnotations();
               for (Annotation annotation : annotations) {
                   if (annotation instanceof STEP) {
                	   this.stepMethodList.add(new StepMethod(modelName, scriptName, ((STEP) annotation).value(), method));
                	   count++;
                	   break;
                   }
               }
            }
    		logger.info("Loaded " + count + " step definitions from " + gScript_p.getScriptName());
    	}
    	catch (Exception e) {
    		ScriptRuntimeException ne = GroovyScriptExec.parseException(gScript_p.getScriptName(), e);
    		throw ne;
    	}
    }

    public void close() {
//    	this.groovyScriptMapList = null;
    }
    
}

 
