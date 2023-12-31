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

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.testoptimal.exec.FSM.ModelMgr;
import com.testoptimal.exec.exception.MBTAbort;
import com.testoptimal.scxml.ScxmlNode;
import com.testoptimal.server.controller.IdeSvc;
import com.testoptimal.server.model.IdeMessage;
import com.testoptimal.util.ArrayUtil;
import com.testoptimal.util.StringUtil;

public abstract class ModelRunner implements ExecListener {
	protected static Logger logger = LoggerFactory.getLogger(ModelRunner.class);

	protected String httpSessId;
	protected String mbtSessId;
	protected ExecutionDirector execDir;
	protected ModelMgr modelMgr;
	protected java.util.Date openTime;
	protected ExecutionSetting execSetting;
	
	public String getHttpSessionID() {
		return this.httpSessId;
	}
	public String getMbtSessionID() { return this.mbtSessId; }

	protected ModelRunner(String httpSessId_p, ModelMgr modelMgr_p) throws Exception {
		if (StringUtil.isEmpty(httpSessId_p)) throw new Exception ("Invalid (null) session id, logout and try again");
		this.httpSessId = httpSessId_p;
		this.mbtSessId = RandomStringUtils.randomAlphanumeric(10);
		this.modelMgr = modelMgr_p;

		if (modelMgr_p==null) {
			throw new Exception ("Model not found");
		}
		this.openTime = new java.util.Date();

	}
	

	public void startMbt (String mbtMode_p, Map<String, Object> options_p) throws Exception, MBTAbort {
    	if (this.isRunning()) {
			this.stopMbt();
			String abortMsg = "Aborted prevously running model execution.";
			IdeSvc.sendIdeMessage(this.httpSessId, new IdeMessage("warn", abortMsg, "MbtStarter.startMbt"));
			logger.error(abortMsg);
			IdeSvc.sendModelState(this.httpSessId, this.getModelMgr().getModelName());
			throw new MBTAbort(abortMsg);
    	}

		try {
			File folder = new File (this.modelMgr.getTempFolderPath());
			if (!folder.exists()) {
				folder.mkdir();
			}
			folder = new File (folder, this.mbtSessId);
			folder.mkdir();
			folder = new File (this.modelMgr.getReportFolderPath());
			if (!folder.exists()) {
				folder.mkdir();
			}
			folder = new File (this.modelMgr.getStatsFolderPath());
			if (!folder.exists()) {
				folder.mkdir();
			}
		}
		catch (Exception e) {
			logger.error("Failed setting model for execution " 
				+ this.modelMgr.getModelName(), e);
		}

    	this.execSetting = new ExecutionSetting(this.execDir, this.modelMgr, options_p);
		
		ScxmlNode scxmlNode = this.modelMgr.getScxmlNode();
		this.execSetting.setMbtMode(mbtMode_p==null?scxmlNode.getMbtNode().getMode(): mbtMode_p);

		List<String> valErr = scxmlNode.validateModel();
		if (!valErr.isEmpty()) {
			throw new MBTAbort ("Model error: " + ArrayUtil.join(valErr, ", "));
		}
		

		this.execDir.prepStart(this);
		this.execDir.start();
    }
	

	/**
	 * Stop the MBT execution.
	 */
	public void stopMbt() {
		this.execDir.setAbort();
		synchronized(this) {	
			this.notify();
		}
	}
	
	public ExecutionDirector getExecDirector() { 
		return this.execDir; 
	}
	
	/**
	 * returns the execution status
	 */
	public ExecutionStatus getExecStatus() {
		return this.execDir.getExecStat();
	}
	
	public ModelMgr getModelMgr() {
		return this.modelMgr;
	}
	
	public boolean isRunning () {
		return this.execDir != null && !this.execDir.isAborted() && this.execDir.isAlive();
	}

	public ExecutionSetting getExecSetting() {
		return this.execSetting;
	}
	
}
