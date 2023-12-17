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

package com.testoptimal.stats;

import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testoptimal.stats.exec.ModelExec;
import com.testoptimal.stats.exec.ModelExec.Status;

public class ModelExecSummary {
	public String modelName;
	public String mbtSessID;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public java.util.Date startDT;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public java.util.Date endDT;	
	
	public String mbtSequencer;
	public ModelExec.Status status;

	public int stateNum = 0;
	public int stateCovered = 0;
	public int stateTraversal = 0;
	public int transNum = 0;
	public int transCovered = 0;
	public int transTraversal = 0;

	public int reqNum = 0;
	public int reqPassed = 0;
	public int reqFailed = 0;
	public int reqCovered = 0;
	public int reqNotCovered = 0;
	public int reqTraversal = 0;

	public int testCaseNum;
	public int testCasePassed;
	public int testCaseFailed;
	public int testStepNum;
	public int testItemNum;
	
	
	@JsonIgnore
	public void summarize(ModelExec modelExec_p) {
		this.stateCovered = (int) modelExec_p.stateMap.values().stream()
				.filter(s -> s.passCount + s.failCount >= s.minTravRequired).count();
		this.transCovered = (int) modelExec_p.transMap.values().stream()
				.filter(s -> s.passCount + s.failCount >= s.minTravRequired).count();
		this.stateTraversal = modelExec_p.stateMap.values().stream().collect(Collectors.summingInt(s -> s.passCount+s.failCount));
		this.transTraversal = modelExec_p.transMap.values().stream().collect(Collectors.summingInt(s -> s.passCount+s.failCount));
		
		this.reqNum = modelExec_p.reqList.size();
		this.reqPassed = (int) modelExec_p.reqList.stream().filter(r -> r.passCount > 0 && r.failCount == 0).count();
		this.reqFailed = (int) modelExec_p.reqList.stream().filter(r -> r.failCount > 0).count();
		this.reqNotCovered = modelExec_p.reqList.size() - this.reqPassed - this.reqFailed;
		this.reqCovered = this.reqNum - this.reqNotCovered;
		this.reqTraversal = (int) modelExec_p.reqList.stream().collect(Collectors.summingInt(s -> s.passCount + s.failCount));
		
		this.testCaseNum = modelExec_p.tcList.size();
		this.testCasePassed = (int) modelExec_p.tcList.stream().filter(t -> t.status==Status.passed).count();
		this.testCaseFailed = (int) modelExec_p.tcList.stream().filter(t -> t.status==Status.failed).count();
		this.testStepNum = (int) modelExec_p.tcList.stream().flatMap(t -> t.stepList.stream()).count();
		this.testItemNum = (int) modelExec_p.tcList.stream().flatMap(t -> t.stepList.stream()).flatMap(s -> s.itemList.stream()).count();
		this.status = this.testCaseFailed == 0 && this.testCasePassed > 0? ModelExec.Status.passed: ModelExec.Status.failed;
	}
}
	