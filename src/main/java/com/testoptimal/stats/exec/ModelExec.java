package com.testoptimal.stats.exec;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testoptimal.exec.ExecutionSetting;
import com.testoptimal.exec.FSM.ModelMgr;
import com.testoptimal.exec.FSM.RequirementMgr;
import com.testoptimal.scxml.ScxmlNode;
import com.testoptimal.server.config.Config;
import com.testoptimal.server.config.ConfigVersion;
import com.testoptimal.stats.ModelExecSummary;
import com.testoptimal.stats.TagExec;
import com.testoptimal.util.StringUtil;

public class ModelExec {
	public static enum Status {passed, failed};
	
	public String modelName; // this is set for informational only, does not get written to database.
	public String mbtSessID;
	public String filePath;
	public String statDesc;
	
	public ModelExecSummary execSummary;
	
	public Map<String, Object> execOptions = new java.util.HashMap<>();
	
	public List<ExecTestCase> tcList = new java.util.ArrayList<>();
	public List<ExecReq> reqList = new java.util.ArrayList<>();
	public Map<String, ExecStateTrans> stateMap = new java.util.HashMap<>();
	public Map<String, ExecStateTrans> transMap = new java.util.HashMap<>();
		
	private transient AtomicInteger tcCounter = new AtomicInteger(0);
		private transient ExecTestCase curTestCase;
	private transient int maxTestCaseNum = 1;

	public ModelExec (String mbtSessID_p, ModelMgr modelMgr_p, ExecutionSetting execSetting_p) {
		this.modelName = modelMgr_p.getModelName();
		this.mbtSessID = mbtSessID_p;
		this.filePath = modelMgr_p.getStatsFolderPath() + this.mbtSessID + ".json";
		this.mbtSessID = mbtSessID_p;
		ScxmlNode scxml = modelMgr_p.getScxmlNode();
		this.maxTestCaseNum = scxml.getMiscNode().getMaxTestCaseNum();
		this.execSummary = new ModelExecSummary();
		this.execSummary.modelName = this.modelName;
		this.execSummary.mbtSessID = this.mbtSessID;
		this.execSummary.startDT = new java.sql.Timestamp(System.currentTimeMillis());
		this.execSummary.mbtSequencer = execSetting_p.getCurMbtMode();
		this.statDesc = (String) execSetting_p.getOption("statDesc");
		if (StringUtil.isEmpty(this.statDesc)) {
			this.statDesc = "Exec " + this.execSummary.startDT;
		}
		this.execOptions.putAll(execSetting_p.getOptions());
		this.execOptions.put("AUTVer", scxml.getVersionAUT()); 
		this.execOptions.put("ReqVer", scxml.getVersionReq()); 
		this.execOptions.put("TOVer", ConfigVersion.getReleaseLabel());
		this.execOptions.put("ModelVer", scxml.getVersion());
		this.execOptions.put("Server", Config.getHostName());
		
		scxml.getAllStates().stream().forEach(s -> {
			this.stateMap.put(s.getUID(), new ExecStateTrans(s));
			s.getTransitions().stream().forEach(t-> this.transMap.put(t.getUID(), new ExecStateTrans(t)));
		});
		this.execSummary.stateNum = (int) this.stateMap.size();
		this.execSummary.transNum = this.transMap.size();
		
		try {
			this.reqList = RequirementMgr.getInstance().getRequirement(modelMgr_p).stream().map(r -> new ExecReq(r.name, r.priority)).collect(Collectors.toList());
		}
		catch (Exception e) {
			//
		}
	}

	
	@JsonIgnore
	public ExecTestCase newTestCase () {
		String tcName = StringUtil.genTCName("TC_", tcCounter.addAndGet(1), 6);
		this.curTestCase = new ExecTestCase(tcName);
		this.tcList.add(this.curTestCase);
		if (this.tcList.size() >= this.maxTestCaseNum) {
			this.tcList = this.tcList.subList(this.tcList.size() - this.maxTestCaseNum, this.maxTestCaseNum);
		}
		return this.curTestCase;
	}
	

	@JsonIgnore
	public void addTestStepState (long perfMillis_p, String UID_p, List<TagExec> checkList_p) {
		this.addTestStep(perfMillis_p, this.stateMap.get(UID_p), checkList_p);
	}
	public void addTestStepTrans (long perfMillis_p, String UID_p, List<TagExec> checkList_p) {
		this.addTestStep(perfMillis_p, this.transMap.get(UID_p), checkList_p);
	}
	private void addTestStep (long perfMillis_p, ExecStateTrans st_p, List<TagExec> checkList_p) {
		this.curTestCase.addStep(perfMillis_p, st_p, checkList_p);

		Map<String, List<TagExec>> checkMap = checkList_p.stream().filter(c -> c.getReqTag()!=null).collect(Collectors.groupingBy(c -> c.getReqTag()));
		checkMap.keySet().forEach( t -> {
			Optional<ExecReq> rOpt = this.reqList.stream().filter(r -> r.reqTag.equals(t)).findFirst();
			ExecReq req;
			if (rOpt.isPresent()) {
				req = rOpt.get();
			}
			else {
				req = new ExecReq(t, "medium");
				this.reqList.add(req);
			}
			req.passCount += checkMap.get(t).stream().filter(s -> s.isPassed()).count();
			req.failCount += checkMap.get(t).stream().filter(s -> !s.isPassed()).count();
		});
	}
	

	@JsonIgnore
	public ExecTestCase getCurTestCase () {
		return this.curTestCase;
	}
	
	@JsonIgnore
	public void complete() {
		if (this.curTestCase != null) {
			this.curTestCase.completeTestCase();
		}
		this.execSummary.endDT = new java.util.Date();
		this.execSummary.summarize(this);
	}
}
