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

import java.util.List;

import com.testoptimal.exec.mscript.MbtScriptExecutor;
import com.testoptimal.util.StringUtil;

/**
 * Contains a single tag execution info, e.g. one traversal of transition for a tag.
 * @author YXL01
 *
 */
public class TagExec implements Comparable<TagExec> {
	public static final String StateTraversal = "State Traversal";
	public static final String TransTraversal = "Transition Traversal";
	
	private String reqTag;
	private String stateName;
	private String transName;
	private String trace;
	
	/**
	 * state id, stateid.transid, mcase id, user assigned id which
	 * takes the precedent.
	 */
	private String assertID; 
	
	private boolean passed;
	
	private String execMsg;
		
	public TagExec (MbtScriptExecutor scriptExec_p, String reqTag_p, boolean passed_p, 
			String execMsg_p, String assertID_p, String stateName_p, String transName_p, String uid_p) {
		this.reqTag = reqTag_p;
		this.stateName = stateName_p;
		this.transName = transName_p;
		this.assertID = assertID_p;
		this.passed = passed_p;
		this.execMsg = execMsg_p;
		this.checkNulls();
	}

	private void checkNulls() {
		if (this.stateName==null) this.stateName = "";
		if (this.transName==null) this.transName = "";
		if (this.assertID==null) this.assertID = "";
		if (this.execMsg==null) this.execMsg = "";
	}
	
	public String getReqTag () {
		return this.reqTag;
	}
	
	public String getAssertID() {
		if (this.assertID==null) return "";
		else return this.assertID;
	}

	public boolean isPassed() {
		return this.passed;
	}
	
	public String getExecMsg() {
		return this.execMsg;
	}
	
	public String getStateName() {
		return this.stateName;
	}
	
	public String getTransName() {
		return this.transName;
	}

	public boolean isStateTraversal() {
		if (this.execMsg==null) return  false;
		if (this.execMsg.equalsIgnoreCase(StateTraversal)) return true;
		else return false;
	}
	
	public boolean isTransTraversal() {
		if (this.execMsg==null) return  false;
		if (this.execMsg.equalsIgnoreCase(TransTraversal)) return true;
		else return false;
	}

	public boolean isTraversal() {
		if (this.isStateTraversal() || this.isTransTraversal()) return true;
		else return false;
	}
	

	public static boolean isTagExecListPassed(List<TagExec> tagExecList_p) {
		for (TagExec tagExec: tagExecList_p) {
			if (!tagExec.isPassed()) {
				return false;
			}
		}
		return true;
	}

	public int compareTo (TagExec tagExec_p) {
		int cmp = 0;
		cmp = StringUtil.compareTwoString(tagExec_p.stateName, this.stateName);
		if (cmp!=0) return cmp;
		cmp = StringUtil.compareTwoString(tagExec_p.transName, this.transName);
		if (cmp!=0) return cmp;
		cmp = StringUtil.compareTwoString(tagExec_p.assertID, this.assertID);
		if (cmp!=0) return cmp;
		cmp = (this.passed?0:1) - (tagExec_p.passed?0:1);
		if (cmp!=0) return cmp;
		cmp = StringUtil.compareTwoString(tagExec_p.execMsg, this.execMsg);
		return cmp;
	}

	public void setTrace(String trace_p) {
		this.trace = trace_p;
	}
	
	public String getTrace () {
		return this.trace;
	}
	
}
