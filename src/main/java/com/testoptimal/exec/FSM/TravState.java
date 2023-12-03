package com.testoptimal.exec.FSM;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.testoptimal.db.ModelExecDB;
import com.testoptimal.exception.MBTAbort;
import com.testoptimal.exception.MBTException;
import com.testoptimal.exec.ExecutionDirector;
import com.testoptimal.scxml.StateNode;
import com.testoptimal.stats.TagExec;

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
		ModelExecDB statsCollector = this.execDir.getExecStats();
		if (this.curState.isModelInitial()) {
			statsCollector.newTestCase();
		}
		this.execState();
		if (this.hasFailed()) {
			this.execDir.trigerMBTAction(TravBase.TriggerType.fail);
		}
		long perfMillis = System.currentTimeMillis() - startMillis;
		statsCollector.addTestStep(perfMillis, this.curStateNode.getUID(), this.tagExecList);
		if (this.curState.isModelFinal()) {
			statsCollector.getCurTestCase().completeTestCase();
		}
		else {
			this.execDir.getExecListener().exitState(this.curState);
		}
		return !this.hasFailed();
//		else {
//			String errMsg = this.abortException.getMessage();
//			if (errMsg==null || errMsg.equals("null")) {
//				errMsg = this.abortException.toString();
//			}
//			this.addTagExec(new TagExec(this.scriptExec, null, false, errMsg, "", this.realStateNode.getStateID(), null, this.realStateNode.getUID()));
//			throw this.abortException;
//		}

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
			this.tagExecList.add(new TagExec( this.scriptExec, "Error", false, 
					e.getMessage(), null, this.curState.getStateId(), null, this.getCurUID()));
		}			
	}

//	private TravBase postState (State curState_p) throws Exception, MBTAbort {
//		TravBase nextTravObj = null;
//		Navigator nav = this.execDir.getSequenceNavigator();
//		if (this.execDir.getStopMonitor().checkIfContinue(curState_p.isModelFinal())) {
////			if (!(nav instanceof SequenceNavigatorMarkov) && curState_p.isSuperVertex()) {
////				nextTravObj = new AsyncState (((State)curState_p.getAllTrans(false).get(0).getToNode()), this.execDir);
////			}
////			else {
//				nextTravObj = nav.nextTrav();
////			}
//		}
//		return nextTravObj;
//	}

	public State getState() {
		return this.curState;
	}
	
	public String genPausedAt () {
		return String.format("{ \"type\": \"state\", \"uid\": \"%s\", \"desc\": \"%s\"}", this.curStateNode.getUID(), this.curState.getMarker());
	}

}
