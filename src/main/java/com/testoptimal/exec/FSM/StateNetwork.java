package com.testoptimal.exec.FSM;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.testoptimal.exec.exception.MBTException;
import com.testoptimal.graphing.plantuml.StateDiagram;
import com.testoptimal.scxml.ScxmlNode;
import com.testoptimal.scxml.StateNode;
import com.testoptimal.scxml.TransitionNode;
import com.testoptimal.server.config.Config;
import com.testoptimal.util.StringUtil;

import openOptima.NoSolutionException;
import openOptima.graph.Edge;
import openOptima.graph.Vertex;
import openOptima.network.Arc;
import openOptima.network.postman.PostmanArc;
import openOptima.network.postman.PostmanNetwork;
import openOptima.network.shortestpath.ShortestPath;
import openOptima.network.shortestpath.ShortestPathProblem;

public class StateNetwork extends PostmanNetwork {
	private static boolean printNetwork = StringUtil.isTrue(Config.getProperty("Debug.print.stateNetwork", "N"));
	
	private ScxmlNode scxmlNode;
	private Map <String, State> allStateUIDHash = new java.util.HashMap <> ();
	private List<State> allActiveStateList;
	private List<Transition> allActiveTransList;
	
	private Map <String, Transition> allTransUIDHash = new java.util.HashMap <String, Transition> ();
	private State initialState;
	
	/**
	 * returns the home state of this network.
	 */
	public State getHomeState() { 
		return this.initialState;
	}
	
	public StateNetwork() {
//		this.logObj = Logger.getLogger("com.testoptimal.sequencer");
	}
	
	private List<State> getAllStates (boolean activeOnly_p) {
		List<State> retList = new java.util.ArrayList<State>();
		for (Object stateObj: super.getVertexList(activeOnly_p)) {
			State state = (State) stateObj;
			retList.add(state);
		}
		return retList;
	}

	private List<Transition> getAllTrans(boolean activeOnly_p, boolean incFakeTrans_p) {
		List<Transition> retList = new java.util.ArrayList<Transition>();
		for (Object transObj: super.getArcList(activeOnly_p)) {
			Transition trans = (Transition) transObj;
			if (incFakeTrans_p || !trans.isFake()) {
				retList.add(trans);
			}
		}
		return retList;
	}

	public List<Transition> getActiveTransList () {
		return this.allActiveTransList;
	}
	
	public List<State> getActiveStateList () {
		return this.allActiveStateList;
	}

	public List<Transition> getAllRequiredTrans() {
		List<Transition> retList = new java.util.ArrayList<>();
		for (Transition trans: this.allActiveTransList) {
			if (trans.getMinTraverseCount() > 0) {
				retList.add(trans);
			}
		}
		return retList;
	}

	/**
	 * reads the state transitions from the state machine network and prepares for optimization
	 * search using Lin/Zhao algorithm for directed Chinese Postman Problem.
	 * @param scxmlObj_p
	 * @return HomeState
	 */
	public State init(ScxmlNode scxmlObj_p) throws IOException, Exception {
		this.scxmlNode = scxmlObj_p;
		List<StateNode> iList = scxmlObj_p.getInitialNodes();
		List<StateNode> fList = scxmlObj_p.getFinalNodes();
	    if (iList==null || iList.isEmpty()) {
	    	throw new Exception ("Missing initial state/node.");
	    }
	    if (iList.size()>1) {
	    	throw new Exception ("Multiple initial states/nodes found.");
	    }
	    
	    if (fList.isEmpty()) {
	    	throw new Exception ("Missing final states/nodes");
	    }
	    
	    // add states only and all children states
	    for(StateNode stateNode: scxmlObj_p.getAllStates()){
			State stateObj = new State(stateNode.getStateID(), State.simpleVertex);
			stateObj.setStateNode(stateNode);
			super.addNode(stateObj);
			this.allStateUIDHash.put(stateNode.getUID(), stateObj);
		}

	    this.addTrans (scxmlObj_p.getChildrenStates(), null);
	    this.initialState = this.allStateUIDHash.get(iList.get(0).getUID());
	    this.initialState.setVertexType(Vertex.initialVertex);
	    State fStateObj = null;
	    if (fList.size() == 1) {
	    	fStateObj = this.allStateUIDHash.get(fList.get(0).getUID());
	    }
	    else {
			fStateObj = new State("FinalStateAggregator", State.finalVertex);
			fStateObj.setFake(true);
			super.addNode(fStateObj);
		    for (StateNode fStateNode: fList) {
		    	State fState = this.allStateUIDHash.get(fStateNode.getUID());
				Transition arcObj = new Transition (fState, fStateObj, fState.getStateNode().getUID() + State.FinalExitTransID, 0, true);
				super.addArc(arcObj);
			    fState.setVertexType(Vertex.finalVertex);
		    }
	    }
		Transition arcObj = new Transition (fStateObj, this.initialState, this.initialState.getStateNode().getUID() + State.LoopBackTransID, 1, true);
		super.addArc(arcObj);
	    fStateObj.setVertexType(Vertex.finalVertex);
	    
	    this.allActiveStateList = this.getAllStates(true);
	    this.allActiveTransList = this.getAllTrans(true, false);

	    if (printNetwork) {
			try {
				String fname = StateDiagram.genNetworkGraph("State Network", this, Config.getTempPath(), "StateNetwork");
			}
			catch (Exception e) {
				e.printStackTrace();
			}
	    }

	    return this.getHomeState();
	}



	/**
	 * adds states and transitions into network. Also recursively adds children states.
	 * adds fake trans from super state to its initial state in the submodel/sub-states.
	 * adds fake trans from final states to receiving exit trans. 
	 * @param stateNodeList_p list of states to load
	 * @param parentNode_p current super state, null if at root level (main model)
	 */
	private void addTrans (List<StateNode> stateNodeList_p, State parentState_p) {
	    for(StateNode stateNode: stateNodeList_p){
			State stateObj = this.allStateUIDHash.get(stateNode.getUID());
		    for (TransitionNode transNode: stateNode.getTransitions()) {
                State toState = (State) this.allStateUIDHash.get(transNode.getTargetNode().getUID());
        		Transition arcObj = new Transition(stateObj, toState, transNode.getEvent(), transNode.getTraverseTimes(), false);
        		arcObj.setTransNode(transNode);
        		super.addArc(arcObj);
        		this.allTransUIDHash.put(transNode.getUID(), arcObj);
		    }

			// add virtual trans from parent node
			if (parentState_p!=null) {
				// submodel children states: virtual trans from super state to children initial state
				if (stateNode.getIsInitial()) {
	    			Transition arcObj = new Transition (parentState_p, stateObj, stateObj.getMarker() + State.InitialEntryTransID, 0, true);
	    			super.addArc(arcObj);
				}
				
				// submodel children states: virtual trans from final state to all applicable super state outgoing trans
				else if (stateNode.getIsFinal()) {
					List<Transition> exitTransList = parentState_p.getTransByFinalStateUID(stateNode.getUID());
					for (Transition exitTrans: exitTransList) {
		    			Transition arcObj = new Transition (stateObj, this.allStateUIDHash.get(exitTrans.getTransNode().getTargetUID()), 
		    					stateNode.getStateID() + State.FinalExitTransID, exitTrans.getMinTraverseCount(), false);
		    			arcObj.setForTrans(exitTrans);
		    			arcObj.setTransNode(exitTrans.getTransNode());
		    			super.addArc(arcObj);
//						this.removeArc(exitTrans);
					}
				}
			}
			if (stateNode.getChildrenStates().size()>0) {
				this.addTrans(stateNode.getChildrenStates(), stateObj);
			}
		}
	    if (parentState_p!=null) {
	    	parentState_p.setVertexType(Vertex.superVertex);
	    	parentState_p.getAllTrans(false).stream()
	    		.filter(t -> !t.isFake())
	    		.forEach(t -> this.removeArc(t));
	    }
	}
	
	/**
	 * resets this network for a fresh run. re-apply submodel mcase selection
	 * 
	 */
	public void reset(ModelMgr modelMgr_p) throws Exception {
		for (Transition trans: this.allActiveTransList) {
			trans.reset(modelMgr_p);
		}
		return;
	}



	/**
	 * prints out debug message that describes this network when debug is turned on.
	 */
	public String toString () {
		StringBuffer retBuf = new StringBuffer();
		java.util.ArrayList <Vertex> nodeList = this.getVertexList(true);
		for (int i=0; i<nodeList.size(); i++) {
			State stateObj = (State) nodeList.get(i);
			java.util.ArrayList <Edge> edgeList = stateObj.getEdgesOut();
			for (int j=0; j<edgeList.size(); j++) {
				Transition transObj = (Transition) edgeList.get(j);
				retBuf.append(stateObj.getMarker()).append(", ").append(transObj.getTargetStateId()).append("\n");
			}
		}
		return retBuf.toString();
	}
	
	/**
	 * sets traversal of all transitions to optional except the model's initial node. The initial node is required
	 * to get around the issue that caused LinZhao algorithm to loop.
	 */
	public void setAllTransitionsOptional () {
		for (Transition transObj: this.allActiveTransList) {
			transObj.setTraverseOptional();
		}
	}
	
	/**
	 * resets required number of traverses to the number in mbtNode.
	 */
	public void resetTransitionTraverse() {
		for (Transition transObj: this.allActiveTransList) {
			transObj.setMinMaxCount(transObj.getMinTraverseCount(), Integer.MAX_VALUE);
		}
	}
	
	/**
	 * sets required number of traverses on all transitions included in the state and all of the children states per their transition definition.
	 */
	public void setTransitionTraverse(State stateObj_p, int penalty_p) {
		List <Edge> transList = stateObj_p.getEdgesOut();
		for (int i=0; i<transList.size(); i++) {
			Transition transObj = (Transition) transList.get(i);
			if (transObj.isFake()) continue;
			transObj.setMinMaxCount(transObj.getTransNode().getTraverseTimes(), Integer.MAX_VALUE);
			if (penalty_p>0) {
				transObj.setDist(penalty_p);
				transObj.setTraverseOptional();
			}
		}
		
		if (!stateObj_p.isSuperVertex()) return;
		List<State> stateList = stateObj_p.getChildStates();
		for (int i=0; i<stateList.size(); i++) {
			State stateObj = (State) stateList.get(i);
			if (stateObj.isFake()) continue;
			this.setTransitionTraverse(stateObj, penalty_p);
		}
		return;
	}
	
	/**
	 * finds a state
	 */
	public State findState(String stateID_p) {
		StateNode node = this.scxmlNode.findState(stateID_p);
		return node==null?null:(State) this.allStateUIDHash.get(node.getUID());
	}
	
	public State findStateByUID (String uid_p) {
		return this.allStateUIDHash.get(uid_p);
	}

	public Transition findTransByUID (String uid_p) {
		return this.allTransUIDHash.get(uid_p);
	}

	/**
	 * Finds either trans or state by uid_p passed in.
	 * @param uid_p
	 * @return
	 */
	public Object findByUID (String uid_p) {
		State st = this.allStateUIDHash.get(uid_p);
		if (st!=null) {
			return st;
		}
		return this.allTransUIDHash.get(uid_p);
	}

	
	/**
	 * sets the transition cost to the transition weight substring from max weight of
	 * all transitions.  This way, it allows trans with higher weight to be
	 * selected for repeating.
	 */
	public void setTransCostFromWeight() {
		// find max weight first
		int maxWeight = 0;
		for (Transition transObj: this.allActiveTransList) {
			if (transObj.isFake() || transObj.getTransNode()==null) continue;
			int curWeight = transObj.getTransNode().getWeight();
			if (curWeight > maxWeight) {
				maxWeight = curWeight;
			}
		}
		
		maxWeight = maxWeight * 10;
		for (Transition transObj: this.allActiveTransList) {
			transObj.setDist(maxWeight - transObj.getTransNode().getWeight());
		}
		return;
	}
	

	/**
	 * returns a list of state with specified stereotype
	 * @param stereotype_p
	 */
	public List<State> getStateByStereotype (String stereotype_p) {
		List<State> retList = new java.util.ArrayList<State>();
		for (State state: this.allActiveStateList) {
			StateNode stateNode = state.getStateNode();
			String stereotype = stateNode.getStereotype();
			if (stereotype==null) continue;
			if (stereotype.equalsIgnoreCase(stereotype_p)) {
				retList.add(state);
			}
		}
		return retList;
	}

	public List<Transition> getTransByStereotype (String stereotype_p) {
		List<Transition> activeTransList = this.allActiveTransList;
		List<Transition> retList = new java.util.ArrayList<Transition>();
		for (Transition trans: activeTransList) {
			TransitionNode transNode = trans.getTransNode();
			String stereotype = transNode.getStereotype();
			if (stereotype==null) continue;
			if (stereotype.equalsIgnoreCase(stereotype_p)) {
				retList.add(trans);
			}
		}
		return retList;
	}

	/**
	 * returns the shortest path of transitions from fromState_p to toState_p.
	 */
	public List<Transition> findShortestPath (int fromState_p, int toState_p) throws MBTException {
		List<Transition> retList = new java.util.ArrayList<Transition> ();
		if (fromState_p==toState_p) return retList;
		try {
			ShortestPathProblem shortestpathObj;
		    shortestpathObj = new ShortestPathProblem("openOptima.network.shortestpath.DijkstraAlgorithm");
		    shortestpathObj.init(this);
		    ShortestPath path = shortestpathObj.getShortestPath(fromState_p, toState_p);
			if (path==null) {
				throw new MBTException ("Some states are not reachable. This is usually caused by missing transitions.");
			}
			Arc [] arcList = path.getPathArcs();
			for (int k=0; k<arcList.length; k++) {
				retList.add((Transition) arcList[k]);
			}
			return retList;
		}
		catch (Throwable t) {
			throw new MBTException(t.getMessage());
		}
	}

	/**
	 * returns the transition from the initial state of the network that are most common for reaching all of 
	 * the states and transitions passed in.
	 */
	public Transition findCommonInitialTrans(List<Transition> transList_p, ModelMgr modelMgr_p) throws MBTException {
		int homeStateId = this.getHomeState().getId(); //this.superState.getId(); // this.homeState.getId();
		java.util.HashMap<Transition, Integer> tallyTransList = new java.util.HashMap<Transition, Integer>();
		for (Transition theTrans: transList_p) {
			int transStartStateId = theTrans.getFromNode().getId();
			if (homeStateId==transStartStateId) {
				continue;
			}
			List<Transition> transList = this.findShortestPath(homeStateId, transStartStateId);
			Transition trans = transList.get(0);
			for (Transition aTrans: transList) {
				if (aTrans.getTransNode()!=null) {
					trans = aTrans;
					break;
				}
			}

			Integer intObj = tallyTransList.get(trans);
			if (intObj==null) intObj = new Integer(1);
			else intObj = intObj+1;
			tallyTransList.put(trans, intObj);
		}
		
		int pathLength = 9999;
		Transition retTrans = null;
		boolean retTransHasGuard = false;
		for (java.util.Map.Entry <Transition, Integer> anEntry: tallyTransList.entrySet()) {
			Transition trans = anEntry.getKey();
			Integer intObj = anEntry.getValue();
			if (retTrans == null) {
				retTrans = trans;
				pathLength = intObj;
				if (retTrans.getTransNode()!=null) {
					retTransHasGuard = retTrans.getTransNode().hasGuard();
				}
			}
			else if (retTransHasGuard) { // must be also a realTrans
				if (!trans.getTransNode().hasGuard()) {
					retTrans = trans;
					retTransHasGuard = false;
					pathLength = intObj;
				}
				else if (pathLength>=intObj) {
					retTrans = trans;
					retTransHasGuard = true;
					pathLength = intObj;
				}
			}
			else {
				if (trans.getTransNode().hasGuard()) {
					continue;
				}
				else if (pathLength>=intObj) {
					retTrans = trans;
					retTransHasGuard = false;
					pathLength = intObj;
				}
			}
		}
		return retTrans;
	}

	/**
	 * returns the count of transitions that requires at least one traversal.
	 */
	public int getReqTransCount() {
		int ret = 0;
		for (Transition loopTrans: this.allActiveTransList) {
			if (loopTrans.getMinTraverseCount()>0) {
				ret++;
			}
		}
		return ret;
	}
//	
//	/**
//	 * if only partial of the network is required, ensure there is a path from the home
//	 * to the first required arc to be required to avoid the fragmented path.
//	 * @param fromNode_p
//	 */
//	public void primeForPartialCoverage(int fromNode_p) throws Exception {
//	    ShortestPathProblem shortestpathObj = new ShortestPathProblem("openOptima.network.shortestpath.DijkstraAlgorithm");
//	    shortestpathObj.init(this);
//
//    	// find shortest path to any of the edge/state required for this usecase
//    	// and set those trans required.
//    	ShortestPath pathList [] = shortestpathObj.getShortestPaths(fromNode_p);
//    	if (pathList.length==0) {
//    		throw new NoSolutionException ("Some states are not reachable. This is usually caused by missing transitions.");
//    	}
//    	for (ShortestPath path: pathList) {
//    		List<Arc> arcList = path.extractPathToFirstRequiredArc();
//    		if (arcList!=null) {
//    			for (Arc arc: arcList) {
//    				if (arc.getMinTraverseCount()==0) {
//    					arc.setMinMaxCount(1, Integer.MAX_VALUE);
//    				}
//    			}
//    			break;
//    		}
//    	}
//    	return;
//	}
	
	public State getSubModelInitialState (State subModelState_p) {
		for (State state: this.allActiveStateList) {
			if (state.getSubModelState()==subModelState_p && state.isInitial()) {
				return state;
			}
		}
		return null;
	}
	
	
	public List<State> getSubModelStateList() {
		List<State> retList = new java.util.ArrayList<State>();
		for (State state: this.allActiveStateList) {
			if (state.isFake()) continue;
			if (state.getStateNode().isSubModelState()) {
				retList.add(state);
			}
		}
		return retList;
	}
//	
//	public static List<Transition> cleanPath (List<PostmanArc> pathTransList_p) {
//		List<Transition> retList = new java.util.ArrayList<Transition>();
//		for (PostmanArc transObj: pathTransList_p) {
//			Transition trans = (Transition) transObj;
////			if (trans.isFake() || !trans.isActive()) continue;
//			Transition realTrans = trans.getForTrans();
//			if (realTrans != null) {
//				trans = realTrans;
//			}
////			if (!trans.isFake() || trans.isLoopbackTrans()) {
//			if (!trans.isFake()) {
//				retList.add(trans);
//			}
//		}
//		return retList;
//	}
	

	/**
	 * this method expands the network passed in by splitting each state into a bi-part graph and add transitions to 
	 * link between the newly created states using cartesian product algorithm.
	 * 
	 * This is done by creating a new network object to contain the expanded 
	 * states and transitions and generate the postman path.  Then the original
	 * path is then constructed using the expanded network path.
	 * 
	 * For each transition, create a set of nodes: S_uid and T_uid and the transition iteself connecting the two nodes.
	 * For each node, create trans for each pair of in-trans and out-trans connecting
	 *  the S_'in-trans' and T_'out-trans' nodes. I.e. create a bi-part graph and set trans to have min traversal of 1.
	 */
	public static StateNetwork createCoverageNetwork(StateNetwork networkObj_p) {
		// for coverage mode, clone networkObj, expand each state to a list of bi-part graph.  This is then collapsed
		// when the optimal eular path is found.
		// create one node for each arc using arc.hashCode() as the node Marker.  This will be
		// the end node for the arc.
		java.util.HashMap<String, State> startNodeList = new java.util.HashMap<String, State>();
		java.util.HashMap<String, State> endNodeList = new java.util.HashMap<String, State>();
		StateNetwork postNetwork = new StateNetwork();
		StateNode homeStateNode = networkObj_p.getHomeState().getStateNode();
		String nodeID = homeStateNode.getUID();
		postNetwork.initialState = new State(nodeID, Vertex.initialVertex);
		startNodeList.put(nodeID, postNetwork.initialState);
		postNetwork.addNode(postNetwork.initialState);
		
		List<Transition> transList = networkObj_p.getAllTrans(true,  true);
		Map<String, TransitionNode> transAddedList = new java.util.HashMap<>();
		for (Transition transObj: transList) {
			TransitionNode transNode = transObj.getTransNode();
			if (transNode!=null) {
				if (transAddedList.containsKey(transNode.getUID())) {
					continue;
				}
				transAddedList.put(transNode.getUID(), transNode);
			}
			String transUID = transNode==null? String.valueOf(transObj.hashCode()):transNode.getUID();
			nodeID = "S_" + transUID;
			State nodeObj = new State(nodeID, Vertex.simpleVertex);
			startNodeList.put(transUID, nodeObj);
			postNetwork.addNode(nodeObj);
			
			nodeID = "T_" + transUID;
			nodeObj = new State(nodeID, Vertex.simpleVertex);
			endNodeList.put(transUID, nodeObj);
			postNetwork.addNode(nodeObj);
		}

		// from initial state to all outgoing starting trans from home state
		for (TransitionNode transNode: homeStateNode.getAllTrans()) {
			String transUID = transNode.getUID();
			Transition arcObj = new Transition (postNetwork.initialState, startNodeList.get(transUID), "I_" + transUID, 1, true);
			postNetwork.addArc(arcObj);
		}
		
		// create transitions
		for (Transition transObj: transList) {
			TransitionNode transNode = transObj.getTransNode();
			String transUID = transNode==null? String.valueOf(transObj.hashCode()):transNode.getUID();
//			Transition arcObj = new Transition (startNodeList.get(transUID), endNodeList.get(transUID), transUID, transObj.getMinTraverseCount());
			Transition arcObj = new Transition (startNodeList.get(transUID), endNodeList.get(transUID), transObj.getMarker(), transObj.getMinTraverseCount(), false);
			if (!transObj.isFake()) {
				if (transObj.getForTrans()!=null) {
					Transition forTrans = transObj.getForTrans();
					arcObj.setForTrans(forTrans);
				}
				else {
					arcObj.setForTrans(transObj);
				}
			}
			postNetwork.addArc(arcObj);
		}
		
		// link up bi-part sudo nodes
		for (State stateObj: networkObj_p.allActiveStateList) {
			if (stateObj.isModelInitial()) continue;
			
			List<Edge> inTrans = stateObj.getEdgesInto();
			List<Edge> outTrans = stateObj.getEdgesFrom();
			
			for (Edge inEdgeObj: inTrans) {
				Transition inTransObj = (Transition) inEdgeObj;
				TransitionNode transNode = inTransObj.getTransNode();
				String inTransUID = transNode==null? String.valueOf(inTransObj.hashCode()):transNode.getUID();
				State startNode = endNodeList.get(inTransUID);
				for (Edge outEdgeObj: outTrans) {
					Transition outTransObj = (Transition) outEdgeObj;
					TransitionNode outTransNode = outTransObj.getTransNode();
					String outTransUID = outTransNode==null? String.valueOf(outTransObj.hashCode()):outTransNode.getUID();
					State endNode = startNodeList.get(outTransUID);
//					Transition arcObj = new Transition (startNode, endNode, inTransUID + "_" + outTransUID, 1);
					Transition arcObj = new Transition (startNode, endNode, startNode.getMarker() + "_" + endNode.getMarker(), 1, false);
					postNetwork.addArc(arcObj);
				}
			}
		}
		
		// fake trans loopback to initial node/out going trans nodes
		for (Transition transObj: transList) {
			TransitionNode transNode = transObj.getTransNode();
			String transUID = transNode==null? String.valueOf(transObj.hashCode()):transNode.getUID();
			if (transObj.isLoopbackTrans()) {
				Transition arcObj = new Transition (endNodeList.get(transUID), postNetwork.initialState, transUID, 1, true);
				postNetwork.addArc(arcObj);
			}
		}
		
	    if (printNetwork) {
			try {
				String fname = StateDiagram.genNetworkGraph("State Network", postNetwork, Config.getTempPath(), "StateNetwork_extended");
			}
			catch (Exception e) {
				e.printStackTrace();
			}
	    }
		return postNetwork;
	}
	
	public void markRequiredTrans (List<Transition> transList_p, long optionalCost_p, ModelMgr modelMgr_p) throws Exception {
		for (Transition transObj: this.allActiveTransList) {
			transObj.reset(modelMgr_p);
			if (optionalCost_p>0) transObj.setDist(optionalCost_p);
			transObj.setMinMaxCount(0, Integer.MAX_VALUE);
		}

    	for (Transition transObj: transList_p) {
			transObj.setMinMaxCount(1, Integer.MAX_VALUE);
    	}
	}
	
	public List<Transition> getTransByUIDList(List<String> uidList_p) {
		List<Transition> retList = new java.util.ArrayList<>();
		if (uidList_p==null || uidList_p.isEmpty()) return retList;
    	for (String travUID: uidList_p) {
    		State stateObj = this.allStateUIDHash.get(travUID);
    		if (stateObj==null) {
    			Transition transObj = this.allTransUIDHash.get(travUID);
    			if (transObj!=null) {
    				retList.add(transObj);
    			}
    		}
    		else {
    			for (Transition transObj: stateObj.getAllTrans(false)) {
    				retList.add(transObj);
    			}
    		}
    	}
    	return retList;
	}
	
	public void setLoopbackTrans (boolean activeFlag_p) {
		this.allActiveTransList.stream().filter(t -> t.isLoopbackTrans()).forEach(t -> t.setActive(activeFlag_p));
	}
}
