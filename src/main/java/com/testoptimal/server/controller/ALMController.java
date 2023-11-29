package com.testoptimal.server.controller;

import java.util.List;

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
import com.testoptimal.exec.FSM.ModelMgr;
import com.testoptimal.exec.FSM.RequirementMgr;
import com.testoptimal.server.model.ClientReturn;
import com.testoptimal.server.model.Requirement;

import jakarta.servlet.ServletRequest;

/**
 * 
 * @author yxl01
 *
 */
@RestController
//@Api(tags="ALM")
@RequestMapping("/api/v1/alm")
@JsonIgnoreProperties(ignoreUnknown = true)
@CrossOrigin
public class ALMController {
	private static Logger logger = LoggerFactory.getLogger(ALMController.class);


//	@ApiOperation(value = "Save Requirement", notes="Save Reguirements")
	@RequestMapping(value = "{modelName}/requirement", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ClientReturn> getReqList(@PathVariable (name="modelName", required=true) String modelName,
			@RequestBody List<Requirement> reqList,
			ServletRequest request) throws Exception {
		ModelMgr modelMgr = new ModelMgr(modelName);
		RequirementMgr.getInstance().saveRequirement(modelMgr, reqList);
		return new ResponseEntity<>(ClientReturn.OK(), HttpStatus.OK);
	}

//	@ApiOperation(value = "Requirement", notes="Reguirements")
	@RequestMapping(value = "{modelName}/requirement", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<Requirement>> getReqList(@PathVariable (name="modelName", required=true) String modelName,
			ServletRequest request) throws Exception {
		ModelMgr modelMgr = new ModelMgr(modelName);
		List<Requirement> reqList = RequirementMgr.getInstance().getRequirement(modelMgr);
		return new ResponseEntity<>(reqList, HttpStatus.OK);
	}


	
}