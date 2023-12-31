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

package com.testoptimal.exec;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.LoggerFactory;

import com.testoptimal.exec.FSM.DataSet;
import com.testoptimal.exec.FSM.ModelMgr;
import com.testoptimal.exec.FSM.TravBase;
import com.testoptimal.exec.exception.MBTAbort;
import com.testoptimal.exec.mscript.MScriptInterface.NOT_MSCRIPT_METHOD;
import com.testoptimal.exec.mscript.MbtScriptExecutor;
import com.testoptimal.exec.mscript.groovy.GroovyScript;
import com.testoptimal.exec.mscript.groovy.ScriptRuntimeException;
import com.testoptimal.exec.navigator.Navigator;
import com.testoptimal.exec.navigator.StopMonitor;
import com.testoptimal.scxml.ScxmlNode;
import com.testoptimal.server.config.Config;
import com.testoptimal.stats.StatsMgr;
import com.testoptimal.stats.exec.ModelExec;
import com.testoptimal.util.ScriptLogger;
import com.testoptimal.util.misc.JVMStatus;

/**
 * 
 * @author yxl01
 *
 */
public final class ExecutionDirector extends Thread {
	private static org.slf4j.Logger logger = LoggerFactory.getLogger(ExecutionDirector.class);

	private java.util.Date startDT;
	private java.util.Date endDT;
	
	private ScriptLogger mScriptLogger;
	
	private MbtScriptExecutor scriptExec;
	private List<GroovyScript> gScriptList;
	private List<DataSet> dataSetList;
	
	private boolean aborted = false;
	
	private ModelRunner mbtSession;
	private Navigator navigator;
	
	public String getMbtSessionID() { 
		return this.mbtSession.getMbtSessionID(); 
	}
	private ExecListener execListener;
	
	private ExecutionSetting execSetting;
	
	private boolean genOnly = false;

	public ExecutionSetting getExecSetting() {
		return this.execSetting;
	}
	
	private ModelExec execStats;

	public ExecutionDirector (ExecListener execListener_p) {
		this.execListener = execListener_p;
	}
	
	public void prepStart (ModelRunner mbtSession_p) throws Exception, MBTAbort {
		this.startDT = new Date();
		
		this.execSetting = mbtSession_p.getExecSetting();
		this.execSetting.init();
		this.genOnly = this.execSetting.isGenOnly();
		
		String modelName = this.execSetting.getModelName();
		ModelMgr modelMgr = this.execSetting.getModelMgr();
		
		ScxmlNode scxmlNode = modelMgr.getScxmlNode();
		logger.info ("Starting model " + modelName);
		this.gScriptList = modelMgr.getScriptForExec(mbtSession_p.getMbtSessionID());
		this.dataSetList = modelMgr.getDataSetList();
		
		logger.info(">>> MBT Execution Started");
		logger.info(">>> Sequencer: " + this.execSetting.getCurMbtMode());
		logger.info(">>> License Edition: ProMBT");
		logger.info(">>> Model Version: " + scxmlNode.getVersion());
		logger.info(">>> Build Number: " + scxmlNode.getBuildNum());
		logger.info(">>> Tag Version: " + scxmlNode.getVersionReq());
		logger.info(">>> AUT Version: " + scxmlNode.getVersionAUT());
		JVMStatus jvmStatus = JVMStatus.getJVMStatus();
		logger.info(">>> JVM Heap Max: " + jvmStatus.getMemMaxName());
		logger.info(">>> OS Name: " + Config.getOsName());
		logger.info(">>> OS Version: " + Config.getOsVersion());
		logger.info(">>> JavaVersion: " + Config.getJavaVersion());
		logger.info(">>> TestOptimalVersion: " + Config.versionDesc);
		logger.info(">>> generateOnly: " + this.genOnly);
		this.mScriptLogger = new ScriptLogger(this.getExecSetting().getModelMgr().getTempFolderPath() + "script.log");

		this.mbtSession = mbtSession_p;
		this.execStats = new ModelExec (this.getMbtSessionID(), modelMgr, this.execSetting);
	}

	@Override
	public void run () {
		try {
			this.scriptExec = new MbtScriptExecutor(this);
			ModelMgr modelMgr = this.execSetting.getModelMgr();
			this.scriptExec.initGroovyMScript(modelMgr);
			logger.info("starting model " + modelMgr.getModelName());
			this.navigator = new Navigator(this, this.execSetting.getCurMbtMode());
	
			this.trigerMBTAction(TravBase.TriggerType.start);
			String msg = "Started model " + modelMgr.getModelName();
			logger.info(msg);
			
			this.navigator.navigate();
			
			this.execStats.complete();
			
			this.endDT = new java.util.Date();
			this.trigerMBTAction(TravBase.TriggerType.end);
			if (this.scriptExec!=null) {
				this.scriptExec.close();
				this.scriptExec = null;
			}
			logger.info("Model " + this.execSetting.getModelMgr().getModelName() + " has stopped");
			
			StatsMgr.getInstance().save(this.execStats);
			this.mScriptLogger.close();
		}
		catch (Throwable e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
			String errMsg = e instanceof ScriptRuntimeException? ((ScriptRuntimeException) e).toString(): e.getMessage();
			
			try {
				this.trigerMBTAction(TravBase.TriggerType.end);
			}
			catch (Throwable e2) {
				// ok
			}
			this.execListener.mbtErrored(new MBTAbort(errMsg));
			this.execListener.mbtAbort();
		}
	}	


	/**
	 * performs MBT actions. returns true if executed successfully, false if execution is noop.
	 */
    public boolean trigerMBTAction (TravBase.TriggerType mbtTriggerType_p) throws MBTAbort {
		String mainModelName = this.execSetting.getModelMgr().getModelName();
		boolean isExec = false;
		ModelMgr modelMgr = this.execSetting.getModelMgr();
		try {
			// only mbt_start/end triggers of submodels will be executed
			switch (mbtTriggerType_p) {
				case start:
					this.execListener.enterMbtStart();
					if (!this.genOnly) {
						for (ScxmlNode model: modelMgr.getSubModelList()) {
							isExec = isExec || this.scriptExec.getScriptEngine().callTrigger(model.getModelName(), "MBT_START");
						}
						isExec = isExec || this.scriptExec.getScriptEngine().callTrigger(mainModelName, "MBT_START");				
					}
					this.getExecListener().exitMbtStart();
					break;
				case end:
					this.execListener.enterMbtEnd();
					if (!this.genOnly) {
						for (ScxmlNode model: modelMgr.getSubModelList()) {
							isExec = isExec || this.scriptExec.getScriptEngine().callTrigger(model.getModelName(), "MBT_END");
						}
						isExec = isExec || this.scriptExec.getScriptEngine().callTrigger(mainModelName, "MBT_END");				
					}
					this.execListener.exitMbtEnd();
					break;
				case fail:
					if (!this.genOnly) {
						isExec = isExec || this.scriptExec.getScriptEngine().callTrigger(mainModelName, "MBT_FAIL");
					}
					this.execListener.mbtFailed();
					break;
			}
		}
		catch (Throwable t) {
			throw new MBTAbort(t.toString());
		}
		return isExec;
    }
	
	public void setAbort () {
		this.aborted = true;
		this.interrupt();
	}
	
	public boolean isAborted() {
		return this.aborted;
	}

	public List<GroovyScript> getScriptList() {
		return this.gScriptList;
	}
	
	public List<DataSet> getDataSetList() {
		return this.dataSetList;
	}
	

	public ModelExec getExecStats() {
		return this.execStats;
	}
	

	/**
	 * returns a mbt execution status object.
	 * @return
	 */
	public ExecutionStatus getExecStat() {
		ExecutionStatus execStatusObj = new ExecutionStatus(this);
		return execStatusObj;
	}
	
	/**
	 * <p>INTERNAL USE ONLY</p>
	 */
	@NOT_MSCRIPT_METHOD
	public void log (String msg_p) { 
		this.mScriptLogger.log(msg_p); 
	}

	/**
	 * <p>INTERNAL USE ONLY</p>
	 */
	@NOT_MSCRIPT_METHOD
	public ScriptLogger getMScriptLogger () { 
		return this.mScriptLogger; 
	}

	/**
	 * <p>INTERNAL USE ONLY</p>
	 */
	@NOT_MSCRIPT_METHOD
	public String getMScriptLogFilePath() { 
		return this.mScriptLogger.getLogFilePath(); 
	}
		

	public MbtScriptExecutor getScriptExec() {
		return this.scriptExec;
	}
	
	public Navigator getSequenceNavigator () {
		return this.navigator; //mainModelSeqNav;
	}
	
	public ExecListener getExecListener() {
		return this.execListener;
	}
	
	public long getElapseMillis () {
		if (this.startDT==null) return 0;
		else if (this.endDT==null) {
			return System.currentTimeMillis() - this.startDT.getTime();
		}
		else {
			return this.endDT.getTime() - this.startDT.getTime();
		}
	}
	
	public Date getStartTime() {
		return this.startDT;
	}
	
	public Date getEndTime() {
		return this.endDT;
	}
	
	public boolean isGenOnly() {
		return this.genOnly;
	}
}

