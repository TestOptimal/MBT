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

package com.testoptimal.stats.exec;

import com.testoptimal.scxml.StateNode;
import com.testoptimal.scxml.TransitionNode;

public class ExecStateTrans {
	public String UID;
	public String stateName;
	public String transName;
	public String type;
	public int minTravRequired;
	public Integer expectedMillis;
	public int passCount;
	public int failCount;
	public Integer avgMillis;
	public Integer minMillis;
	public Integer maxMillis;
	public int slowCount;
	
	public ExecStateTrans(StateNode state_p) {
		this.UID = state_p.getUID();
		this.stateName = state_p.getStateID();
		this.type = "state";
		this.minTravRequired = 1;
		this.expectedMillis = state_p.getMaxMillis();
		if (this.expectedMillis < 0 || this.expectedMillis >= 2147483647) {
			this.expectedMillis = null;
		}
	}

	public ExecStateTrans(TransitionNode trans_p) {
		this.stateName = trans_p.getParentStateNode().getStateID();
		this.transName = trans_p.getEvent();
		this.type = "trans";
		this.UID = trans_p.getUID();
		this.minTravRequired = trans_p.getTraverseTimes();
		this.expectedMillis = trans_p.getMaxMillis();
		if (this.expectedMillis < 0 || this.expectedMillis >= 2147483647) {
			this.expectedMillis = null;
		}

	}
}
