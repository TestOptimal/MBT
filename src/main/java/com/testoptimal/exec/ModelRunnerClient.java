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

package com.testoptimal.exec;

import java.util.Map;

import com.testoptimal.exec.FSM.ModelMgr;
import com.testoptimal.exec.FSM.State;
import com.testoptimal.exec.FSM.Transition;
import com.testoptimal.exec.exception.MBTAbort;
import com.testoptimal.server.controller.helper.SessionMgr;

public class ModelRunnerClient extends ModelRunner {
	
	public ModelRunnerClient(String sessionId_p, ModelMgr modelMgr_p) throws Exception {
		super(sessionId_p, modelMgr_p);
		this.execDir = new ExecutionDirector(this);
	}
		
	public void startMbt (String mbtMode_p, Map<String,Object> options_p) throws Exception, MBTAbort {
		super.startMbt(mbtMode_p, options_p);
    }
	
	@Override
	public void stopMbt() {
		super.stopMbt();
	}
	

	@Override
	public void mbtFailed() throws MBTAbort {

	}

	@Override
	public void mbtErrored(MBTAbort e_p) {

	}
	
	@Override
	public void mbtAbort() {

	}


	@Override
	public void enterMbtStart() throws MBTAbort {
		
	}

	@Override
	public void exitMbtStart() throws MBTAbort {

	}

	@Override
	public void enterMbtEnd() {
		
	}

	@Override
	public void exitMbtEnd() {
		SessionMgr.getInstance().closeModel(this);
	}

	@Override
	public void enterState(State stateObj_p) throws MBTAbort {
		
	}

	@Override
	public void exitState(State stateObj_p) throws MBTAbort {

	}

	@Override
	public void enterTrans(Transition transObj_p) throws MBTAbort {
		
	}

	@Override
	public void exitTrans(Transition transObj_p) throws MBTAbort {

	}
}
