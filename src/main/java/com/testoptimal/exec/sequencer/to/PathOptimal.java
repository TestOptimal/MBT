package com.testoptimal.exec.sequencer.to;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.testoptimal.exec.ExecutionDirector;
import com.testoptimal.exec.ExecutionSetting;
import com.testoptimal.exec.FSM.StateNetwork;
import com.testoptimal.exec.FSM.Transition;
import com.testoptimal.exec.navigator.SequencePath;
import com.testoptimal.server.config.Config;
import com.testoptimal.util.ArrayUtil;
import com.testoptimal.util.StringUtil;

import openOptima.NoSolutionException;
import openOptima.graph.Vertex;
import openOptima.network.postman.LinZhaoAlgorithm;
import openOptima.network.postman.PostmanPath;
import openOptima.network.shortestpath.ShortestPathProblem;

public class PathOptimal {
    private Map<String, String> seqParams = new java.util.HashMap<>();
	private int traversedTransCost = 500;
    private ExecutionSetting execSetting;
    private ShortestPathProblem shortestPathObj;
    private StateNetwork networkObj;
	private LinZhaoAlgorithm optimizer;

    public StateNetwork getNetworkObj() {
		return this.networkObj;
	}

	public PathOptimal (ExecutionDirector execDir_p) throws Exception {
    	this.execSetting = execDir_p.getExecSetting();
	    this.seqParams = ArrayUtil.stringToTreeMap(this.execSetting.getMbtNode().getSeqParams(), ";", true);
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
    	this.traversedTransCost = StringUtil.parseInt(this.seqParams.get("TraversedTransCost"), this.traversedTransCost);

        List<Transition> reqTransList = this.networkObj.getTransByUIDList(this.execSetting.getMarkList());
		if (!reqTransList.isEmpty()) {
	    	this.networkObj.markRequiredTrans(reqTransList, this.traversedTransCost, this.execSetting.getModelMgr());
		}
    	this.optimizer = new LinZhaoAlgorithm();
		this.optimizer.init(this.networkObj);
	}
	
	public List<SequencePath> genPathList () throws Exception {
		int homeStateID = this.networkObj.getHomeState().getId(); 
		try {
			PostmanPath pathObj = this.optimizer.getPostmanPath(homeStateID);
			List<Transition> transList = StateNetwork.cleanPath(pathObj.getPathArcs());
			List<SequencePath> retList = SequencePath.breakupToPaths(transList);
			return retList;
		}
		catch (NoSolutionException e) {
			StringBuffer exceptBuf = new StringBuffer();
			java.util.ArrayList<String> procdList = new java.util.ArrayList<String>();
			for (Vertex vertexObj: this.optimizer.getUnReachableVertexList()) {
				String transHashCode = vertexObj.getMarker();
				procdList.add(transHashCode);
			}
			
			// there is a loop that involves the required transition. Euler path found but not covering initial/end state.
			if (procdList.isEmpty()) {
				exceptBuf.append("Unable to find path. Try to add/mark additional transition may help, e.g. transition from initial state.");
			}
			else {
				exceptBuf.append("Unable to reach following states from initial state: ");
				for (int i=1; i<procdList.size(); i++) {
					if (i>1) exceptBuf.append(",");
					exceptBuf.append(procdList.get(i));
				}
			}
			throw new NoSolutionException(exceptBuf.toString());
		}
	}
}
