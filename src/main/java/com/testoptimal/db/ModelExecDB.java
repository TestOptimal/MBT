package com.testoptimal.db;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import com.testoptimal.exec.ExecutionSetting;
import com.testoptimal.exec.FSM.ModelMgr;
import com.testoptimal.exec.FSM.RequirementMgr;
import com.testoptimal.scxml.ScxmlNode;
import com.testoptimal.server.config.Config;
import com.testoptimal.server.config.ConfigVersion;
import com.testoptimal.stats.ModelExecSummary;
import com.testoptimal.stats.TagExec;
import com.testoptimal.util.StringUtil;

public class ModelExecDB {
	public static enum Status {passed, failed};
	
	public String modelName; // this is set for informational only, does not get written to database.
	public String mbtSessID;
	public String filePath;
	public String statDesc;
	
	public ModelExecSummary execSummary;
	
	public Map<String, Object> execOptions = new java.util.HashMap<>();
	
	public List<ExecTestCaseDB> tcList = new java.util.ArrayList<>();
	public List<ExecReqDB> reqList = new java.util.ArrayList<>();
	public List<ExecStateTransDB> stateTransList = new java.util.ArrayList<>();
		
	private transient AtomicInteger tcCounter = new AtomicInteger(0);
		private transient ExecTestCaseDB curTestCase;
	private transient int maxTestCaseNum = 1;

	public ModelExecDB (String mbtSessID_p, ModelMgr modelMgr_p, ExecutionSetting execSetting_p) {
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
		if (Strings.isNullOrEmpty(this.statDesc)) {
			this.statDesc = "Exec " + this.execSummary.startDT;
		}
		this.execOptions.putAll(execSetting_p.getOptions());
		this.execOptions.put("AUTVer", scxml.getVersionAUT()); 
		this.execOptions.put("ReqVer", scxml.getVersionReq()); 
		this.execOptions.put("TOVer", ConfigVersion.getReleaseLabel());
		this.execOptions.put("ModelVer", scxml.getVersion());
		this.execOptions.put("Server", Config.getHostName());
		
		scxml.getAllStates().stream().forEach(s -> {
			this.stateTransList.add(new ExecStateTransDB(s));
			this.stateTransList.addAll(s.getTransitions().stream().map(t-> new ExecStateTransDB(t)).collect(Collectors.toList()));
		});
		this.execSummary.stateNum = (int) this.stateTransList.stream().filter(s -> s.transName == null).count();
		this.execSummary.transNum = this.stateTransList.size() - this.execSummary.stateNum;
		
		try {
			this.reqList = RequirementMgr.getInstance().getRequirement(modelMgr_p).stream().map(r -> new ExecReqDB(r.name, r.priority)).collect(Collectors.toList());
		}
		catch (Exception e) {
			//
		}
	}

	
	@JsonIgnore
	public ExecTestCaseDB newTestCase () {
		String tcName = StringUtil.genTCName("TC_", tcCounter.addAndGet(1), 6);
		this.curTestCase = new ExecTestCaseDB(tcName);
		this.tcList.add(this.curTestCase);
		if (this.tcList.size() >= this.maxTestCaseNum) {
			this.tcList = this.tcList.subList(this.tcList.size() - this.maxTestCaseNum, this.maxTestCaseNum);
		}
		return this.curTestCase;
	}
	

	@JsonIgnore
	public void addTestStep (long perfMillis_p, String UID_p, List<TagExec> checkList_p) {
		Optional<ExecStateTransDB> e = this.stateTransList.stream().filter(s -> UID_p.equals(s.UID)).findFirst();
		if (e.isPresent()) {
			this.curTestCase.addStep(perfMillis_p, e.get(), checkList_p);
		}
		Map<String, List<TagExec>> checkMap = checkList_p.stream().collect(Collectors.groupingBy(c -> c.getReqTag()));
		checkMap.keySet().forEach( t -> {
			Optional<ExecReqDB> rOpt = this.reqList.stream().filter(r -> r.reqTag.equals(t)).findFirst();
			ExecReqDB req;
			if (rOpt.isPresent()) {
				req = rOpt.get();
			}
			else {
				req = new ExecReqDB(t, "medium");
				this.reqList.add(req);
			}
			req.passCount += checkMap.get(t).stream().filter(s -> s.isPassed()).count();
			req.failCount += checkMap.get(t).stream().filter(s -> !s.isPassed()).count();
		});
	}
	

	@JsonIgnore
	public ExecTestCaseDB getCurTestCase () {
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
	
	@JsonIgnore
	public Map<String, ExecStateTransDB>getStateTransMap() {
		return this.stateTransList.stream().collect(Collectors.toMap(st -> st.UID, st -> st));
	}
}
