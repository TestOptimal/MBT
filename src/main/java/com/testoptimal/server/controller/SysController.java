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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.testoptimal.client.CodeAssist;
import com.testoptimal.client.CodeAssistMgr;
import com.testoptimal.exec.navigator.Navigator;
import com.testoptimal.license.SerialNum;
import com.testoptimal.server.AsyncShutdown;
import com.testoptimal.server.config.Config;
import com.testoptimal.server.config.ConfigVersion;
import com.testoptimal.server.model.ClientReturn;
import com.testoptimal.server.model.SysInfo;
import com.testoptimal.util.StringUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * 
 * @author yxl01
 *
 */
@RestController
@Api(tags="System")
@RequestMapping("/api/v1/sys")
@JsonIgnoreProperties(ignoreUnknown = true)
@CrossOrigin
public class SysController {
	private static Logger logger = LoggerFactory.getLogger(SysController.class);
	public static final String[] ClientConfigPropList = new String[] {
			"IDE.shortcuts.ide", "alertMsg", "modelFolder", "IDE.msgHideMillis", "welcomed"};

	
	@ApiOperation(value = "Config Settings", notes="Config setting list")
	@RequestMapping(value = "config", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> getConfigList(ServletRequest request) throws Exception {
		HttpSession session = ((HttpServletRequest) request).getSession();
		Map<String,Object> retProp = new java.util.HashMap<>();
		retProp.put("uid", "config");
		retProp.put("typeCode", "config");
		retProp.put("License.Ack", StringUtil.isTrue(Config.getProperty("License.Ack"))); 

		retProp.put("TestOptimalVersion", Config.versionDesc);
		retProp.put("releaseDate", ConfigVersion.getReleaseDate());
		retProp.put("serialNum", SerialNum.getSerialNum());
		retProp.put("sequencers", Navigator.getSequencerList());

	    for (String propKey: ClientConfigPropList) {
	    	String propVal = Config.getProperty(propKey);
	    	if (propVal!=null) {
		    	retProp.put(propKey, propVal);
	    	}
	    }
		retProp.put("HttpSessionID", session.getId());
		return new ResponseEntity<>(retProp, HttpStatus.OK);
	}
	
	@ApiOperation(value = "Get SysInfo", notes="Get SysInfo")
	@RequestMapping(value = "sysinfo", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SysInfo> getSysInfo() throws Exception {
		return new ResponseEntity<>(SysInfo.getSysInfo(), HttpStatus.OK);
	}
	
	@ApiOperation(value = "Shutdown", notes="Shutdown server")
	@RequestMapping(value = "shutdown", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
//	@PreAuthorize("hasPermission('hasAccess','ADMIN')")
	public ResponseEntity<ClientReturn> shutdown () throws Exception {
		AsyncShutdown aShut = new AsyncShutdown(50);
		aShut.start();
		return new ResponseEntity<>(ClientReturn.OK(), HttpStatus.OK);
	}
	
	@ApiOperation(value = "Model Script CodeAssist", notes="Model script code assist list")
	@RequestMapping(value = "CodeAssist/model", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, List<CodeAssist>>> caListModel(@RequestParam (name="pluginList", required=false) List<String> pluginList,
		ServletRequest request) throws Exception {
		if (pluginList==null) {
			pluginList = new java.util.ArrayList<>();
		}
		return new ResponseEntity<>(CodeAssistMgr.getMScriptCAList(pluginList), HttpStatus.OK);
	}

	@ApiOperation(value = "DataDesign Script CodeAssist", notes="DataDesign script code assist list")
	@RequestMapping(value = "CodeAssist/data", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, List<CodeAssist>>> caListData(@RequestParam (name="pluginList", required=false) List<String> pluginList,
		ServletRequest request) throws Exception {
		return new ResponseEntity<>(CodeAssistMgr.getMScriptCAListData(pluginList), HttpStatus.OK);
	}

	@ApiOperation(value = "Script CodeAssist for expr", notes="script code assist list")
	@RequestMapping(value = "CodeAssist/expr", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<CodeAssist>> caListExpr (
		@RequestBody List<String> expr,
		ServletRequest request) throws Exception {
		return new ResponseEntity<>(CodeAssistMgr.getMScriptCAListByExpr (expr), HttpStatus.OK);
	}

	@ApiOperation(value = "Save Config", notes="save config entries")
	@RequestMapping(value = "config", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
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

