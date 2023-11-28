package com.testoptimal.mscript;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.testoptimal.db.ExecStateTransDB;
import com.testoptimal.db.ModelExecDB;
import com.testoptimal.db.TestCaseStepDB;
import com.testoptimal.exception.MBTAbort;
import com.testoptimal.exception.MBTException;
import com.testoptimal.exec.ExecutionDirector;
import com.testoptimal.exec.ExecutionSetting;
import com.testoptimal.exec.FSM.ModelMgr;
import com.testoptimal.exec.FSM.State;
import com.testoptimal.exec.FSM.Transition;
import com.testoptimal.graphing.GenGraph;
import com.testoptimal.mcase.MCase;
import com.testoptimal.mcase.MCaseMgr;
import com.testoptimal.page.Page;
import com.testoptimal.page.PageMgr;
import com.testoptimal.plugin.MScriptInterface.IGNORE_INHERITED_METHOD;
import com.testoptimal.scxml.ScxmlNode;
import com.testoptimal.scxml.StateNode;
import com.testoptimal.scxml.TransitionNode;
import com.testoptimal.server.config.Config;
import com.testoptimal.stats.TagExec;

/**
 * <p>
 * Provides system functions for MScript.
 * </p>
 * 
 *
 */
@IGNORE_INHERITED_METHOD
public class Sys {
	private MbtScriptExecutor scriptExec;
	private ModelMgr modelMgr;
	private ExecutionDirector execDir;
	private ExecutionSetting execSetting;

	/**
	 * <p>INTERNAL USE ONLY</p>
	 * @param scriptExec_p
	 */
	public Sys(MbtScriptExecutor scriptExec_p) {
		this.scriptExec = scriptExec_p;
		this.modelMgr = this.scriptExec.getModelMgr();
		this.execDir = this.scriptExec.getExecDirector();
		this.execSetting = this.execDir.getExecSetting();
	}


	/**
	 * logs a message to MScript log
	 * @param msg_p
	 */
	public void log(Object msg_p) {
		this.execDir.log(msg_p.toString());
	}
	
	/**
	 * sets the MScript log option to prepend timestamp to each log message.
	 * This option overrides "log.mscript.timestamp = true/false" in /config/config.properities file.
	 * @param bool_p
	 */
	public void setLogTimestamp (boolean bool_p) {
		this.execDir.getMScriptLogger().setWriteTimestamp(bool_p);
	}

	/**
	 * raises Exception to cause the MBT execution to abort.
	 * <p>
	 * Can be used anywhere in mScript. It will trigger Error if one is
	 * declared.
	 * </p>
	 * 
	 * <p>
	 * This function is ignored when running the model in debug mode and AUT is
	 * disabled. The reason for this behavior is to allow users to continue to debug
	 * MScript error. Instead of abort the execution, it will just display an abort
	 * alert message indicating where the abort was triggered.
	 * 
	 * <p>
	 * <p>
	 * Example: <code>$sys.abort('Fatal error detected')</code>
	 * 
	 */
	public void abort(String msg_p) throws Exception {
		this.execDir.interrupt();
		this.scriptExec.addReqCheck(null, false, msg_p, null);
		throw new Exception(msg_p);
	}
		
//
//	/**
//	 * sets the minimum number of traversal required for the specified transition.
//	 * 
//	 * <p>
//	 * Use this function to dynamically change the number of times you want to see
//	 * transition traversed. Note this function is only available with the Random
//	 * sequencer or Greedy sequencer. It will cause error if used with other
//	 * sequencers.
//	 * 
//	 * <p>
//	 * Example:
//	 * 
//	 * <pre>
//	 * $sys.setMinTraverseTimes('state1','trans1','5')
//	 * </pre>
//	 * 
//	 * <p>
//	 * Be aware that changing the minimal number of traversed required for the
//	 * transition may affect when the model execution will stop.
//	 * 
//	 * @param stateID_p
//	 *            name of the state
//	 * @param transID_p
//	 *            name of the transition within the stateID_p
//	 * @param minTraverseTimes_p
//	 *            0 or positive integer.
//	 * @throws MBTException
//	 */
//	@MSCRIPT_METHOD
//	public void setMinTraverseTimes(String stateID_p, String transID_p, String minTraverseTimes_p) throws Exception {
//		ScxmlNode scxml = this.modelMgr.getScxmlNode();
//		MbtMode mbtMode = scxml.getMbtNode().getMbtMode();
//		if (mbtMode != MbtMode.Random) {
//			this.abort("$setMinTraverseTimes is only valid for Random Sequencer.");
//			return;
//		}
//
//		State state = this.execDir.getExecSetting().getNetworkObj().findState(stateID_p);
//		if (state == null)
//			throw new MBTException("State Not Found: " + stateID_p);
//
//		Transition trans = state.getTransition(transID_p);
//		if (trans == null)
//			throw new MBTException("Transition Not Found: " + transID_p);
//
//		int travTimes = StringUtil.parseInt(minTraverseTimes_p, -1);
//		if (travTimes < 0)
//			throw new MBTException("Invalid minTraverseTimes value: " + minTraverseTimes_p);
//
//		trans.setMinTraverseCountRT(travTimes);
//		StateTransStats transStats = this.execWorker.getTravExecMgr().getModelExecStats().getModelExecStats()
//				.getStateTransStats(trans.getTransNode());
//		if (transStats != null)
//			transStats.setMinTravCount(travTimes);
//		return;
//	}
//
//	/**
//	 * sets the maximum number of traversal for the specified transition.
//	 * 
//	 * <p>
//	 * By default all transitions can be traversed infinite number of times. At
//	 * times you may wish to limit certain transitions not be traversed above a
//	 * specific number.
//	 * 
//	 * <p>
//	 * Use this function to dynamically change the number of times you want to limit
//	 * the number of times the transition can be traversed. Note this function is
//	 * only available with the Random sequencer or Greedy sequencer. It will cause
//	 * error if used with other sequencers.
//	 * 
//	 * <p>
//	 * 
//	 * Example:
//	 * 
//	 * <pre>
//	 * $sys.setMaxTraverseTimes('state1','trans1','5')
//	 * </pre>
//	 * 
//	 * <p>
//	 * Be aware that changing the maximum number of traversed for the transition may
//	 * affect when the model execution will stop and also cause model execution to
//	 * loop if additional traversal of this transition is required to complete the
//	 * model execution to meet the desired test coverage.
//	 * 
//	 * @param stateID_p
//	 *            name of the state
//	 * @param transID_p
//	 *            name of the transition within the stateID_p
//	 * @param maxTraverseTimes_p
//	 *            0 or positive integer.
//	 * @throws MBTException
//	 */
//	@MSCRIPT_METHOD
//	public void setMaxTraverseTimes(String stateID_p, String transID_p, String maxTraverseTimes_p) throws Exception {
//		ScxmlNode scxml = this.modelMgr.getScxmlNode();
//		MbtMode mbtMode = scxml.getMbtNode().getMbtMode();
//		if (mbtMode != MbtMode.Random) {
//			this.abort("$setMaxTraverseTimes is only valid for Random Sequencer.");
//			return;
//		}
//
//		State state = this.execSetting.getNetworkObj().findState(stateID_p);
//		if (state == null)
//			throw new MBTException("State Not Found: " + stateID_p);
//
//		Transition trans = state.getTransition(transID_p);
//		if (trans == null)
//			throw new MBTException("Transition Not Found: " + transID_p);
//
//		int travTimes = StringUtil.parseInt(maxTraverseTimes_p, -1);
//		if (travTimes < 0)
//			throw new MBTException("Invalid maxTraverseTimes value: " + maxTraverseTimes_p);
//
//		trans.setMaxTraverseCountRT(travTimes);
//		return;
//	}



	/**
	 * returns the trace in a string of the traversals separated by comma leading up to
	 * the current state/transition.
	 * 
	 * <p>
	 * Example: place in <code>MBT_FAIL</code> or <code>MBT_ERROR</code> triggers 
	 * to print out the test sequence transitions
	 * that led to the current state/transition where the error/failure occurred.
	 * </p>
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * $SYS.trace()
	 * </pre>
	 * 
	 * @return String, example output as follows 'transName:transUID'.
	 *  <p>
	 * 		ContinueShopping:U101,AddItem:U112
	 *  </p>
	 * @throws Exception
	 */
	public String trace() throws Exception {
		return this.trace(0, ",");
	}
	
	/**
	 * returns the trace in a string of last maxLogCount_p traversals leading up to
	 * the current state/transition. Trace sequence is separated by the specified
	 * delimiter.
	 * 
	 * <p>
	 * Example: place in <code>MBT_FAIL</code> or <code>MBT_ERROR</code> triggers 
	 * to print out the test sequence transitions
	 * that led to the current state/transition where the error/failure occurred.
	 * </p>
	 * 
	 * <pre>
	 * $SYS.trace(5,'|')
	 * </pre>
	 * 
	 * @param maxTraceCount_p
	 *            max number of traversals
	 * @param delimiter_p
	 *            delimiter to be used to separate the traversals.
	 * @return String, example output as follows 'transName:transUID'.
	 *  <p>
	 * 		ContinueShopping:U101|AddItem:U112
	 *  </p>
	 * @throws Exception
	 */
	public String trace(int maxTraceCount_p, String delimiter_p) throws Exception {
		ModelExecDB collStat = this.execDir.getExecStats();
		Map<String, ExecStateTransDB> transMap = collStat.getStateTransMap();
		List<TestCaseStepDB> stepList = collStat.getCurTestCase().stepList.stream()
				.filter(s-> transMap.get(s.UID).type.equalsIgnoreCase("trans"))
				.collect(Collectors.toList());
		
		if (maxTraceCount_p > 0 && stepList.size() > maxTraceCount_p) {
			stepList = stepList.subList(0, maxTraceCount_p);
		}
		
		return stepList.stream().map(s -> transMap.get(s.UID).transName + ":" + s.UID)
			.collect (Collectors.joining(delimiter_p));
	}

	/**
	 * generate traversal graph from home/start state to the current state.
	 * Typically you would call this function in the final state's trigger to
	 * generate a test path that corresponds to a test case/scenario.
	 * 
	 * <p>
	 * CAUTION: this function is not thread-safe, i.e. if you are running multiple
	 * concurrent threads, the behavior may be un-predictable - it may contain
	 * traversals from other threads concurrently running.
	 * 
	 * @param fileName_p
	 *            name of the image file without extension. You may include "." in
	 *            the file name, but the system will always add file extension that
	 *            matches the image even if you have supplied the right extension.
	 * @return file name to the image generated and stored in the report folder
	 *         of the model.
	 **/
	public String genPath(String fileName_p) throws Exception {
		String filePath = GenGraph.genObjectSequence(this.execDir, this.modelMgr.getReportFolderPath(), fileName_p, 2);
		File file = new File (filePath);
		return file.getName();
	}

	/**
	 * generate Message Sequence Chart (MSC) from the home/start state to the
	 * current state. Typically you would call this function in the final state's
	 * trigger to generate a test sequence that corresponds to a test case /
	 * scenario.
	 * 
	 * <p>
	 * CAUTION: this function is not thread-safe, i.e. if you are running multiple
	 * concurrent threads, the behavior may be un-predictable - it may contain
	 * traversals from other threads concurrently running.
	 * 
	 * @param fileName_p
	 *            name of the image file without extension. You may include "." in
	 *            the file name, but the system will add file extension that matches
	 *            the image even if you have supplied the right extension.
	 * @return file name to the image generated and stored in the report folder
	 *         of the model.
	 * @throws Exception
	 */
	public String genMSC(String chartLabel_p, String fileName_p) throws Exception {
		String fileName = fileName_p.trim();
		if (!fileName_p.endsWith(".png")) {
			fileName += ".png";
		}
		String filePath = GenGraph.genMSC(chartLabel_p, this.execDir, this.modelMgr.getReportFolderPath(), fileName,
				null, null);
		File file = new File (filePath);
		return file.getName();
	}

	/**
	 * generate Message Sequence Chart (MSC) from the home/start state to the
	 * current state. Typically you would call this function in the final state's
	 * onentry trigger to generate a test sequence that corresponds to a test case /
	 * scenario.
	 * 
	 * <p>
	 * CAUTION: this function is not thread-safe, i.e. if you are running multiple
	 * concurrent threads, the behavior may be un-predictable - it may contain
	 * traversals from other threads concurrently running.
	 * 
	 * @param fileName_p
	 *            name of the image file without extension. You may include "." in
	 *            the file name, but the system will add file extension that matches
	 *            the image even if you have supplied the right extension.
	 * @param skinName_p
	 *            name of the skin (Rose or BlueModern) to style the MSC.
	 * @return file name to the image generated and stored in the report folder
	 *         of the model.
	 * @throws Exception
	 */
	public String genMSC(String chartLabel_p, String fileName_p, String skinName_p) throws Exception {
		String fileName = fileName_p.trim() + ".png";
		String filePath = GenGraph.genMSC(chartLabel_p, this.execDir, this.modelMgr.getReportFolderPath(), fileName,
				skinName_p, null);
		File file = new File (filePath);
		return file.getName();
	}

	/**
	 * generate Message Sequence Chart (MSC) from the home/start state to the
	 * current state. Typically you would call this function in the final state's
	 * trigger to generate a test sequence that corresponds to a test case /
	 * scenario.
	 * 
	 * <p>
	 * CAUTION: this function is not thread-safe, i.e. if you are running multiple
	 * concurrent threads, the behavior may be un-predictable - it may contain
	 * traversals from other threads concurrently running.
	 * 
	 * @param fileName_p
	 *            name of the image file without extension. You may include "." in
	 *            the file name, but the system will add file extension that matches
	 *            the image even if you have supplied the right extension.
	 * @param skinName_p
	 *            name of the skin (Rose or BlueModern) to style the MSC.
	 * @param bkgColor_p background color to be used for MSC, use HTML color code
	 * @return file name to the image generated and stored in the report folder
	 *         of the model.
	 * @throws Exception
	 */
	public String genMSC(String chartLabel_p, String fileName_p, String skinName_p, String bkgColor_p)
			throws Exception {
		String fileName = fileName_p.trim() + ".png";
		String filePath = GenGraph.genMSC(chartLabel_p, this.execDir, this.modelMgr.getReportFolderPath(), fileName,
				skinName_p, bkgColor_p);
		File file = new File (filePath);
		return file.getName();
	}

	/**
	 * change the current transition's guard hint at runtime. The guard hint and satisfying
	 * hint work together. A transition with the matching satisfying hint will
	 * satisfy the guard of the transition with the guard hint when traversed.
	 * 
	 * <p>
	 * Guard hint is a code (or codes separated by comma).
	 * </p>
	 * 
	 * Example:
	 * 
	 * <pre>
	 * $SYS.setCurTransGuardHint('Login')
	 * </pre>
	 * <p>
	 * The above example sets the guard hint on the current transition to
	 * <code>Login</code>. When traversing this transition but the guard fails, the
	 * sequencer will attempt to find the transitions with the satisfying hint that
	 * matches the guard hint.
	 * </p>
	 * <p>
	 * The assumption is that when the transition with the matching satisfying hint
	 * is traversed, it will make the guard condition to true and this this
	 * transition can be traverse next.
	 *
	 * </p>
	 * <p>Can not be used in Debug Expression</p>
	 * 
	 * @param hint_p
	 */
	public boolean setTransGuardHint(String hint_p) {
		Transition transObj = this.execDir.getSequenceNavigator().getCurTravObj().getCurTrans();
		if (transObj == null) {
			return false;
		} 
		else {
			transObj.getTransNode().setGuardHint(hint_p);
			return true;
		}
	}
	

	/**
	 * change the current transition's satisfying hint at runtime. The guard hint and
	 * satisfying hint work together. A transition with the matching satisfying hint
	 * will satisfy the guard of the transition with guard hint when traversed.
	 * 
	 * <p>
	 * Must be executing a transition.
	 * </p>
	 * <p>
	 * Satisfying hint is a code (or codes separated by comma).
	 * </p>
	 * 
	 * Example:
	 * 
	 * <pre>
	 * $SYS.setCurTransSatisfyingHint('Login')
	 * </pre>
	 * <p>
	 * The above example sets the satisfying hint on the current transition to
	 * <code>Login</code>.
	 * </p>
	 * <p>Can not be used in Debug Expression</p>
	 * 
	 * @param hint_p
	 * @return true if successfully set, false if not currently executing a transition
	 */
	public boolean setCurTransSatisfyingHint(String hint_p) {
		Transition transObj = this.execDir.getSequenceNavigator().getCurTravObj().getCurTrans();
		if (transObj == null) {
			return false;
		} 
		else {
			transObj.getTransNode().setSatisfyingHint(hint_p);
			return true;
		}
	}

	/**
	 * returns the state node.
	 * 
	 * @param stateID_p state id or state uid
	 */
	public StateNode findState (String stateID_p) {
		StateNode state = this.modelMgr.getScxmlNode().findState(stateID_p);
		if (state==null) state = this.modelMgr.getScxmlNode().findStateByUID(stateID_p);
		return state;
	}
	
	/**
	 * returns the transition node.
	 * 
	 * @param stateID_p state id or uid
	 * @param transID_p transition id only
	 */
	public TransitionNode findTransition (String stateID_p, String transID_p) {
		StateNode state = this.findState(stateID_p);
		if (state==null) return null;
		return state.findTrans(transID_p);
	}

	/**
	 * initialize random number generator used by the sequencer to the specific
	 * seed. By default system uses the default seed for the random number
	 * generator. Changing the random number generator seed will affect the actual
	 * test sequence generated while maintaining the same test coverage of the
	 * model. At times you may wish to try different seed just to get a different
	 * variation from the test sequence generated with the default seed.
	 * 
	 * <p>
	 * This function is usually called in the MBT_start trigger. However you may
	 * call it anywhere in MScript.
	 * 
	 * @param seed_p
	 */
	public boolean setSeed(String seed_p) {
		try {
			long seed = Long.parseLong(seed_p);
			this.execSetting.setSeed(seed);
			return true;
		} catch (Exception e) {
			return false;
		}
	}


	/**
	 * Returns the value (String) for the environment variable (global variable in current
	 * server). Initial values can be set in config.properties file as "ENV.varName=value".
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * $SYS.getEnvVar('var1')
	 * </pre>
	 *
	 * @param varName_p  name of the variable.
	 */
	public String getEnvVar(String varName_p) throws MBTException {
		return Config.getProperty("ENV." + varName_p);
	}

	/**
	 * Set environment variable to the specified value - String only.
	 * The environment variables are configured/initialized from config.properties file and 
	 * any updates by the model scripts will be saved into config.properties file, as ENV.varName=value. 
	 * 
	 * Example:
	 * 
	 * <pre>
	 * $SYS.setEnvVar('myVar','123')
	 * </pre>
	 * 
	 * @param varName_p
	 *            variable name
	 * @param toValue_p
	 *            new value
	 */
	public void setEnvVar(String varName_p, String toValue_p) throws Exception {
		Config.setProperty("ENV." + varName_p, toValue_p);
		Config.save();
	}


	/**
	 * returns execution director object of current model execution.
	 * Execution director object has methods to obtain execution setting
	 * object and virtual user (VU, threads) execution information.
	 * @return
	 */
	public ExecutionDirector getExecDir() {
		return this.execDir;
	}

	/**
	 * performs the assert on the condition passed in to be true.  If check is
	 * successful, add positive check on the requirement tag, else add a negative
	 * check on the requirement tag with the failMsg_p passed in.
	 * <p>
	 * 
	 * @param tag_p
	 *            one single tag id
	 * @param condition_p
	 * 			  boolean condition
	 * @param failMsg_p
	 *            message to be added to this check if condition_p is false.
	 * 
	 */
	public void assertTrue (String tag_p, boolean condition_p, String failMsg_p) throws Exception {
		if (condition_p) {
			this.scriptExec.addReqCheck(tag_p, true, null, null);
		}
		else {
			this.scriptExec.addReqCheck(tag_p, false, failMsg_p, null);
		}
	}

	/**
	 * performs the assert on the condition passed in to be true.  If check is
	 * successful, add positive check on the requirement tag, else add a negative
	 * check on the requirement tag with the failMsg_p passed in.
	 * <p>
	 * 
	 * @param tag_p
	 *            one single tag id
	 * @param condition_p
	 * 			  boolean condition
	 * @param failMsg_p
	 *            message to be added to this check if condition_p is false
	 * @param assertID_p
	 *            user assigned unique id for this specific call, this is usually
	 *            used to find the same defect in the defect system from previous
	 *            runs.
	 * 
	 */
	public void assertTrue (String tag_p, boolean condition_p, String failMsg_p, String assertID_p) throws Exception {
		if (condition_p) {
			this.scriptExec.addReqCheck(tag_p, true, null, assertID_p);
		}
		else {
			this.scriptExec.addReqCheck(tag_p, false, failMsg_p, assertID_p);
		}
	}


	/**
	 * performs the assert on the condition passed in to be false.  If check is
	 * successful, add positive check on the requirement tag, else add a negative
	 * check on the requirement tag with the failMsg_p passed in.
	 * <p>
	 * 
	 * @param tag_p
	 *            one single tag id
	 * @param condition_p
	 * 			  boolean condition
	 * @param failMsg_p
	 *            message to be added to this check if condition_p is true.
	 * 
	 */
	public void assertFalse (String tag_p, boolean condition_p, String failMsg_p) throws Exception {
		this.assertTrue(tag_p, !condition_p, failMsg_p);
	}

	/**
	 * performs the assert on the condition passed in to be false.  If check is
	 * successful, add positive check on the requirement tag, else add a negative
	 * check on the requirement tag with the failMsg_p passed in.
	 * <p>
	 * 
	 * @param tag_p
	 *            one single tag id
	 * @param condition_p
	 * 			  boolean condition
	 * @param failMsg_p
	 *            message to be added to this check if condition_p is true
	 * @param assertID_p
	 *            user assigned unique id for this specific call, this is usually
	 *            used to find the same defect in the defect system from previous
	 *            runs.
	 * 
	 */
	public void assertFalse (String tag_p, boolean condition_p, String failMsg_p, String assertID_p) throws Exception {
		this.assertTrue(tag_p, !condition_p, failMsg_p, assertID_p);
	}

	
	/**
	 * adds a successful requirement check/validation message to the requirement
	 * stat. A requirement may undergo several checks/validations per traversal of
	 * the state/transition.
	 * <p>
	 * 
	 * @param tag_p
	 *            one single tag id
	 * @param msg_p
	 *            message to be added to this check.
	 * 
	 */
	public void addReqPassed(String tag_p, String msg_p) throws Exception {
		this.scriptExec.addReqCheck(tag_p, true, msg_p, null);
	}

	/**
	 * adds a successful requirement check/validation message to the requirement
	 * stat. A requirement may undergo several checks/validations per traversal of
	 * the state/transition.
	 * <p>
	 * 
	 * @param tag_p
	 *            one single tag id
	 * @param msg_p
	 *            message to be added to this check.
	 * @param assertID_p
	 *            user assigned unique id for this specific call, this is usually
	 *            used to find the same defect in the defect system from previous
	 *            runs.
	 * 
	 */
	public void addReqPassed(String tag_p, String msg_p, String assertID_p) throws Exception {
		this.scriptExec.addReqCheck(tag_p, true, msg_p, assertID_p);
	}

	/**
	 * adds a failed requirement check/validation message to the requirement stat. A
	 * requirement may undergo many checks/validations per traversal of the
	 * state/transition with some passed and some failed. Call this method for each
	 * failed check and call $sys.addReqPassed () for each successful check.
	 * <p>
	 * 
	 * @param tag_p
	 *            one single tag id
	 * @param msg_p
	 *            message to be added to this check.
	 */
	public void addReqFailed(String tag_p, String msg_p) throws Exception {
		this.scriptExec.addReqCheck(tag_p, false, msg_p, null);
	}

	/**
	 * adds a failed requirement check/validation message to the requirement stat. A
	 * requirement may undergo many checks/validations per traversal of the
	 * state/transition with some passed and some failed. Call this method for each
	 * failed check and call $sys.addReqPassed () for each successful check.
	 * 
	 * The assertID_p is specified to uniquely identify this failed check in defect
	 * system.
	 * 
	 * @param tag_p
	 *            one single tag id
	 * @param msg_p
	 *            message to be added to this check.
	 * @param assertID_p
	 *            user assigned unique id for this specific call, this is usually
	 *            used to find the same defect in the defect system from previous
	 *            runs.
	 * 
	 */
	public void addReqFailed(String tag_p, String msg_p, String assertID_p) throws Exception {
		this.scriptExec.addReqCheck(tag_p, false, msg_p, assertID_p);
	}

	/**
	 * returns the model execution stats collection object.
	 * @return
	 * @throws Exception
	 */
	public ModelExecDB getStats() throws Exception {
		ModelExecDB modelExec = this.scriptExec.getExecDirector().getExecStats();
		return modelExec;
	}

	/**
	 * returns model object. Model object contains state diagram nodes and transitions and
	 * model version information.
	 * @return
	 */
	public ScxmlNode getModelObj() {
		return this.modelMgr.getScxmlNode();
	}
	
	/**
	 * returns relative file path to the model folder
	 * @return
	 */
	public String getModelRelativePath() {
		return modelMgr.getRelativePath();
	}
	
	/**
	 * returns absolute file path to the model folder
	 * @return
	 */
	public String getModelAbsolutePath() {
		return modelMgr.getFolderPath();
	}
			
	/**
	 * returns current test path name.
	 */
	public void setPathName (String name_p) {
		this.scriptExec.setPathName(name_p);
	}

	/**
	 * set current test path name.
	 */
	public String getPathName () {
		return this.scriptExec.getPathName();
	}

	/**
	 * if the current model is a sub model.  Use this function
	 * in the sub model to detect if it is being run inside another models.
	 * 
	 * @return 
	 */
	public boolean isSubModel () {
		String modelName = this.modelMgr.getScxmlNode().getModelName();
		return modelName.startsWith("_sub_");
	}

	/**
	 * returns current state object
	 * <p>Can not be used in Debug Expression</p>
	 * @return
	 */
	public StateNode getCurState() {
		State state = this.execDir.getSequenceNavigator().getCurTravObj().getCurState();
		if (state==null) {
			return null;
		}
		else {
			return state.getStateNode();
		}
	}
	
	/**
	 * returns current transition
	 * <p>Can not be used in Debug Expression</p>
	 * @return
	 */
	public TransitionNode getCurTrans () {
		Transition trans = this.execDir.getSequenceNavigator().getCurTravObj().getCurTrans();
		if (trans==null) return null;
		else return trans.getTransNode();
	}

	/**
	 * returns the UID of current traversal which can be either a transition or a state.
	 * <p>Can not be used in Debug Expression</p>
	 * @return
	 */
	public String getCurTravUID () {
		TransitionNode trans = this.getCurTrans();
		if (trans==null) {
			StateNode state = this.getCurState();
			if (state==null) return null;
			else return state.getUID();
		}
		else return trans.getUID();
	}
//
//	/**
//	 * returns the data set assigned to the current state / transition.
//	 * <p>Can not be used in Debug Expression</p>
//	 * @return DataSet object.
//	 */
//	public DataSet getCurDataSet () throws MBTAbort {
//		String dsName = "";
//		Transition trans = AsyncTrans.getCurTrans();
//		if (trans!=null) {
//			TransitionNode transNode = trans.getTransNode();
//			if (transNode == null) {
//				return null;
//			}
//			dsName = transNode.getDataSet();
//			if (StringUtil.isEmpty(dsName)) {
//				return null;
//			}
//		}
//		else {
//			StateNode stateNode = AsyncState.getCurState().getStateNode();
//			if (stateNode == null) {
//				return null;
//			}
//			dsName = stateNode.getDataSet();
//			if (StringUtil.isEmpty(dsName)) {
//				return null;
//			}
//		}
//		return this.scriptExec.getDataMgr().getDataSet(dsName);
//	}

//	/**
//	 * returns the the data for the field name specified from the dataset assigned to the current state / transition.
//	 * @return field data value.
//	 */
//	public Object getData(String fieldName_p) throws MBTAbort {
//		DataSet ds = this.getCurDataSet();
//		if (ds==null) return null;
//		return ds.getData(fieldName_p);
//	}
//	

//	/**
//	 * activates a specific state in the current model resulting in all transitions originating from the specified state to
//	 * be dispatched for immediate execution.  Any transitions with guard conditions must have
//	 * guard conditions evaluated to true in order to participate in this operation.
//	 * 
//	 * <p>This should only be used for Concurrent models</p>
//	 * <p>Can not be used in Debug Expression</p>
//	 * 
//	 */
//	public void activateState(String stateID_p) throws Exception, MBTAbort {
//		StateNetwork net = this.execSetting.getNetworkObj();
//		State nextState = net.findState(stateID_p);
//		if (nextState==null) {
//			throw new MBTException ("Invalid state " + stateID_p);
//		}
//		if (nextState!=null) {
//			AsyncState asyncState = new AsyncState(nextState, this.execWorker);
//			this.execWorker.execTrav(asyncState);
//		}
//	}
//	

//	/**
//	 * returns DataMgr which manages all data sets created/registered for the model execution.
//	 * 
//	 * @return
//	 */
//	public DataMgr getDataMgr () {
//		return this.scriptExec.getDataMgr();
//	}
//
//	/**
//	 * returns the data set that has been created and registered to the model execution.
//	 * @param dsName_p
//	 * @return
//	 */
//	public DataSet getDataSet (String dsName_p) throws MBTAbort {
//		return this.scriptExec.getDataMgr().getDataSet(dsName_p);
//	}

	/**
	 * returns PageMgr which manages all page objects created/registered for the model execution.
	 * 
	 * @return
	 */
	public PageMgr getPageMgr () {
		return this.scriptExec.getPageMgr();
	}

	/**
	 * returns page object for the page name specified.
	 * 
	 * @param pageName_p
	 * @return
	 * @throws MBTAbort if page not found
	 */
	public Page page (String pageName_p) throws MBTAbort {
		return this.scriptExec.getPageMgr().page(pageName_p);
	}

	/**
	 * returns MCaseMgr which manages all MCases created/registered for the model execution.
	 * 
	 * @return
	 */
	public MCaseMgr getMCaseMgr () {
		return this.scriptExec.getMCaseMgr();
	}
	
	/**
	 * Adds a new mcase and returns the MCase created that can be used to add navigation actions.
	 * For example, 
	 * 		$SYS.addMCase('test case 1')
	 * 			.navigateToState('state 1')
	 * 			.exeuteTransition('trans 1', { state -&gt; $SYS.log('MCase 1')});
	 * 
	 * @param mcaseName_p
	 * @return
	 * @throws MBTAbort
	 */
	public MCase addMCase (String mcaseName_p) throws MBTAbort {
		return this.scriptExec.getMCaseMgr().addMCase(mcaseName_p);
	}

	/**
	 * adds an UI page.
	 * 
	 * @param pageName_p
	 * @return Page object to be used for chained action calls.
	 * @throws MBTAbort
	 */
	public Page addPage (String pageName_p) throws MBTAbort {
		return this.scriptExec.getPageMgr().addPage(pageName_p);
	}

//	/**
//	 * reads a tab delimited file from model's dataset folder.
//	 * @param filePath_p
//	 * @return List&lt;Map&lt;String, Object&gt;&gt; or null if file does not exist or empty
//	 * @throws Exception
//	 */
//	public DataSet readDataSet (String dsName_p) throws Exception {
//		return new DataSet(dsName_p, this.modelMgr.getDatasetFolderPath());
//	}
//
//	/**
//	 * reads a tab delimited file from model's report folder and index it on
//	 * the specified column for quick access.
//	 * 
//	 * You can save the indexed map returned from this function to a user variable.
//	 * for example: $VAR.uimap = $SYS.readIndexMap ("myUIMap.tsv", "uiid");
//	 * 
//	 * You access the data element with: $SYS.log($VAR.uimap.uiid101.col2);
//	 * 
//	 * @param filePath_p
//	 * @param indexColName_p
//	 * @return
//	 * @throws Exception
//	 */
//	public Map<String, Map<String, Object>> readIndexMap (String filePath_p, String indexColName_p) throws Exception {
//		List<Map<String, Object>> dataRows = this.readTSV(filePath_p);
//		if (dataRows==null || dataRows.isEmpty()) {
//			return null;
//		}
//		Map<String, Map<String, Object>> retList = new java.util.HashMap<>(dataRows.size());
//		dataRows.stream().forEach(r -> {
//			retList.put((String)r.get(indexColName_p), r);
//		});
//		return retList;
//	}
//	
	/**
	 * returns all user variables.
	 * @return HashMap of variables
	 */
	public Map<String, Object> getVars () {
		return this.scriptExec.getVarMap();
	}
	
	/**
	 * returns list of dataset  names.
	 * @return list of string
	 */
	public List<String> listDataSets () {
		return new ArrayList<String>(this.scriptExec.getDataSetMap().keySet());
	}
	
	
	
	public String getModelFolderPath() {
		return this.scriptExec.getModelMgr().getFolderPath();
	}
	

	public List<String> getLastFailure () {
		List<TagExec> failList = this.execDir.getSequenceNavigator().getCurTravObj().getFailedTagChecks();
		if (failList.isEmpty()) {
			return null;
		}
		List<String> retList = failList.stream().map(s -> s.getExecMsg()).collect(Collectors.toList());
		return retList;
	}

//	public String getLastError () {
//		MBTAbort ex = this.execDir.getSequenceNavigator().getCurTravObj().getMBTAbortException();
//		if (ex == null) {
//			return null;
//		}
//		return ex.getMessage();
//	}
	
}