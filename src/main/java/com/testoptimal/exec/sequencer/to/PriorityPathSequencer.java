package com.testoptimal.exec.sequencer.to;

import java.util.Comparator;
import java.util.List;

import com.testoptimal.exec.ExecutionDirector;
import com.testoptimal.exec.FSM.Transition;
import com.testoptimal.exec.navigator.SequencePath;
import com.testoptimal.exec.navigator.TraversalCount;

import openOptima.NoSolutionException;
import openOptima.NotImplementedException;

/**
 * Generates paths choosing path with preference on higher weight transitions.
 * It uses the complement of trans weight (max trans weight - trans weight) as the distance
 * and calls shortest path to find the shortest path to any of the final nodes.
 * It then increments the distance of the traversed path by a delta specified in the config and
 * repeat the shortest path step.  If the same path is found, it increments the weight
 * of all traversed path by the delta and try to find the shortest path.  It does this until
 * all transitions are traversed.
 * 
 * <p>The Algorithm:
 * <ol>
 * 	<li>Reverse weight to convert longest path to shortest path: (max {trans weight} - trans weight) * <code>scale</code>
 * 		set scale = 1000*maxTransWeight and set maxPenalty = 1000*scale, set traversal count for all trans to 0.
 *  <li>Loop until all trans are satisfied (# of times required met)
 *  	<ol>
 *  		<li>find an unsatisfied trans with least weight
 *  		<li>find shortest path from home state to the transition
 *  		<li>adjust the weight of all transitions on the path by the following rules:
 *  			<ol>
 *  				<li>if transition is not satisfied, leave weight unchanged
 *  				<li>if transition is satisfied, set weight = previous weight + max weight * <code>scale</code>
 *  				<li>if transition is at or over the max # of traversal, set weight = previous weight + <code>max penalty weight</code>
 *  				<li>increment traversal count by 1
 *  			</ol>
 *  		</li>
 *  		<li>find shortest path from the transition back to home state
 *  		<li>adjust the weight of all transitions on the return path using the same rules above.
 *  	</ol>
 * 	</li>
 *  <li>sort paths according to their weight in decending order
 * </ol>
 * 
 * <p>Configuration settings:
 * 	<ul>
 * 		<li>max penalty weight - weight to be added to transitions that have been over traversed
 * 		<li>max depth count - max # of depth count allowed to augment the paths.
 * 		<li>prefer path length - prefered # of transitions in each final path.
 * 	</ul>
 * 
 * @author yxl01
 *
 */
public class PriorityPathSequencer extends SequencerPathBase {
	public PriorityPathSequencer (ExecutionDirector execDir_p) throws Exception {
    	super(execDir_p);
	}
	
	
	@Override
	public List<SequencePath> genSeqPaths() throws NoSolutionException,
			InterruptedException, NotImplementedException {
    	List<Transition> reqTransList = this.getNetworkObj().getAllRequiredTrans();
    	reqTransList.sort(new PriorityTrans());
    	TraversalCount coverageObj = this.getReqTransCount();
    	
    	List<SequencePath> retList = new java.util.ArrayList<SequencePath>();
    	for (int i=0; i < reqTransList.size(); i++) {
    		Transition transObj = reqTransList.get(i);
    		if (coverageObj.getTravCountLeft(transObj)>0) {
	    		SequencePath path = this.genPathVia(transObj);
	    		coverageObj.addTravPath(path); 
	    		retList.add(path);
	    		for (Transition trans: path.getTransList()) {
	    			if (coverageObj.getTravCountLeft(trans) <= 0) {
	    				trans.setDist(trans.getDist() + this.getTraversedTransCost());
	    			}
	    		}
	    		i--; // repeat the same trans to get it covered enough times
    		}
		}
    	
    	coverageObj.reset();
		return retList;
	}
}
