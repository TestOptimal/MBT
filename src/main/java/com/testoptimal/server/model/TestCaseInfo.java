package com.testoptimal.server.model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.testoptimal.stats.exec.ExecStateTrans;
import com.testoptimal.stats.exec.ExecTestCase;
import com.testoptimal.stats.exec.ModelExec;
import com.testoptimal.stats.exec.TestCaseStep;
import com.testoptimal.stats.exec.TestCaseStepItem;
import com.testoptimal.util.StringUtil;

public class TestCaseInfo {
	public String tcName;
	public ModelExec.Status status = ModelExec.Status.failed;
	public List<StepInfo> stepList = new java.util.ArrayList<>();

	public TestCaseInfo (ExecTestCase tcObj_p, Map<String, ExecStateTrans> stateTransMap_p) {
		this.tcName = tcObj_p.tcName;
		this.status = tcObj_p.status;
		
		this.stepList = tcObj_p.stepList.stream()
			.map(step -> {
				return new StepInfo(step, stateTransMap_p);
			})
			.collect(Collectors.toList());
	}
	
	public class StepInfo {
		public int stepID;
		public String stateName;
		public String transName;
		public String UID;
		public ModelExec.Status status;
		public List<ReqExecInfo> reqExecList;
		public StepInfo (TestCaseStep step_p, Map<String, ExecStateTrans> stateTransMap_p) {
			ExecStateTrans stateTrans = stateTransMap_p.get(step_p.UID);
			this.stateName = stateTrans.stateName;
			this.transName = stateTrans.transName;
			this.UID = stateTrans.UID;
			this.status = step_p.status;
			this.reqExecList = step_p.itemList.stream().map(item -> {
				return new ReqExecInfo(item);
			}).collect(Collectors.toList());
		}
	}
	
	public class ReqExecInfo {
		public String assertCode;
		public String reqTag;
		public ModelExec.Status status;
		public String checkMsg;
		
		public ReqExecInfo (TestCaseStepItem item_p) {
			this.assertCode = item_p.assertCode;
			this.reqTag = StringUtil.isEmpty(item_p.reqTag)?"empty":item_p.reqTag;
			this.checkMsg = item_p.checkMsg;
			this.status = item_p.status;
		}
	}
}

