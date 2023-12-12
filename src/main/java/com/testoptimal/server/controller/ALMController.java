package com.testoptimal.server.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.testoptimal.exec.FSM.ModelMgr;
import com.testoptimal.exec.FSM.RequirementMgr;
import com.testoptimal.server.model.ClientReturn;
import com.testoptimal.server.model.Requirement;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.ServletRequest;

/**
 * 
 * @author yxl01
 *
 */
@RestController
@RequestMapping("/api/v1/alm")
@SecurityRequirement(name = "basicAuth")
@JsonIgnoreProperties(ignoreUnknown = true)
@CrossOrigin
public class ALMController {
	@PutMapping(value = "model/{modelName}/requirement", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ClientReturn> getReqList(@PathVariable (name="modelName", required=true) String modelName,
			@RequestBody List<Requirement> reqList,
			ServletRequest request) throws Exception {
		ModelMgr modelMgr = new ModelMgr(modelName);
		RequirementMgr.getInstance().saveRequirement(modelMgr, reqList);
		return new ResponseEntity<>(ClientReturn.OK(), HttpStatus.OK);
	}

	@GetMapping(value = "model/{modelName}/requirement", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<Requirement>> getReqList(@PathVariable (name="modelName", required=true) String modelName,
			ServletRequest request) throws Exception {
		ModelMgr modelMgr = new ModelMgr(modelName);
		List<Requirement> reqList = RequirementMgr.getInstance().getRequirement(modelMgr);
		return new ResponseEntity<>(reqList, HttpStatus.OK);
	}


	
}