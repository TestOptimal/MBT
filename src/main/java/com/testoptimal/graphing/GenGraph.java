package com.testoptimal.graphing;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.testoptimal.exception.MBTException;
import com.testoptimal.exec.ExecutionDirector;
import com.testoptimal.exec.FSM.ModelMgr;
import com.testoptimal.graphing.plantuml.MSC_Graph;
import com.testoptimal.graphing.plantuml.StateDiagram;
import com.testoptimal.scxml.ScxmlNode;
import com.testoptimal.scxml.StateNode;
import com.testoptimal.scxml.TransitionNode;
import com.testoptimal.stats.exec.ExecStateTrans;
import com.testoptimal.stats.exec.ModelExec;
import com.testoptimal.stats.exec.TestCaseStep;
import com.testoptimal.util.FileUtil;

/**
 * @startuml
 *  List <|-- ArrayList
 *  url of List is [[http://www.google.com]]
 * @enduml
 *
 * In that example, the following .cmapx is generated (with the .png file)
 * <pre>
 * <map id="unix" name="unix">
 * <area shape="rect" id="node2" href="http://www.google.com"
 * title="&lt;TABLE&gt;" alt="" coords="17,5,92,69"/>
 * </map>
 * </pre>
 *
 */
public class GenGraph {

	public static String genObjectSequence (ExecutionDirector execDir_p, String outputFolderPath_p, 
		String graphFileName_p, int minSize_p) throws Exception {
		ModelMgr modelMgr = execDir_p.getExecSetting().getModelMgr();
//		List<StateNode> initNodes = modelMgr.getScxmlNode().getInitialNodes();
//		String homeStateID = "";
//		if (initNodes.size()>0) {
//			homeStateID = initNodes.get(0).getStateID();
//		}
//		java.util.ArrayList<Object> newLogList = new java.util.ArrayList<Object>();
//		List<ExecutionLog> logList = execDir_p.getExecLogList();
//		ScxmlNode scxmlObj = modelMgr.getScxmlNode();
//		for (int i=logList.size()-1; i>=0; i--) {
//			ExecutionLog logObj = logList.get(i);
//			if (logObj.type.equals("Transition") && logObj instanceof ExecutionLog) {
//				TransitionNode transObj = scxmlObj.findTransByUID(logObj.uid);
//				if (transObj==null) continue;
//				newLogList.add(0, logObj);
//				if (transObj.getParentStateNode().getStateID().equals(homeStateID) &&
//					minSize_p <= newLogList.size() && 
//					(newLogList.size()>1 || newLogList.size()==1 && transObj.getTargetNode()!=transObj.getParentStateNode())) { 
//					break;
//				}
//			}
//		}
		
		ModelExec collStat = execDir_p.getExecStats();
		String imgFilePath = FileUtil.concatFilePath(modelMgr.getReportFolderPath(),  graphFileName_p);
		String graphFilePath = StateDiagram.genTraversalGraph(collStat, modelMgr, collStat, imgFilePath);
		return graphFilePath;
	}
	

	public static String genMSC (String chartLabel_p, ExecutionDirector execDir_p, String outputFolderPath_p, String graphFileName_p, String skinName_p, String bkgColor_p) throws Exception {
		ModelMgr modelMgr = execDir_p.getExecSetting().getModelMgr();
		String graphFilePath = FileUtil.concatFilePath(outputFolderPath_p, graphFileName_p);
		GenGraph.genLastHomeRunMSC(chartLabel_p, modelMgr, execDir_p, graphFilePath, skinName_p, bkgColor_p);
		return graphFilePath;
	}
	
	public static boolean genLastHomeRunMSC (String chartLabel_p, ModelMgr modelMgr_p, ExecutionDirector execDir_p, String imgFilePath_p, String skinName_p, String bkgColor_p) throws Exception {
		ModelExec modelExec = execDir_p.getExecStats();
		// add messages to MSC chart object
		ScxmlNode scxml = modelMgr_p.getScxmlNode();
		
		// get a specific instance of MSC chart to create MSC based on specific message list
		MSC_Graph msc = new MSC_Graph(chartLabel_p);
		
		List<StateNode> homeStateList = scxml.getInitialNodes();
		if (homeStateList.isEmpty()) throw new MBTException ("unable to find initial node");
		String homeStateUID = homeStateList.get(0).getUID();
		
//		Map<String, ExecStateTrans>  stateTransMap = modelExec.getStateTransMap();
		
		modelExec.getCurTestCase().stepList.stream()
			.filter( s-> modelExec.stateMap.containsKey(s.UID))
			.forEach ( s -> {
				ExecStateTrans stateTrans = modelExec.stateMap.get(s.UID);
				StateNode stateNode = scxml.findStateByUID(stateTrans.UID);
				String fromStereotype = stateNode.getStereotype();
				msc.addMsgNode("", stateTrans.stateName, stateTrans.UID, fromStereotype);
			});
		
		List<TestCaseStep> stepList = modelExec.getCurTestCase().stepList.stream().filter(s->modelExec.transMap.containsKey(s.UID)).collect(Collectors.toList());
		for (int i=stepList.size()-1; i>=0; i--) {
			TestCaseStep stepStats = stepList.get(i);
			TransitionNode transNode = scxml.findTransByUID(stepStats.UID);
			String fromStateUID = transNode.getParentStateNode().getUID();
			String toStateUID = transNode.getTargetUID();
			String transStereotype = transNode.getStereotype();
			msc.insertMsg(fromStateUID, toStateUID, transNode.getDisplayEvent(), transStereotype, null, -1, "");
			if (homeStateUID.equals(toStateUID)) break; // found home run start node
		}
		boolean generated = msc.genMSC(imgFilePath_p, skinName_p, bkgColor_p, "MSC.include"); //genTravMSC(modelMgr_p, newTravList, imgFilePath_p);
		if (!generated) {
			return false;
		}
		else return true;
	}

}
