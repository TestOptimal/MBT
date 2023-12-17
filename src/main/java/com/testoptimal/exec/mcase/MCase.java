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

package com.testoptimal.exec.mcase;

import java.util.List;

import groovy.lang.Closure;

/**
 * MCase - custom test case consists of a serial states and transitions.  The states and transitions
 * may not need to be adjacent to each other, in which case TestOptimal is able to find the shortest
 * path to traverse the model to the next state/transition in MCase.
 *
 * MCase is executed by MCase Sequencer.
 * 
 * For example:
 * $SYS.getMCaseMgr().newMCase ("use case name")
 * 		.navigateToState ("state id", {);
 * mcase.executeTransition ("trans id", func);
 * 
 * @author yxl01
 *
 */
public class MCase {
	private String name;
	private List<MStep> stepList = new java.util.ArrayList<>();
	
	public MCase (String name_p) {
		this.name = name_p;
	}
	
	/**
	 * navigate from current state to the desired state through the shortest path.
	 * @param stateID_p - can be a state id (if unique) or state UID.
	 * @return MCase object for chaining additional navigations.
	 */
	public MCase navigateToState (String stateID_p) {
		MStep step = new MStep();
		step.toID = stateID_p;
		step.isState = true;
		this.stepList.add(step);
		return this;
	}

//	/**
//	 * navigate from current state to the desired state through the shortest path.
//	 * @param stateID_p - can be a state id (if unique) or state UID.
//	 * @param funcBefore_p closure function to be executed before state TRIGGER
//	 * @return MCase object for chaining additional navigations.
//	 */
//	public MCase navigateToState (String stateID_p, Closure <Object> funcBefore_p) {
//		MStep step = new MStep();
//		step.toID = stateID_p;
//		step.isState = true;
//		step.funcBefore = funcBefore_p;
//		this.stepList.add(step);
//		return this;
//	}
//
//	/**
//	 * navigate from current state to the desired state through the shortest path.
//	 * @param stateID_p - can be a state id (if unique) or state UID.
//	 * @param funcBefore_p closure function to be executed before state TRIGGER
//	 * @param funcAfter_p closure function to be executed after state TRIGGER
//	 * @return MCase object for chaining additional navigations.
//	 */
//	public MCase navigateToState (String stateID_p, Closure <Object> funcBefore_p, Closure<Object> funcAfter_p) {
//		MStep step = new MStep();
//		step.toID = stateID_p;
//		step.isState = true;
//		step.funcBefore = funcBefore_p;
//		step.funcAfter = funcAfter_p;
//		this.stepList.add(step);
//		return this;
//	}

	/**
	 * from current state jump/skip to the desired state without actually
	 * traversing/executing any state/trans.  In conjunction with this function,
	 * you must also adjust AUT state accordingly.
	 * 
	 * @param stateID_p - can be a state id (if unique) or state UID.
	 * @return MCase object for chaining additional navigations.
	 */
	public MCase skipToState (String stateID_p) {
		MStep step = new MStep();
		step.toID = stateID_p;
		step.isState = true;
		this.stepList.add(step);
		step.naveTo = false;
		return this;
	}

	/**
	 * execute the transition from the current state.
	 * @param transID_p - can be a transition id (if unique) or transtion UID.
	 * @return MCase object for chaining additional navigations.
	 */
	public MCase executeTransition (String transID_p) {
		MStep step = new MStep();
		step.toID = transID_p;
		step.isState = false;
		this.stepList.add(step);
		return this;
	}

	/**
	 * execute the transition from the current state, and execute the
	 * function passed in before the default transition TRIGGER has been executed.
	 * 
	 * @param transID_p - can be a transition id (if unique) or transition UID.
	 * @param funcBefore_p closure function
	 * @return MCase object for chaining additional navigations.
	 */
	public MCase executeTransition (String transID_p, Closure<Object> funcBefore_p) {
		MStep step = new MStep();
		step.toID = transID_p;
		step.isState = false;
		step.funcBefore = funcBefore_p;
		this.stepList.add(step);
		return this;
	}
	
	/**
	 * execute the transition from the current state, and execute the
	 * function passed in before and after the default transition TRIGGER.
	 * 
	 * @param transID_p
	 * @param funcBefore_p closure function
	 * @param funcAfter_p closure function
	 * @return MCase objectc for chaining additional navigation
	 */
	public MCase executeTransition (String transID_p, Closure<Object> funcBefore_p, Closure<Object> funcAfter_p) {
		MStep step = new MStep();
		step.toID = transID_p;
		step.isState = false;
		step.funcBefore = funcBefore_p;
		step.funcBefore = funcAfter_p;
		this.stepList.add(step);
		return this;
	}
	

	/**
	 * returns the name of this MCase
	 * @return
	 */
	public String getName() {
		return this.name;
	}
	
	public List<MStep> getStepList () {
		return this.stepList;
	}

}
