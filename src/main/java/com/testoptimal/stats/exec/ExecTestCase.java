/***********************************************************************************************
 * Copyright (c) 2009-2024 TestOptimal.com
 *
 * This file is part of TestOptimal MBT.
 *
 * TestOptimal MBT is free software: you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *
 * TestOptimal MBT is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See 
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with TestOptimal MBT. 
 * If not, see <https://www.gnu.org/licenses/>.
 ***********************************************************************************************/

package com.testoptimal.stats.exec;

import java.sql.Timestamp;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.testoptimal.stats.TagExec;

public class ExecTestCase {
	public String tcName;
	public ModelExec.Status status;
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Timestamp startTS;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Timestamp endTS;
	
	public List<TestCaseStep> stepList = new java.util.ArrayList<>();
	
	
	protected ExecTestCase(String tcName_p) {
		this.tcName = tcName_p;
		this.startTS = new java.sql.Timestamp(System.currentTimeMillis());

	}
	
	protected TestCaseStep addStep (long perfMillis_p, ExecStateTrans stateTrans_p, List<TagExec> checkList_p) {
		TestCaseStep step = new TestCaseStep(stateTrans_p.UID, checkList_p);
		step.perfMillis = perfMillis_p;
		this.stepList.add(step);
		if (!checkList_p.isEmpty()) {
			if (stateTrans_p.avgMillis==null) {
				stateTrans_p.avgMillis = (int) step.perfMillis;
				stateTrans_p.minMillis = (int) step.perfMillis;
				stateTrans_p.maxMillis = (int) step.perfMillis;
			}
			else {
				int cnt1 = stateTrans_p.passCount+stateTrans_p.failCount;
				stateTrans_p.avgMillis = (int) (stateTrans_p.avgMillis * cnt1 + step.perfMillis) / (cnt1 + 1);
				stateTrans_p.minMillis = (int) Math.min(stateTrans_p.minMillis, step.perfMillis);
				stateTrans_p.maxMillis = (int) Math.max(stateTrans_p.maxMillis, step.perfMillis);
			}
			if (stateTrans_p.expectedMillis!=null && stateTrans_p.expectedMillis > 0 && step.perfMillis > stateTrans_p.expectedMillis) {
				stateTrans_p.slowCount++;
			}
		}
		if (step.status == ModelExec.Status.passed) {
			stateTrans_p.passCount++;
		}
		else stateTrans_p.failCount++;
		this.status = this.stepList.stream().filter(step1-> step1.status != ModelExec.Status.passed).count() > 0?ModelExec.Status.failed:ModelExec.Status.passed;
		return step;
	}
	
	public void completeTestCase () {
		this.endTS = new java.sql.Timestamp(System.currentTimeMillis());
		this.status = this.stepList.stream().filter(step1-> step1.status != ModelExec.Status.passed).count() > 0?ModelExec.Status.failed:ModelExec.Status.passed;
	}
	
}
