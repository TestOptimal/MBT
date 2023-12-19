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

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
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
import com.testoptimal.server.config.Config;
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
	
	@GetMapping(value = "model/{modelName}/session/{mbtSessID}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ModelExec> getModelExec (@PathVariable (name="modelName", required=true) String modelName,
			@PathVariable (name="mbtSessID", required=true) String mbtSessID,
			ServletRequest request) throws Exception {
		String httpSessID = ((HttpServletRequest) request).getSession().getId();
		ModelMgr modelMgr = new ModelMgr(modelName);
		ModelExec modelExec = StatsMgr.findModelExec(modelMgr, mbtSessID, httpSessID);
		return new ResponseEntity<>(modelExec, HttpStatus.OK);
	}


	@GetMapping(value = "model/{modelName}/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<ModelExec>> getModelExecList (@PathVariable (name="modelName", required=true) String modelName,
			ServletRequest request) throws Exception {
		ModelMgr modelMgr = new ModelMgr(modelName);
		List<ModelExec> statsList = StatsMgr.getInstance().getStatsList(modelMgr);
		return new ResponseEntity<>(statsList, HttpStatus.OK);
	}
	
	@GetMapping(value = "dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DashboardStats> getDashboard (ServletRequest request) throws Exception {
		// perform cleanup 
		int retentionDays = Integer.parseInt(Config.getProperty("Dashboard.days", "14"));
		DashboardStats.purge(retentionDays);
		
		DashboardStats dstats = DashboardStats.getStats();
		return new ResponseEntity<>(dstats, HttpStatus.OK);
	}
}
