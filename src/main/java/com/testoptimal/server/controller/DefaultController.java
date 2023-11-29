package com.testoptimal.server.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.testoptimal.exec.navigator.Navigator;
import com.testoptimal.license.SerialNum;
import com.testoptimal.server.config.Config;
import com.testoptimal.server.config.ConfigVersion;
import com.testoptimal.server.model.ClientReturn;
import com.testoptimal.server.security.UserMgr;
import com.testoptimal.util.StringUtil;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * 
 * @author yxl01
 *
 */
@RestController
//@Api(tags="Security")
@RequestMapping("/api/v1/sec")
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultController {
	private static Logger logger = LoggerFactory.getLogger(DefaultController.class);

//	@ApiOperation(value = "check", notes="login")
	@RequestMapping(value = "check", method = RequestMethod.POST)
	public ResponseEntity<ClientReturn> checkPost (ServletRequest request,
			@RequestParam (name="username", required=false) String username,
			@RequestParam (name="password", required=false) String password,
			@RequestHeader(name="username", required=false) String headerUsername, 
			@RequestHeader(name="password", required=false) String headerPassword) throws Exception {
		return this.check(request, username, password, headerUsername, headerPassword);
	}
	
//	@ApiOperation(value = "check", notes="login")
	@RequestMapping(value = "check", method = RequestMethod.GET)
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
		if (!StringUtil.isTrue(Config.getProperty("License.Ack"))) {
			return new ResponseEntity<> (ClientReturn.Alert("Not authorized"), HttpStatus.OK);
		}
		else {
			if (UserMgr.getInstance().authenticate(username, password)) {
				return new ResponseEntity<> (ClientReturn.OK(), HttpStatus.OK);
			}
			else {
				return new ResponseEntity<> (ClientReturn.Alert("Invalid email or password"), HttpStatus.OK);
			}
		}
	}

//	@ApiOperation(value = "register", notes="login")
	@RequestMapping(value = "register", method = RequestMethod.POST)
	public ResponseEntity<ClientReturn> registerPost (ServletRequest request,
			@RequestParam (name="username", required=false) String username,
			@RequestParam (name="password", required=false) String password,
			@RequestHeader(name="username", required=false) String headerUsername, 
			@RequestHeader(name="password", required=false) String headerPassword
		) throws Exception {
		return this.register(request, username, password, headerUsername, headerPassword);
	}
	
//	@ApiOperation(value = "register", notes="login")
	@RequestMapping(value = "register", method = RequestMethod.GET)
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
		if (Config.getProperty("License.Ack","N").equals("Y")) {
			UserMgr.getInstance().registerUser(username, password);
			Config.setProperty("License.Ack", "Y");
			Config.save();			
			return new ResponseEntity<> (ClientReturn.OK(), HttpStatus.OK);
		}
		else {
			return new ResponseEntity<> (ClientReturn.Alert("User already registered"), HttpStatus.OK);			
		}
	}
	
//	@ApiOperation(value = "Config Settings", notes="Config setting list")
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

	    for (String propKey: SysController.ClientConfigPropList) {
	    	String propVal = Config.getProperty(propKey);
	    	if (propVal!=null) {
		    	retProp.put(propKey, propVal);
	    	}
	    }
	    
//	    List<String> pList = PluginMgr.getAllPluginList().stream()
//				.map(p-> p.getPluginID())
//				.collect(Collectors.toList());
//		retProp.put("pluginList", pList);
		retProp.put("HttpSessionID", session.getId());
		return new ResponseEntity<>(retProp, HttpStatus.OK);
	}
	

//	private void notifyUserRegistration (String userID_p) {
//		 new Thread() {
//	         public void run() {
//	     		Map<String,Object> params = new java.util.HashMap<>();
//	    		params.put("userID", userID_p);
////	    		try {
////	    			String msg = TOServerMgr.callTOAction("userRegistry", params);
////	    			System.out.println(msg);
////	    		}
////	    		catch (Throwable e) {
////	    			System.out.printf(e.getLocalizedMessage());
////	    		}
//	         }
//	      }.start();
//	}
}

