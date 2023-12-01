package com.testoptimal.server.controller;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

//import org.apache.catalina.connector.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.Gson;
import com.testoptimal.exec.ModelRunner;
import com.testoptimal.exec.FSM.ModelMgr;
import com.testoptimal.mscript.groovy.GroovyScript;
import com.testoptimal.scxml.DataTemplate;
import com.testoptimal.scxml.ScxmlNode;
import com.testoptimal.server.controller.helper.SessionMgr;
import com.testoptimal.server.model.ClientReturn;
import com.testoptimal.server.model.IdeMessage;
import com.testoptimal.server.model.ModelInfo;
import com.testoptimal.util.FileUtil;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * 
 * @author yxl01
 *
 */
@RestController
//@Api(tags="Model")
@RequestMapping("/api/v1/model")
@SecurityRequirement(name = "basicAuth")
@JsonIgnoreProperties(ignoreUnknown = true)
@CrossOrigin
public class ModelController {
	private static Logger logger = LoggerFactory.getLogger(ModelController.class);

//	@ApiOperation(value = "Open Model", notes="Open model")
	@RequestMapping(value = "{modelName}/getModel", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ModelInfo> openModel(@PathVariable (name="modelName", required=true) String modelName,
			ServletRequest request, Principal principal) throws Exception {
		String httpSessID = ((HttpServletRequest) request).getSession().getId();
		ModelRunner mbtSess = SessionMgr.getInstance().getMbtStarterForModel(modelName, httpSessID);
		ModelMgr modelMgr;
		if (mbtSess==null) {
			modelMgr = new ModelMgr(modelName);
		}
		else {
			modelMgr = mbtSess.getModelMgr();
		}
		
//		// alert user if model is locked by another user
//		String lockEmail = modelMgr.lockModel(principal.getName());
//		if (lockEmail!=null) {
//			IdeSvc.sendIdeMessage(httpSessID, new IdeMessage("alert", "Model is currently locked by " + lockEmail, "ModelControler.openModel"));
//		}
		Gson gson = new Gson ();
		ModelInfo openInfo = new ModelInfo();
		ScxmlNode scxml = modelMgr.getScxmlNode();
		scxml.setModelName(modelName);
		openInfo.modelJson = gson.toJson(scxml);
		openInfo.templateJson = gson.toJson(new DataTemplate());
		openInfo.folderPath = modelMgr.getRelativePath();
		openInfo.lockAcquired = true; // modelMgr.hasLock();
		openInfo.valid = true;
		
		IdeSvc.sendModelState(httpSessID, modelName);
		RuntimeController.sendIdeRuntimeSessions(httpSessID);
		return new ResponseEntity<>(openInfo, HttpStatus.OK);
	}

//	@ApiOperation(value = "Close Model", notes="close a model")
	@RequestMapping(value = "{modelName}/close", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ClientReturn> closeModel(@PathVariable (name="modelName", required=true) String modelName,
			ServletRequest request) throws Exception {
		String httpSessID = ((HttpServletRequest) request).getSession().getId();
		ModelRunner mbtSess = SessionMgr.getInstance().closeModel(modelName, httpSessID);
		return new ResponseEntity<>(ClientReturn.OK(), HttpStatus.OK);
	}
	
//	@ApiOperation(value = "Save Model", notes="Save a model definition")
	@RequestMapping(value = "{modelName}/saveModel", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ClientReturn> saveModel(@PathVariable (name="modelName", required=true) String modelName,
			@RequestBody String modelJson, ServletRequest request) throws Exception {
		String httpSessID = ((HttpServletRequest) request).getSession().getId();
		ModelMgr modelMgr = new ModelMgr(modelName);

//		// alert user if model is locked by another user
//		String lockEmail = modelMgr.lockModel();
//		if (lockEmail!=null) {
//			String errMsg = "Model is currently locked by " + lockEmail + ". You will not be able to save the changes until you acquire the lock on the model.";
//			IdeSvc.sendIdeMessage(httpSessID, new IdeMessage("warn", errMsg, "IdeSvc.saveModel"));
//			throw new Exception (errMsg);
//		}
		Gson gson = new Gson();
		ScxmlNode scxmlObj = gson.fromJson(modelJson, ScxmlNode.class);
		scxmlObj.save(modelMgr.getModelFolderPath());

//		modelMgr.loadScxml(); // to get submodel reloaded
		IdeSvc.sendIdeData(httpSessID, "saved", "Model");
		List<String> exceptList = modelMgr.getExceptionList();
		if (!exceptList.isEmpty()) {
			IdeSvc.sendIdeMessage(httpSessID, new IdeMessage("warn", exceptList.toString(), "ModelSvc.saveModel"));
		}
		return new ResponseEntity<>(ClientReturn.OK(), HttpStatus.OK);
	}
	
//	@ApiOperation(value = "Model Log", notes="Model log file")
	@RequestMapping(value = "{modelName}/log", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> modelScripts(@PathVariable (name="modelName", required=true) String modelName,
			ServletRequest request) throws Exception {
		String httpSessID = ((HttpServletRequest) request).getSession().getId();
		ModelRunner mbtSess = SessionMgr.getInstance().getMbtStarterForModel(modelName, httpSessID);
		if (mbtSess==null) {
			throw new Exception ("Model not running");
		}
		String filePath = mbtSess.getExecDirector().getMScriptLogFilePath();
		return new ResponseEntity<String>(FileUtil.readFile(filePath).toString(), HttpStatus.OK);
	}
	

	
//	@ApiOperation(value = "Model Scripts", notes="Model scripts, including submodels")
	@RequestMapping(value = "{modelName}/getScript", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<GroovyScript>> getScripts(@PathVariable (name="modelName", required=true) String modelName,
			ServletRequest request) throws Exception {
		ModelMgr modelMgr = new ModelMgr(modelName);
		List<GroovyScript> list = modelMgr.getScriptList();
		list.stream().filter(s-> !s.getModelName().equalsIgnoreCase(modelName)).forEach(s-> {
			s.setReadonly();
		});
		return new ResponseEntity<>(list, HttpStatus.OK);
	}

//	@ApiOperation(value = "Save Model Scripts", notes="Save model scripts")
	@RequestMapping(value = "{modelName}/saveScript", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ClientReturn> saveScripts(@PathVariable (name="modelName", required=true) String modelName,
			@RequestBody List<GroovyScript> scriptList,
			ServletRequest request) throws Exception {
		ModelMgr modelMgr = new ModelMgr(modelName);
		HttpSession session = ((HttpServletRequest) request).getSession();
		scriptList = scriptList.stream().filter(s-> !s.isReadonly()).collect(Collectors.toList());
		modelMgr.saveScriptList(scriptList);
		IdeSvc.sendIdeData(session.getId(), "saved", "Script");
		return new ResponseEntity<>(ClientReturn.OK(), HttpStatus.OK);
	}
	

}
