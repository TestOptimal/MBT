package com.testoptimal.exec.sequencer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.testoptimal.exec.ExecutionDirector;
import com.testoptimal.exec.ExecutionSetting;
import com.testoptimal.exec.FSM.ModelMgr;
import com.testoptimal.exec.FSM.StateNetwork;
import com.testoptimal.exec.FSM.Transition;
import com.testoptimal.exec.exception.MBTAbort;
import com.testoptimal.exec.exception.MBTException;
import com.testoptimal.exec.mscript.MbtScriptExecutor;
import com.testoptimal.exec.navigator.SequencePath;
import com.testoptimal.exec.navigator.Sequencer;
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
	
	public SequencerBase (ExecutionDirector execDir_p) throws MBTAbort, Exception {
		this.execDir = execDir_p;
		this.execSetting = this.execDir.getExecSetting();
		this.scriptExec = this.execDir.getScriptExec();
		this.networkObj = this.execSetting.getNetworkObj();
		this.transGuard = new TransGuard(this.scriptExec, this.execSetting);
		this.scxmlNode = this.execSetting.getModelMgr().getScxmlNode();
		
//        StateNetwork networkObj = execSetting.getNetworkObj();
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
		logger.info("paths to cover: " + this.pathList.size());
		if (!this.pathList.isEmpty()) {
			this.curPathIdx = 0;
			this.curPath = this.pathList.get(0); 
			this.curPath.reset();
		}
	}
	
	@Override
	public Transition getNext() throws MBTAbort {
		if (this.curPath == null) return null;
		Transition trans = this.curPath.nextTrans();
		boolean newPath = (trans == null);
		if (newPath) {
			this.curPathIdx++;
			if (this.curPathIdx >= this.pathList.size()) return null;
			this.curPath = this.pathList.get(this.curPathIdx);
			this.curPath.reset();
			trans = this.curPath.nextTrans();
			this.transGuard.reset();
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
	
	public StateNetwork getNetworkObj() {
		return this.networkObj;
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
	
	abstract public List<SequencePath> genPathList() throws Exception;
}
