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

package com.testoptimal.server.controller.helper;

import java.util.ArrayList;
import java.util.List;

import com.testoptimal.exec.ModelRunner;

public class SimpleSessionMgr extends SessionMgr {
	private ModelRunner MbtSession;
	
	@Override
	public void addMbtStarter(ModelRunner mbtStarter_p) {
		if (this.MbtSession!=null) {
			this.MbtSession.stopMbt();
		}
		this.MbtSession = mbtStarter_p;
	}

	@Override
	public List<ModelRunner> getMbtStarterForUserSession(String sessId_p) {
		List<ModelRunner> list = new ArrayList<ModelRunner>();
		if (this.MbtSession!=null) {
			list.add(this.MbtSession);
		}
		return list;
	}

	@Override
	public ModelRunner getMbtStarterForModel(String modelName_p, String sessId_p) {
		if (this.MbtSession!=null && this.MbtSession.getModelMgr().getModelName().equals(modelName_p)) {
			return this.MbtSession;
		}
		else return null;
	}

	@Override
	public ModelRunner getMbtStarterForMbtSession(String modelName_p, String mbtSessId_p) {
		if (this.MbtSession!=null && this.MbtSession.getMbtSessionID().equals(mbtSessId_p)) {
			return this.MbtSession;
		}
		else return null;
	}

	@Override
	public void closeModel(String modelName_p, String sessId_p) {
		this.MbtSession = null;
	}

	@Override
	public void closeModel(ModelRunner modelExec_p) {
		this.MbtSession = null;
	}

	@Override
	public void closeModelAll(String sessId_p) {
		this.MbtSession = null;
	}

}