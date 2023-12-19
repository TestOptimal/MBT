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

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.testoptimal.exec.mscript.StepMethod;
import com.testoptimal.util.ArrayUtil;
import com.testoptimal.util.FileUtil;
import com.testoptimal.util.misc.SysLogger;

/**
 * groovy script source read in from the model folder.  This is not executable, it's used
 * to create GroovyScriptExec for execution.
 * 
 * @author yxl01
 *
 */
public class GroovyScript {
	private static Logger logger = LoggerFactory.getLogger(GroovyScript.class);
	private static String ScriptSuffix = ".gvy";
	
	private String modelName; // SYS or model name
	private String scriptName; // no file extension
	private String script;
	private int loadSeqNum = 1;
	private transient boolean readonly = false;
	public GroovyScript() {};
	
	public void setReadonly () {
		this.readonly = true;
	}
	
	public boolean isReadonly () {
		return this.readonly;
	}
	
	public static List<GroovyScript> readModelScript (String modelName_p, String folderPath_p) throws Exception {
		File [] scriptFileList = FileUtil.getFileList(folderPath_p);
		List<GroovyScript> retList = new java.util.ArrayList<>();
		for (File file: scriptFileList) {
			String fileName = file.getName();
			if (fileName.endsWith(".gvy")) {
				String scriptName = fileName.substring(0, fileName.length()-4);
				String script = FileUtil.readFile(file.getPath()).toString();
				GroovyScript gScript = new GroovyScript();
				gScript.modelName = modelName_p;
				gScript.scriptName = scriptName;
				gScript.script = resolveInclude(script);
				if (gScript.scriptName.equalsIgnoreCase("TRIGGERS")) {
					gScript.loadSeqNum = 10;
				}
				else if (gScript.scriptName.equalsIgnoreCase("PAGES")) {
					gScript.loadSeqNum = 20;
				}
				else if (gScript.scriptName.equalsIgnoreCase("STEPS")) {
					gScript.loadSeqNum = 30;
				}
				else gScript.loadSeqNum = 100;

				retList.add(gScript);
			}
		}
		retList.sort((GroovyScript s1, GroovyScript s2)-> s1.loadSeqNum - s2.loadSeqNum);
		return retList;
	}
	
	private static String resolveInclude (String script_p) {
		Pattern pattern = Pattern.compile("^(@INCLUDE_FILE[ \\t]*)([(]?['\"]+)(.*)(['\"]+[)]?)([;]?)$", Pattern.CASE_INSENSITIVE);
		StringBuffer retBuf = new StringBuffer();
		for (String line: script_p.split("\n")) {
			try {
				Matcher m = pattern.matcher(line);
				if (m.find()) {
					retBuf.append(FileUtil.readFile(m.group(3))).append("\n");
				}
				else {
					retBuf.append(line).append("\n");
				}
			}
			catch (Exception e) {
				retBuf.append(line).append("\n");
				logger.warn("unable to load " + line + ": " + e.getMessage());
			}
		}
		return retBuf.toString();
    }

	public void saveScript (String outFolderPath_p) throws ScriptRuntimeException {
		if (!this.readonly) {
			String filePath = FileUtil.concatFilePath(outFolderPath_p, this.scriptName + ScriptSuffix);
			try {
				FileUtil.writeToFile(filePath, this.script);
			}
			catch (Exception e) {
				String errMsg = e==null?"Nullpointer": e.getMessage();
				throw new ScriptRuntimeException (this.scriptName, "", 0, 0, "Failed to write script to file " + filePath + ": " + errMsg);
			}
		}
	}
	
	public String getModelName() {
		return this.modelName;
	}
	
	public String getScriptName() {
		return this.scriptName;
	}

	public String getScriptFileName () {
		return this.scriptName + ScriptSuffix;
	}

	public String getScript () {
		return this.script;
	}
	
	public int getLoadSeqNum () {
		return this.loadSeqNum;
	}

	/**
	 * STEP statement must starts with ">>" followed by the step text
	 * @param gScript_p
	 * @param stepList_p
	 */
   public static GroovyScript resolveSteps (GroovyScript gScript_p, List<StepMethod> stepList_p) throws ScriptRuntimeException {
	   String scriptLines [] = gScript_p.getScript().split("\n");
	   String modelName = gScript_p.getModelName();
	   StringBuffer scriptBuf = new StringBuffer();
	   List<String> errList = new java.util.ArrayList<>();
	   int lineNum = 1;
	   Pattern ptn = Pattern.compile("^([ \\t]*)(>>[ \\t]*)(.*)$");
	   for (String line: scriptLines) {
		   Matcher m = ptn.matcher(line);
		   if (m.find()) {
			   String tabs = m.group(1);
			   String remainingText = m.group(3);
			   StepMethod method = StepMethod.findMethod (modelName, stepList_p, remainingText); 
			   if (method==null) {
				   errList.add("Step definition not found (line " + lineNum + "): " + remainingText);
			   }
			   else {
				   try {
					   line = tabs + method.genOutput(remainingText) + " // " + remainingText;
				   }
				   catch (Exception e) {
					   String msg = e.getMessage() + "(line " + lineNum + ")";
					   errList.add(msg);
					   SysLogger.logWarn(msg);
				   }
			   }
		   }
		   lineNum++;
		   scriptBuf.append(line).append("\n");
	   }
	   GroovyScript retGScript = new GroovyScript();
	   retGScript.modelName = modelName;
	   retGScript.scriptName = gScript_p.getScriptName();
	   retGScript.script = scriptBuf.toString();
	   retGScript.loadSeqNum = gScript_p.loadSeqNum;
	   if (errList.size()>0) {
		   throw new ScriptRuntimeException(gScript_p.getScriptName(), "STEP text", 0, 0, ArrayUtil.join(errList, ";"));
	   }
	   return retGScript;
   }

   public void setModelName (String modelName_p) {
	   this.modelName = modelName_p;
   }
}
