package com.testoptimal.exec.sequencer.to;

import java.util.List;
import java.util.Map;

import com.testoptimal.exec.FSM.State;
import com.testoptimal.exec.FSM.Transition;
import com.testoptimal.exec.navigator.SequencePath;

import openOptima.NoSolutionException;
import openOptima.NotImplementedException;
import openOptima.network.shortestpath.ShortestPathProblem;

/**
 * input: set of required transition (the Trans)
 * fitness score calc (Fitness): # of req trans contained in the path
 * 
 * setup: set Trans required, cost 1 unit, all other optional with cost M units = percentage P of # of transitions in graph
 * loop: seed phase
 * 		select a req trans not coverred
 * 		find shortest path from initial to source state, trans, backto initial state, 
 * 		calc fitness score
 * 		add path to population
 * 		if all Trans are covered
 * 			goto "spawn phase" loop
 * 		else
 * 			repeat "seed phase" loop
 * 		end if
 * end loop
 * 
 * set consecutive mutation fail loop = 0
 * loop: spawn phase
 * 		tally paths in the population by Fitness score
 * 		randomly choose two paths using Fitness score as weight
 * 		randomly choose a common state
 * 		crossover two paths to generate two new paths
 * 		mutate two new path to generate additional two new paths
 * 		add new paths that are not present in population
 * 		if population has not changed since last loop
 * 			increment consecutive mutation fail loop by 1
 * 		else
 * 			increment consecutive mutation fail loop = 0
 * 		end if
 * 
 * 		if consecutive mutation fail loop over limit
 * 			break "spawn phase" loop
 * 		end if
 * 	end loop 
 *    
 * This object is shared by multiple MBT worker threads to obtain a
 * path from the path queue list.
 * 
 * Path queue list is populated by a standalond thread that runs
 * in the background generating paths to fill the path queue list.
 * 
 * The getNextPath() is blocked if the path queue is empty until
 * an available path is inserted into the queue.
 * 
 * @author yxl01
 *
 */
public class MutantPath {
	private ShortestPathProblem shortPathProb;
	public MutantPath (ShortestPathProblem shortPathProb_p) {
		this.shortPathProb = shortPathProb_p;
	}
	
	public List<SequencePath> genSeqPaths(State startState_p, List<Transition> reqTransList_p) throws NoSolutionException,
			InterruptedException, NotImplementedException {
    	Map<String,SequencePath> uniquePathList = new java.util.HashMap<>();
    	List<Transition> viaTransList = new java.util.ArrayList<>();
    	
		List<SequencePath> pathList = new java.util.ArrayList<SequencePath>();
    	for (Transition transObj: reqTransList_p) {
    		viaTransList.clear();
    		viaTransList.add(transObj);
    		SequencePath path = new SequencePath(SequencerPathBase.genTransListVia(startState_p, viaTransList, this.shortPathProb));
			if (uniquePathList.put(path.getPathId(), path)==null) {
				pathList.add(path);
			}
		}
		if (pathList.size() <= 2) return pathList;
		
    	int path1Idx = 0;
    	int path2Idx = 1;
    	while (true) {
    		List<SequencePath> newPathList = this.mutatePaths (pathList.get(path1Idx), pathList.get(path2Idx));
    		for (SequencePath path: newPathList) {
    			if (uniquePathList.put(path.getPathId(), path)==null) {
    				pathList.add(path);
    			}
    		}
    		int pathSize = pathList.size();
			path2Idx++;
    		if (path2Idx >= pathList.size()) {
    			path1Idx++;
    			path2Idx = path1Idx + 1;
    			if (path1Idx >= pathSize || path2Idx >= pathSize) {
    				break;
    			}
    		}
    	}
		return pathList;
	}

	/**
	 * @return
	 * @throws NoSolutionException
	 * @throws InterruptedException
	 */
    private List<SequencePath> mutatePaths (SequencePath path1_p, SequencePath path2_p) throws NoSolutionException, InterruptedException  {
    	List<SequencePath> retList = new java.util.ArrayList<SequencePath>();
    	List<State> path1StateList = path1_p.getPathStateList();
    	List<State> path2StateList = path2_p.getPathStateList();
    	List<Transition> path1TransList = path1_p.getTransList();
    	List<Transition> path2TransList = path2_p.getTransList();
    	int length1 = path1TransList.size();
    	int length2 = path2TransList.size();

		for (int i1=1; i1 < path1StateList.size(); i1++) {
			State state1 = path1StateList.get(i1);
			int i2 = path2StateList.indexOf(state1);
			if (i2 > 0 && i2 < path2StateList.size()-1) {
				List<Transition> path1SegA = path1TransList.subList(0, i1+1);
				List<Transition> path1SegB = path1TransList.subList(i1+1, length1);
				List<Transition> path2SegA = path2TransList.subList(0, i2+1);
				List<Transition> path2SegB = path2TransList.subList(i2+1, length2);
				
				List<Transition> newTransList1 = new java.util.ArrayList<>(path1SegA);
				newTransList1.addAll(path2SegB);
				retList.add(new SequencePath(newTransList1));
				List<Transition> newTransList2 = new java.util.ArrayList<>(path2SegA);
				newTransList2.addAll(path1SegB);
				retList.add(new SequencePath(newTransList2));
			}
		}

		return retList;
	}
}
