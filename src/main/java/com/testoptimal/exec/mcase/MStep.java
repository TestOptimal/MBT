package com.testoptimal.exec.mcase;

import com.testoptimal.plugin.MScriptInterface.IGNORE_INHERITED_METHOD;

import groovy.lang.Closure;

@IGNORE_INHERITED_METHOD
public class MStep {
	/**
	 * 
	 * default to navigate to, if set to false, it will just jump to the target state/trans.
	 * use this setting to cause the MCase to ends without navigating to final states.
	 */
	public boolean naveTo = true;
	public boolean isState = false;
	public String toID;
	public Closure<Object> funcBefore;
	public Closure<Object> funcAfter;
	
	public Object performBefore (Object [] args) throws Exception {
		if (this.funcBefore == null) {
			return null;
		}
		else {
			return this.funcBefore.call(this, args);
		}
	}

	public Object performAfter (Object [] args) throws Exception {
		if (this.funcAfter == null) {
			return null;
		}
		else {
			return this.funcAfter.call(this, args);
		}
	}
}