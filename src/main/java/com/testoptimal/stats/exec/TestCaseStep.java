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
