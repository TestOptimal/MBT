package com.testoptimal.exec.mscript;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.testoptimal.exception.MBTAbort;
import com.testoptimal.exec.ExecutionDirector;
import com.testoptimal.exec.FSM.DataSet;
import com.testoptimal.exec.FSM.ModelMgr;
import com.testoptimal.exec.FSM.Transition;
import com.testoptimal.exec.mcase.MCaseMgr;
import com.testoptimal.exec.mscript.groovy.GroovyEngine;
import com.testoptimal.exec.mscript.groovy.GroovyScript;
import com.testoptimal.page.PageMgr;
import com.testoptimal.plugin.MScriptInterface;
import com.testoptimal.plugin.MScriptInterface.IGNORE_INHERITED_METHOD;
import com.testoptimal.plugin.RandPlugin;
import com.testoptimal.scxml.ScxmlNode;
import com.testoptimal.scxml.StateNode;
import com.testoptimal.scxml.TransitionNode;
import com.testoptimal.stats.TagExec;
import com.testoptimal.stats.exec.ExecTestCase;
import com.testoptimal.util.StringUtil;

/**
 * <p>Provides system functions for MScript.</p>
 * 
 * 
 * <p>Copyright &#169; 2008 - 2020 TestOptimal LLC. All Rights Reserved.</p>
 * 
 *
 */
@IGNORE_INHERITED_METHOD
public class MbtScriptExecutor implements MScriptInterface {
	private static Logger logger = LoggerFactory.getLogger(MbtScriptExecutor.class);
	
	private PageMgr pageMgr;
	private ModelMgr modelMgr;
	private Map<String,Object> varMap = new java.util.HashMap<>();
	private Map<String,DataSet> dsMap = new java.util.HashMap<>();
	private MCaseMgr mCaseMgr;
	private GroovyEngine groovyEngine;
	
	private ExecutionDirector execDirector;
	private Exec execObj;

	public GroovyEngine getScriptEngine() { return this.groovyEngine; }

	public ScxmlNode getScxmlNode() { 
		if (this.execDirector==null) return null;
		return this.execDirector.getExecSetting().getModelMgr().getScxmlNode(); 
	}

	public ModelMgr getModelMgr() { 
		return this.modelMgr; 
	}
	
	public Exec getSys() {
		return this.execObj;
	}
	
	public ExecutionDirector getExecDirector() { 
		return this.execDirector; 
	}
	
	/**
	 * Constructor, inititalizes the mbtControl with the webAppMBT object and the current working directory.
	 */
	public MbtScriptExecutor (ExecutionDirector execDirector_p) throws Exception {
		this.execDirector = execDirector_p;
		this.modelMgr = this.execDirector.getExecSetting().getModelMgr();
		this.pageMgr = new PageMgr();
		this.execObj = new Exec(this);
		this.mCaseMgr = new MCaseMgr();
	}
	

	public void initGroovyMScript (ModelMgr modelMgr_p) throws MBTAbort, Exception {
		this.groovyEngine = new GroovyEngine(modelMgr_p.getModelName(), modelMgr_p);
		if (this.execDirector.getExecSetting().isGenOnly()) {
			this.groovyEngine.setNoOp();
		}
		
		// these must be defined to CodeAssist.genCAList for Script Editor
		this.groovyEngine.addSysProperty("$EXEC", this.execObj);
		this.groovyEngine.addSysProperty("$UTIL", new Util());
		this.groovyEngine.addSysProperty("$VAR", this.varMap);
//		this.groovyEngine.addSysProperty("$DATASET", this.dsMap);
//		this.groovyEngine.addSysProperty("$RAND", new RandPlugin());
		
		// create GroovyScript object for main model
		List<GroovyScript> scriptList = this.execDirector.getScriptList();
		this.execDirector.getDataSetList().forEach(d -> this.dsMap.put(d.dsName, d));
		
		this.groovyEngine.addGroovyScriptList(scriptList);
		
		// execute initScript if specified in run options
		String initScript = this.getExecDirector().getExecSetting().getInitScript();
		if (!Strings.isBlank(initScript)) {
			this.groovyEngine.evalExpr("initScript", initScript);
		}
	}

	public GroovyEngine getGroovyEngine () {
		return this.groovyEngine;
	}

	/**
	 * perform the cleanup including causing all plugins to be closed/destroyed.
	 * <p>use this method to perform any cleanup you might want to do before the execution is completed.
	 * A typical use might be to write a record to database to indicate the execution completed and time
	 * of completion.
	 * </p>
	 */
	public void close () {
		if (!this.execDirector.getExecSetting().isGenOnly()) {

		}
	}

	/**
	 * Sets the value for the variable. If null value_p passed in, it will act as removal.
	 * 
	 * @param varName_p
	 * @param value_p
	 */
	public void setVar(String varName_p, Object value_p) {
		this.varMap.put(varName_p, value_p);
	}

	public Object getVar (String varName_p) {
		return this.varMap.get(varName_p);
	}

	public PageMgr getPageMgr() {
		return this.pageMgr;
	}
	
//	public void addReqCheck(String tag_p, boolean passed_p, String msg_p, String assertID_p) throws Exception {
//		StateNode curState = this.execObj.getCurState();
//		String uid = curState.getUID();
//		Transition trans = this.execDirector.getSequenceNavigator().getCurTravObj().getCurTrans();
//		String transID = null;
//		if (trans!=null && trans.getTransNode()!=null) {
//			transID = trans.getTransNode().getDesc();			
//			uid = trans.getTransNode().getUID();
//		}
//		TagExec tagExec = new TagExec(this, tag_p, passed_p, msg_p, assertID_p, curState.getStateID(), transID, uid);
//		this.execDirector.getSequenceNavigator().getCurTravObj().addTagExec(tagExec);
//	}

	public Object runMScript (String script_p) throws Exception {
		return this.groovyEngine.evalExpr("DynamicExpr", script_p);
	}
//	
//	public Exec getSysObj() {
//		return this.execObj;
//	}
//	
	public MCaseMgr getMCaseMgr() {
		return this.mCaseMgr;
	}

	public boolean evalGuard(TransitionNode transNode_p) {
		if (transNode_p==null) return true;
		String guardExpr = transNode_p.getGuard();
		if (StringUtil.isEmpty(guardExpr)) {
			return true;
		}
		try {
			Object ret = this.groovyEngine.evalExpr("Guard on Transition " + transNode_p.getEvent(), guardExpr);
			if (ret==null) return false;
			return StringUtil.isTrue(ret.toString());
		}
		catch (Throwable t) {
			logger.warn("Error evaluating guard on transition " + transNode_p.getEvent() + ": " + t.getMessage());
			return false;
		}
	}
	
	public void setPathName (String name_p) {
		ExecTestCase tcObj = this.getExecDirector().getExecStats().getCurTestCase();
		if (tcObj!=null) {
			tcObj.tcName = name_p;
		}
	}
	
	public String getPathName () {
		ExecTestCase tcObj = this.getExecDirector().getExecStats().getCurTestCase();
		if (tcObj!=null) {
			return tcObj.tcName;
		}
		else return null;
	}
	
	public Map<String, Object> getVarMap() {
		return this.varMap;
	}

	public Map<String, DataSet> getDataSetMap() {
		return this.dsMap;
	}
}

