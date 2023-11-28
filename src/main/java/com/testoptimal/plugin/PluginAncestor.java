package com.testoptimal.plugin;

import com.testoptimal.plugin.MScriptInterface.TO_PLUGIN;

/**
 * 
 * 
 * Abstract class, ancestor of all MBT plugins.
 * @author yxl01
 *
 */
@TO_PLUGIN
public abstract class PluginAncestor implements MScriptInterface {
	public abstract String getPluginID();

	public abstract String getPluginDesc();
	
	/**
	 * INTERNAL USE ONLY.
	 * This usually involves starting up AUT (app under testing).
	 * @throws Exception
	 */
	@NOT_MSCRIPT_METHOD
	public abstract void start() throws Exception;
	
	/**
	 * entry to the model initial state and final state
	 */
	@NOT_MSCRIPT_METHOD
	public void enterInitialState () {
		return;
	}

	/**
	 */
	@NOT_MSCRIPT_METHOD
	public void exitFinalState () {
		return;
	}
	
	/**
	 * INTERNAL USE ONLY.
	 * closes the AUT, disconnect from the external resource e.g. session to Selenium server, closing watij IE object, etc.
	 */
	@NOT_MSCRIPT_METHOD
	public abstract void close();
	
}
