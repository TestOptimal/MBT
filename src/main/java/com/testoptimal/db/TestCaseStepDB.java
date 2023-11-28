package com.testoptimal.db;

import java.util.List;
import java.util.stream.Collectors;

import com.testoptimal.stats.TagExec;

public class TestCaseStepDB {
	public String UID;
	public ModelExecDB.Status status = ModelExecDB.Status.failed;
	public long perfMillis;
	
	public List<TestCaseStepItemDB> itemList = new java.util.ArrayList<>();
	
	public TestCaseStepDB (String UID_p, List<TagExec> checkList_p) {
		this.UID = UID_p;
		this.itemList = checkList_p.stream()
						.map(c-> new TestCaseStepItemDB(c.getReqTag()==null?"":c.getReqTag(), c.isPassed()?ModelExecDB.Status.passed:ModelExecDB.Status.failed, c.getExecMsg(), c.getAssertID()))
						.collect(Collectors.toList());
		int failCount = (int)this.itemList.stream().filter(item-> !item.isPassed()).count();
		this.status = failCount>0?ModelExecDB.Status.failed:ModelExecDB.Status.passed;
	}
}
