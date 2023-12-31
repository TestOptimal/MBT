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

package com.testoptimal.server.controller;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.testoptimal.exec.FSM.ModelMgr;
import com.testoptimal.exec.exception.MBTException;
import com.testoptimal.graphing.plantuml.MSC_Graph;
import com.testoptimal.graphing.plantuml.StateDiagram;
import com.testoptimal.scxml.ScxmlNode;
import com.testoptimal.scxml.StateNode;
import com.testoptimal.scxml.SwimlaneNode;
import com.testoptimal.scxml.SwimlaneNode.Lane;
import com.testoptimal.scxml.TransitionNode;
import com.testoptimal.server.model.IdeMessage;
import com.testoptimal.stats.StatsMgr;
import com.testoptimal.stats.exec.ExecStateTrans;
import com.testoptimal.stats.exec.ModelExec;
import com.testoptimal.util.FileUtil;
import com.testoptimal.util.StringUtil;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 
 * @author yxl01
 *
 */
@RestController
@RequestMapping("/api/v1/graph")
@SecurityRequirement(name = "basicAuth")
@JsonIgnoreProperties(ignoreUnknown = true)
@CrossOrigin
public class GraphController {
	private static Logger logger = LoggerFactory.getLogger(GraphController.class);

	@GetMapping(value = "model/{modelName}/model", produces = MediaType.IMAGE_PNG_VALUE )
	public ResponseEntity<UrlResource> genModelGraph (@PathVariable (name="modelName", required=true) String modelName,
			ServletRequest request) throws Exception {
		ModelMgr modelMgr =  new ModelMgr(modelName);
		String httpSessID = ((HttpServletRequest) request).getSession().getId();
		String imgFilePath = "", imgFileName;
		String folderPath = modelMgr.getTempFolderPath();
		String transColor = "#333333";
		imgFileName = StateDiagram.genModelGraph(modelMgr, folderPath, "model", transColor, "StateDiagramModel.include");
		imgFilePath = FileUtil.concatFilePath(folderPath, imgFileName);
    	try {
    		UrlResource resource = new UrlResource((new File(imgFilePath)).toURI());
		    return ResponseEntity.ok()
//	            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
	            .body(resource);
		 } 
    	 catch (MalformedURLException e) {
			 IdeSvc.sendIdeMessage(httpSessID, new IdeMessage("warn", "Could not send file: " + imgFilePath, "GraphControler"));
			throw new Exception ("Failed to generate graph for model " + modelName);
		 }    	
	}
	
	@GetMapping(value = "model/{modelName}/coverage", produces = MediaType.IMAGE_PNG_VALUE )
	public ResponseEntity<UrlResource> genCoverageGraph (@PathVariable (name="modelName", required=true) String modelName,
			@RequestParam (name="mbtSessID", required=false, defaultValue="undefined") String mbtSessID,
			ServletRequest request) throws Exception {
		ModelMgr modelMgr = new ModelMgr(modelName);
		String httpSessID = ((HttpServletRequest) request).getSession().getId();
		String imgFilePath = "", imgFileName;
		String folderPath = modelMgr.getTempFolderPath();
		ModelExec modelExec = StatsMgr.findModelExec(modelMgr, mbtSessID, httpSessID);
		if (modelExec==null) {
			 IdeSvc.sendIdeMessage(httpSessID, new IdeMessage("error", "Model execution not found", "GraphController.genModelSequenceGraph"));
			 throw new Exception ("No model execution is currently open");
		}
		Map<String, String> colorList = getCoverageColorList(modelExec);
		colorList.put("*", "#green");
		imgFileName = StateDiagram.genModelGraphWithColor("Model Execution Coverage Graph", modelMgr, 
				folderPath, "coverage",  colorList, "StateDiagramCoverage.include");
		imgFilePath = FileUtil.concatFilePath(folderPath, imgFileName);
    	try {
    		UrlResource resource = new UrlResource((new File(imgFilePath)).toURI());
		    return ResponseEntity.ok()
//	            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
	            .body(resource);
		 } 
    	 catch (MalformedURLException e) {
			 IdeSvc.sendIdeMessage(httpSessID, new IdeMessage("warn", "Could not send file: " + imgFilePath, "GraphControler"));
			throw new Exception ("Failed to generate graph for model " + modelName);
		 }    	
	}

	@GetMapping(value = "model/{modelName}/sequence", produces = MediaType.IMAGE_PNG_VALUE)
	public ResponseEntity<UrlResource> genModelSequenceGraph (@PathVariable (name="modelName", required=true) String modelName,
			@RequestParam (name="mbtSessID", required=false, defaultValue="undefined") String mbtSessID,
			ServletRequest request) throws Exception {
		ModelMgr modelMgr = new ModelMgr(modelName);
		String httpSessID = ((HttpServletRequest) request).getSession().getId();
//		MbtStarter mbtSess = MbtSessionMgr.getMbtSessionByModelName(modelName);
		String imgFilePath = "";

		String graphFilePath = "";
		ModelExec modelExec = StatsMgr.findModelExec(modelMgr, mbtSessID, httpSessID);
		if (modelExec==null) {
			 IdeSvc.sendIdeMessage(httpSessID, new IdeMessage("error", "Model execution not found", "GraphController.genModelSequenceGraph"));
			 throw new Exception ("No model execution is currently open");
		}
		String tempFolderPath = modelMgr.getTempFolderPath();
		graphFilePath = tempFolderPath + "seqGraph";
		imgFilePath = StateDiagram.genTraversalGraph(modelMgr, modelExec, graphFilePath);
    	try {
    		UrlResource resource = new UrlResource((new File(imgFilePath)).toURI());
		    return ResponseEntity.ok()
//	            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
	            .body(resource);
		 } 
    	 catch (MalformedURLException e) {
			IdeSvc.sendIdeMessage(httpSessID, new IdeMessage("warn", "Could not send file: " + imgFilePath, "GraphControler"));
			throw new Exception ("Failed to generate graph for model " + modelName);
		 }    	
	}

	@GetMapping(value = "model/{modelName}/msc", produces = MediaType.IMAGE_PNG_VALUE)
	public ResponseEntity<UrlResource> modelTravMSC(@PathVariable (name="modelName", required=true) String modelName,
			@RequestParam (name="mbtSessID", required=false, defaultValue="undefined") String mbtSessID,
			ServletRequest request) throws Exception {
		ModelMgr modelMgr = new ModelMgr(modelName);
		String httpSessID = ((HttpServletRequest) request).getSession().getId();
		String imgFolder = modelMgr.getTempFolderPath();
		String imgFileName = "travMSC.png";
		ModelExec modelExec = StatsMgr.findModelExec(modelMgr, mbtSessID, httpSessID);
		if (modelExec==null) {
			IdeSvc.sendIdeMessage(httpSessID, new IdeMessage("warn", "moel.stats.not.found", "GraphControler"));
			throw new MBTException ("model.stats.not.found");
		}
		String imgFilePath = FileUtil.concatFilePath(imgFolder, imgFileName);
		
		boolean generated = genTravMSC(modelMgr, modelExec, imgFilePath);
		
		// generate image
		if (!generated) {
			String msg = "Unable to generate MSC for model " + modelName;
			IdeSvc.sendIdeMessage(httpSessID, new IdeMessage("error", msg, "GraphController.modelTravMSC"));
			throw new Exception (msg);
		}
    	try {
    		UrlResource resource = new UrlResource((new File(imgFilePath)).toURI());
		    return ResponseEntity.ok()
//	            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
	            .body(resource);
		 } 
    	 catch (MalformedURLException e) {
			 IdeSvc.sendIdeMessage(httpSessID, new IdeMessage("warn", "Could not send file: " + imgFilePath, "GraphControler"));
			throw new Exception ("Failed to generate graph for model " + modelName);
		 }    	
	}
	
	public static Map<String, String> getCoverageColorList(ModelExec mbtStat_p) throws Exception {
		
		String SatisfiedColor = "darkgreen";
		String PartialColor = "gold";
		String UntraversedColor = "crimson";
		Map<String, ExecStateTrans> allMap = new java.util.HashMap<>(mbtStat_p.stateMap.size()+mbtStat_p.transMap.size());
		allMap.putAll(mbtStat_p.stateMap);
		allMap.putAll(mbtStat_p.transMap);
		
		Map<String, String> map = new java.util.HashMap<>(allMap.size());
		allMap.values().stream().forEach (s-> {
			int tcount = s.passCount+s.failCount;
			if (tcount >= s.minTravRequired) {
				map.put(s.UID, SatisfiedColor);
			}
			else if (tcount > 0) {
				map.put(s.UID, PartialColor);
			}
			else {
				map.put(s.UID, UntraversedColor);
			}
		});

		return map;
	}
	
	/**
	 * create MSC for the travList passed in.
	 * Consider using "== Initialisation ==" to mark end of iteration and "autonumber 0 1" to restart seq#.
	 * @param reqInfo_p
	 * @return true if successful, false if failed.
	 * @throws Exception
	 */
	private static boolean genTravMSC(ModelMgr modelMgr_p, ModelExec modelExec_p, String imgFilePath_p) throws Exception {
		ScxmlNode scxml = modelMgr_p.getScxmlNode();
		String chartTitle = "Test Sequence Diagram";
		MSC_Graph msc = new MSC_Graph(chartTitle);
		
		// create boxes and their participants
		modelExec_p.tcList.stream().forEach(tc -> {
			tc.stepList.stream()
				.filter(s -> modelExec_p.stateMap.containsKey(s.UID))
				.forEach(s -> {
					ExecStateTrans stateTrans = modelExec_p.stateMap.get(s.UID);
					StateNode stateNode = scxml.findStateByUID(stateTrans.UID);
					String fromStereotype = stateNode.getStereotype();
					ScxmlNode stateScxmlNode = stateNode.getScxmlNode();
					
					// only submodel gets the named box
					if (stateScxmlNode == scxml) {
						msc.addMsgNode("", stateNode.getStateID(), stateNode.getUID(), fromStereotype);
					}
					else {
						String subModelName =  stateScxmlNode.getModelName();
						if (!StringUtil.isEmpty(subModelName)) {
							msc.addMsgNode(subModelName, stateNode.getStateID(), stateNode.getUID(), fromStereotype);
						}
					}
				});
		});

		// add swimlanas as boxes and the grouping will override previous box grouping membership.
		List<SwimlaneNode> swimList = scxml.getMiscNode().getSwimlanes();
		for (int sIdx=0; sIdx<swimList.size(); sIdx++) {
			SwimlaneNode swim = swimList.get(sIdx);
			List<Lane> laneList = swim.getLanes();
			for (int lIdx=0; lIdx<laneList.size(); lIdx++) {
				Lane lane = laneList.get(lIdx);
				List<StateNode> stList = swim.getStateList(scxml, lIdx);
				for (StateNode state: stList) {
					msc.addNodeToGroup(state.getUID(), lane.name);
				}
			}
		}

		// add messages
		modelExec_p.tcList.stream().forEach(tc -> {
			tc.stepList.stream().filter(s -> modelExec_p.transMap.containsKey(s.UID)).forEach(s -> {
				ExecStateTrans stateTrans = modelExec_p.transMap.get(s.UID);
				TransitionNode transNode = scxml.findTransByUID(stateTrans.UID);
				StateNode stateNode = transNode.getParentStateNode();
				String transStereotype = null;
				String msg = (transNode==null || transNode.isHideName())?"":stateTrans.transName;
				msg += " <b><i><size:11>T1</size></i></b>";
				String color = s.status==ModelExec.Status.passed? null:"#FF1100"; // red
				msc.addMsg (stateNode.getUID(), transNode.getTargetUID(), msg, transStereotype, color, -1, tc.tcName);
			});
		});

		// generate image
		List<StateNode> initNodes = modelMgr_p.getScxmlNode().getInitialNodes();
		String initNodeUID = null;
		if (initNodes.size()==1) {
			initNodeUID = initNodes.get(0).getUID();
		}
		boolean generated = msc.genMSC(imgFilePath_p, initNodeUID, "MSC.include");
		return generated;
	}
}
