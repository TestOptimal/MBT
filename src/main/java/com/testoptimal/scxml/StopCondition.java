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

package com.testoptimal.scxml;

import java.util.Map;

import com.testoptimal.util.StringUtil;

public class StopCondition implements Cloneable {
	/**
	 * Value of zero indicates no stopping criteria
	 */
	private Integer transCoverage;
	public Integer getTransCoverage() { 
		return this.transCoverage; 
	}

	private Integer transCount;
	/**
	 * returns the transition traversal count to stop the execution when exceeded.
	 * @return
	 */
	public Integer getTransCount() { 
		return this.transCount; 
	}
	
	private Integer elapseTime; // in minutes
	public Integer getElapseTime() { 
		return this.elapseTime; 
	}
	
	/**
	 * returns the number of minutes to stop the execution
	 * @return
	 */
	
	private Integer homeRunCount; 
	public Integer getHomeRunCount() { 
		return this.homeRunCount; 
	}
	
	private Integer reqCoverage; 
	public Integer getReqCoverage() { 
		return this.reqCoverage; 
	}
	
	private boolean stopAtFinalOnly = true;
	public boolean isStopAtFinalOnly () {
		return this.stopAtFinalOnly;
	}

	public void setOptions (Map<String, Object> options_p) {
		for (java.util.Map.Entry<String, Object> entry : options_p.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (value==null || StringUtil.isEmpty(value.toString())) continue;
			if (key.equalsIgnoreCase("stopMinute") || key.equalsIgnoreCase("stopMinutes") ||
				key.equalsIgnoreCase("stopElapseTime")) {
    			this.elapseTime = (int) value;
    		}
    		else if (key.equalsIgnoreCase("stopTraversal") || key.equalsIgnoreCase("stopTraversals")) {
    			this.transCount = (int) value;
    		}
    		else if (key.equalsIgnoreCase("stopTransCoveragePct")) {
    			this.transCoverage = (int) value;
    		}
    		else if (key.equalsIgnoreCase("stopReqCoveragePct")) {
    			this.reqCoverage = (int) value;
    		}
    		else if (key.equalsIgnoreCase("stopAtFinalOnly")) {
    			this.stopAtFinalOnly = (boolean) value;
    		}
		}
	}

	public StopCondition clone() throws CloneNotSupportedException {  
	   return (StopCondition) super.clone();  
	}
}
