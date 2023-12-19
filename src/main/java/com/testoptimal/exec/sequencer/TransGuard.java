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

package com.testoptimal.exec.sequencer;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.testoptimal.exec.ExecutionSetting;
import com.testoptimal.exec.FSM.State;
import com.testoptimal.exec.FSM.StateNetwork;
import com.testoptimal.exec.FSM.Transition;
import com.testoptimal.exec.exception.MBTException;
import com.testoptimal.exec.mscript.MbtScriptExecutor;
import com.testoptimal.exec.navigator.SequencePath;
import com.testoptimal.scxml.TransitionNode;

import openOptima.NoSolutionException;

public class TransGuard {
	private static Logger logger = LoggerFactory.getLogger(TransGuard.class);

	private MbtScriptExecutor scriptExec;
	private StateNetwork networkObj;
	
	private int consecutiveGuardResolves = 0;
	private Transition guardedTrans = null;
	private int MaxGuardResolves = 5;
	private Random rand = new Random();

	public TransGuard(MbtScriptExecutor scriptExec_p, ExecutionSetting execSetting_p, StateNetwork netWorkObj_p) {
		this.scriptExec = scriptExec_p;
		this.networkObj = netWorkObj_p;
		Integer g = (Integer) execSetting_p.getOption("Optimal.MaxGuardResolves");
		if (g!=null) {
			this.MaxGuardResolves = g;
		}

	}
	
	public void reset() {
		this.guardedTrans = null;
		this.consecutiveGuardResolves = 0;
	}
	
	// returns the next trans to traverse
	public Transition checkTrans(Transition trans_p, SequencePath seqPath_p ) throws MBTException {
		if (this.scriptExec.evalGuard(trans_p.getTransNode())) {
			if (trans_p==this.guardedTrans) {
				logger.info("Guard resolved on transition " + this.guardedTrans.getTransNode().getEvent() + " at attempt " + this.consecutiveGuardResolves + "");
				this.consecutiveGuardResolves = 0;
				this.guardedTrans = null;
			}
			return trans_p;
		}
		
		// guard failed, find alternate trans to traverse and adjust the path
		this.consecutiveGuardResolves++;
		if (this.guardedTrans==null) {
			this.guardedTrans = trans_p;
		}
		if (this.consecutiveGuardResolves > this.MaxGuardResolves) {
			throw new MBTException ("Unable to resolve guard on transition " + guardedTrans.getTransNode().getEvent() + ": " + this.guardedTrans.getTransNode().getGuard());
		}
		logger.info("searching for alternate path to resolve guard for transition " + this.guardedTrans.getTransNode().getEvent() + ", attempt: " + this.consecutiveGuardResolves);
		State curState = (State) trans_p.getFromNode();
		Transition altTrans = this.findAltTrans(curState, trans_p, seqPath_p);
		if (altTrans==null) {
			throw new MBTException ("Unable to find alternate path to resolve guard error on transition " + this.guardedTrans.getEventId() + ": " + this.guardedTrans.getTransNode().getGuard());
		}
		this.findAltPath(curState, trans_p, altTrans, seqPath_p);
		return altTrans;
	}
	
	private Transition findAltTrans (State curState_p, Transition trans_p, SequencePath seqPath_p) throws MBTException {
		TransitionNode transNode = trans_p.getTransNode();
		List<Transition> list1 = curState_p.getEdgesOut().stream()
				.map(t -> (Transition) t)
				.filter(t -> this.scriptExec.evalGuard(((Transition) t).getTransNode()))
				.toList();
		List<Transition> list2 = list1.stream().filter(t -> transNode.isGuardResolver(t.getTransNode().getUID())).toList();
		if (list2.isEmpty()) {
			list2 = list1;
		}
		if (list2.isEmpty()) {
			String exceptMsg = curState_p.getStateId() + "." + trans_p.getTransNode().getEvent() 
					+ ": Unresolved guard: " + trans_p.getTransNode().getGuard();
			throw new MBTException (exceptMsg);
		}
		Transition altTrans = list2.get(this.rand.nextInt(list2.size()));
		return altTrans;
	}
	
	private List<Transition> findAltPath (State curState_p, Transition trans_p, Transition altTrans_p, SequencePath seqPath_p) throws MBTException {
		Transition resolverTrans = null;
		TransitionNode transNode = trans_p.getTransNode();
		if (transNode.isGuardResolver(altTrans_p.getTransNode().getUID())) {
			resolverTrans = altTrans_p;
		}
		else {
			List<Transition> tlist = this.networkObj.getTransByUIDList(Arrays.asList(trans_p.getTransNode().getGuardResolvers().split(",")));
			if (tlist.isEmpty()) {
				tlist = curState_p.getAllIncomingTrans();
			}
			else if (tlist.size() > 1) {
				List<Transition> list4 = tlist.stream().filter(t -> t.getMinTraverseCount() > 0).toList();
				if (list4.size() > 1) {
					tlist = list4;
				}
			}
			resolverTrans = tlist.get(this.rand.nextInt(tlist.size()));
		}
		
		double transCost = trans_p.getDist();
		trans_p.setDist(5000);
		List<Transition> altPath = this.networkObj.findShortestPath(altTrans_p.getToNode().getId(), resolverTrans.getFromNode().getId());
		if (altTrans_p!=resolverTrans) {
			altPath.add(resolverTrans);
			altPath.addAll(this.networkObj.findShortestPath(((State)resolverTrans.getToNode()).getId(), ((State)altTrans_p.getFromNode()).getId()));
		}
		trans_p.setDist(transCost);
		altPath.add(0, altTrans_p);		
		seqPath_p.addAltRoute (altPath);
		return altPath;
	}
}
