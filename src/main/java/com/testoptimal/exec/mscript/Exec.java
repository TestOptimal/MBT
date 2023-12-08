package com.testoptimal.exec.mscript;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import com.testoptimal.exec.ExecutionDirector;
import com.testoptimal.exec.ExecutionSetting;
import com.testoptimal.exec.FSM.DataSet;
import com.testoptimal.exec.FSM.ModelMgr;
import com.testoptimal.exec.FSM.TravBase;
import com.testoptimal.exec.exception.MBTAbort;
import com.testoptimal.exec.mcase.MCase;
import com.testoptimal.exec.page.Page;
import com.testoptimal.graphing.GenGraph;
import com.testoptimal.stats.TagExec;
import com.testoptimal.stats.exec.ModelExec;
import com.testoptimal.stats.exec.TestCaseStep;

/**
 * <p>
 * Provides system functions for MScript.
 * </p>
 * 
 *
 */
public class Exec {
	private MbtScriptExecutor scriptExec;
	private ModelMgr modelMgr;
	private ExecutionDirector execDir;
	private ExecutionSetting execSetting;

	public Exec(MbtScriptExecutor scriptExec_p) {
		this.scriptExec = scriptExec_p;
		this.modelMgr = this.scriptExec.getModelMgr();
		this.execDir = this.scriptExec.getExecDirector();
		this.execSetting = this.execDir.getExecSetting();
	}

	/**
	 * logs a message to model log
	 */
	public void log(Object msg_p) {
		this.execDir.log(msg_p.toString());
	}

	/**
	 * cause model execution to abort and trigger Error if declared.
	 */
	public void abort(String msg_p) throws Exception {
		this.execDir.interrupt();
		throw new Exception(msg_p);
	}
		
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
		ModelExec collStat = this.execDir.getExecStats();
		List<TestCaseStep> stepList = collStat.getCurTestCase().stepList.stream()
				.filter(s-> collStat.transMap.containsKey(s.UID))
				.collect(Collectors.toList());
		
		if (maxTraceCount_p > 0 && stepList.size() > maxTraceCount_p) {
			stepList = stepList.subList(0, maxTraceCount_p);
		}
		
		return stepList.stream().map(s -> collStat.transMap.get(s.UID).transName + ":" + s.UID)
			.collect (Collectors.joining(delimiter_p));
	}

	/**
	 * generate traversal graph from home/start state to the current state.
	 * Typically you would call this function in the final state's trigger to
	 * generate a test path that corresponds to a test case/scenario.
	 * 
	 * <p>
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
	 * trigger to generate a test sequence that corresponds to a test case /
	 * scenario.
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
	public void setSeed(long seed_p) {
		this.execSetting.setSeed(seed_p);
	}

	/**
	 * returns the model execution stats collection object.
	 * @return
	 * @throws Exception
	 */
	public ModelExec getStats() throws Exception {
		ModelExec modelExec = this.scriptExec.getExecDirector().getExecStats();
		return modelExec;
	}

	/**
	 * returns the report folder file path for the current running model.
	 */
	public String getReportFolderPath() {
		return this.modelMgr.getReportFolderPath();
	}
	
	public List<Page> pages() {
		return this.scriptExec.getPageMgr().getPageList();
	}
	public Page addPage (String name_p) throws MBTAbort {
		return this.scriptExec.getPageMgr().addPage(name_p);
	}
	public Page page (String name_p) {
		return this.scriptExec.getPageMgr().getPage(name_p);
	}

	public List<MCase> MCases () {
		return this.scriptExec.getMCaseMgr().getMCaseList();
	}
	public MCase addMCase (String name_p) {
		return this.scriptExec.getMCaseMgr().addMCase(name_p);
	}
	public MCase mcase (String name_p) {
		return this.scriptExec.getMCaseMgr().getMCase(name_p);
	}

	public List<DataSet> datasets () {
		return this.execDir.getDataSetList();
	}
	public DataSet dataset (String name_p) {
		return this.execDir.getDataSetList().stream().filter(d -> d.dsName.equals(name_p)).findFirst().orElse(null);
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
	 * returns the current traversal object which is a wrapper of either state or transition.  Use this
	 * traversal object to log success/failure.
	 * 
	 * @return
	 */
	public TravBase getCurTraverseObj() {
		return this.execDir.getSequenceNavigator().getCurTravObj();
	}
	
	
	public List<String> getLastFailure () {
		List<TagExec> failList = this.execDir.getSequenceNavigator().getCurTravObj().getFailedTagChecks();
		if (failList.isEmpty()) {
			return null;
		}
		List<String> retList = failList.stream().map(s -> s.getExecMsg()).collect(Collectors.toList());
		return retList;
	}
	
	public ExecutionSetting getExecSetting() {
		return this.execSetting;
	}
	
	public String getModelName() {
		return this.modelMgr.getModelName();
	}
}