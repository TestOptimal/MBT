package com.testoptimal.scxml;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.testoptimal.plugin.MScriptInterface.IGNORE_INHERITED_METHOD;
import com.testoptimal.plugin.MScriptInterface.NOT_MSCRIPT_METHOD;
import com.testoptimal.server.config.ConfigVersion;
import com.testoptimal.util.FileUtil;
import com.testoptimal.util.StringUtil;

@IGNORE_INHERITED_METHOD
public final class ScxmlNode {
	private String uid;
	public String getUID() { return this.uid;}

	private String typeCode = "scxml";
	public String getTypeCode() { return this.typeCode; }
	
	private String desc = "";
	public void setDesc (String desc_p) {
		this.desc = desc_p;
	}

	public String getDesc() {
		return this.desc;
	}

	private String notepad = "";
	public String getNotepad() {return this.notepad;}
	
	private List<StateNode> childrenStates = new java.util.ArrayList<StateNode>();
	public List<StateNode> getChildrenStates () {
		return this.childrenStates;
	}

	private String modelName = "";

	public String getModelName () { return this.modelName; }
	public void setModelName(String filename_p) { 
		if (filename_p==null) {
			this.modelName = "";
			return;
		}
		this.modelName = filename_p; 
	}
	
	/**
	 * model version
	 */
	private String version = "1.0";
	public String getVersion() { return this.version; }

	private String versionAUT = "1.0";
	public String getVersionAUT() { return this.versionAUT; }

	/**
	 * TO version last used to update this model. Changed by modelMgr
	 *
	 */
	private String versionTO; // TO version used to last modify this model
	public String getVersionTO() { return this.versionTO; }
	public void setVersionTag(String tagVersion_p) {
		this.versionReq = tagVersion_p;
	}

	private String versionReq;
	public String getVersionReq() {
		return this.versionReq;
	}


	/**
	 * build#, auto incremented by 1 each archive, reset to 0 when version is changed.
	 */
	private int buildNum = 0;
	public int getBuildNum() { return this.buildNum; }
	public int incrementBuildNum() { return ++this.buildNum; }
	public void setBuildNum(int buildNum_p) {
		this.buildNum = buildNum_p;
	}
	
	private MbtNode mbtNode = new MbtNode();
	public MbtNode getMbtNode() { 
		return this.mbtNode; 
	}
	
	private MiscNode miscNode = new MiscNode();
	public MiscNode getMiscNode() {
		return this.miscNode;
	}
		
	private String modelType = "FSM";
	public void setModelType (String modelType_p) {
		this.modelType = modelType_p;
		if (StringUtil.isEmpty(this.modelType)) this.modelType = "FSM";
	}
		
	/**
	 * returns the TransitionNode for the uid passed in.
	 */
	public TransitionNode findTransByUID(String uid_p) {
		for (StateNode stateObj: this.childrenStates) {
			TransitionNode transObj = stateObj.findTransByUID(uid_p);
			if (transObj!=null) return transObj;
		}
		return null;
	}
	
	/**
	 * returns true if this model is a subModel. subModel is identified by the modelName
	 * starting with "_sub".
	 */
	@JsonIgnore
	public boolean isSubModel() {
		if (this.modelName!=null && this.modelName.startsWith("_sub")) return true;
		return false;
	}

	/**
	 * returns list of colors for each node (trans/state) in the model in HashMap table
	 * with key = UID and value = color name.
	 * @return
	 */
	@JsonIgnore
	public java.util.HashMap<String, String> getColorList() {
		java.util.HashMap<String, String> colorList = new java.util.HashMap<String, String>();
		for (StateNode stateObj: this.childrenStates) {
			stateObj.addToColorList(colorList);
		}
		return colorList;
	}

	public List<StateNode> findStateByStereotype(String stereotype_p) {
		List<StateNode> retList = new java.util.ArrayList<StateNode>();
		for (StateNode checkState: this.childrenStates) {
			if (checkState.hasStereotype(stereotype_p)) {
				retList.add(checkState);
			}
		}
		return retList;
	}
	
	
	/**
	 * <p>INTERNAL USE ONLY</p>
	 */
	@NOT_MSCRIPT_METHOD
	@JsonIgnore
	public List<TransitionNode> getAllTrans() {
		List<TransitionNode> retList = new java.util.ArrayList<TransitionNode>();
		for (StateNode state: this.childrenStates) {
			retList.addAll(state.getAllTrans());
		}
		return retList;
	}
	
	/**
	 * <p>INTERNAL USE ONLY</p>
	 * recursively sum up all states
	 * @return
	 */
	@NOT_MSCRIPT_METHOD
	@JsonIgnore
	public List<StateNode> getAllStates() {
		List<StateNode> retList = new java.util.ArrayList<StateNode>();
		for (StateNode state: this.childrenStates) {
			retList.add(state);
			retList.addAll(state.getAllStates());
		}
		return retList;
	}

	/**
	 * finds a state with the given state id/marker.  It traverses through all its children.
	 * @return StateNode or null if not found
	 */
	public StateNode findState(String stateMarker_p) {
		for (StateNode stateObj: this.childrenStates) {
			if (stateObj.getStateID().equalsIgnoreCase(stateMarker_p)) return stateObj;
			StateNode tempStateObj = stateObj.findState(stateMarker_p);
			if (tempStateObj!=null) return tempStateObj;
		}
		return null;
	}

	/**
	 * <p>INTERNAL USE ONLY</p>
	 * sets the readOnly attribute for the model and all of its children objects (states and trans)
	 */
	@NOT_MSCRIPT_METHOD
	@JsonIgnore
	public void setReadOnly(boolean readOnly_p) {
		for (StateNode stateObj: this.getAllStates()) {
			stateObj.setReadOnly(readOnly_p);
		}

		for (TransitionNode transObj: this.getAllTrans()) {
			transObj.setReadOnly(readOnly_p);
		}
	}

	/**
	 * finds a state with the given state uid.  It traverses through all its children.
	 * @return StateNode or null if not found
	 */
	public StateNode findStateByUID(String uid_p) {
		for (StateNode stateObj: this.childrenStates) {
			if (stateObj.getUID().equals(uid_p)) return stateObj;
			StateNode tempStateObj = stateObj.findStateByUID(uid_p);
			if (tempStateObj!=null) return tempStateObj;
		}
		return null;
	}

	/**
	 * <p>INTERNAL USE ONLY</p>
	 */
	@NOT_MSCRIPT_METHOD
	public void sortChildStates () {
		Collections.sort(this.childrenStates);
		for (StateNode stateNode: this.getChildrenStates()) {
			stateNode.sortChildStates();
		}
	}

	/**
	 * returns a list of all initial nodes.  
	 * @return List of StateNode in array that are initial children states of this state.
	 */
	@JsonIgnore
	public java.util.ArrayList<StateNode> getInitialNodes() {
		java.util.ArrayList<StateNode> tempList = new java.util.ArrayList<StateNode>();
		for (StateNode s: this.childrenStates) {
			if (s.getIsInitial()) {
				tempList.add(s);
			}
		}
		return tempList;
	}
	
	@JsonIgnore
	public boolean hasInitialNode() {
		List<StateNode> tempList = this.getInitialNodes();
		if (tempList==null || tempList.isEmpty()) return false;
		else return true;
	}
	

	/**
	 * returns a list of all final nodes.  
	 * @return List of StateNode in array that are final children states of this state.
	 */
	@JsonIgnore
	public java.util.ArrayList<StateNode> getFinalNodes() {
		java.util.ArrayList<StateNode> tempList = new java.util.ArrayList<StateNode>();
		for (StateNode s: this.childrenStates) {
			if (s.getIsFinal()) {
				tempList.add(s);
			}
		}
		return tempList;
	}

	/**
	 * returns the number of children states including all sub-substates, etc.
	 * @return
	 */
	@JsonIgnore
	public int getStateCount() {
		return this.getAllStates().size();
	}

	public StateNode findStateByUIDExcludeSubModel(String uid_p) {
		for (StateNode stateObj: this.childrenStates) {
			if (stateObj.getUID().equals(uid_p)) return stateObj;
			if (stateObj.isSubModelState()) continue;
			StateNode tempStateObj = stateObj.findStateByUID(uid_p);
			if (tempStateObj!=null) return tempStateObj;
		}
		return null;
	}

	@JsonIgnore
	public int getCyclomaticComplexity (boolean mainOnly_p) {
		int retCC = 0;
		for (StateNode child: this.childrenStates) {
			if (child.isSubModelState() && mainOnly_p) continue;
			retCC += child.getComplexity(mainOnly_p);
		}
		return retCC + 1;
	}

	/**
	 * <p>INTERNAL USE ONLY</p>
	 */
	@NOT_MSCRIPT_METHOD
	public void prepScxml() {
		for (StateNode x: this.childrenStates) {
			x.setParentStateNode(null, this);
		}
		
		List<StateNode> allStates = this.getAllStates();
		Map<String, StateNode> stateMapList = new java.util.HashMap<String, StateNode>();
		for (StateNode x: allStates) {
			stateMapList.put(x.getUID(), x);
		}
		for (TransitionNode trans: this.getAllTrans()) {
			trans.setTargetNode(stateMapList.get(trans.getTargetUID()));
		}
	}
	
	/**
	 * <p>INTERNAL USE ONLY</p>
	 */
	@NOT_MSCRIPT_METHOD
	public List<String> validateModel () {
		List<String> retList = new java.util.ArrayList<>();
		List<StateNode> initList = this.getInitialNodes();
		if (initList.isEmpty()) {
			retList.add("model.missing.initial");
		}
		if (initList.size()>1) {
			retList.add("model.multi.initial");
		}

		List<StateNode> finalList = this.getFinalNodes();
		if (finalList.isEmpty()) {
			retList.add("model.missing.final");
		}

		// final states can not have outgoing transitions
		for (StateNode state: finalList) {
			if (state.getTransCount()>0) {
				retList.add("final state containing outgoing transition: " + state.getStateID());
			}
		}
		return retList;
	}

	/**
	 * <p>INTERNAL USE ONLY</p>
	 */
	@NOT_MSCRIPT_METHOD
	public void save (String modelFolderPath_p) throws Exception {
		this.versionTO = ConfigVersion.getReleaseLabel();
		
		// remove submodel sub states before saving as they are not part of the main model
		this.childrenStates.stream().filter(s -> !s.getChildrenStates().isEmpty())
			.forEach(s -> {
				s.getChildrenStates().clear();
		});
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String modelJson = gson.toJson(this);
		FileUtil.writeToFile(modelFolderPath_p + "model.json", modelJson);
	}
	
	public void autoLayout () {
		// assign default position so that IDE can display it
		int offsetLeft = 100;
		int offsetTop = 100;
		int canvasWidth = this.miscNode.getCanvasWidth() - 2 * offsetLeft;
		int spacingHorizontal = 300;
		int spacingVertical = 200;
		this.childrenStates.stream().forEach(s -> {
			int stateIdx = this.childrenStates.indexOf(s);
			Map<String, Float> posMap = new java.util.HashMap<>();
			posMap.put("left", (float) offsetLeft + (spacingHorizontal * stateIdx) % canvasWidth);
			posMap.put("top", (float) offsetTop + (spacingHorizontal * stateIdx / canvasWidth) * spacingVertical);
			posMap.put("width", 75f);
			posMap.put("height", 100f);
			s.setPosition(posMap);
		});
	}
}
