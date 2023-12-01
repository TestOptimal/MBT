package com.testoptimal.server.controller;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.testoptimal.exception.MBTAbort;
import com.testoptimal.exec.ExecutionStatus;
import com.testoptimal.exec.ModelRunner;
import com.testoptimal.exec.ModelRunnerIDE;
import com.testoptimal.exec.FSM.ModelMgr;
import com.testoptimal.server.Application;
import com.testoptimal.server.config.Config;
import com.testoptimal.server.controller.helper.SessionInfo;
import com.testoptimal.server.controller.helper.SessionMgr;
import com.testoptimal.server.model.ClientReturn;
import com.testoptimal.server.model.ExecStatusInfo;
import com.testoptimal.server.model.IdeMessage;
import com.testoptimal.server.model.RunRequest;
import com.testoptimal.util.FileUtil;
import com.testoptimal.util.ModelFile;
import com.testoptimal.util.StringUtil;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Only Runtime edition can run model async.
 * 
 * @author yxl01
 *
 */
@RestController
@RequestMapping("/api/v1/runtime")
@SecurityRequirement(name = "basicAuth")
@JsonIgnoreProperties(ignoreUnknown = true)
@CrossOrigin
public class RuntimeController {
	private static Logger logger = LoggerFactory.getLogger(RuntimeController.class);

	@GetMapping(value = "model/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<String>> listModel() throws Exception {
		String modelFolder = Config.getModelRoot();
		List<ModelFile> modelList = ModelFile.getAllModelList(new File(modelFolder));
		List<String> retList = modelList.stream().map(m ->  m.modelName).collect(Collectors.toList());
		return new ResponseEntity<>(retList, HttpStatus.OK);
	}


	@GetMapping(value = "session/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<SessionInfo>> listSessions (ServletRequest request) throws Exception {
		String httpSessID = ((HttpServletRequest) request).getSession().getId();

		List<SessionInfo> sessList = SessionMgr.getInstance().getMbtStarterForUserSession(httpSessID).stream().map(mbtSess-> new SessionInfo(mbtSess.getModelMgr().getModelName(), mbtSess.getMbtSessionID(), mbtSess.isRunning())).collect(Collectors.toList());
		return new ResponseEntity<>(sessList , HttpStatus.OK);
	}

	@PostMapping(value = "model/run/async", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> runModelAsync (
			@RequestBody RunRequest runReq, 
			ServletRequest request) throws Exception, MBTAbort {
		String httpSessID = ((HttpServletRequest) request).getSession().getId();
		logger.info("model: " + runReq.modelName);
		try {
			Boolean Debug = (Boolean)runReq.options.get("debug");
			boolean debug = Debug==null?false: Debug;
			if (runReq.options==null) {
				runReq.options = new java.util.HashMap<>();
			}
			if (runReq.options.get("autoClose")==null) {
				runReq.options.put("autoClose", true);
			}

			ModelRunnerIDE mbtSess = new ModelRunnerIDE(httpSessID, new ModelMgr(runReq.modelName));
			SessionMgr.getInstance().addMbtStarter(mbtSess);
			mbtSess.startMbt(false,runReq.mbtMode, runReq.options);
			
			Map<String, Object> m = ClientReturn.map("mbtSessID", mbtSess.getMbtSessionID());
			m.put("statsURL", Application.genURL(Config.getHostName(), Application.getPort()) + "/api/v1/stats/exec/" + runReq.modelName + "/-1");
			logger.info("model exec started, mbtSessID: " + mbtSess.getMbtSessionID());
			m.put("status", "running");
			return new ResponseEntity<>(m, HttpStatus.OK);
		}
		catch (Exception e) {
			String msg = e.getMessage();
			logger.error("Error running model " + runReq.modelName + ": " + msg);
			Map<String, Object> m = ClientReturn.map("status", "error");
			m.put("statusMsg", msg);
			return new ResponseEntity<>(m, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping(value = "model/{modelName}/monitor", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ExecStatusInfo> monitorStats (@PathVariable (name="modelName", required=true) String modelName,
			ServletRequest request) throws Exception {
		String httpSessID = ((HttpServletRequest) request).getSession().getId();
		ModelRunner mbtSess = SessionMgr.getInstance().getMbtStarterForModel(modelName, httpSessID);
		if (mbtSess == null) {
			return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
		}
		ExecStatusInfo execInfo = new ExecStatusInfo();
		execInfo.execStatus = mbtSess.getExecDirector().getExecStat();
		return new ResponseEntity<>(execInfo, HttpStatus.OK);
	}

	@GetMapping(value = "session/stop", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ClientReturn> interruptAllSessions (
		ServletRequest request) throws Exception {
		String httpSessID = ((HttpServletRequest) request).getSession().getId();
		SessionMgr.getInstance().getMbtStarterForUserSession(httpSessID).forEach(s -> s.stopMbt());
		return new ResponseEntity<>(ClientReturn.OK(), HttpStatus.OK);
	}

	@GetMapping(value = "session/close", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ClientReturn> closeAllSessions (
		ServletRequest request) throws Exception {
		String httpSessID = ((HttpServletRequest) request).getSession().getId();
		SessionMgr.getInstance().closeModelAll(httpSessID);
		return new ResponseEntity<>(ClientReturn.OK(), HttpStatus.OK);
	}

	@GetMapping(value = "session/{mbtSessionID}/stop", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ClientReturn> closeSession (
			@PathVariable (name="mbtSessionID", required=true) String mbtSessID,
		ServletRequest request) throws Exception {
		SessionMgr.getInstance().getMbtStarterForMbtSession(mbtSessID).stopMbt();
		return new ResponseEntity<>(ClientReturn.OK(), HttpStatus.OK);
	}

	
	@GetMapping(value = "session/{mbtSessionID}/status", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ExecutionStatus> sessionStatus (ServletRequest request,
			@PathVariable (name="mbtSessionID", required=true) String mbtSessID) throws Exception {
		ModelRunner sessObj = SessionMgr.getInstance().getMbtStarterForMbtSession(mbtSessID);
		if (sessObj==null) {
			String errMsg = "Unknown mbtSessionID: " + mbtSessID;
			logger.info(errMsg);
			throw new Exception (errMsg);
		}
		return new ResponseEntity <> (sessObj.getExecStatus(), HttpStatus.OK);
	}

	@GetMapping(value = "model/{modelName}/log", produces = MediaType.TEXT_PLAIN_VALUE)
	public String getScriptLog (@PathVariable (name="modelName", required=true) String modelName,
			ServletRequest request) throws Exception {
		String httpSessID = ((HttpServletRequest) request).getSession().getId();
		ModelRunner mbtSess = SessionMgr.getInstance().getMbtStarterForModel(modelName, httpSessID);
		if (mbtSess==null) {
			return "";
		}
		else {
			String logFilePath = mbtSess.getExecDirector().getMScriptLogFilePath();
			String logString = logFilePath + "\n\n" + FileUtil.readFile(logFilePath);
			return logString;
		}
	}


	@GetMapping(value = "model/{modelName}/screenshot/{snapTime}", produces = MediaType.IMAGE_PNG_VALUE)
	public ResponseEntity<UrlResource> getModelScreenshotBySnapTime  (@PathVariable (name="modelName", required=true) String modelName,
			@PathVariable (name="snapTime", required=true) String snapTime,
			ServletRequest request) throws Exception {
		String httpSessID = ((HttpServletRequest) request).getSession().getId();
		ModelRunner mbtSess = SessionMgr.getInstance().getMbtStarterForModel(modelName, httpSessID);
		ModelMgr modelMgr = mbtSess.getModelMgr();
		String snapScreenPath = modelMgr.getScreenshotFolderPath();
		File folderReader = new File(snapScreenPath, "");
	    String[] fileList = folderReader.list();
	    if (StringUtil.isEmpty(snapTime)) {
	    	throw new Exception ("snapscreen.snaptime.missing");
	    }
	    String snapToken = "_" + snapTime + ".";
		for (int i = 0; fileList != null && i < fileList.length; i++) {
			if (fileList[i].contains(snapToken)) {
				File screenFile = new File(snapScreenPath + fileList[i]);
	    		UrlResource resource = new UrlResource(screenFile.toURI());
			    return ResponseEntity.ok()
		            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
		            .body(resource);
			}
		}
		throw new Exception ("snapscreen.file.notfound");
   	}
	
	@GetMapping(value = "model/{modelName}/close", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ClientReturn> closeModel(@PathVariable (name="modelName", required=true) String modelName,
		ServletRequest request) throws Exception {
		String httpSessID = ((HttpServletRequest) request).getSession().getId();
		ModelRunner mbtSess = SessionMgr.getInstance().closeModel(modelName, httpSessID);
		IdeSvc.sendIdeMessage(httpSessID, new IdeMessage("alert", "Model session closed: " + modelName, "MbtController.closeModel"));
		RuntimeController.sendIdeRuntimeSessions(httpSessID);
		return new ResponseEntity<>(ClientReturn.OK(), HttpStatus.OK);
	}	
	
	
	public static void sendIdeRuntimeSessions (String httpSessionID_p) throws Exception {
		List<SessionInfo> sessList = SessionMgr.getInstance().getMbtStarterForUserSession(httpSessionID_p).stream().map(mbtSess-> new SessionInfo(mbtSess.getModelMgr().getModelName(), mbtSess.getMbtSessionID(), mbtSess.isRunning())).collect(Collectors.toList());
		IdeSvc.sendIdeData(httpSessionID_p, "runtime/list", sessList);
	}
}
