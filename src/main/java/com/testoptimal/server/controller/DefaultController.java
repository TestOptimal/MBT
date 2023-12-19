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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.testoptimal.exec.navigator.Navigator;
import com.testoptimal.server.config.Config;
import com.testoptimal.server.config.ConfigVersion;
import com.testoptimal.server.model.ClientReturn;
import com.testoptimal.server.model.SysInfo;
import com.testoptimal.server.security.UserMgr;
import com.testoptimal.util.FileUtil;
import com.testoptimal.util.misc.SerialNum;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * 
 * @author yxl01
 *
 */
@Hidden
@RestController
@RequestMapping("/api/v1/default")
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultController {
	private static Logger logger = LoggerFactory.getLogger(DefaultController.class);

	@PostMapping("check")
	public ResponseEntity<ClientReturn> checkPost (ServletRequest request,
			@RequestParam (name="username", required=false) String username,
			@RequestParam (name="password", required=false) String password,
			@RequestHeader(name="username", required=false) String headerUsername, 
			@RequestHeader(name="password", required=false) String headerPassword) throws Exception {
		return this.check(request, username, password, headerUsername, headerPassword);
	}
	
	@GetMapping("check")
	public ResponseEntity<ClientReturn> check (ServletRequest request,
			@RequestParam (name="username", required=false) String username,
			@RequestParam (name="password", required=false) String password,
			@RequestHeader(name="username", required=false) String headerUsername, 
			@RequestHeader(name="password", required=false) String headerPassword) throws Exception {
		if (headerUsername!=null) {
			username = headerUsername;
			password = headerPassword;
		}
		logger.info("logging in user: " + username);
		if (UserMgr.getInstance().authenticate(username, password)) {
			return new ResponseEntity<> (ClientReturn.OK(), HttpStatus.OK);
		}
		else {
			return new ResponseEntity<> (ClientReturn.Alert("Invalid email or password"), HttpStatus.OK);
		}
	}

	@PostMapping("register")
	public ResponseEntity<ClientReturn> registerPost (ServletRequest request,
			@RequestParam (name="username", required=false) String username,
			@RequestParam (name="password", required=false) String password,
			@RequestHeader(name="username", required=false) String headerUsername, 
			@RequestHeader(name="password", required=false) String headerPassword
		) throws Exception {
		return this.register(request, username, password, headerUsername, headerPassword);
	}
	
	@GetMapping("register")
	public ResponseEntity<ClientReturn> register (ServletRequest request,
			@RequestParam (name="username", required=false) String username,
			@RequestParam (name="password", required=false) String password,
			@RequestHeader(name="username", required=false) String headerUsername, 
			@RequestHeader(name="password", required=false) String headerPassword
		) throws Exception {
		if (headerUsername!=null) {
			username = headerUsername;
			password = headerPassword;
		}
		
		if (username==null) {
			return new ResponseEntity<> (ClientReturn.Alert("Missing username"), HttpStatus.OK);
		}
		logger.info("register user: " + username);
		UserMgr.getInstance().registerUser(username, password);
		Config.save();			
		return new ResponseEntity<> (ClientReturn.OK(), HttpStatus.OK);
	}

	@GetMapping(value = "sysinfo", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SysInfo> getSysInfo() throws Exception {
		return new ResponseEntity<>(SysInfo.getSysInfo(), HttpStatus.OK);
	}
	
	
	@GetMapping(value = "config", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> getConfigList(ServletRequest request) throws Exception {
		HttpSession session = ((HttpServletRequest) request).getSession();
		Map<String,Object> retProp = new java.util.HashMap<>();
		retProp.put("uid", "config");
		retProp.put("typeCode", "config");

		retProp.put("TestOptimalVersion", Config.versionDesc);
		retProp.put("releaseDate", ConfigVersion.getReleaseDate());
		retProp.put("serialNum", SerialNum.getSerialNum());
		retProp.put("sequencers", Navigator.getSequencerList());

	    for (String propKey: SysController.ClientConfigPropList) {
	    	String propVal = Config.getProperty(propKey);
	    	if (propVal!=null) {
		    	retProp.put(propKey, propVal);
	    	}
	    }
		retProp.put("HttpSessionID", session.getId());
		return new ResponseEntity<>(retProp, HttpStatus.OK);
	}
	
	@GetMapping(value = "ideCss", produces = "text/css" )
	public ResponseEntity<UrlResource> genModelGraph (ServletRequest request) throws Exception {
		try {
			String fPath = FileUtil.concatFilePath(Config.getConfigPath(), "ide.css");
	    	return FileController.getFile(fPath, "ide.css");
 		}
		catch (Exception e) {
			return null;
		}
	}
}

