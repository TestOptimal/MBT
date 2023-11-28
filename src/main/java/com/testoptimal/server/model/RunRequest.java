package com.testoptimal.server.model;

import java.util.Map;

public class RunRequest {
	/**
	 * name of the model.
	 */
	public String modelName;
	
	/**
	 * For StateModel execution only.
	 */
	public String mbtMode;
	
	public String statDesc = "Run_" + (new java.util.Date()).toString();
	
	/**
	 * For StateModel executions:
	 *   <ul>
	 *   <li>generateOnly: true/false, if true only generate test cases/paths without executing scripts</li>
	 *   <li>stopMinute: stop condition - stop MBT execution after the specified number of minutes</li>
	 *   <li>stopTraversal: stop condition - stop MBT execution after the specified number of trasition traversals have been reached</li>
	 *   <li>stopTransCoveragePct: stop condition - stop MBT execution when the transition coverage has reached the specified percentage</li>
	 *   <li>stopReqCoveragePct: stop condition - stop MBT execution when the requirement coverage has reached the specified percentage</li>
	 *   <li>stopAtFinalOnly: stop execution at final states only</li>
	 *   <li>seqParams: additional sequencer parameters in the format of code=value, separate multiple settings with a semi-colon</li>
	 *   <li>threadNum: number of threads (virtual users)</li>
	 *   <li>threadDelay: number of milliseconds to wait between threads, used for load testing to control gradual ramping up load on AUT</li>
	 *   <li>batchID: batch id (int) to associate this execution to</li>
	 *   <li>batchGroup: the batch group to associate this execution to</li>
	 *   <li>seed: int to set the seed to be used by random number generator</li>
	 *   <li>initScript: groovy script to be executed before model execution. use this to set user variables. This overrides "Initialization Script" in model setting.</li>
	 *   <li>autoClose: true/false to set Auto Close model after model execution.</li>
	 *   <li>MCaseList: array of MCase names to execute the specified MCases</li>
	 *   <li>statDesc: to save execution stats to the database</li>
	 * 	 <li>markList: array of UIDs for states and/or transitions</li>
	 * 	 <li>debug: boolean true to enable debug - internal use only by IDE</li>
	 *   <li>breakpoints: array of UIDs for states and/or transitions, internal use only</li>
	 *   <li>submitEmail: email/id that submitted the model for execution</li>
	 *   <li>hostList: list of host names preferred to run the model</li>
	 *   <li>catCodes: list of catCode separated by comma to find runtime server to run the model</li>
	 *   </ul>
	 * 
	 */
	public Map<String, Object> options = new java.util.HashMap<>();
	
	public RunRequest (String modelName_p) {
		this.modelName = modelName_p;
	}
	
	public RunRequest() { }
}
