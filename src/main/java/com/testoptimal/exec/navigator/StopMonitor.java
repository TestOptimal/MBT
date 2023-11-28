package com.testoptimal.exec.navigator;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.testoptimal.exception.MBTAbort;
import com.testoptimal.exec.ExecutionDirector;
import com.testoptimal.exec.ExecutionSetting;
import com.testoptimal.exec.FSM.State;
import com.testoptimal.scxml.StopCondition;
import com.testoptimal.server.model.Requirement;

public class StopMonitor {
	private TraversalCount transCoverage;
	private TraversalCount stateCoverage;
	private TraversalCount reqmntCoverage;
	private long startedMillis;
	private boolean stopAtFinalOnly;
	
	private State homeState;
	private int totalPathCount;
	private AtomicInteger execPathCount = new AtomicInteger(0);
	private ExecutionDirector execDir;
	
	private StopCondition stopCond;
	private int stopTransCoverage;
	private int stopTransCount;
	private int stopReqCoverage;
	private int stopHomeRunCount;
	private int stopElapseMillis;

	
	public StopMonitor (Navigator nav_p, ExecutionDirector execDir_p) throws MBTAbort, Exception {
		this.execDir = execDir_p;
		ExecutionSetting execSetting = this.execDir.getExecSetting();
		this.startedMillis = execDir_p.getStartTime().getTime();
		this.stopCond = execSetting.getStopCondition();
		
		Map<String, Integer> reqmntMap = new java.util.HashMap<>();
		for (Requirement tag: execSetting.getModelMgr().getRequirementList()) {
			reqmntMap.put(tag.name, 1);
		}
		this.reqmntCoverage = new TraversalCount(reqmntMap);
		this.transCoverage = nav_p.getTravTransCount();
		this.stateCoverage = nav_p.getTravStateCount();
		this.homeState = execSetting.getNetworkObj().getHomeState();
		String curMbtMode = execSetting.getCurMbtMode();
		if (curMbtMode.equalsIgnoreCase("Random")) {
			this.totalPathCount = execDir_p.getSequenceNavigator().getPathCount();
		}
		
		this.stopAtFinalOnly = execSetting.isStopAtFinalOnly();
		this.stopTransCoverage = this.stopCond.getTransCoverage()==null?0: this.stopCond.getTransCoverage();
		this.stopTransCount = this.stopCond.getTransCount()==null?0: this.stopCond.getTransCount();
		this.stopReqCoverage = this.stopCond.getReqCoverage()==null?0: this.stopCond.getReqCoverage();
		this.stopHomeRunCount = this.stopCond.getHomeRunCount()==null?0: this.stopCond.getHomeRunCount();
		this.stopElapseMillis = this.stopCond.getElapseTime()==null?0: this.stopCond.getElapseTime()*60000;
	}
	
	public int getProgressPercent () {
		double completeRate = 0, rate;
		boolean atLeastOneCond = false;
		if (this.stopElapseMillis > 0) {
			rate = 1.0 * (System.currentTimeMillis() - this.startedMillis) / this.stopElapseMillis;
			completeRate = Math.max(completeRate, rate);
			atLeastOneCond = true;
		}
		if (this.stopHomeRunCount > 0 && this.stateCoverage!=null) {
			rate = 1.0 * this.stateCoverage.getTravCount(this.homeState.getStateNode().getUID()) / this.stopHomeRunCount;
			completeRate = Math.max(completeRate, rate);
			atLeastOneCond = true;
		}
		if (this.stopTransCount > 0 && this.transCoverage!=null) {
			rate = 1.0 * this.transCoverage.getTravCount() / this.stopTransCount;
			completeRate = Math.max(completeRate, rate);
			atLeastOneCond = true;
		}
		if (this.stopReqCoverage > 0 && this.reqmntCoverage!=null) {
			rate = 1.0 * this.reqmntCoverage.getCoveragePct() / this.stopReqCoverage;
			completeRate = Math.max(completeRate, rate);
			atLeastOneCond = true;
		}
		if (this.stopTransCoverage > 0 && this.transCoverage!=null) {
			rate = 1.0 * this.transCoverage.getCoveragePct() / this.stopTransCoverage;
			completeRate = Math.max(completeRate, rate);
			atLeastOneCond = true;
		}
		// default to 100% trans coverage if it's not one of the path sequencer. 
		// path based sequencers stops when it runs out of paths
		if (!atLeastOneCond && this.totalPathCount > 0) {
			rate = 1.0 * this.execPathCount.get() / this.totalPathCount ;
			completeRate = Math.max(completeRate, rate);
		}
		
		if (!atLeastOneCond) {
			// default to trans coverage
			rate = 1.0 * this.transCoverage.getCoveragePct() / 100;
			completeRate = Math.max(completeRate, rate);
		}
		return (int) Math.floor(completeRate * 100);
	}
	
	public void setTotalPathCount (int count_p) {
		this.totalPathCount = count_p;
	}
	
	/**
	 * returns true if the execution can continue, false if the execution should stop/abort.
	 * @return
	 */
	public boolean checkIfContinue (boolean atFinalState_p) {
		if (this.execDir.isInterrupted() || 
			this.getProgressPercent() >= 100) {
			if (this.stopAtFinalOnly && !atFinalState_p) {
				return true;
			}
			else return false;
		}
		else return true;
	}

	public TraversalCount getStateCoverage() {
		return this.stateCoverage;
	}
	
	public TraversalCount getTransCoverage() {
		return this.transCoverage;
	}
	
	public int addOneExecPath() {
		return this.execPathCount.incrementAndGet();
	}
	
}
