package com.testoptimal.exec.sequencer.to;

import java.util.List;

import com.testoptimal.exec.FSM.StateNetwork;
import com.testoptimal.exec.FSM.Transition;
import com.testoptimal.exec.navigator.SequencePath;

import openOptima.NoSolutionException;
import openOptima.graph.Vertex;
import openOptima.network.postman.LinZhaoAlgorithm;
import openOptima.network.postman.PostmanPath;

public class OptimalPath {
	private StateNetwork networkObj;
	private LinZhaoAlgorithm optimizer;
	
	public OptimalPath (StateNetwork networkObj_p) throws Exception {
		this.networkObj = networkObj_p;
    	this.optimizer = new LinZhaoAlgorithm();
		this.optimizer.init(this.networkObj);
	}
	
	public List<Transition> genPathList () throws Exception {
		int homeStateID = this.networkObj.getHomeState().getId(); 
		try {
			PostmanPath pathObj = this.optimizer.getPostmanPath(homeStateID);
			List<Transition> transList = StateNetwork.cleanPath(pathObj.getPathArcs());
			return transList;
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
