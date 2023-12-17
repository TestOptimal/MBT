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

package com.testoptimal.server.model;

import com.testoptimal.exec.ModelRunner;
import com.testoptimal.server.controller.helper.SessionMgr;

// IDE model state
public class ModelState {
	public String modelName;
	public boolean running = false;
	public String mbtSessID;
	
	public ModelState (String modelName_p, String sessId_p) {
		this.modelName = modelName_p;
		ModelRunner sess = SessionMgr.getInstance().getMbtStarterForModel(modelName_p, sessId_p);
		if (sess!=null) {
			this.running =  sess.isRunning();
			this.mbtSessID = sess.getMbtSessionID();
		}
	}
}
