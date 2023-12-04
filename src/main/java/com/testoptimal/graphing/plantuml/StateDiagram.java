package com.testoptimal.graphing.plantuml;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.testoptimal.exec.FSM.ModelMgr;
import com.testoptimal.exec.FSM.StateNetwork;
import com.testoptimal.graphing.GraphState;
import com.testoptimal.graphing.GraphTrans;
import com.testoptimal.scxml.ScxmlNode;
import com.testoptimal.scxml.StateNode;
import com.testoptimal.scxml.TransitionNode;
import com.testoptimal.server.config.Config;
import com.testoptimal.stats.exec.ExecStateTrans;
import com.testoptimal.stats.exec.ModelExec;
import com.testoptimal.stats.exec.TestCaseStep;
import com.testoptimal.util.FileUtil;
import com.testoptimal.util.StringUtil;

import net.sourceforge.plantuml.SourceStringReader;

/**
 * Generate state machine graph using PlantUML. 
 * <pre>
 * @startuml img/state_img03.png
 *
 * 		[*] -> State1
 *		State1 --> State2 : Succeeded
 *		State1 --> [*] : Aborted
 *		State2 --> State3 : Succeeded
 *		State2 -[#color,dashed]-> [*] : Aborted
 *		state State3 {
 *		  	state "Accumulate Enough Data\nLong State Name" as long1
 *			long1 : Just a test
 *			[*] --> long1
 *			long1 --> long1 : New Data
 * 			long1 --> ProcessData : Enough Data
 * 		}
 * 		State3 --> State3 : Failed
 *		State3 --> [*] : Succeeded / Save Result
 *		State3 --> [*] : Aborted
 *
 *	@enduml
 * </pre>
 * Plantuml uses graphviz on traversal and model diagrams
 * Requires -DGRAPHVIZ_DOT=lib\GraphViz\bin\dot.exe
 *       
 * @author yxl01
 *
 */
public class StateDiagram {
	private static String[] threadColorList = new String[] {"#000000", "#0000FF", "#A52A2A", "#FFA500", "#FF0000", 
		"#FFC0CB", "#FFD700", "#FF6347", "#4B0082", " 	#7CFC00", "#F08080", "#D2B48C", " 	#EE82EE"};
//	public static String[] threadColorList = new String[] {"black", "blue", "brown", "orange", "red", 
//		"pink", "gold", "tomato", "indigo", "lawngreen", "lightcoral", "tan", "violet"};
	
	public static boolean printPlantUML = false;
	
	static {
		printPlantUML = StringUtil.isTrue(Config.getProperty ("Debug.print.plantUML","N"));
	}

	/**
	 * generate state diagram for traversal graph - should be migrated to use ActivityDiagram when we can set color for each trans.
	 * @return image file path
	 */
	public static String genTraversalGraph (ModelExec collStat_p, ModelMgr modelMgr_p, 
			ModelExec modelExec_p, String filePath_p) throws Exception {
		ScxmlNode scxml = modelMgr_p.getScxmlNode();
		Map<String,ExecStateTrans> stateTransMap = modelExec_p.getStateTransMap();
		List<TestCaseStep> stepList = collStat_p.getCurTestCase().stepList.stream().filter(s->stateTransMap.get(s.UID).type.equalsIgnoreCase("trans")).collect(Collectors.toList());

		
		String orient = ""; // default
		boolean includeAllState = StringUtil.isTrue(Config.getProperty("Graph.traversal.allstate"));

		// use seq# as the key
		java.util.HashMap<String, String> colorList = new java.util.HashMap<String, String>();
		int i=0;
		List<StateNode> graphStateList = new java.util.ArrayList<StateNode>();
		List<TransitionNode> transList = new java.util.ArrayList<>();

		for (TestCaseStep stepObj: stepList) {
			if (stateTransMap.get(stepObj.UID).type.equalsIgnoreCase("state")) {
				continue;
			}
			TransitionNode transObj = scxml.findTransByUID(stepObj.UID);
			StateNode stateObj = transObj.getParentStateNode();
			transList.add(transObj);
			if (stepObj.status != ModelExec.Status.passed) {
				colorList.put(String.valueOf(i), "red");
			}
			i++;
			if (!graphStateList.contains(stateObj) && 
				scxml.findStateByUIDExcludeSubModel(stateObj.getUID())!=null
//				stateObj.getParentStateNode()!=null && 
//				stateObj.getParentStateNode().getParentStateNode()==null
				) {
					graphStateList.add(stateObj);
			}
			stateObj = transObj.getTargetNode();
			if (!graphStateList.contains(stateObj) &&
				scxml.findStateByUIDExcludeSubModel(stateObj.getUID())!=null
//				stateObj.getParentStateNode()!=null &&
//				stateObj.getParentStateNode().getParentStateNode()==null
				) {
				graphStateList.add(stateObj);
			}
		}

//		List<StateNode> finalStateList = scxml.getFinalNodes();
		GraphState rootGraphState = new GraphState("root", "root", null, "root");
		if (includeAllState) {
			graphStateList = modelMgr_p.getScxmlNode().getChildrenStates();
		}
		
		// gen graphState list
		rootGraphState.addChildState(graphStateList);

		colorList.put("*", "#777777");
		List<GraphTrans> gTransList = GraphTrans.genTransList(modelMgr_p, transList, rootGraphState, colorList);
		String plantUML = toPlantUML("Test Sequence Graph", rootGraphState, gTransList, orient, false, true, "StateDiagramSeq.include");
		String pngFilePath = filePath_p;
		if (!pngFilePath.endsWith(".png")) {
			pngFilePath += ".png";
		}
		OutputStream png = new FileOutputStream(pngFilePath);

		if (StateDiagram.printPlantUML) {
			FileUtil.writeToFile(pngFilePath + ".plantuml", plantUML);
		}
		SourceStringReader reader = new SourceStringReader(plantUML);

		try {
			String desc = reader.generateImage(png);
	
			// Return a null string if no generation
			if (desc==null || desc.equalsIgnoreCase("(Error)")) {
				String plantUML_file = filePath_p + ".plantUML";
				FileUtil.writeToFile(plantUML_file, plantUML);
				throw new Exception ("Unable to generate Traversal Graph. Please send file " + plantUML_file + " to support@testoptimal");
			}
			StringBuffer htmlText = new StringBuffer("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><html><body>");
			int idx = desc.indexOf("<map id=");
			if (idx>0) {
				String mapText = desc.substring(idx);
				htmlText.append(mapText);
			}
			return pngFilePath;
		}
		catch (Exception e) {
			String plantUML_file = filePath_p + ".plantUML";
			FileUtil.writeToFile(plantUML_file, plantUML+ "\n\nError: " + e.getMessage());
			throw new Exception ("Unable to generate Traversal Graph. Please send file " + plantUML_file + " to support@testoptimal");
		}
		finally {
			png.close();
		}

	}
	
	
	// Traversal Graph
	public static String genTraversalGraph (ModelMgr modelMgr_p, 
			ModelExec modelExec_p, String filePath_p) throws Exception {
		ScxmlNode scxml = modelMgr_p.getScxmlNode();
		List<TestCaseStep> stepList = modelExec_p.tcList.stream().flatMap(tc -> tc.stepList.stream()).collect(Collectors.toList());

		Map<String,ExecStateTrans> stateTransMap = modelExec_p.getStateTransMap();
		String orient = "";
		boolean includeAllState = StringUtil.isTrue(Config.getProperty("Graph.traversal.allstate"));

		// use seq# as the key
		java.util.HashMap<String, String> colorList = new java.util.HashMap<String, String>();
		int i=0;
		List<StateNode> graphStateList = new java.util.ArrayList<StateNode>();
		List<TransitionNode> transList = new java.util.ArrayList<>();

		for (TestCaseStep stepObj: stepList) {
			if (stateTransMap.get(stepObj.UID).type.equalsIgnoreCase("state")) {
				continue;
			}
			TransitionNode transObj = scxml.findTransByUID(stepObj.UID);
			StateNode stateObj = transObj.getParentStateNode();
			transList.add(transObj);
			if (stepObj.status != ModelExec.Status.passed) {
				colorList.put(String.valueOf(i), "red");
			}
			i++;
			if (!graphStateList.contains(stateObj) && 
				scxml.findStateByUIDExcludeSubModel(stateObj.getUID())!=null
//				stateObj.getParentStateNode()!=null && 
//				stateObj.getParentStateNode().getParentStateNode()==null
				) {
					graphStateList.add(stateObj);
			}
			stateObj = transObj.getTargetNode();
			if (!graphStateList.contains(stateObj) &&
				scxml.findStateByUIDExcludeSubModel(stateObj.getUID())!=null
//				stateObj.getParentStateNode()!=null &&
//				stateObj.getParentStateNode().getParentStateNode()==null
				) {
				graphStateList.add(stateObj);
			}
		}

//		List<StateNode> finalStateList = scxml.getFinalNodes();
		GraphState rootGraphState = new GraphState("root", "root", null, "root");
		if (includeAllState) {
			graphStateList = modelMgr_p.getScxmlNode().getChildrenStates();
		}
		
		// gen graphState list
		rootGraphState.addChildState(graphStateList);

		colorList.put("*", "#777777");
		List<GraphTrans> gTransList = GraphTrans.genTransList(modelMgr_p, transList, rootGraphState, colorList);
		String plantUML = toPlantUML("Test Sequence Graph", rootGraphState, gTransList, orient, false, true, "StateDiagramSeq.include");
		String pngFilePath = filePath_p;
		if (!pngFilePath.endsWith(".png")) {
			pngFilePath += ".png";
		}
		OutputStream png = new FileOutputStream(pngFilePath);

		if (StateDiagram.printPlantUML) {
			FileUtil.writeToFile(pngFilePath + ".plantuml", plantUML);
		}
		SourceStringReader reader = new SourceStringReader(plantUML);

		try {
			String desc = reader.generateImage(png);
	
			// Return a null string if no generation
			if (desc==null || desc.equalsIgnoreCase("(Error)")) {
				String plantUML_file = filePath_p + ".plantUML";
				FileUtil.writeToFile(plantUML_file, plantUML);
				throw new Exception ("Unable to generate Traversal Graph. Please send file " + plantUML_file + " to support@testoptimal");
			}
			StringBuffer htmlText = new StringBuffer("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><html><body>");
			int idx = desc.indexOf("<map id=");
			if (idx>0) {
				String mapText = desc.substring(idx);
				htmlText.append(mapText);
			}
			return pngFilePath;
		}
		catch (Exception e) {
			String plantUML_file = filePath_p + ".plantUML";
			FileUtil.writeToFile(plantUML_file, plantUML+ "\n\nError: " + e.getMessage());
			throw new Exception ("Unable to generate Traversal Graph. Please send file " + plantUML_file + " to support@testoptimal");
		}
		finally {
			png.close();
		}

	}
	
	// Model Graph.
	public static String genModelGraph (ModelMgr modelMgr_p, String folder_p, String fileName_p, String transColor_p, String includeFile_p) throws Exception {
		java.util.HashMap<String, String> colorList = new java.util.HashMap<String, String>();
		colorList.put("*", transColor_p);
		ScxmlNode scxml = modelMgr_p.getScxmlNode();
		int cc = scxml.getCyclomaticComplexity(false);
		String graphTitle = "Model Graph [Cyclomatic Complexity: " + cc + "]";
//		String graphTitle = "Model Graph";
		return genModelGraphWithColor (graphTitle, modelMgr_p, folder_p, fileName_p, colorList, includeFile_p);
	}
	
	// Coverage Graph
	public static String genModelGraphWithColor (String title_p, ModelMgr modelMgr_p, String folder_p, String fileName_p,
			Map<String, String> colorList_p, String includeFile_p) throws Exception {

		ScxmlNode scxmlNode = modelMgr_p.getScxmlNode();
		String orient = "";
		List<TransitionNode> transList = scxmlNode.getAllTrans();
		
		GraphState rootGraphState = new GraphState("root", "root", null, "root");
		rootGraphState.addChildState(modelMgr_p.getScxmlNode().getChildrenStates());
		List<GraphTrans> gTransList = GraphTrans.genTransList(modelMgr_p, transList, rootGraphState, colorList_p);

		String plantUML = toPlantUML(title_p, rootGraphState, gTransList, orient, true, false, includeFile_p);
        String imgFileName = fileName_p + ".png";
		String imgFile = FileUtil.concatFilePath(folder_p, imgFileName);
		try (		OutputStream png = new FileOutputStream(imgFile);) {
			if (StateDiagram.printPlantUML) {
				FileUtil.writeToFile(imgFile + ".plantuml", plantUML);
			}

			SourceStringReader reader = new SourceStringReader(plantUML);

			// Write the first image to "png"
			String desc = reader.generateImage(png);
			return imgFileName;
		}
	}

	// used by StateNetwork for debugging
	public static String genNetworkGraph (String title_p, StateNetwork networkObj_p, String folder_p, String fileName_p) throws Exception {
		String orient = "L";
		GraphState rootGraphState = new GraphState("root", "root", null, "root");
		rootGraphState.addChildState(networkObj_p);
		List<GraphTrans> gTransList = GraphTrans.genTransList(networkObj_p, rootGraphState);

		String plantUML = toPlantUML(title_p, rootGraphState, gTransList, orient, true, false, "");
        String imgFileName = fileName_p + ".png";
//        String htmlFileName = fileName_p + ".html";
		String imgFile = FileUtil.concatFilePath(folder_p, imgFileName);
//		String graphHtmlFile = FileUtil.concatFilePath(folder_p, htmlFileName);
		OutputStream png = new FileOutputStream(imgFile);

		if (StateDiagram.printPlantUML) {
			FileUtil.writeToFile(imgFile + ".plantuml", plantUML);
		}

		SourceStringReader reader = new SourceStringReader(plantUML);

		try {
			// Write the first image to "png"
			String desc = reader.generateImage(png);
			return imgFileName;
		}
		finally {
			png.close();
		}
	}

	private static String toPlantUML (String title_p, GraphState rootGraphState_p, List<GraphTrans> transList_p, 
			String orient_p, boolean genSink_p, boolean addSeqNum_p, String includeFile_p) throws Exception {
		StringBuffer nodeBuf = new StringBuffer();
		StringBuffer edgeBuf = new StringBuffer();
		nodeBuf.append(getChildStatePlantUML(rootGraphState_p, genSink_p));
		int i = 0;
		for (GraphTrans trans: transList_p) {
			String fromNodeID = trans.getFromState().getStateAsID();
			String toNodeID = trans.getToState().getStateAsID();
			String transLabel = trans.getTransLabel();
			i++;
			if (addSeqNum_p) {
				transLabel = "[" + i + "] " +  transLabel;
			}
			edgeBuf.append(fromNodeID).append(" -").append(trans.getTransArrow()).append("-> ").append(toNodeID).append(" : ").append(transLabel).append("\n");
		}

		StringBuffer retBuf = new StringBuffer();
		retBuf.append("@startuml\n");
		retBuf.append("title <font size=18 color=#777777>").append(title_p).append("\n");
		if (StringUtil.isEmpty(orient_p)) {
			orient_p = Config.getProperty("Graph.orient");
		}
		if (!StringUtil.isEmpty(orient_p)) {
			if (orient_p.equalsIgnoreCase("Horizontal")) {
				retBuf.append("left to right direction\n");
			}
			else if (orient_p.equals("Vertical")) {
				retBuf.append("top to bottom direction\n");
			}
		}
		
		if (!StringUtil.isEmpty(includeFile_p)) {
			retBuf.append("!include ").append(FileUtil.concatFilePath(Config.getConfigPath(), includeFile_p)).append("\n");
		}
		retBuf.append(nodeBuf).append(edgeBuf);
		retBuf.append("@enduml\n");
		return retBuf.toString();
	}

	
	private static String getChildStatePlantUML (GraphState graphState_p, 
			boolean genSink_p) {
		StringBuffer ret = new StringBuffer();
//		int finalStateCount = 0;
		for (GraphState state: graphState_p.getChildStateList()) {
			String stateLabel = state.getStateLabel();
			String stateAsID = state.getStateAsID();
			ret.append("state \"").append(stateLabel).append("\" as ").append(stateAsID);
			if (state.hasChildStates()) {
				ret.append(" {\n");
				ret.append(getChildStatePlantUML(state, false));
				ret.append("}\n");
			}
			ret.append("\n");
			if (state.isInitialState()) {
				ret.append("[*] --> ").append(stateAsID).append("\n");
			}
			
			if (state.isFinalState()) {
//				finalStateCount++;
				if (genSink_p) ret.append(stateAsID).append(" --> [*]\n");
				else ret.append(stateAsID).append(" -[#AAAAAA]-> [*]\n");

			}
		}
		
//		if (finalStateCount<=0) {
//			for (GraphState state: graphState_p.getChildStateList()) {
//				String stateID = state.getStateAsID();
//				if (state.isInitialState()) continue;
//				ret.append(stateID).append(" -[#FF0000]-> [*]\n");
//			}			
//		}
		return ret.toString();
	}
	
//	private static String getExamplePlantUML (String chartDesc_p) {
//		StringBuffer ret = new StringBuffer();
//		ret.append("@startuml\n");
//		ret.append("skinparam backgroundcolor #FFFFFF\n");
//		ret.append("skinparam state {\n");
////		ret.append("   BackgroundColor #97c3d4\n");
//		ret.append("   ArrowColor #333333\n");
//		ret.append("   BorderColor #333333\n");
//		ret.append("   StartColor #97c3d4\n");
//		ret.append("   EndColor #97c3d4\n");
////		ret.append("   FontColor brown\n");
//		ret.append("}\n");
//
//		ret.append("scale 350 width\n");
//		ret.append("[*] --> NotShooting\n");
//		ret.append("\n");
//		ret.append("state NotShooting {\n");
//		ret.append("[*] --> Idle\n");
//		ret.append("}\n");
//		ret.append("\n");
//		ret.append("state \"Configuring\" as N_Config {\n");
//		ret.append("[*] -down-> NewValueSelection\n");
//		ret.append("\n");
//		ret.append("state NewValuePreview {\n");
//		ret.append("state \"State1\" as N_State1\n");
//		ret.append("N_State1 -> State2\n");
//		ret.append("}\n");		  
//		ret.append("Idle -[#red,bold,dashed]-> N_Config : EvConfig\n");
//		ret.append("Idle -[#blue]-> N_State1: just a test main to submodel\n");
//		ret.append("N_Config -[dotted]-> Idle : EvConfig\n");
//		ret.append("N_State1 -[dashed]-> Idle : EvNewValue\n");
//		ret.append("NewValueSelection --> NewValuePreview : EvNewValue\n");
//		ret.append("NewValuePreview -[#red,dashed]-> NewValueSelection : EvNewValueRejected\n");
//		ret.append("NewValuePreview --> NewValueSelection : EvNewValueSaved\n");
//		ret.append("@enduml\n");	
//		return ret.toString();
//	}
	
//	public static String genExampleGraph (String chartDesc_p, String folder_p, String fileName_p) throws Exception {
//		
//		String plantUML =  getExamplePlantUML(chartDesc_p);
//        String imgFileName = fileName_p + ".png";
//        String htmlFileName = fileName_p + ".html";
//		String imgFile = FileUtil.concatFilePath(folder_p, imgFileName);
//		String graphHtmlFile = FileUtil.concatFilePath(folder_p, htmlFileName);
//		OutputStream png =new FileOutputStream(imgFile);
//
//		if (StateDiagram.printPlantUML) {
//			FileUtil.writeToFile(imgFile + ".plantuml", plantUML);
//		}
//
//		SourceStringReader reader = new SourceStringReader(plantUML);
//
//		try {
//			// Write the first image to "png"
//			String desc = reader.generateImage(png);
//	
//			// Return a null string if no generation
//			if (desc==null || desc.equalsIgnoreCase("(Error)")) {
//				FileUtil.writeToFile(imgFile + ".err", plantUML);
//				throw new Exception ("Unable to generate Activity Diagram");
//			}
//			StringBuffer htmlText = new StringBuffer("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><html><body>");
//			int idx = desc.indexOf("<map id=");
//			if (idx>0) {
//				String mapText = desc.substring(idx);
//				htmlText.append(mapText);
//			}
//			htmlText.append("<img src='").append(imgFileName).append("' USEMAP='#plantuml_map'/></body></html>");
//			FileUtil.writeToFile(graphHtmlFile, htmlText);
//			return graphHtmlFile;
//		}
//		finally {
//			png.close();
//		}
//
//	}
	

//	private static String genGraphFromFile (String plantUMLFile_p, String folder_p, String fileName_p) throws Exception {
//		
//		StringBuffer plantUML =  FileUtil.readFile(plantUMLFile_p);
//        String imgFileName = fileName_p + ".png";
//        String htmlFileName = fileName_p + ".html";
//		String imgFile = FileUtil.concatFilePath(folder_p, imgFileName);
//		String graphHtmlFile = FileUtil.concatFilePath(folder_p, htmlFileName);
//		OutputStream png =new FileOutputStream(imgFile);
//
//		if (StateDiagram.printPlantUML) {
//			FileUtil.writeToFile(imgFile + ".plantuml", plantUML);
//		}
//
//		SourceStringReader reader = new SourceStringReader(plantUML.toString());
//
//		try {
//			// Write the first image to "png"
//			String desc = reader.generateImage(png);
//	
//			// Return a null string if no generation
//			if (desc==null || desc.equalsIgnoreCase("(Error)")) {
//				FileUtil.writeToFile(imgFile + ".err", plantUML);
//				throw new Exception ("Unable to generate Activity Diagram");
//			}
//			StringBuffer htmlText = new StringBuffer("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><html><body>");
//			int idx = desc.indexOf("<map id=");
//			if (idx>0) {
//				String mapText = desc.substring(idx);
//				htmlText.append(mapText);
//			}
//			htmlText.append("<img src='").append(imgFileName).append("' USEMAP='#plantuml_map'/></body></html>");
//			FileUtil.writeToFile(graphHtmlFile, htmlText);
//			return graphHtmlFile;
//		}
//		finally {
//			png.close();
//		}
//	}
}
