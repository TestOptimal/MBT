package com.testoptimal.exec;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.testoptimal.exec.FSM.ModelMgr;
import com.testoptimal.exec.FSM.StateNetwork;
import com.testoptimal.exec.FSM.Transition;
import com.testoptimal.exec.mscript.MScriptInterface.IGNORE_INHERITED_METHOD;
import com.testoptimal.exec.mscript.MScriptInterface.NOT_MSCRIPT_METHOD;
import com.testoptimal.scxml.MbtNode;
import com.testoptimal.scxml.ScxmlNode;
import com.testoptimal.scxml.StateNode;
import com.testoptimal.scxml.StopCondition;
import com.testoptimal.util.StringUtil;

@IGNORE_INHERITED_METHOD
public class ExecutionSetting {
	private static Logger logger = LoggerFactory.getLogger(ExecutionSetting.class);
//	public static enum MbtMode {
//		Optimal, Random, Priority, Pairwise,
//		MCase, MarkedOptimal, MarkedSerial
//	}
	private static final int DefaultSeed = 37;

	/**
	 * initScript - string
	 * maxTestCaseNum - int
	 * submit_email - string
	 * statDesc - string
	 * batchID - int
	 * batchDesc - string
	 * 
	 */
	private Map<String, Object> execOptions = new java.util.HashMap<>();
	
	private long randSeed = DefaultSeed;
	private Random randNumObj = new Random(this.randSeed);
	public Random getRandObj() { 
		return this.randNumObj; 
	}
	
	private MbtNode mbtNode;
	private transient String curMbtMode;
	
	private StateNetwork networkObj;
	private ModelMgr modelMgr;
	private String initialStateID = "";
	private boolean stopAtFinalState = false;
	
	private StopCondition stopCond;
	private boolean genOnly = false;
	
	private ExecutionDirector execDir;
	
	public ExecutionSetting (ExecutionDirector execDir_p, ModelMgr modelMgr_p, Map<String, Object> options_p) throws Exception {
		this.execDir = execDir_p;
		this.modelMgr = modelMgr_p;
		this.execOptions.clear();
		ScxmlNode scxml = this.modelMgr.getScxmlNode();
		this.mbtNode = scxml.getMbtNode();
		this.curMbtMode = this.mbtNode.getMode();
		ScxmlNode scxmlNode = this.modelMgr.getScxmlNode();
		this.stopAtFinalState = scxmlNode.getMbtNode().getStopCond().isStopAtFinalOnly();
		this.stopCond = this.mbtNode.getStopCond().clone();
		
		this.execOptions.putAll(options_p);
		this.mbtNode.setOptions(options_p);
		Boolean g = (Boolean) this.execOptions.get("generateOnly");
		if (g!=null) this.genOnly = g;
	}

	public Map<String, Object> getOptions () {
		return this.execOptions;
	}

	public Object getOption(String key_p) {
		return this.execOptions.get(key_p);
	}
	
//	public int getNumThread () {
//		return this.mbtNode.getExecThreadNum();
//	}
	
	/**
	 * <p>INTERNAL USE ONLY</p>
	 */
	@NOT_MSCRIPT_METHOD
	public void init () throws Exception {
		this.randNumObj = new Random(this.randSeed);
		ScxmlNode scxmlNode = this.modelMgr.getScxmlNode();
		this.networkObj = new StateNetwork ();
		this.networkObj.init(scxmlNode);
		java.util.ArrayList<StateNode> initStateList = scxmlNode.getInitialNodes();
		if (initStateList!=null && !initStateList.isEmpty()) this.initialStateID = initStateList.get(0).getStateID();
		
	    // reset counters here
	    this.networkObj.reset();
	}
	
	
	public String getModelName() {
		return this.modelMgr.getModelName();
	}

//	
//	public List<String> getDisabledList () {
//		List<String> list = (List<String>) this.execOptions.get("disabledList");
//		return list;
//	}
//	
	public List<String> getMarkList() {
		List<String> marklist = (List<String>) this.execOptions.get("markList");
		return marklist;
	}
	
	/**
	 * <p>INTERNAL USE ONLY</p>
	 */
	@NOT_MSCRIPT_METHOD
	public float nextRand () {
		return this.randNumObj.nextFloat();
	}

	/**
	 * <p>INTERNAL USE ONLY</p>
	 * random number 0 (inclusive) - limit_p (exclusive)
	 * @param limit_p
	 * @return
	 */
	@NOT_MSCRIPT_METHOD
	public int nextRand (int limit_p) {
		return this.randNumObj.nextInt(limit_p);
	}
		
	/**
	 * <p>INTERNAL USE ONLY</p>
	 */
	@NOT_MSCRIPT_METHOD
	public int nextRand (int int1_p, int int2_p) {
		int ret = this.randNumObj.nextInt(int2_p - int1_p);
		return ret + int1_p;
	}
	
	/**
	 * <p>INTERNAL USE ONLY</p>
	 */
	@NOT_MSCRIPT_METHOD
	public Random getSeededRandObj() {
		return new Random(this.randSeed);
	}
	
	/**
	 * <p>INTERNAL USE ONLY</p>
	 */
	@NOT_MSCRIPT_METHOD
	public void setSeed (long seed_p) {
		this.randNumObj.setSeed(seed_p);
		this.randSeed = seed_p;
	}
	
	/**
	 * returns the max size of transition log allowed.  When this limit is reached, the older log entries will be deleted
	 * to make space for new log entries.
	 */
	public int getMaxExecLogSize() {
		Integer s = (Integer) this.execOptions.get("maxTestCaseNum");
		if (s==null) {
			return this.getModelMgr().getScxmlNode().getMiscNode().getMaxTestCaseNum(); 
		}
		else {
			return (int) s;
		}
	}

	public String getInitScript () {
		String script = (String) this.execOptions.get("initScript");
		if (StringUtil.isEmpty(script)) {
			return this.getModelMgr().getScxmlNode().getMbtNode().getInitScript(); 
		}
		else {
			return script;
		}
	}
	
	/**
	 * <p>INTERNAL USE ONLY</p>
	 */
	@NOT_MSCRIPT_METHOD
	public ModelMgr getModelMgr() { 
		return this.modelMgr; 
	}
	
	/**
	 * <p>INTERNAL USE ONLY</p>
	 */
	@NOT_MSCRIPT_METHOD
	public StateNetwork getNetworkObj() { 
		return this.networkObj; 
	}

	public String getCurMbtMode() { 
		return this.curMbtMode; 
	}
	
	public MbtNode getMbtNode() { return this.mbtNode; }

	public String getInitialStateID() { return this.initialStateID; }

	public List<Transition> getAllTransitions() {
		return this.networkObj.getActiveTransList();
	}


	public void setMbtMode(String mbtMode_p) {
		if (mbtMode_p==null) return;
		this.curMbtMode = mbtMode_p;
	}
	
	public String getMbtSessionID() {
		return this.execDir.getMbtSessionID();
	}
	

	public StopCondition getStopCondition () {
		return this.stopCond;
	}
	
	public boolean isStopAtFinalOnly () {
		return this.stopAtFinalState;
	}
	
	public boolean isGenOnly () {
		return this.genOnly;
	}
}
