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

package com.testoptimal.exec.sequencer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.testoptimal.exec.ExecutionDirector;
import com.testoptimal.exec.ExecutionSetting;
import com.testoptimal.exec.FSM.StateNetwork;
import com.testoptimal.exec.FSM.Transition;
import com.testoptimal.exec.exception.MBTAbort;
import com.testoptimal.exec.exception.MBTException;
import com.testoptimal.exec.mscript.MbtScriptExecutor;
import com.testoptimal.exec.navigator.SequencePath;
import com.testoptimal.exec.navigator.Sequencer;
import com.testoptimal.exec.navigator.StopMonitor;
import com.testoptimal.scxml.ScxmlNode;

public abstract class SequencerBase implements Sequencer {
	private static Logger logger = LoggerFactory.getLogger(SequencerBase.class);

	private ExecutionDirector execDir;
	private ExecutionSetting execSetting;
	private MbtScriptExecutor scriptExec;
	
	private List<SequencePath> pathList;
	private int curPathIdx = -1;
	private SequencePath curPath = null;
	private TransGuard transGuard;
	private int traversedTransCost = 5000;
	private StateNetwork networkObj;
	private ScxmlNode scxmlNode;
	protected StopMonitor stopMonitor;
	private int maxPaths;
	private int loopMax = 100;
	private int loopIdx = 0;
	
	public SequencerBase (ExecutionDirector execDir_p) throws Exception {
		this.execDir = execDir_p;
		this.execSetting = this.execDir.getExecSetting();
		this.scriptExec = this.execDir.getScriptExec();
		this.scxmlNode = this.execSetting.getModelMgr().getScxmlNode();
		this.networkObj = new StateNetwork ();
		this.networkObj.init(this.scxmlNode);
		this.transGuard = new TransGuard(this.scriptExec, this.execSetting, this.networkObj);
	}
	
	@Override
	public void prepToNavigate(StopMonitor stopMonitor_p) throws Exception {
		this.stopMonitor = stopMonitor_p;
		this.maxPaths = this.stopMonitor.getMacPaths();
		
        List<Transition> reqTransList = networkObj.getTransByUIDList(execSetting.getMarkList());
		if (!reqTransList.isEmpty()) {
			networkObj.getActiveTransList().stream().forEach(t -> {
				t.reset();
				t.setMinMaxCount(0, Integer.MAX_VALUE);
				if (this.traversedTransCost>0) t.setDist(this.traversedTransCost);
			});
	    	reqTransList.forEach(t-> t.setMinMaxCount(1, Integer.MAX_VALUE));
		}

		this.pathList = this.genPathList();
		if (this.pathList.size() >= this.maxPaths) {
			this.pathList.subList(0, this.maxPaths);
		}
		logger.info("paths to cover: " + this.pathList.size());
		this.curPathIdx = 0;
		this.curPath = this.pathList.get(0); 
		this.curPath.reset();
	}
	
	public int getMaxPaths() {
		return this.maxPaths;
	}
	
	@Override
	public StateNetwork getNetworkObj() {
		return this.networkObj;
	}
	
	@Override
	public Transition getNext() throws MBTAbort {
		Transition trans = this.curPath.nextTrans();
		boolean newPath = (trans == null);
		if (newPath) {
			this.curPathIdx++;
			if (this.curPathIdx >= this.pathList.size()) {
				this.loopIdx++;
				if (this.loopIdx >= this.loopMax) {
					return null;
				}
				this.curPathIdx = 0;
			}
			this.curPath = this.pathList.get(this.curPathIdx);
			this.curPath.reset();
			trans = this.curPath.nextTrans();
			this.transGuard.reset();
		}

		if (this.curPath.getCurTransIdx()==1) {
			this.scriptExec.setPathName(this.curPath.getPathDesc());
		}
		
		try {
			trans = this.transGuard.checkTrans(trans, this.curPath);
			return trans;
		}
		catch (MBTException e) {
			throw new MBTAbort(e.getMessage());
		}
	}
	
	public boolean isStartingPath() {
		return this.curPath.isStartingPath();
	}
	
	public boolean isEndingPath() {
		return this.curPath.isEndingPath();
	}

	@Override
	public int getPathCount() {
		return this.pathList.size();
	}
	
	public int getTraversedTransCost() {
		return this.traversedTransCost;
	}
	public MbtScriptExecutor getScriptExec() {
		return this.scriptExec;
	}
	public ExecutionSetting getExecSetting() {
		return this.execSetting;
	}
	public ScxmlNode getScxmlNode() {
		return this.scxmlNode;
	}
	
	public void setLoopMax (int loopMax_p) {
		this.loopMax = loopMax_p;
	}
	
	abstract public List<SequencePath> genPathList() throws Exception;
}
