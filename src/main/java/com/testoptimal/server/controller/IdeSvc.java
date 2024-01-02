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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;

import com.testoptimal.exec.ModelRunner;
import com.testoptimal.exec.ModelRunnerIDE;
import com.testoptimal.exec.FSM.ModelMgr;
import com.testoptimal.exec.mscript.MbtScriptExecutor;
import com.testoptimal.scxml.ScxmlNode;
import com.testoptimal.scxml.StateNode;
import com.testoptimal.scxml.TransitionNode;
import com.testoptimal.server.config.Config;
import com.testoptimal.server.controller.helper.SessionMgr;
import com.testoptimal.server.model.GuardStatus;
import com.testoptimal.server.model.IdeMessage;
import com.testoptimal.server.model.ModelProp;
import com.testoptimal.server.model.ModelState;
import com.testoptimal.server.model.RunRequest;
import com.testoptimal.server.model.sys.DebugExpr;
import com.testoptimal.util.FileUtil;
import com.testoptimal.util.StringUtil;

@EnableScheduling
@Controller
public class IdeSvc {
	private static IdeSvc wsSvc;
	private static String HttpSessionName = "HTTPSESSIONID";
	private static IdeMessage ModelNotRunning = new IdeMessage("warn", "Model not running", "IdeSvc.stopModel");
	private static Logger logger = LoggerFactory.getLogger(IdeSvc.class);

    @Autowired
    private SimpMessagingTemplate template;
    
    @Autowired
    private SimpMessagingTemplate brokerMessagingTemplate;

    public IdeSvc () {
		if (wsSvc == null) {
			wsSvc = this;
		}
	}
    
	public static void broadcastIdeMessage (String msg_p) {
		if (msg_p!=null) {
			wsSvc.brokerMessagingTemplate.convertAndSend("/ide/alert", msg_p);
		}
	}

	public static void sendIdeMessage (String httpSessID_p, IdeMessage msgObj_p) {
		if (msgObj_p!=null) {
			wsSvc.template.convertAndSend("/ide/" + httpSessID_p + "/alert", msgObj_p);
//			wsSvc.template.convertAndSend("/ide/alert", msgObj_p);
		}
	}

	public static void sendIdeData (String httpSessID_p, String action_p, Object messageObj_p) {
		if (httpSessID_p!=null) {
			wsSvc.template.convertAndSend("/ide/" + httpSessID_p + "/" + action_p, messageObj_p);
		}
//		if (messageObj_p!=null) {
//			wsSvc.template.convertAndSend("/ide/" + action_p, messageObj_p);
//		}
	}

	public void sendIdeMessageLocal (SimpMessageHeaderAccessor accessor, IdeMessage msgObj_p) {
		wsSvc.template.convertAndSend("/ide/" + getHttpSessionId(accessor) + "/alert", msgObj_p);
//		wsSvc.template.convertAndSend("/ide/alert", msgObj_p);
	}

	public void sendIdeDataLocal (SimpMessageHeaderAccessor accessor, String action_p, Object messageObj_p) {
		wsSvc.template.convertAndSend("/ide/" + getHttpSessionId(accessor) + "/" + action_p, messageObj_p==null?"null":messageObj_p);
//		wsSvc.template.convertAndSend("/ide/" + action_p, messageObj_p==null?"null":messageObj_p);
	}

	public static void sendModelState (String httpSessionID, String modelName) {
    	ModelState m = new ModelState(modelName, httpSessionID);
		sendIdeData(httpSessionID, "model.state", m);
	}

	private String getHttpSessionId (SimpMessageHeaderAccessor accessor) {
		return (String)accessor.getSessionAttributes().get(HttpSessionName);
	}
	
	@MessageMapping("/init")
	public void init (SimpMessageHeaderAccessor accessor) throws Exception {
		wsSvc.template.convertAndSend("/ide/httpSess", this.getHttpSessionId(accessor));

//		RuntimeController.sendIdeRuntimeSessions(this.getHttpSessionId(accessor));
	}

	@MessageMapping("/{modelName}/getModelState")
	public void getModelState (@DestinationVariable("modelName") String modelName, SimpMessageHeaderAccessor accessor) {
		ModelRunner sess = (ModelRunnerIDE) SessionMgr.getInstance().getMbtStarterForModel(modelName, this.getHttpSessionId(accessor));
		if (sess!=null) {
	   		sendModelState(this.getHttpSessionId(accessor), modelName);
	   	}
	}
	
    @MessageMapping("/{modelName}/startModel")
   	public void startModel (@DestinationVariable("modelName") String modelName, 
   			@Payload RunRequest req, SimpMessageHeaderAccessor accessor) {
    	req.modelName = modelName;
    	String httpSessionId = this.getHttpSessionId(accessor);
    	
    	ModelRunnerIDE sess = (ModelRunnerIDE) SessionMgr.getInstance().getMbtStarterForModel(modelName, httpSessionId);
 	   	if (sess != null) {
 	   		if (sess.isRunning()) {
	   			sess.resumeRun();
	   			return;
 	   		}
 	   		sess.stopMbt();
 	   		SessionMgr.getInstance().closeModel(sess);
   		}

 	   	// start model execution
    	if (StringUtil.isEmpty((String)req.options.get("submitedBy"))) {
    		req.options.put("submitedBy", Config.getProperty("License.Email"));
    	}
    	try {
    		sess = new ModelRunnerIDE (httpSessionId, new ModelMgr(req.modelName));
    		SessionMgr.getInstance().addMbtStarter(sess);
    		sess.startMbt(false, req.mbtMode, req.options);
	    	sendModelState(this.getHttpSessionId(accessor), modelName);
//			RuntimeController.sendIdeRuntimeSessions(httpSessionId);
    	}
    	catch (Throwable e) {
    		String msg = e.getMessage();
    		if (msg==null) msg = "nullpointer exception";
    		this.sendIdeMessageLocal(accessor, new IdeMessage("warn", msg, "IdeSvc.startModel"));
    	}
   	}

    @MessageMapping("/{modelName}/debugModel")
   	public void debugModel (@DestinationVariable("modelName") String modelName, 
   			@Payload RunRequest req, SimpMessageHeaderAccessor accessor) {
    	req.modelName = modelName;
    	String httpSessionId = this.getHttpSessionId(accessor);
    	ModelRunnerIDE sess = (ModelRunnerIDE) SessionMgr.getInstance().getMbtStarterForModel(modelName, httpSessionId);
 	   	if (sess != null) {
 	   		if (sess.isRunning()) {
	   			sess.resumeDebug();
	   			return;
 	   		}
 	   		sess.stopMbt();
 	   		SessionMgr.getInstance().closeModel(sess);
   		}
 	   	try {
 			sess = new ModelRunnerIDE(httpSessionId, new ModelMgr(req.modelName));
    		SessionMgr.getInstance().addMbtStarter(sess);
    		sess.startMbt(true, req.mbtMode, req.options);
	    	sendModelState(this.getHttpSessionId(accessor), modelName);
//			RuntimeController.sendIdeRuntimeSessions(httpSessionId);
    	}
    	catch (Throwable e) {
    		this.sendIdeMessageLocal(accessor, new IdeMessage("warn", e.getMessage(), "IdeSvc.debugModel"));
    	}
   	}

    @MessageMapping("/{modelName}/setBreakpoints")
   	public void setBreakpoints (@DestinationVariable("modelName") String modelName, 
   			@Payload String[] uidList, SimpMessageHeaderAccessor accessor) {
    	ModelRunnerIDE sess = (ModelRunnerIDE) SessionMgr.getInstance().getMbtStarterForModel(modelName, this.getHttpSessionId(accessor));
 	   	if (sess!=null && sess.isRunning()) {
		   sess.setBreakpoints(Arrays.asList(uidList));
	   }
   	}

    @MessageMapping("/{modelName}/stopModel")
   	public void stopModel (@DestinationVariable("modelName") String modelName, 
   		SimpMessageHeaderAccessor accessor) {
    	String httpSessID = this.getHttpSessionId(accessor);
    	ModelRunner sess = (ModelRunnerIDE) SessionMgr.getInstance().getMbtStarterForModel(modelName, httpSessID);
 	   	if (sess !=null) {
 		   sess.stopMbt();
 		   SessionMgr.getInstance().closeModel(sess);
	   	}
	   	else {
 			this.sendIdeMessageLocal(accessor, ModelNotRunning);
	   	}
 	   	sendModelState(this.getHttpSessionId(accessor), modelName);
   	}

    @MessageMapping("/{modelName}/pauseModel")
   	public void pauseModel (@DestinationVariable("modelName") String modelName, 
   	   		SimpMessageHeaderAccessor accessor) {
    	ModelRunnerIDE sess = (ModelRunnerIDE) SessionMgr.getInstance().getMbtStarterForModel(modelName, this.getHttpSessionId(accessor));
 	   	if (sess != null && sess.isRunning()) {
	   		sess.requestPause();
	   	}
	   	else {
 			this.sendIdeMessageLocal(accessor, ModelNotRunning);
	   	}
 	   	sendModelState(this.getHttpSessionId(accessor), modelName);
    }

    @MessageMapping("/{modelName}/stepOver")
   	public void stepOver (@DestinationVariable("modelName") String modelName, 
   	   		SimpMessageHeaderAccessor accessor) {
    	ModelRunnerIDE sess = (ModelRunnerIDE) SessionMgr.getInstance().getMbtStarterForModel(modelName, this.getHttpSessionId(accessor));
 	   	if (sess != null && sess.isRunning()) {
			sess.stepOver();
	   	}
	   	else {
 			this.sendIdeMessageLocal(accessor, ModelNotRunning);
	   	}
 	   	sendModelState(this.getHttpSessionId(accessor), modelName);
    }

//    @MessageMapping("/{modelName}/lock")
//	public void lockModel (@DestinationVariable("modelName") String modelName,
//   	   		SimpMessageHeaderAccessor accessor) {
//    	try {
//			ModelMgr modelMgr = new ModelMgr(modelName);
//			modelMgr.forceLock();
//			this.sendIdeDataLocal(accessor, "locked", modelName);
//    	}
//    	catch (Exception e) {
//    		this.sendIdeMessageLocal(accessor, new IdeMessage("warn", e.getMessage(), "IdeSvc.lockModel"));
//    	}
//	}
//

    @MessageMapping("/{modelName}/rename")
	public void renameModel (@DestinationVariable ("modelName") String modelName,
			@Payload String newModelName,
			SimpMessageHeaderAccessor accessor) {
    	try {
    		SessionMgr.getInstance().closeModel(modelName, this.getHttpSessionId(accessor));
	    	ModelMgr modelMgr = new ModelMgr(modelName);
			String folderPath = FileUtil.findModelFolder(newModelName);
			if (folderPath != null) {
				this.sendIdeMessageLocal(accessor, new IdeMessage("warn", "Model already exists: " + newModelName, "IdeSvc.renameModel"));
				return;
			}
			String fromPath = modelMgr.getFolderPath();
			String toPath = fromPath.replace(modelMgr.getModelName(), newModelName);
			FileUtil.renameFile(fromPath, toPath);
			this.sendIdeDataLocal(accessor, "renamed", newModelName);
    	}
    	catch (Exception e) {
    		this.sendIdeMessageLocal(accessor, new IdeMessage("warn", e.getMessage(), "IdeSvc.renameModel"));
    	}
	}


    @MessageMapping("/newModel")
	public void newModel (@Payload ModelProp modelProp,
			SimpMessageHeaderAccessor accessor) {
    	try {
	    	String newModelFolder = FileUtil.findModelFolder(modelProp.modelName);
	    	if (newModelFolder!=null) {
				this.sendIdeMessageLocal(accessor, new IdeMessage("error", "Unable to create new model, model already exists: " + modelProp.modelName, "IdeSvc.newModel"));
				return;
	    	}
			String modelRoot = Config.getModelRoot();
	    	String modelPath = modelProp.folderPath;
			if (modelPath.equalsIgnoreCase("Root") || modelPath.contentEquals("/")) {
				modelPath = modelRoot;
			}
			else if (modelPath.startsWith("/")) {
				modelPath = FileUtil.concatFilePath(modelRoot, modelPath.substring(1));
			}
			else {
				modelPath = FileUtil.concatFilePath(modelRoot, modelPath);
			}
			if (!FileUtil.exists(modelPath)) {
	    		this.sendIdeMessageLocal(accessor, new IdeMessage("error", "Invalid folder path: " + modelPath, "IdeSvc.newMOdel"));
	    		return;
			}
	
			if (modelPath.contains("..")) {
	    		this.sendIdeMessageLocal(accessor, new IdeMessage("error", "Contains invalid characters .. in folder path", "IdeSvc.newMOdel"));
	    		return;
	    	}
			
			modelPath = FileUtil.concatFilePath(modelPath, modelProp.modelName);
			modelPath = modelPath.replace("//", "/");
			this.copyModelFolder(FileUtil.concatFilePath(modelRoot, "_template", modelProp.template), modelPath);
			
//			ModelMgr modelMgr = ModelCacheMgr.getModelMgr(modelProp.modelName);
			this.sendIdeDataLocal(accessor, "model.created", modelProp.modelName);
    	}
    	catch (Exception e) {
    		this.sendIdeMessageLocal(accessor, new IdeMessage("warn", e.getMessage(), "IdeSvc.newModel"));
    	}
	}
	
    @MessageMapping("/{modelName}/saveAs")
	public void saveAs(@DestinationVariable ("modelName") String modelName,
			@Payload String newModelFilePath,
			SimpMessageHeaderAccessor accessor) {
    	try {
	    	if (newModelFilePath.endsWith(".fsm")) {
	    		newModelFilePath = newModelFilePath.substring(0, newModelFilePath.lastIndexOf("."));
	    	}
	    	ModelMgr modelMgr = new ModelMgr(modelName);
	    	if (newModelFilePath.contains("..")) {
				this.sendIdeMessageLocal(accessor, new IdeMessage("error", "Model path contains invalid character ..", "IdeSvc.newModel"));
				return;
	    	}
	    	String[] pList = newModelFilePath.split("/");
	    	String newModelName = pList[pList.length-1];
	    	String newModelFolder;
	    	if (pList.length == 1) {
	    		// save in same folder
	    		newModelFolder = modelMgr.getFolderPath().replace(modelMgr.getModelName(), newModelName);
	    	}
	    	else if (pList[0].equals("")) {
	    		// path from model root
	    		newModelFolder = Config.getModelRoot() + newModelFilePath.substring(1) + ".fsm";
	    	}
	    	else {
	    		// relative path from current folder
	    		newModelFolder = modelMgr.getFolderPath().replace(modelMgr.getModelName(), newModelFilePath);
	    	}
	    	if (FileUtil.exists(newModelFolder)) {
				this.sendIdeMessageLocal(accessor, new IdeMessage("error", "Unable to create new model, model already exists: " + newModelName, "IdeSvc.newModel"));
	    	}
    		this.copyModelFolder(modelMgr.getFolderPath(), newModelFolder);
    		this.sendIdeDataLocal(accessor, "model.saveas.done", newModelName);
    	}
		catch (Exception e) {
			this.sendIdeMessageLocal(accessor, new IdeMessage("error", e.getMessage(), "IdeSvc.newModel"));
		}
	}

	private void copyModelFolder (String fromFolderPath_p, String toFolderPath_p) throws Exception {
		if (!FileUtil.exists(fromFolderPath_p)) {
			throw new Exception ("Copy from folder does not exist: " + fromFolderPath_p);
		}
		if (FileUtil.exists(toFolderPath_p)) {
			throw new Exception ("target model folder already exists: " + toFolderPath_p);
		}

		int cnt = FileUtil.copyFolder(fromFolderPath_p, toFolderPath_p, false);
		if (cnt <= 0) throw new Exception ("model.copy.failed");
	}

    @MessageMapping("/{modelName}/guard/{stateUID}")
	public void sendGuardStatus (@DestinationVariable ("modelName") String modelName,
			@DestinationVariable String stateUID,
			SimpMessageHeaderAccessor accessor) {
    	try {
    		ModelRunner sess = (ModelRunnerIDE) SessionMgr.getInstance().getMbtStarterForModel(modelName, this.getHttpSessionId(accessor));
     	   	if (sess != null && sess.isRunning()) {
		    	MbtScriptExecutor scriptExec = sess.getExecDirector().getScriptExec();
		    	ModelMgr modelMgr = sess.getModelMgr();
		    	ScxmlNode scxmlNode = modelMgr.getScxmlNode();
		    	StateNode stateNode = scxmlNode.findStateByUID(stateUID);
		    	List<GuardStatus> guardList = new java.util.ArrayList<>();
		    	for (TransitionNode transNode: stateNode.getTransitions()) {
		    		GuardStatus g = new GuardStatus();
		    		g.uid = transNode.getUID();
		    		g.guardExpr = transNode.getGuard();
		    		g.evalStatus = true;
		    		if (scriptExec!=null) {
		    			g.evalStatus = scriptExec.evalGuard(transNode);
		    		}
		    	}
		    	this.sendIdeDataLocal(accessor, "guardStatus", guardList);
		   	}
     	   	else {
				this.sendIdeMessageLocal(accessor, ModelNotRunning);
		   	}
    	}
    	catch (Throwable e) {
    		this.sendIdeMessageLocal(accessor, new IdeMessage("warn", e.getMessage(), "IdeSvc.sendGuardStatus"));
    	}
    }
    
    @MessageMapping("/{modelName}/expr")
	public void execExpr (@DestinationVariable ("modelName") String modelName,
			@Payload String script,
			SimpMessageHeaderAccessor accessor) {
    	ModelRunner sess = (ModelRunnerIDE) SessionMgr.getInstance().getMbtStarterForModel(modelName, this.getHttpSessionId(accessor));
 	   	if (sess != null && sess.isRunning()) {
	   		List<Object> rsltList = Arrays.asList(script.split("\n")).stream()
   				.map(expr -> {
   					DebugExpr d = new DebugExpr();
   					if (StringUtil.isEmpty(expr)) {
   						return null;
   					}
					d.expr = expr;
   			   		try {
   						Object retObj = sess.getExecDirector().getScriptExec().runMScript(expr);
   						d.result = retObj;
   			   		}
   			   		catch (Exception e) {
   						d.result = e.toString();
   			   		}
   			   		return d;
   				})
   				.filter(d -> d!=null)
   				.collect(Collectors.toList());
			this.sendIdeDataLocal(accessor, "debug.expr", rsltList);
	   	}
 	   	else {
			this.sendIdeMessageLocal(accessor, ModelNotRunning);
			this.sendIdeDataLocal(accessor, "debug.expr", ModelNotRunning.text);
 	   	}
    }

}
