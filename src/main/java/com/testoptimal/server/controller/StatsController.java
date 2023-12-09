package com.testoptimal.server.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.testoptimal.exec.FSM.ModelMgr;
import com.testoptimal.stats.DashboardStats;
import com.testoptimal.stats.StatsMgr;
import com.testoptimal.stats.exec.ModelExec;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 
 * @author yxl01
 *
 */
@RestController
@RequestMapping("/api/v1/stats")
@SecurityRequirement(name = "basicAuth")
@JsonIgnoreProperties(ignoreUnknown = true)
@CrossOrigin
public class StatsController {
	@GetMapping(value = "exec/{modelName}/{mbtSessID}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ModelExec> getModelExec (@PathVariable (name="modelName", required=true) String modelName,
			@PathVariable (name="mbtSessID", required=true) String mbtSessID,
			ServletRequest request) throws Exception {
		String httpSessID = ((HttpServletRequest) request).getSession().getId();
		ModelMgr modelMgr = new ModelMgr(modelName);
		ModelExec modelExec = StatsMgr.findModelExec(modelMgr, mbtSessID, httpSessID);
		return new ResponseEntity<>(modelExec, HttpStatus.OK);
	}


	@GetMapping(value = "exec/{modelName}/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<ModelExec>> getModelExecList (@PathVariable (name="modelName", required=true) String modelName,
			ServletRequest request) throws Exception {
		ModelMgr modelMgr = new ModelMgr(modelName);
		List<ModelExec> statsList = StatsMgr.getInstance().getStatsList(modelMgr);
		return new ResponseEntity<>(statsList, HttpStatus.OK);
	}
	
	@GetMapping(value = "dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DashboardStats> getDashboard (ServletRequest request) throws Exception {
		DashboardStats dstats = DashboardStats.getStats();
		return new ResponseEntity<>(dstats, HttpStatus.OK);
	}
}
