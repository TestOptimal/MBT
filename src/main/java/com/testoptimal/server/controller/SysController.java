package com.testoptimal.server.controller;

import java.util.List;
import java.util.Map;

//import org.jasypt.encryption.StringEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.testoptimal.exec.navigator.Navigator;
import com.testoptimal.server.Application;
import com.testoptimal.server.config.Config;
import com.testoptimal.server.config.ConfigVersion;
import com.testoptimal.server.controller.helper.CodeAssist;
import com.testoptimal.server.controller.helper.CodeAssistMgr;
import com.testoptimal.server.model.ClientReturn;
import com.testoptimal.util.DateUtil;
import com.testoptimal.util.misc.SerialNum;

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
@RequestMapping("/api/v1/sys")
@JsonIgnoreProperties(ignoreUnknown = true)
@CrossOrigin
public class SysController {
	private static Logger logger = LoggerFactory.getLogger(SysController.class);
	public static final String[] ClientConfigPropList = new String[] { "License.Edition", "License.exceptions",
			"License.Agreement.url", "License.Agreement.label", "License.Email",
			"IDE.shortcuts.ide", "alertMsg", "modelFolder", "IDE.msgHideMillis", "welcomed"};

	
	@GetMapping(value = "sysinfo", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> getConfigList(ServletRequest request) throws Exception {
		HttpSession session = ((HttpServletRequest) request).getSession();
		Map<String,Object> retProp = new java.util.HashMap<>();
	    for (String propKey: ClientConfigPropList) {
	    	String propVal = Config.getProperty(propKey);
	    	if (propVal!=null) {
		    	retProp.put(propKey, propVal);
	    	}
	    }
		retProp.put("uid", "config");
		retProp.put("typeCode", "config");
		retProp.put("TestOptimalVersion", Config.versionDesc);
		retProp.put("releaseDate", ConfigVersion.getReleaseDate());
		retProp.put("serialNum", SerialNum.getSerialNum());
		retProp.put("sequencers", Navigator.getSequencerList());
		retProp.put("hostport", Config.getHostName()+":"+Application.getPort());
		retProp.put("ipaddress", Config.getIpAddress());
		retProp.put("sysID", Config.getSysID());
		retProp.put("osName", Config.getOsName());
		retProp.put("osVersion", Config.getOsVersion());
		retProp.put("javaVersion",  Config.getJavaVersion());
		retProp.put("TestOptimalVersion", Config.versionDesc);
		retProp.put("releaseDate", DateUtil.dateToString(ConfigVersion.getReleaseDate(), ConfigVersion.DateFormat));
		retProp.put("HttpSessionID", session.getId());
		return new ResponseEntity<>(retProp, HttpStatus.OK);
	}
	
	
//	@SecurityRequirement(name = "basicAuth")
//	@GetMapping(value = "shutdown", produces = MediaType.APPLICATION_JSON_VALUE)
////	@PreAuthorize("hasPermission('hasAccess','ADMIN')")
//	public ResponseEntity<ClientReturn> shutdown () throws Exception {
//		AsyncShutdown aShut = new AsyncShutdown(50);
//		aShut.start();
//		return new ResponseEntity<>(ClientReturn.OK(), HttpStatus.OK);
//	}
	
	@SecurityRequirement(name = "basicAuth")
	@GetMapping(value = "CodeAssist/model", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, List<CodeAssist>>> caListModel(
		ServletRequest request) throws Exception {
		return new ResponseEntity<>(CodeAssistMgr.getMScriptCAList(), HttpStatus.OK);
	}

	@SecurityRequirement(name = "basicAuth")
	@GetMapping(value = "CodeAssist/data", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, List<CodeAssist>>> caListData(
		ServletRequest request) throws Exception {
		return new ResponseEntity<>(CodeAssistMgr.getMScriptCAListData(), HttpStatus.OK);
	}

	@SecurityRequirement(name = "basicAuth")
	@PostMapping(value = "CodeAssist/expr", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<CodeAssist>> caListExpr (
		@RequestBody List<String> expr,
		ServletRequest request) throws Exception {
		return new ResponseEntity<>(CodeAssistMgr.getMScriptCAListByExpr (expr), HttpStatus.OK);
	}

	@SecurityRequirement(name = "basicAuth")
	@PutMapping(value = "config", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ClientReturn> saveConfig (
			@RequestBody Map<String, String> map) {
		try {
			map.forEach((key,value) -> 
				Config.setProperty(key, value)
			);
			Config.save();
			return new ResponseEntity<>(ClientReturn.OK(), HttpStatus.OK);
		}
		catch (Exception e) {
			return new ResponseEntity<>(ClientReturn.Alert(e.getMessage()), HttpStatus.OK);
		}
	}


}

