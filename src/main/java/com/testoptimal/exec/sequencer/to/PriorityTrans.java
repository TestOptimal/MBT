package com.testoptimal.exec.sequencer.to;

import java.util.Comparator;

import com.testoptimal.exec.FSM.Transition;

public class PriorityTrans implements Comparator <Transition>{
	public int compare (Transition compTrans1_p, Transition compTrans2_p) {
//		if (!(compTrans1_p instanceof Transition) ||
//			!(compTrans2_p instanceof Transition)) {
//			return 0;
//		}
		
		double p1 = (compTrans1_p).getDist();
		double p2 = (compTrans2_p).getDist();
		if (p1<p2) return -1;
		if (p1==p2) return 0;
		else return 1;
	}	
}
