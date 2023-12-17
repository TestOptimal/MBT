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

import openOptima.graph.Edge;
import openOptima.graph.Vertex;
import openOptima.network.Arc;
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

	public List<Transition> getActiveTransList () {
		return this.allActiveTransList;
	}

	public List<Transition> getAllRequiredTrans() {
		return this.allActiveTransList.stream().filter(t -> t.getMinTraverseCount() > 0).toList();
	}

	/**
	 * reads the state transitions from the state machine network and prepares for optimization
	 * search using Lin/Zhao algorithm for directed Chinese Postman Problem.
	 * 
	 * without fake transitions.
	 * 
	 * @param scxmlObj_p
	 * @return HomeState
	 */
	public State init2 (ScxmlNode scxmlObj_p) throws IOException, Exception {
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

	    // add trans for states in main model
	    this.addTrans (scxmlObj_p.getChildrenStates(), null);
	    this.initialState = this.allStateUIDHash.get(iList.get(0).getUID());
	    this.initialState.setVertexType(Vertex.initialVertex);
	    
	    fList.stream().forEach(s -> this.allStateUIDHash.get(s.getUID()).setVertexType(Vertex.finalVertex));
	    
	    this.allActiveStateList = this.allStateUIDHash.values().stream().map(s->s).toList();
	    this.allActiveTransList = this.allTransUIDHash.values().stream().map(t->t).toList();
		this.allActiveTransList.forEach(t -> t.reset());
	    return this.getHomeState();
	}



	/**
	 * adds states and transitions into network. Also recursively adds children states.
	 * adds fake trans from super state to its initial state in the submodel/sub-states.
	 * adds fake trans from final states to receiving exit trans. 
	 * @param stateNodeList_p list of states to load
	 * @param parentNode_p current super state, null if at root level (main model)
	 */
	private void addTrans2 (List<StateNode> stateNodeList_p, State parentState_p) {
		stateNodeList_p.stream().map(stateNode -> this.allStateUIDHash.get(stateNode.getUID()))
			.forEach(s -> {
				StateNode stateNode = s.getStateNode();
				stateNode.getTransitions().stream().forEach(transNode -> {
					State toState = (State) this.allStateUIDHash.get(transNode.getTargetNode().getUID());
	        		Transition arcObj = new Transition(s, toState, transNode.getEvent(), transNode.getTraverseTimes(), false);
	        		arcObj.setTransNode(transNode);
	        		super.addArc(arcObj);
	        		this.allTransUIDHash.put(transNode.getUID(), arcObj);
				});
				if (stateNode.getChildrenStates().size()>0) {
					this.addTrans(stateNode.getChildrenStates(), s);
				}
			});
	    if (parentState_p!=null) {
	    	parentState_p.setVertexType(Vertex.superVertex);
	    }
	}
	
	
	/**
	 * reads the state transitions from the state machine network and prepares for optimization
	 * search using Lin/Zhao algorithm for directed Chinese Postman Problem.
	 * 
	 * with fake transitions
	 * 
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
	    
	    this.allActiveStateList = super.getVertexList(true).stream().map(s -> (State) s).toList();
	    this.allActiveTransList = super.getArcList(true).stream().map(t -> (Transition) t).filter(t -> !((Transition)t).isFake()).toList();
	    if (printNetwork) {
			try {
				String fname = StateDiagram.genNetworkGraph("State Network", this, Config.getTempPath(), "StateNetwork");
			}
			catch (Exception e) {
				e.printStackTrace();
			}
	    }
		this.allActiveTransList.forEach(t -> t.reset());

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
	
//	/**
//	 * resets this network for a fresh run. re-apply submodel mcase selection
//	 * 
//	 */
//	public void reset() {
//		this.allActiveTransList.forEach(t -> t.reset());
//	}


	public State findStateByUID (String uid_p) {
		return this.allStateUIDHash.get(uid_p);
	}

	public Transition findTransByUID (String uid_p) {
		return this.allTransUIDHash.get(uid_p);
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
		
		List<Transition> transList = networkObj_p.getArcList(true).stream().map(t -> (Transition) t).toList();
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
}
