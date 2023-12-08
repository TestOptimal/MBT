package com.testoptimal.exec.FSM;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.testoptimal.exec.ExecutionDirector;
import com.testoptimal.exec.exception.MBTAbort;
import com.testoptimal.exec.exception.MBTException;
import com.testoptimal.scxml.StateNode;
import com.testoptimal.scxml.TransitionNode;
import com.testoptimal.stats.TagExec;
import com.testoptimal.stats.exec.ModelExec;

public class TravTrans extends TravBase {
	private static Logger logger = LoggerFactory.getLogger(TravTrans.class);

	private Transition curTrans = null;
	private TransitionNode curTransNode = null;
	private StateNode fromStateNode;

	public TravTrans (Transition travObj_p, boolean newPath_p, ExecutionDirector execDir_p) {
		super(execDir_p, newPath_p);
		this.curTrans = travObj_p;
		this.curTransNode = this.curTrans.getTransNode();
		this.fromStateNode = this.curTransNode.getParentStateNode();
	}

	@Override
	public boolean travRun() throws MBTAbort {
		this.execDir.getExecListener().enterTrans(this.curTrans);
		this.stopMonitor.getTransCoverage().addTravTrans(this.curTrans);
		long startMillis = System.currentTimeMillis();
		this.execTrans();
		if (this.hasFailed() && !this.execDir.isGenOnly()) {
			this.execDir.trigerMBTAction(TravBase.TriggerType.fail);
		}
		long perfMillis = System.currentTimeMillis() - startMillis;
		ModelExec statsCollector = this.execDir.getExecStats();
		statsCollector.addTestStepTrans(perfMillis, this.curTransNode.getUID(),  this.tagExecList);
		this.execDir.getExecListener().exitTrans(this.curTrans);
		return !this.hasFailed();
	}
	
	private void execTrans () throws MBTAbort {
//		String dsName = this.realTransNode.getDataSet();
//		if (!StringUtil.isEmpty(dsName)) {
//			DataSet ds = this.scriptExec.getDataMgr().getDataSet(dsName);
//			ds.setRowIndex(this.stopMonitor.getTransCoverage().getTravCount(this.realTransObj.getTransNode().getUID()) - 1); // rowIndex is 0-based
//		}

		// execute additional script function
//		if (this.stepObj!=null && this.stepObj.funcBefore!=null) {
//			try {
//				this.stepObj.funcBefore.call(this.curTransNode);
//			}
//			catch (Exception e) {
//				this.addTagExec(new TagExec(this.scriptExec, null, false, e.getMessage(), "", this.fromStateNode.getStateID(), this.curTransNode.getEvent(), this.curTransNode.getUID()));
//			}
//		}
		
		try {
			if (!this.execDir.isGenOnly()) {
				scriptExec.getGroovyEngine().callTrigger(this.curTransNode.modelName(), "ALL_TRANS");
				scriptExec.getGroovyEngine().callTrigger(this.curTransNode.modelName(), this.curTransNode.getUID());
			}
		}
		catch (MBTException e) {
			this.tagExecList.add(new TagExec( this.scriptExec, "Error", false, 
					e.getMessage(), null, this.curTransNode.getParentStateNode().getStateID(), this.curTrans.getEventId(),this.getCurUID()));
		}
		
//		// execute additional script function
//		if (this.stepObj!=null && this.stepObj.funcAfter!=null) {
//			try {
//				this.stepObj.funcAfter.call(this.curTransNode);
//			}
//			catch (Exception e) {
//				this.addTagExec(new TagExec(this.scriptExec, null, false, e.getMessage(), "", this.fromStateNode.getStateID(), this.curTransNode.getEvent(), this.curTransNode.getUID()));
//			}
//		}
	}
//
//	private AsyncBase postTrans(Transition curTrans_p) throws Exception, MBTAbort {
//		if (this.execDir.getStopMonitor().checkIfContinue(false)) {
//			SequenceNavigator nav = this.execDir.getSequenceNavigator();
//			State aState = (State)curTrans_p.getToNode();
//			if (aState!=null) {
//				AsyncState asyncState = new AsyncState(aState, this.execDir);
//				return asyncState;
//			}
//		}
//		
//		this.execDir.getExecStatsCollector().doneTestCaseForVU();
//		return null;
//	}

	public Transition getTrans() {
		return this.curTrans;
	}

	public String genPausedAt () {
		return String.format("{ \"type\": \"transition\", \"uid\": \"%s\", \"desc\": \"%s\"}", this.curTransNode.getUID(), this.curTrans.getEventId());
	}

}
