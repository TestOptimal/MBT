package com.testoptimal.server.model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.testoptimal.db.ExecStateTransDB;
import com.testoptimal.db.ExecTestCaseDB;
import com.testoptimal.db.ModelExecDB;
import com.testoptimal.db.TestCaseStepDB;
import com.testoptimal.db.TestCaseStepItemDB;
import com.testoptimal.util.StringUtil;

public class TestCaseInfo {
	public String tcName;
	public ModelExecDB.Status status = ModelExecDB.Status.failed;
	public List<StepInfo> stepList = new java.util.ArrayList<>();

	public TestCaseInfo (ExecTestCaseDB tcObj_p, Map<String, ExecStateTransDB> stateTransMap_p) {
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
		public ModelExecDB.Status status;
		public List<ReqExecInfo> reqExecList;
		public StepInfo (TestCaseStepDB step_p, Map<String, ExecStateTransDB> stateTransMap_p) {
			ExecStateTransDB stateTrans = stateTransMap_p.get(step_p.UID);
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
		public ModelExecDB.Status status;
		public String checkMsg;
		
		public ReqExecInfo (TestCaseStepItemDB item_p) {
			this.assertCode = item_p.assertCode;
			this.reqTag = StringUtil.isEmpty(item_p.reqTag)?"empty":item_p.reqTag;
			this.checkMsg = item_p.checkMsg;
			this.status = item_p.status;
		}
	}
}

