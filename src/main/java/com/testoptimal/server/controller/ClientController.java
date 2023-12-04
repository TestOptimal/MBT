package com.testoptimal.server.controller;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.Gson;
import com.testoptimal.exception.MBTAbort;
import com.testoptimal.exec.ModelRunner;
import com.testoptimal.exec.ModelRunnerAgent;
import com.testoptimal.exec.ModelRunnerIDE;
import com.testoptimal.exec.FSM.ModelMgr;
import com.testoptimal.scxml.ScxmlNode;
import com.testoptimal.server.Application;
import com.testoptimal.server.config.Config;
import com.testoptimal.server.controller.helper.SessionMgr;
import com.testoptimal.server.model.ClientReturn;
import com.testoptimal.server.model.RunRequest;
import com.testoptimal.server.model.TestCmd;
import com.testoptimal.server.model.TestResult;
import com.testoptimal.server.model.parser.GherkinModel;
import com.testoptimal.server.model.parser.ModelParserGherkin;
import com.testoptimal.stats.exec.ExecStateTrans;
import com.testoptimal.stats.exec.ModelExec;
import com.testoptimal.util.FileUtil;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 
 * @author yxl01
 *
 */
@RestController
@RequestMapping("/api/v1/client")
@SecurityRequirement(name = "basicAuth")
@JsonIgnoreProperties(ignoreUnknown = true)
@CrossOrigin
public class ClientController {
	private static Logger logger = LoggerFactory.getLogger(ClientController.class);
	public static enum Status { success, error, timeout};

	@PostMapping(value = "model/start", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> startModel (
			@RequestBody RunRequest runReq, 
			ServletRequest request) throws Exception, MBTAbort {
		String httpSessID = ((HttpServletRequest) request).getSession().getId();
		logger.info("model: " + runReq.modelName);
		try {
			if (runReq.options==null) {
				runReq.options = new java.util.HashMap<>();
			}
			Integer timeoutMillis = (Integer) runReq.options.get("timeoutMillis");
			if (timeoutMillis==null) {
				timeoutMillis = 5000;
			}
			ModelRunnerAgent mbtSess = new ModelRunnerAgent(httpSessID, new ModelMgr(runReq.modelName), timeoutMillis);
			SessionMgr.getInstance().addMbtStarter(mbtSess);
			mbtSess.startMbt(runReq.mbtMode, runReq.options);
			
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

	@GetMapping(value = "model/stop", produces = MediaType.APPLICATION_JSON_VALUE)
	public void stopModel (
			@RequestParam (name="mbtSessID", required=true) String mbtSessID,
			ServletRequest request) throws Exception, MBTAbort {
		logger.info("model: " + mbtSessID);
		ModelRunnerAgent mbtSess = (ModelRunnerAgent) SessionMgr.getInstance().getMbtStarterForMbtSession(mbtSessID);
		if (mbtSess!=null && mbtSess.isRunning()) {
			mbtSess.stopMbt();
		}
		else {
			throw new Exception ("Model not running");
		}
	}
	
	@GetMapping(value = "model/next", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TestCmd> nextStep (
			@RequestParam (name="mbtSessID", required=true) String mbtSessID,
			@RequestParam (name="timeoutMillis", required=false) int timeoutMillis,
			ServletRequest request) throws Exception, MBTAbort {
		logger.info("model: " + mbtSessID);
		ModelRunnerAgent mbtSess = (ModelRunnerAgent) SessionMgr.getInstance().getMbtStarterForMbtSession(mbtSessID);
		TestCmd cmdObj;
		if (mbtSess!=null && mbtSess.isRunning()) {
			cmdObj = mbtSess.fetchCmd(timeoutMillis);
		}
		else {
			cmdObj = new TestCmd(TestCmd.Type.ERROR, "Model not running", null);
		}
		return new ResponseEntity<>(cmdObj, HttpStatus.OK);
	}
	
	@PostMapping(value = "model/result", produces = MediaType.APPLICATION_JSON_VALUE)
	public void sendResult (
			@RequestParam (name="mbtSessID", required=true) String mbtSessID,
			@RequestBody TestResult result,
			ServletRequest request) throws Exception, MBTAbort {
		logger.info("model: " + mbtSessID);
		ModelRunnerAgent mbtSess = (ModelRunnerAgent) SessionMgr.getInstance().getMbtStarterForMbtSession(mbtSessID);
		if (mbtSess!=null && mbtSess.isRunning()) {
			mbtSess.sendResult(result);
		}
		else {
			throw new Exception("Model not running");
		}
	}
	

	@GetMapping(value = "model/stats", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RunResult> getModelExec (
			@RequestParam (name="mbtSessID", required=true) String mbtSessID,
			ServletRequest request) throws Exception {
		String httpSessID = ((HttpServletRequest) request).getSession().getId();
		ModelExec modelExec = null;
		ModelRunner mbtSess = SessionMgr.getInstance().getMbtStarterForMbtSession(mbtSessID);
		if (mbtSess == null || mbtSess.getExecDirector().getExecStats()==null) {
			throw new Exception ("Model closed? No execution results found for model session " + mbtSessID);
		}
		modelExec = mbtSess.getExecDirector().getExecStats();
		RunResult result = this.makeReuslts(modelExec);
		
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@PostMapping(value = "model/upload", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ClientReturn> uploadModelJson (
			@RequestBody String modelJson, 
			ServletRequest request) throws Exception {
		
		ScxmlNode scxmlObj;
		Gson gson = new Gson();
		scxmlObj = gson.fromJson(modelJson, ScxmlNode.class);
		scxmlObj.autoLayout();
		
		String modelName = scxmlObj.getModelName();
		logger.info("ClientController.uploadModelJson: " + modelName);

    	String modelRoot = Config.getModelRoot();
    	this.checkClientFolder("client");

		String modelPath = FileUtil.findModelFolder(modelName);
    	if (modelPath==null) { // do not override existing trigger file if already exists (model folder already exists)
        	String clientFolderPath = FileUtil.concatFilePath(modelRoot, "client");
    		modelPath = FileUtil.concatFilePath(clientFolderPath, modelName + ".fsm");
    		
    		String tplPath = FileUtil.concatFilePath(modelRoot, ".tpl", "fsm.tpl");
    		int cnt = FileUtil.copyFolder(tplPath, modelPath, true);
    		if (cnt <= 0) throw new Exception ("Model create failed");

    		String triggerFilePath = FileUtil.concatFilePath(modelPath, "model", "TRIGGERS.gvy");
			FileUtil.copyFile(FileUtil.concatFilePath(modelRoot, ".tpl", "TRIGGERS.gvy"), triggerFilePath); 
    	}
    	    	
		ModelMgr modelMgr = new ModelMgr(modelName);
		modelPath = modelMgr.getModelFolderPath();
		scxmlObj.save(modelPath);
		
		return new ResponseEntity<>(ClientReturn.OK(), HttpStatus.OK);
	}

	@PostMapping(value = "model/upload/gherkin", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ClientReturn> uploadModelGherkin (
			@RequestBody String modelGherkin, 
			ServletRequest request) throws Exception {
		ScxmlNode scxmlObj;
		GherkinModel gm = ModelParserGherkin.parse(Arrays.asList(modelGherkin.split("\n")));
		scxmlObj = gm.toScxml();
		scxmlObj.autoLayout();
		String modelName = scxmlObj.getModelName();
		logger.info("ClientController.uploadModelGherkin: " + modelName);

    	String modelRoot = Config.getModelRoot();
    	this.checkClientFolder("client");

		String modelPath = FileUtil.findModelFolder(modelName);
    	if (modelPath==null) {
        	String clientFolderPath = FileUtil.concatFilePath(modelRoot, "client");
    		modelPath = FileUtil.concatFilePath(clientFolderPath, modelName + ".fsm");
    		
    		String tplPath = FileUtil.concatFilePath(modelRoot, ".tpl", "fsm.tpl");
    		int cnt = FileUtil.copyFolder(tplPath, modelPath, true);
    		if (cnt <= 0) throw new Exception ("Model create failed");
    	}
		String triggerFilePath = FileUtil.concatFilePath(modelPath, "model", "TRIGGERS.gvy");
		FileUtil.writeToFile(triggerFilePath, gm.getScripts());

		ModelMgr modelMgr = new ModelMgr(modelName);
		modelPath = modelMgr.getModelFolderPath();
		scxmlObj.save(modelPath);
		
		return new ResponseEntity<>(ClientReturn.OK(), HttpStatus.OK);
	}
//
//	@RequestMapping(value = "model/upload/csv", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
//	public ResponseEntity<ClientReturn> uploadModelCSV (
//			@RequestParam (name="modelName", required=true) String modelName,
//			@RequestBody String modelCSV, 
//			ServletRequest request) throws Exception {
//		ModelParserCSV parser = new ModelParserCSV();
//		try ( CSVReader reader = new CSVReader(new StringReader(modelCSV));) {
//			List<String[]> r = reader.readAll();
//			if (parser.parse(r)) {
//				
//			}
//			else {
//				throw new Exception ("Upload model in CSV failed to parse: " + parser.getErrMsg());
//			}
//		}
//		ScxmlNode scxml = parser.getScxmlNode();
//		scxml.setModelName(modelName);
//		scxml.autoLayout();
//		logger.info("ClientController.uploadModelTab: " + modelName);
//
//    	String modelRoot = Config.getModelRoot();
//    	this.checkClientFolder("client");
//
//		String modelPath = FileUtil.findModelFolder(modelName);
//    	if (modelPath==null) {
//        	String clientFolderPath = FileUtil.concatFilePath(modelRoot, "client");
//    		modelPath = FileUtil.concatFilePath(clientFolderPath, modelName + ".fsm");
//    		
//    		String tplPath = FileUtil.concatFilePath(modelRoot, ".tpl", "fsm.tpl");
//    		int cnt = FileUtil.copyFolder(tplPath, modelPath, true);
//    		if (cnt <= 0) throw new Exception ("Model create failed");
//    	}
//		String triggerFilePath = FileUtil.concatFilePath(modelPath, "model", "TRIGGERS.gvy");
//		FileUtil.writeToFile(triggerFilePath, parser.getScripts().stream().collect(Collectors.joining("\n")));
//
//		ModelMgr modelMgr = new ModelMgr(modelName);
//		modelPath = modelMgr.getModelFolderPath();
//		scxml.save(modelPath);
//		
//		return new ResponseEntity<>(ClientReturn.OK(), HttpStatus.OK);
//	}
//	

	@PostMapping(value = "model/gen", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RunResult> modelSeq (
			@RequestBody RunRequest runReq, 
			ServletRequest request) throws Exception, MBTAbort {
		logger.info("model: " + runReq.modelName);
		runReq.options.put("generateOnly", true);
		String httpSessID = ((HttpServletRequest) request).getSession().getId();
		long startMS = System.currentTimeMillis();
		long timeoutMillis = 10000;
		if (runReq.options.get("timeoutMillis")!=null) {
			timeoutMillis = (int) runReq.options.get("timeoutMillis");
		}
		
		ModelRunnerIDE sess = new ModelRunnerIDE(httpSessID, new ModelMgr(runReq.modelName));
		SessionMgr.getInstance().addMbtStarter(sess);
		sess.startMbt(false, runReq.mbtMode, runReq.options);

		ModelExec modelExec = sess.getExecDirector().getExecStats();
		RunResult result = this.makeReuslts(modelExec);
		result.execMillis = System.currentTimeMillis() - startMS;
		logger.info("model exec ended, mbtSessID: " + sess.getMbtSessionID());
		
		List<String> exceptList = sess.getModelMgr().getExceptionList();
		if (!exceptList.isEmpty()) {
			throw new Exception (exceptList.toString());
		}
		else if (sess.isRunning()) {
			sess.stopMbt();
			if (result.execMillis > timeoutMillis) {
				result.errorMsg = "TIMEOUT after " + timeoutMillis + "ms" + ", adjust option timeoutMillis";
				result.status = Status.timeout;
			}
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	
	private RunResult makeReuslts (ModelExec modelExec_p) {
		RunResult result = new RunResult ();
		Map<String, ExecStateTrans> stateTransMap = new java.util.HashMap<>();
		result.results.put("MbtMode", modelExec_p.execSummary.mbtSequencer);
		result.results.put("Status", modelExec_p.execSummary.status);
		result.results.put("ExecTime", new java.util.Date());
		result.results.put("ExecOptions", modelExec_p.execOptions);
		
		modelExec_p.stateTransList.stream().forEach(s -> {
			stateTransMap.put(s.UID, s);
		});
		List<List<String[]>> pathList = modelExec_p.tcList.stream()
			.map(tc -> {
				List<String[]> path = new java.util.ArrayList<String[]>(); 
				path = tc.stepList.stream()
					.map(s -> stateTransMap.get(s.UID))
					.filter(s -> s.type.equalsIgnoreCase("TRANS"))
					.map (s -> new String[] {s.stateName, s.transName, (s.failCount>0?"failed":"passed")})
					.collect(Collectors.toList());
				return path;
			})
			.collect(Collectors.toList());
		result.results.put("pathList", pathList);

		int tcFailed = modelExec_p.tcList.stream()
				.filter(tc -> tc.status == ModelExec.Status.failed)
				.collect(Collectors.toList()).size();
		result.results.put("tcFailed", String.valueOf(tcFailed));

		
		int transCovered = modelExec_p.stateTransList.stream()
			.filter(s -> s.type.equalsIgnoreCase("TRANS") && s.passCount + s.failCount > 0)
			.collect(Collectors.toList()).size();
		result.results.put("transCovered", String.valueOf(transCovered));

		int transSatisfied = modelExec_p.stateTransList.stream()
				.filter(s -> s.type.equalsIgnoreCase("TRANS") && s.passCount > 0 && s.passCount + s.failCount >= s.minTravRequired)
				.collect(Collectors.toList()).size();
		result.results.put("transSatisfied", String.valueOf(transSatisfied));

		int transFailed = modelExec_p.stateTransList.stream()
				.filter(s -> s.type.equalsIgnoreCase("TRANS") && s.failCount > 0)
				.collect(Collectors.toList()).size();
		result.results.put("transFailed", String.valueOf(transFailed));
		
		int stateCovered = modelExec_p.stateTransList.stream()
				.filter(s -> s.type.equalsIgnoreCase("STATE") && s.passCount > 0)
				.collect(Collectors.toList()).size();
		result.results.put("stateCovered", String.valueOf(stateCovered));

		result.results.put("pathCount", String.valueOf(pathList.size()));
		int stepCount = modelExec_p.tcList.stream()
				.map(t -> t.stepList.size())
				.reduce(0, (a, b) -> a + b);
		result.results.put("stepCount", String.valueOf(stepCount));
		return result;
	}

	
	private void checkClientFolder (String clientFolder_p) {
		File aFile = new File (Config.getModelRoot() + clientFolder_p);
		if (aFile.exists()) return;
		else aFile.mkdir();
	}
	
	class RunResult {
		public Status status = Status.success;
		public String errorMsg;
		public long execMillis;
		public Map<String, Object> results = new java.util.TreeMap();
	}
}
