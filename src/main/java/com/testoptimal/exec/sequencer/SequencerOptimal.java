package com.testoptimal.exec.sequencer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.testoptimal.exec.ExecutionDirector;
import com.testoptimal.exec.FSM.StateNetwork;
import com.testoptimal.exec.FSM.Transition;
import com.testoptimal.exec.exception.MBTAbort;
import com.testoptimal.exec.navigator.PathBuilder;
import com.testoptimal.exec.navigator.SequencePath;

import openOptima.NoSolutionException;
import openOptima.graph.Vertex;
import openOptima.network.postman.LinZhaoAlgorithm;
import openOptima.network.postman.PostmanPath;

/**
 * generates optimal test paths using Chinese Postman Problem algorithm.
 * 
 * @author yxl01
 *
 */
public class SequencerOptimal extends SequencerBase {
	private static Logger logger = LoggerFactory.getLogger(SequencerOptimal.class);

	public SequencerOptimal (ExecutionDirector execDir_p) throws MBTAbort, Exception {
		super(execDir_p);
	}
	
	public List<SequencePath> genPathList () throws Exception {
		StateNetwork networkObj = this.getNetworkObj();
		int homeStateID = networkObj.getHomeState().getId(); 
		LinZhaoAlgorithm optimizer;
    	optimizer = new LinZhaoAlgorithm();
		optimizer.init(networkObj);
		
		try {
			PostmanPath pathObj = optimizer.getPostmanPath(homeStateID);
			List<Transition> transList = pathObj.getPathArcs().stream()
					.map(t -> (Transition) t )
					.toList();
			List<SequencePath> retList = PathBuilder.breakUpIntoPaths(transList);
			return retList;
		}
		catch (NoSolutionException e) {
			StringBuffer exceptBuf = new StringBuffer();
			java.util.ArrayList<String> procdList = new java.util.ArrayList<String>();
			for (Vertex vertexObj: optimizer.getUnReachableVertexList()) {
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
