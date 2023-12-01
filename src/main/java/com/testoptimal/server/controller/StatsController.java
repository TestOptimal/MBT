package com.testoptimal.server.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.testoptimal.db.ModelExecDB;
import com.testoptimal.exec.FSM.ModelMgr;
import com.testoptimal.server.config.Config;
import com.testoptimal.stats.DashboardStats;
import com.testoptimal.stats.StatsMgr;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 
 * @author yxl01
 *
 */
@RestController
//@Api(tags="Stats")
@RequestMapping("/api/v1/stats")
@SecurityRequirement(name = "basicAuth")
@JsonIgnoreProperties(ignoreUnknown = true)
@CrossOrigin
public class StatsController {

	private static Logger logger = LoggerFactory.getLogger(StatsController.class);
		
//	@ApiOperation(value = "Stats getModelExec", notes="Stats getModelExec")
	@RequestMapping(value = "exec/{modelName}/{mbtSessID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ModelExecDB> getModelExec (@PathVariable (name="modelName", required=true) String modelName,
			@PathVariable (name="mbtSessID", required=true) String mbtSessID,
			ServletRequest request) throws Exception {
		String httpSessID = ((HttpServletRequest) request).getSession().getId();
		ModelMgr modelMgr = new ModelMgr(modelName);
		ModelExecDB modelExec = StatsMgr.findModelExec(modelMgr, mbtSessID, httpSessID);
		return new ResponseEntity<>(modelExec, HttpStatus.OK);
	}


//	@ApiOperation(value = "Stats getModelExecList", notes="Stats getModelExecList")
	@RequestMapping(value = "exec/{modelName}/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<ModelExecDB>> getModelExecList (@PathVariable (name="modelName", required=true) String modelName,
			ServletRequest request) throws Exception {
//		String httpSessID = ((HttpServletRequest) request).getSession().getId();
		ModelMgr modelMgr = new ModelMgr(modelName);
		List<ModelExecDB> statsList = StatsMgr.getInstance().getStatsList(modelMgr);
		return new ResponseEntity<>(statsList, HttpStatus.OK);
	}
	
//	@ApiOperation(value = "Stats dashboard", notes="Stats dashboard")
//	@OpenAPIDefinition(info = @Info(title = "Foos API", version = "v1"))
//	@SecurityRequirement(name = "basicAuth")
	@RequestMapping(value = "dashboard", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DashboardStats> getDashboard (ServletRequest request) throws Exception {
		DashboardStats dstats = StatsMgr.getDashboardStats();
		return new ResponseEntity<>(dstats, HttpStatus.OK);
	}
	
	/**
	 * purges stats with history 
	 */
	@Scheduled(cron="${cron.cleanup.modelstats.expression}") // cron="0 **** ?" or cron="${cron.expression}, fixedDelayString="${...}"
	public void dashboardCollectJob () {
		int keepDays = Integer.parseInt(Config.getProperty("Stats.KeepDays", "7"));
		int limitStats = Integer.parseInt(Config.getProperty("Stats.Limit", "10"));
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, keepDays);
		Date keepDate = cal.getTime();
		logger.info("running model stats cleanup (" + keepDays + "days, max " + limitStats + " executions) ...");
		int modelNum = StatsMgr.purgeStats(keepDate, limitStats);
		logger.info("stats cleanup completed, models affected " + modelNum);
	}	
}
