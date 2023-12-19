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

package com.testoptimal.exec.navigator;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.testoptimal.exec.ExecutionDirector;
import com.testoptimal.exec.FSM.Action;
import com.testoptimal.exec.FSM.State;
import com.testoptimal.exec.FSM.Transition;
import com.testoptimal.exec.FSM.TravBase;
import com.testoptimal.exec.FSM.TravState;
import com.testoptimal.exec.FSM.TravTrans;
import com.testoptimal.exec.exception.MBTAbort;

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
		return seqConstructorMap.keySet().stream().map(s -> s).sorted().toList();	
	}
	
	private ExecutionDirector execDir;
	private Sequencer sequencer;
	private TravBase curTravObj;
	private Transition curTrans;
	private StopMonitor stopMonitor;

	public Navigator (ExecutionDirector execDir_p, String mbtMode_p) throws Exception {
		this.execDir = execDir_p;
		this.stopMonitor = new StopMonitor(this.execDir.getExecSetting(), this);
		this.sequencer = getSequencer(mbtMode_p, this.execDir);
		this.sequencer.prepToNavigate(this.stopMonitor);
		this.stopMonitor.start(this.sequencer);
	}
	
	public void navigate () throws MBTAbort {
		while (true) {
			this.curTrans = this.sequencer.getNext();
			if (this.curTrans == null) {
				break;
			}
			if (this.curTrans instanceof Action) {
				((Action) this.curTrans).run();
				continue;
			}
			State atState = (State)this.curTrans.getFromNode();
			State toState = (State) this.curTrans.getToNode();
			
			if (!atState.isSuperVertex()) {
				this.curTravObj = new TravState(atState, this.execDir);
				if (this.execDir.isAborted()) break;
				this.curTravObj.travRun();
			}

			this.curTravObj = new TravTrans(this.curTrans, this.execDir);
			if (this.execDir.isAborted()) break;
			this.curTravObj.travRun();
			
			if (toState.isSuperVertex()) {
				this.curTravObj = new TravState(toState, this.execDir);
				if (this.execDir.isAborted()) break;
				this.curTravObj.travRun();
			}
			
			boolean atFinal = toState.isModelFinal();
			if (this.sequencer.isEndingPath()) {
				this.curTravObj = new TravState(toState, this.execDir);
				if (this.execDir.isAborted()) break;
				this.curTravObj.travRun();
			}

			if (this.execDir.isAborted() || !this.stopMonitor.checkIfContinue(atFinal)) {
				break;
			}
		}
	}
	
	public TravBase getCurTravObj() {
		return this.curTravObj;
	}
	
	public int getPathCount() {
		return this.sequencer.getPathCount();
	}
	
	public Sequencer getSequencer() {
		return this.sequencer;
	}
	
	public StopMonitor getStopMonitor() {
		return this.stopMonitor;
	}
}
