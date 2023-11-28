package com.testoptimal.exec.sequencer.to;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.testoptimal.exec.ExecutionDirector;
import com.testoptimal.exec.ExecutionSetting;
import com.testoptimal.exec.FSM.State;
import com.testoptimal.exec.FSM.StateNetwork;
import com.testoptimal.exec.FSM.Transition;
import com.testoptimal.exec.navigator.SequencePath;
import com.testoptimal.exec.navigator.TraversalCount;
import com.testoptimal.server.config.Config;
import com.testoptimal.util.ArrayUtil;
import com.testoptimal.util.StringUtil;

import openOptima.NoSolutionException;
import openOptima.network.Arc;
import openOptima.network.shortestpath.ShortestPath;
import openOptima.network.shortestpath.ShortestPathProblem;
/**
 * Sequencer based on test paths (as opposed to random walk).
 * 
 * Reverse weight for shortest path: (max {trans weight} - trans weight) * <code>scale</code>
 * <p>Configuration settings:
 * 	<ul>
 * 		<li>scale - multiplier of weight to make the cost of traversed transitions at a magnitude higher than
 * 			untraversed transitions.
 * 		</li>
 * </ul>
 * 
 * @author yxl01
 *
 */
abstract public class SequencerPathBase {
	private static Logger logger = LoggerFactory.getLogger(SequencerPathBase.class);

    private Map<String, String> seqParams = new java.util.HashMap<>();
	private int traversedTransCost = 500;
    private ExecutionSetting execSetting;
    private ShortestPathProblem shortestPathObj;
    private StateNetwork networkObj;

    public StateNetwork getNetworkObj() {
		return this.networkObj;
	}
    protected ExecutionSetting getExecSetting() { 
    	return this.execSetting; 
    }

    private List<SequencePath> pathList;
    
    public SequencerPathBase (ExecutionDirector execDir_p) throws Exception {
    	logger.info("Initializing Sequencer " + this.getClass());
	    this.seqParams = ArrayUtil.stringToTreeMap(execDir_p.getExecSetting().getMbtNode().getSeqParams(), ";", true);
	    
    	this.execSetting = execDir_p.getExecSetting();
        this.networkObj = this.execSetting.getNetworkObj();

        float scale = StringUtil.parseFloat(this.seqParams.get("scale"), StringUtil.parseFloat(Config.getProperty("Sequencer.scale"), 1.0f));
    	List<Transition> allTrans = this.getNetworkObj().getActiveTransList();
    	double maxWeight = allTrans.stream()
    			.max(Comparator.comparingDouble(Transition::getDist0))
                .get().getDist0();
    	allTrans.forEach(t -> t.setDist((maxWeight - t.getDist0()) * scale));
        
	    this.shortestPathObj = new ShortestPathProblem("openOptima.network.shortestpath.DijkstraAlgorithm");
	    this.shortestPathObj.init(this.networkObj);

	    this.traversedTransCost = StringUtil.parseInt(Config.getProperty("Sequencer.TraversedTransCost"), this.traversedTransCost);
    	this.traversedTransCost = StringUtil.parseInt(this.getSeqParam("TraversedTransCost"), this.traversedTransCost);
    }

    public String getSeqParam(String key_p) {
    	return this.seqParams.get(key_p);
    }
    
	public ShortestPathProblem getShortestPathObj() {
		return this.shortestPathObj;
	}
	
	public TraversalCount getReqTransCount() {
		Map<String, Integer> transReqMap = new java.util.HashMap<>();
		for (Transition trans: this.getNetworkObj().getAllRequiredTrans()) {
			transReqMap.put(trans.getTransNode().getUID(), trans.getMinTraverseCount());
		}
		return new TraversalCount(transReqMap);
	}

	public TraversalCount getReqStateCount() {
		return new TraversalCount(new java.util.HashMap<>());
	}

    public int getTraversedTransCost () {
    	return this.traversedTransCost;
    }
    
	public int genPathList() throws Exception {
		this.pathList = this.genSeqPaths();
		logger.info("Test paths generated: " + this.pathList.size());
		
		// disable loopback trans so that alt path can not use it to find alt path
		this.getNetworkObj().setLoopbackTrans(false);
		return this.pathList.size();
	}
	
//	public int addPaths (List<SequencePath> pathList_p) {
//		int cnt = 0;
//		for (SequencePath path: pathList_p) {
//			String pathId = path.getPathId();
//			if (!this.pathIdMap.containsKey(pathId)) {
//				this.pathIdMap.put(pathId, path);
//				this.pathList.add(path);
//				cnt++;
//			}
//		}
//		return cnt;
//	}
	
	protected SequencePath genPathVia(Transition viaTrans_p) 
			throws NoSolutionException {
		List<Transition> transList = genTransListVia (this.getNetworkObj().getHomeState(), viaTrans_p, this.getShortestPathObj());
		SequencePath ret = new SequencePath(transList);
		return ret;
	}


	public static List<Transition> genTransListVia (State initialState_p, Transition viaTrans_p, ShortestPathProblem shortPathProb_p) 
		throws NoSolutionException {
		List<Transition> transList = genTransListVia (initialState_p, Arrays.asList(new Transition[] {viaTrans_p}), shortPathProb_p);
		return transList;
	}

	public static List<Transition> genTransListVia (State initialState_p, List<Transition> viaTransList_p, ShortestPathProblem shortPathProb_p) 
		throws NoSolutionException {
		int initialID = initialState_p.getId();
		int fromStateID = initialID;
		int toStateID;
		List<Transition> transList = new java.util.ArrayList<>();
		for (Transition trans: viaTransList_p) {
			State viaState = (State) trans.getFromNode();
			toStateID = viaState.getId();
			if (fromStateID!=toStateID) {
				transList.addAll(getShortestPathTransList(shortPathProb_p, fromStateID, toStateID));
			}
			fromStateID = trans.getToNode().getId();
			transList.add(trans);
		}
		if (!viaTransList_p.isEmpty()) {
			Transition lastTrans = viaTransList_p.get(viaTransList_p.size()-1);
			if (!lastTrans.getToNode().isFinalVertex()) {
				transList.addAll(getShortestPathTransList(shortPathProb_p, fromStateID, initialID));
			}
		}
		return transList;
	}

	public static List<Transition> getShortestPathTransList (ShortestPathProblem shortPathProb_p, int fromStateID_p, int toStateID_p) 
		throws NoSolutionException {
		ShortestPath sPath1 = shortPathProb_p.getShortestPath(fromStateID_p, toStateID_p);
		List<Transition> transList = new java.util.ArrayList<>();
		for (Arc arc: sPath1.getPathArcs()) {
			Transition aTrans = (Transition) arc;
//			if (!aTrans.isFake()) {
				transList.add(aTrans);
//			}
		}
		return transList;
	}
	
	public List<SequencePath> getPathList() {
		return this.pathList;
	}
	public int getPathCount() {
		return this.pathList.size();
	}

	abstract protected List<SequencePath> genSeqPaths () throws Exception;

}
