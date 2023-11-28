package com.testoptimal.exec.FSM;

import java.util.List;
import java.util.stream.Collectors;

import com.testoptimal.exception.MBTAbort;
import com.testoptimal.exec.ExecutionDirector;
import com.testoptimal.exec.navigator.StopMonitor;
import com.testoptimal.mcase.MStep;
import com.testoptimal.mscript.MbtScriptExecutor;
import com.testoptimal.stats.TagExec;

abstract public class TravBase {
	public static enum TriggerType {start, end, fail, state, trans};

	protected ExecutionDirector execDir;
	protected StopMonitor stopMonitor;
	protected MbtScriptExecutor scriptExec;
	protected List<TagExec> tagExecList = new java.util.ArrayList<>();
	protected MStep stepObj;
	private boolean newPathInd = false;
	
	public boolean isNewPath () {
		return this.newPathInd;
	}
	
	public State getCurState() {
		if (this instanceof TravState) {
			return ((TravState) this).getState();
		}
		else if (this instanceof TravTrans) {
			Transition trans = ((TravTrans) this).getCurTrans();
			return (State) trans.getFromNode();
		}
		else return null;
	}

	public Transition getCurTrans() {
		if (this instanceof TravState) {
			return null;
		}
		else if (this instanceof TravTrans) {
			return ((TravTrans) this).getTrans();
		}
		else return null;
	}

	public String getCurUID () {
		if (this instanceof TravState) {
			return ((TravState) this).getState().getStateNode().getUID();
		}
		else if (this instanceof TravTrans) {
			return ((TravTrans) this).getTrans().getTransNode().getUID();
		}
		else return null;
	}

	public TravBase (ExecutionDirector execDir_p, boolean newPath_p) {
		this.execDir = execDir_p;
		this.stopMonitor = this.execDir.getStopMonitor();
		this.scriptExec = this.execDir.getScriptExec();
		this.newPathInd = newPath_p;
	}
	
	public abstract boolean travRun() throws MBTAbort;
	
	public void addTagExec (TagExec tagExec_p) {
		this.tagExecList.add(tagExec_p);
	}
	
	public boolean hasFailed () {
		return this.tagExecList.stream().anyMatch(t -> !t.isPassed());
	}
	
	public List<TagExec> getFailedTagChecks () {
		return this.tagExecList.stream().filter(t -> !t.isPassed()).collect(Collectors.toList());
	}
	
	public void addSnapID (long snapMillis_p) {
		this.tagExecList.stream().forEach(t -> t.addSnapScreen(snapMillis_p));
	}
	
	public MStep getMStep () {
		return this.stepObj;
	}
	
	public void setMStep(MStep stepObj_p) {
		this.stepObj = stepObj_p;
	}
	
	public abstract String genPausedAt();
	
}