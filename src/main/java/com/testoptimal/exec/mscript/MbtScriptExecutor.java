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
import com.testoptimal.exec.mscript.MScriptInterface.IGNORE_INHERITED_METHOD;
import com.testoptimal.exec.mscript.groovy.GroovyEngine;
import com.testoptimal.exec.mscript.groovy.GroovyScript;
import com.testoptimal.page.PageMgr;
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

	public void close () {
		if (!this.execDirector.getExecSetting().isGenOnly()) {

		}
	}

	public PageMgr getPageMgr() {
		return this.pageMgr;
	}
	
	public Object runMScript (String script_p) throws Exception {
		return this.groovyEngine.evalExpr("DynamicExpr", script_p);
	}

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

