package com.testoptimal.stats.exec;

import java.util.List;
import java.util.stream.Collectors;

import com.testoptimal.stats.TagExec;

public class TestCaseStep {
	public String UID;
	public ModelExec.Status status = ModelExec.Status.failed;
	public long perfMillis;
	
	public List<TestCaseStepItem> itemList = new java.util.ArrayList<>();
	
	public TestCaseStep (String UID_p, List<TagExec> checkList_p) {
		this.UID = UID_p;
		this.itemList = checkList_p.stream()
						.map(c-> new TestCaseStepItem(c.getReqTag()==null?"":c.getReqTag(), c.isPassed()?ModelExec.Status.passed:ModelExec.Status.failed, c.getExecMsg(), c.getAssertID()))
						.collect(Collectors.toList());
		int failCount = (int)this.itemList.stream().filter(item-> !item.isPassed()).count();
		this.status = failCount>0?ModelExec.Status.failed:ModelExec.Status.passed;
	}
}
