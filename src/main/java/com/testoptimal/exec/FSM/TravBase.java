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

package com.testoptimal.exec.FSM;

import java.util.List;
import java.util.stream.Collectors;

import com.testoptimal.exec.ExecutionDirector;
import com.testoptimal.exec.exception.MBTAbort;
import com.testoptimal.exec.mcase.MStep;
import com.testoptimal.exec.mscript.MbtScriptExecutor;
import com.testoptimal.exec.navigator.StopMonitor;
import com.testoptimal.stats.TagExec;

abstract public class TravBase {
	public static enum TriggerType {start, end, fail, state, trans};

	protected ExecutionDirector execDir;
	protected StopMonitor stopMonitor;
	protected MbtScriptExecutor scriptExec;
	protected List<TagExec> tagExecList = new java.util.ArrayList<>();
	protected MStep stepObj;
	private boolean newPathInd = false;
	
	public boolean isNewPath () {
		return this.newPathInd;
	}
	
	public State getCurState() {
		if (this instanceof TravState) {
			return ((TravState) this).getState();
		}
		else if (this instanceof TravTrans) {
			Transition trans = ((TravTrans) this).getCurTrans();
			return (State) trans.getFromNode();
		}
		else return null;
	}

	public Transition getCurTrans() {
		if (this instanceof TravState) {
			return null;
		}
		else if (this instanceof TravTrans) {
			return ((TravTrans) this).getTrans();
		}
		else return null;
	}

	public String getCurUID () {
		if (this instanceof TravState) {
			return ((TravState) this).getState().getStateNode().getUID();
		}
		else if (this instanceof TravTrans) {
			return ((TravTrans) this).getTrans().getTransNode().getUID();
		}
		else return null;
	}

	public TravBase (ExecutionDirector execDir_p, boolean newPath_p) {
		this.execDir = execDir_p;
		this.stopMonitor = this.execDir.getSequenceNavigator().getStopMonitor();
		this.scriptExec = this.execDir.getScriptExec();
		this.newPathInd = newPath_p;
	}
	
	public abstract boolean travRun() throws MBTAbort;
	
	public void addTagExec (String tag_p, boolean passed_p, String msg_p, String assertID_p) {
		String transID = this.getCurTrans()==null? null: this.getCurTrans().getTransNode().getUID();
		TagExec tagExec = new TagExec(this.scriptExec, tag_p, passed_p, msg_p, assertID_p, this.getCurState().getStateId(), transID, this.getCurUID());
		this.tagExecList.add(tagExec);
	}
	
	public boolean hasFailed () {
		return this.tagExecList.stream().anyMatch(t -> !t.isPassed());
	}
	
	public List<TagExec> getFailedTagChecks () {
		return this.tagExecList.stream().filter(t -> !t.isPassed()).collect(Collectors.toList());
	}
	
	public MStep getMStep () {
		return this.stepObj;
	}
	
	public void setMStep(MStep stepObj_p) {
		this.stepObj = stepObj_p;
	}
	
	public abstract String genPausedAt();
	
	

	/**
	 * performs the assert on the condition passed in to be true.  If check is
	 * successful, add positive check on the requirement tag, else add a negative
	 * check on the requirement tag with the failMsg_p passed in.
	 * <p>
	 * 
	 * @param tag_p
	 *            requirement tag
	 * @param condition_p
	 * 			  boolean condition
	 * @param failMsg_p
	 *            message to be added to this check if condition_p is false.
	 * 
	 */
	public void assertTrue (String tag_p, boolean condition_p, String failMsg_p) throws Exception {
		if (condition_p) {
			this.addTagExec(tag_p, true, null, null);
		}
		else {
			this.addTagExec(tag_p, false, failMsg_p, null);
		}
	}

	/**
	 * performs the assert on the condition passed in to be true.  If check is
	 * successful, add positive check on the requirement tag, else add a negative
	 * check on the requirement tag with the failMsg_p passed in.
	 * <p>
	 * 
	 * @param tag_p
	 *            requirement tag
	 * @param condition_p
	 * 			  boolean condition
	 * @param failMsg_p
	 *            message to be added to this check if condition_p is false
	 * @param assertID_p
	 *            user assigned unique id for this specific call, this is usually
	 *            used to find the same defect in the defect system from previous
	 *            runs.
	 * 
	 */
	public void assertTrue (String tag_p, boolean condition_p, String failMsg_p, String assertID_p) throws Exception {
		if (condition_p) {
			this.addTagExec(tag_p, true, null, assertID_p);
		}
		else {
			this.addTagExec(tag_p, false, failMsg_p, assertID_p);
		}
	}


	/**
	 * performs the assert on the condition passed in to be false.  If check is
	 * successful, add positive check on the requirement tag, else add a negative
	 * check on the requirement tag with the failMsg_p passed in.
	 * <p>
	 * 
	 * @param tag_p
	 *            requirement tag
	 * @param condition_p
	 * 			  boolean condition
	 * @param failMsg_p
	 *            message to be added to this check if condition_p is true.
	 * 
	 */
	public void assertFalse (String tag_p, boolean condition_p, String failMsg_p) throws Exception {
		this.assertTrue(tag_p, !condition_p, failMsg_p);
	}

	/**
	 * performs the assert on the condition passed in to be false.  If check is
	 * successful, add positive check on the requirement tag, else add a negative
	 * check on the requirement tag with the failMsg_p passed in.
	 * <p>
	 * 
	 * @param tag_p
	 *            requirement tag
	 * @param condition_p
	 * 			  boolean condition
	 * @param failMsg_p
	 *            message to be added to this check if condition_p is true
	 * @param assertID_p
	 *            user assigned unique id for this specific call, this is usually
	 *            used to find the same defect in the defect system from previous
	 *            runs.
	 * 
	 */
	public void assertFalse (String tag_p, boolean condition_p, String failMsg_p, String assertID_p) throws Exception {
		this.assertTrue(tag_p, !condition_p, failMsg_p, assertID_p);
	}

	
	/**
	 * adds a successful requirement check/validation message to the requirement
	 * stat. A requirement may undergo several checks/validations per traversal of
	 * the state/transition.
	 * <p>
	 * 
	 * @param tag_p
	 *            requirement tag
	 * @param msg_p
	 *            message to be added to this check.
	 * 
	 */
	public void addReqPassed(String tag_p, String msg_p) throws Exception {
		this.addTagExec(tag_p, true, msg_p, null);
	}

	/**
	 * adds a successful requirement check/validation message to the requirement
	 * stat. A requirement may undergo several checks/validations per traversal of
	 * the state/transition.
	 * <p>
	 * 
	 * @param tag_p
	 *            requirement tag
	 * @param msg_p
	 *            message to be added to this check.
	 * @param assertID_p
	 *            user assigned unique id for this specific call, this is usually
	 *            used to find the same defect in the defect system from previous
	 *            runs.
	 * 
	 */
	public void addReqPassed(String tag_p, String msg_p, String assertID_p) throws Exception {
		this.addTagExec(tag_p, true, msg_p, assertID_p);
	}

	/**
	 * adds a failed requirement check/validation message to the requirement stat. A
	 * requirement may undergo many checks/validations per traversal of the
	 * state/transition with some passed and some failed. Call this method for each
	 * failed check and call $sys.addReqPassed () for each successful check.
	 * <p>
	 * 
	 * @param tag_p
	 *            requirement tag
	 * @param msg_p
	 *            message to be added to this check.
	 */
	public void addReqFailed(String tag_p, String msg_p) throws Exception {
		this.addTagExec(tag_p, false, msg_p, null);
	}

	/**
	 * adds a failed requirement check/validation message to the requirement stat. A
	 * requirement may undergo many checks/validations per traversal of the
	 * state/transition with some passed and some failed. Call this method for each
	 * failed check and call $sys.addReqPassed () for each successful check.
	 * 
	 * The assertID_p is specified to uniquely identify this failed check in defect
	 * system.
	 * 
	 * @param tag_p
	 *            requirement tag
	 * @param msg_p
	 *            message to be added to this check.
	 * @param assertID_p
	 *            user assigned unique id for this specific call, this is usually
	 *            used to find the same defect in the defect system from previous
	 *            runs.
	 * 
	 */
	public void addReqFailed(String tag_p, String msg_p, String assertID_p) throws Exception {
		this.addTagExec(tag_p, false, msg_p, assertID_p);
	}
}