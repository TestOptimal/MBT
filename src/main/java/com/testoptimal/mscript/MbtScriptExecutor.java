package com.testoptimal.mscript;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.testoptimal.db.ExecTestCaseDB;
import com.testoptimal.exception.MBTAbort;
import com.testoptimal.exec.ExecutionDirector;
import com.testoptimal.exec.FSM.ModelMgr;
import com.testoptimal.exec.FSM.TravBase;
import com.testoptimal.mcase.MCaseMgr;
import com.testoptimal.mscript.groovy.GroovyEngine;
import com.testoptimal.mscript.groovy.GroovyScript;
import com.testoptimal.page.PageMgr;
import com.testoptimal.plugin.MScriptInterface;
import com.testoptimal.plugin.MScriptInterface.IGNORE_INHERITED_METHOD;
import com.testoptimal.plugin.RandPlugin;
import com.testoptimal.scxml.ScxmlNode;
import com.testoptimal.scxml.TransitionNode;
import com.testoptimal.stats.TagExec;
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
	
//	private PluginMgr pluginMgr = null;
//	private DataMgr dataMgr; 
	private PageMgr pageMgr;
	private ModelMgr modelMgr;
	private Map<String,Object> varMap = new java.util.HashMap<>();
	private Map<String,DataSet> dsMap = new java.util.HashMap<>();
	private MCaseMgr mCaseMgr;
	private GroovyEngine groovyEngine;
	
	private ExecutionDirector execDirector;
	private Sys sysObj;

	public GroovyEngine getScriptEngine() { return this.groovyEngine; }

	public ScxmlNode getScxmlNode() { 
		if (this.execDirector==null) return null;
		return this.execDirector.getExecSetting().getModelMgr().getScxmlNode(); 
	}

	public ModelMgr getModelMgr() { 
		return this.modelMgr; 
	}
	
	public Sys getSys() {
		return this.sysObj;
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
//		this.dataMgr = new DataMgr(modelMgr, execWorker_p);
//		this.pluginMgr = new PluginMgr(modelMgr, this);
		this.pageMgr = new PageMgr();
		this.sysObj = new Sys(this);
		this.mCaseMgr = new MCaseMgr();
	}
	

	public void initGroovyMScript (ModelMgr modelMgr_p) throws MBTAbort, Exception {
		this.groovyEngine = new GroovyEngine(modelMgr_p.getModelName(), modelMgr_p);
		if (this.execDirector.getExecSetting().isGenOnly()) {
			this.groovyEngine.setNoOp();
		}
		
		// these must be defined to CodeAssist.genCAList for Script Editor
		this.groovyEngine.addSysProperty("$SYS", this.sysObj);
		this.groovyEngine.addSysProperty("$UTIL", new Util());
		this.groovyEngine.addSysProperty("$VAR", this.varMap);
		this.groovyEngine.addSysProperty("$DATASET", this.dsMap);
		this.groovyEngine.addSysProperty("$RAND", new RandPlugin());

		// add autoLoad plugins
//		List<String> pluginIdList = modelMgr_p.getScxmlNode().getPluginList();
//		PluginMgr.newPlugins(pluginIdList).stream().filter(p-> pluginIdList.indexOf(p.getPluginID())>=0).forEach(p -> {
//			this.pluginMgr.addPlugin(p);
//		});
//		this.pluginMgr.addPlugins (pluginIdList);
		
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
//		
//	public DataMgr getDataMgr() {
//		return this.dataMgr; 
//	}
//	

	/**
	 * perform the cleanup including causing all plugins to be closed/destroyed.
	 * <p>use this method to perform any cleanup you might want to do before the execution is completed.
	 * A typical use might be to write a record to database to indicate the execution completed and time
	 * of completion.
	 * </p>
	 */
	public void close () {
		if (!this.execDirector.getExecSetting().isGenOnly()) {
//			this.pluginMgr.close();
	//		Util.stopScreenRecording();
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

//	public PluginMgr getPluginMgr() {
//		return this.pluginMgr;
//	}
	
	public PageMgr getPageMgr() {
		return this.pageMgr;
	}
	
	public void addReqCheck(String tag_p, boolean passed_p, String msg_p, String assertID_p) throws Exception {
		String stateID = this.sysObj.getCurState().getStateID();
		String transID = "";
		TransitionNode transNode = this.sysObj.getCurTrans();
		if (transNode!=null) {
			transID = transNode.getDesc();
		}
		String uid = this.sysObj.getCurTravUID();
		TagExec tagExec = new TagExec(this, tag_p, passed_p, msg_p, assertID_p, stateID, transID, uid);
		this.execDirector.getSequenceNavigator().getCurTravObj().addTagExec(tagExec);
	}

	public Object runMScript (String script_p) throws Exception {
		return this.groovyEngine.evalExpr("DynamicExpr", script_p);
	}
	
	public Sys getSysObj() {
		return this.sysObj;
	}
	
	public MCaseMgr getMCaseMgr() {
		return this.mCaseMgr;
	}

//
//	/**
//	 * performs MBT actions. returns true if executed successfully, false if execution is noop.
//	 */
//    public boolean trigerMBTAction (TravBase.TriggerType mbtTriggerType_p) throws MBTAbort {
//		String mainModelName = this.getModelMgr().getModelName();
//		boolean isExec = false;
//		try {
//			// only mbt_start/end triggers of submodels will be executed
//			switch (mbtTriggerType_p) {
//				case start:
//					this.execDirector.getExecListener().enterMbtStart();
//					for (ScxmlNode model: this.modelMgr.getSubModelList()) {
//						isExec = isExec || this.groovyEngine.callTrigger(model.getModelName(), "MBT_START");
//					}				
//					isExec = isExec || this.groovyEngine.callTrigger(mainModelName, "MBT_START");				
//					this.execDirector.getExecListener().exitMbtStart();
//					break;
//				case end:
//					this.execDirector.getExecListener().enterMbtEnd();
//					for (ScxmlNode model: this.modelMgr.getSubModelList()) {
//						isExec = isExec || this.groovyEngine.callTrigger(model.getModelName(), "MBT_END");
//					}				
//					isExec = isExec || this.groovyEngine.callTrigger(mainModelName, "MBT_END");				
//					this.execDirector.getExecListener().exitMbtEnd();
//					break;
//				case fail:
//					isExec = isExec || this.groovyEngine.callTrigger(mainModelName, "MBT_FAIL");
//					this.execDirector.getExecListener().mbtFailed();
//					break;
//			}
//		}
//		catch (Throwable t) {
//			throw new MBTAbort(t.toString());
//		}
//		return isExec;
//    }
    
	public boolean evalGuard(TransitionNode transNode_p) {
		if (transNode_p==null || this.execDirector.getExecSetting().isGenOnly()) return true; // fake trans does not have TransitionNode.
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
		ExecTestCaseDB tcObj = this.getExecDirector().getExecStats().getCurTestCase();
		if (tcObj!=null) {
			tcObj.tcName = name_p;
		}
	}
	
	public String getPathName () {
		ExecTestCaseDB tcObj = this.getExecDirector().getExecStats().getCurTestCase();
		if (tcObj!=null) {
			return tcObj.tcName;
		}
		else return null;
	}
	

	public String genSnapScreenFilePath (long snapMillis_p) throws Exception {
		StringBuffer fileName =  new StringBuffer();
		ModelMgr modelMgr = this.getModelMgr();
		fileName.append(modelMgr.getScreenshotFolderPath());
		fileName.append("snap_").append(snapMillis_p);
		return fileName.toString();
	}
	
	public Map<String, Object> getVarMap() {
		return this.varMap;
	}

	public Map<String, DataSet> getDataSetMap() {
		return this.dsMap;
	}
}

