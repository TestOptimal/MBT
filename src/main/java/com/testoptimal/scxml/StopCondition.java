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
