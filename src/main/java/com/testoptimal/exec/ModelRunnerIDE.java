package com.testoptimal.exec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.testoptimal.exec.FSM.ModelMgr;
import com.testoptimal.exec.FSM.State;
import com.testoptimal.exec.FSM.Transition;
import com.testoptimal.exec.exception.MBTAbort;
import com.testoptimal.server.controller.IdeSvc;
import com.testoptimal.server.controller.helper.SessionMgr;
import com.testoptimal.server.model.IdeMessage;
import com.testoptimal.server.model.ModelState;
import com.testoptimal.stats.TagExec;

public class ModelRunnerIDE extends ModelRunner {
	public static enum MsgType {warn, info, error};
	private static long MonitorStatsMillis = 5000;

	private boolean pauseNext = false;	
	private boolean debugMode = false;
	private List<String> breakPoints = new ArrayList<>();
	private long lastStatsMillis = System.currentTimeMillis();

	public ModelRunnerIDE(String sessionId_p, ModelMgr modelMgr_p) throws Exception {
		super(sessionId_p, modelMgr_p);
		this.execDir = new ExecutionDirector(this);
	}

	public void setBreakpoints (List<String> bpList_p) {
		this.breakPoints = bpList_p;
		this.pauseNext = false;
	}
	
	public void requestPause() {
		this.debugMode = true;
		this.pauseNext = true;
	}
	
	public void resumeRun () {
		this.debugMode = false;
		this.pauseNext = false;
		synchronized(this) {
			this.notify();
		}
	}

	public void resumeDebug () {
		this.debugMode = true;
		this.pauseNext = false;
		synchronized(this) {
			this.notify();
		}
	}
	
	public void stepOver () {
		this.debugMode = true;
		this.pauseNext = true;
		synchronized(this) {
			this.notify();
		}
	}

	public void stopMbt() {
		this.debugMode = false;
		super.stopMbt();
	}
	
	
	public void startMbt (boolean debug_p, String mbtMode_p, Map<String,Object> options_p) throws Exception, MBTAbort {
		this.sendMessage(MsgType.info, "starting model excution ...");
		this.debugMode = debug_p;
		this.pauseNext = this.debugMode;
		List<String> blist = (List<String>) options_p.get("breakpoints");
		if (blist==null) {
			this.breakPoints.clear();
		}
		else {
			this.breakPoints = blist;
		}
		super.startMbt(mbtMode_p, options_p);
    }

	public void sendMessage(MsgType type_p, String message_p) {
		IdeSvc.sendIdeMessage(this.httpSessId, new IdeMessage(type_p.name(), message_p, "MbtStarter.ExecAlert"));
		
	}

	@Override
	public void enterMbtStart() throws MBTAbort {
		this.sendMessage(MsgType.info, "MBT_START");
		
		ModelState m = new ModelState(this.modelMgr.getScxmlNode().getModelName(), this.httpSessId);
		IdeSvc.sendIdeData(this.httpSessId, "model.started", m);
	}

	@Override
	public void exitMbtStart() throws MBTAbort {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterMbtEnd() {
		this.sendExecStatus(true);
	}

	@Override
	public void exitMbtEnd() {
		try {
			logger.info ("MBT execution completed: " + this.modelMgr.getScxmlNode().getModelName());
		}
		catch (Exception e) {}
		IdeSvc.sendIdeData(this.httpSessId, "model.paused", "");
		
		this.sendMessage(MsgType.info, "MBT_END");
		
		ModelState m = new ModelState(this.modelMgr.getScxmlNode().getModelName(), this.httpSessId);
		IdeSvc.sendIdeData(this.httpSessId, "model.ended", m);
		this.sendExecStatus(true);
	}

	@Override
	public void mbtFailed() throws MBTAbort {
//		IdeSvc.sendIdeData(this.httpSessId, "model.paused", this.execDir.getSequenceNavigator().getCurTravObj().genPausedAt());

		List<TagExec> failList = this.execDir.getSequenceNavigator().getCurTravObj().getFailedTagChecks();
		List<String> retList = failList.stream().map(s -> s.getExecMsg()).collect(Collectors.toList());
    	IdeSvc.sendIdeMessage(this.httpSessId, new IdeMessage("error", retList.toString(), "MbtStarter.fail"));
		this.sendExecStatus(true);
	}

	@Override
	public void mbtErrored(MBTAbort e_p) {
//		IdeSvc.sendIdeData(this.httpSessId, "model.paused", this.execDir.getSequenceNavigator().getCurTravObj().genPausedAt());

		String errMsg = e_p.getMessage();
    	if (errMsg != null && errMsg.startsWith("java.lang.Exception:")) errMsg = errMsg.substring(21);
    	IdeSvc.sendIdeMessage(this.httpSessId, new IdeMessage("error", errMsg, "MbtStarter.error"));
		ModelState m = new ModelState(this.modelMgr.getScxmlNode().getModelName(), this.httpSessId);
		IdeSvc.sendIdeData(this.httpSessId, "model.ended", m);
	}

	@Override
	public void enterState(State stateObj_p) throws MBTAbort {
		this.sendMessage(MsgType.info, "Traversing state: " + stateObj_p.getStateId());

		String uid = stateObj_p.getStateNode().getUID();
		if (this.pauseNext || this.debugMode && this.breakPoints.indexOf(uid)>=0) {
			this.pauseNext = false;
			IdeSvc.sendIdeData(this.httpSessId, "model.paused", this.execDir.getSequenceNavigator().getCurTravObj().genPausedAt());
			synchronized (this) {
				try {
					this.wait();
				}
				catch (InterruptedException e) {
					throw new MBTAbort(e.getMessage());
				}
			}
		}
	}

	@Override
	public void exitState(State stateObj_p) throws MBTAbort {
		this.sendExecStatus(false);		
	}

	@Override
	public void enterTrans(Transition transObj_p) throws MBTAbort {
		this.sendMessage(MsgType.info, "Traversing transition: " + transObj_p.getEventId());
		String uid = transObj_p.getTransNode().getUID();
		if (this.pauseNext || this.debugMode && this.breakPoints.indexOf(uid)>=0) {
			this.pauseNext = false;
			IdeSvc.sendIdeData(this.httpSessId, "model.paused", this.execDir.getSequenceNavigator().getCurTravObj().genPausedAt());
			synchronized (this) {
				try {
					this.wait();
				}
				catch (InterruptedException e) {
					throw new MBTAbort(e.getMessage());
				}
			}
		}
	}

	@Override
	public void exitTrans(Transition transObj_p) throws MBTAbort {
		this.sendExecStatus(false);
	}
	
	@Override
	public void mbtAbort() {
    	IdeSvc.sendIdeMessage(this.httpSessId, new IdeMessage("error", "model execution aborted", "MbtStarter.abort"));
		ModelState m = new ModelState(this.modelMgr.getScxmlNode().getModelName(), this.httpSessId);
		IdeSvc.sendIdeData(this.httpSessId, "model.ended", m);

	}
	
	private void sendExecStatus(boolean force_p) {
		if (force_p || System.currentTimeMillis() - this.lastStatsMillis >= MonitorStatsMillis) {
			try {
				this.lastStatsMillis = System.currentTimeMillis();
				ExecutionStatus execInfo = this.execDir.getExecStat();
				IdeSvc.sendIdeData(this.httpSessId, "model.stats", execInfo);
			}
			catch (Exception e) {
				//
			}
		}
	}
}
