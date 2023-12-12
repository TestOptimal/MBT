package com.testoptimal.exec.FSM;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.testoptimal.exec.ExecutionDirector;
import com.testoptimal.exec.exception.MBTAbort;
import com.testoptimal.exec.exception.MBTException;
import com.testoptimal.scxml.StateNode;
import com.testoptimal.stats.TagExec;
import com.testoptimal.stats.exec.ModelExec;

public class TravState extends TravBase {
	private static Logger logger = LoggerFactory.getLogger(TravState.class);

	private State curState;
	private StateNode curStateNode;

	public TravState (State travObj_p, boolean newPath_p, ExecutionDirector execDir_p) {
		super(execDir_p, newPath_p);
		this.curState = travObj_p;
		this.curStateNode = this.curState.getStateNode();
	}

	@Override
	public boolean travRun() throws MBTAbort {
		this.execDir.getExecListener().enterState(this.curState);
		long startMillis = System.currentTimeMillis();
		ModelExec statsCollector = this.execDir.getExecStats();
		if (this.curState.isModelInitial()) {
			statsCollector.newTestCase();
		}
		this.execState();
		if (this.hasFailed()) {
			this.execDir.trigerMBTAction(TravBase.TriggerType.fail);
		}
		long perfMillis = System.currentTimeMillis() - startMillis;
		statsCollector.addTestStepState(perfMillis, this.curStateNode.getUID(), this.tagExecList);
		if (this.curState.isModelFinal()) {
			statsCollector.getCurTestCase().completeTestCase();
		}
		else {
			this.execDir.getExecListener().exitState(this.curState);
		}
		return !this.hasFailed();
	}
		
		
	private void execState () throws MBTAbort {
		try {
			if (!this.execDir.isGenOnly()) {
				String modelName = this.curState.getStateNode().modelName();
				this.scriptExec.getGroovyEngine().callTrigger(modelName, "ALL_STATES");
				this.scriptExec.getGroovyEngine().callTrigger(modelName, this.curStateNode.getUID());
			}
		}
		catch (MBTException e) {
			this.addTagExec(null, false, e.getMessage(), null);
		}			
	}

	public State getState() {
		return this.curState;
	}
	
	public String genPausedAt () {
		return String.format("{ \"type\": \"state\", \"uid\": \"%s\", \"desc\": \"%s\"}", this.curStateNode.getUID(), this.curState.getMarker());
	}

}
