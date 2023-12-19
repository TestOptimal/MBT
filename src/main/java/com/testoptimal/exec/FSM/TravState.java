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

package com.testoptimal.exec.FSM;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.testoptimal.exec.ExecutionDirector;
import com.testoptimal.exec.exception.MBTAbort;
import com.testoptimal.exec.exception.MBTException;
import com.testoptimal.scxml.StateNode;
import com.testoptimal.stats.exec.ModelExec;

import groovy.lang.Closure;

public class TravState extends TravBase {
	private static Logger logger = LoggerFactory.getLogger(TravState.class);

	private State curState;
	private StateNode curStateNode;

	public TravState (State travObj_p, ExecutionDirector execDir_p) {
		super(execDir_p);
		this.curState = travObj_p;
		this.curStateNode = this.curState.getStateNode();
	}

	@Override
	public boolean travRun() throws MBTAbort {
		this.execDir.getExecListener().enterState(this.curState);
		this.stopMonitor.getStateCoverage().addTravState(this.curStateNode.getUID());
		
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
