package com.testoptimal.exec.navigator;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.testoptimal.exec.ExecutionDirector;
import com.testoptimal.exec.ExecutionSetting;
import com.testoptimal.exec.FSM.State;
import com.testoptimal.exec.FSM.Transition;
import com.testoptimal.exec.FSM.TravBase;
import com.testoptimal.exec.FSM.TravState;
import com.testoptimal.exec.FSM.TravTrans;
import com.testoptimal.exec.exception.MBTAbort;
import com.testoptimal.exec.mscript.MbtScriptExecutor;

public class Navigator {
	private static Map<String, Constructor> seqConstructorMap = new HashMap<>();
	public static Sequencer getSequencer (String seqMode_p, ExecutionDirector execDir_p) throws Exception {
    	Constructor constructor = seqConstructorMap.get(seqMode_p);
    	if (constructor == null) throw new Exception ("Sequencer not found " + seqMode_p);
    	Sequencer seqObj = (Sequencer) constructor.newInstance(execDir_p);
    	return seqObj;
	}
	public static void addSequencer (String seqMode_p, String classPath_p) throws Exception {
    	Class aClass = Class.forName(classPath_p);
    	Constructor constructor = aClass.getConstructor(ExecutionDirector.class);
		seqConstructorMap.put(seqMode_p, constructor);
	}
	public static List<String>  getSequencerList () {
		return seqConstructorMap.keySet().stream().map(s -> s).collect(Collectors.toList());	
	}
	
	private MbtScriptExecutor scriptExec;
	private ExecutionDirector execDir;
	private ExecutionSetting execSetting;
	private Sequencer sequencer;
	private TravBase curTravObj;
	private Transition curTrans;
	private TraversalCount travTransCount;
	private TraversalCount travStateCount;
	private StopMonitor stopMonitor;

	public Navigator (ExecutionDirector execDir_p, String mbtMode_p) throws Exception {
		this.execDir = execDir_p;
		this.scriptExec = this.execDir.getScriptExec();
		this.execSetting = this.execDir.getExecSetting();

		Map<String, Integer> transReqMap = new java.util.HashMap<>();
		for (Transition trans: execDir_p.getExecSetting().getNetworkObj().getAllRequiredTrans()) {
			transReqMap.put(trans.getTransNode().getUID(), trans.getMinTraverseCount());
		}
		this.travTransCount = new TraversalCount(transReqMap);
		this.travStateCount = new TraversalCount(new java.util.HashMap<>());
		this.sequencer = getSequencer(mbtMode_p, this.execDir);
	}
	
	public void navigate () throws MBTAbort {
		this.stopMonitor = this.execDir.getStopMonitor();
		
		while (true) {
			this.curTrans = this.sequencer.getNext();
			if (this.curTrans == null) break;
			
			State atState = (State)this.curTrans.getFromNode();
			State toState = (State) this.curTrans.getToNode();
			this.curTravObj = new TravState(atState, true, this.execDir);
			if (this.execDir.isAborted()) break;
			this.curTravObj.travRun();
			this.curTravObj = new TravTrans(this.curTrans, false, this.execDir);
			if (this.execDir.isAborted()) break;
			this.curTravObj.travRun();
			boolean atFinal = toState.isModelFinal();
			if (this.sequencer.isEndingPath()) {
				this.curTravObj = new TravState(toState, true, this.execDir);
				if (this.execDir.isAborted()) break;
				this.curTravObj.travRun();
			}

			if (!this.stopMonitor.checkIfContinue(atFinal)) {
				break;
			}
		}
	}
	
	
	public TraversalCount getTravStateCount() {
		return this.travStateCount;
	}
	
	public TraversalCount getTravTransCount() {
		return this.travTransCount;
	}
	
	public TravBase getCurTravObj() {
		return this.curTravObj;
	}
	
	public int getPathCount() {
		return this.sequencer.getPathCount();
	}
}
