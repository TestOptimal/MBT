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
import com.testoptimal.scxml.TransitionNode;
import com.testoptimal.stats.exec.ModelExec;

public class TravTrans extends TravBase {
	private static Logger logger = LoggerFactory.getLogger(TravTrans.class);

	private Transition curTrans = null;
	private TransitionNode curTransNode = null;

	public TravTrans (Transition travObj_p, ExecutionDirector execDir_p) {
		super(execDir_p);
		this.curTrans = travObj_p;
		this.curTransNode = this.curTrans.getTransNode();
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
		try {
			if (!this.execDir.isGenOnly()) {
				scriptExec.getGroovyEngine().callTrigger(this.curTransNode.modelName(), "ALL_TRANS");
				scriptExec.getGroovyEngine().callTrigger(this.curTransNode.modelName(), this.curTransNode.getUID());
			}
		}
		catch (MBTException e) {
			this.addTagExec(null, false, e.getMessage(), null);
		}
	}

	public Transition getTrans() {
		return this.curTrans;
	}

	public String genPausedAt () {
		return String.format("{ \"type\": \"transition\", \"uid\": \"%s\", \"desc\": \"%s\"}", this.curTransNode.getUID(), this.curTrans.getEventId());
	}

}
