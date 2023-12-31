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

package com.testoptimal.server;

import java.util.Arrays;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

import com.testoptimal.exec.FSM.RequirementMgr;
import com.testoptimal.exec.navigator.Navigator;
import com.testoptimal.exec.plugin.PluginMgr;
import com.testoptimal.server.config.Config;
import com.testoptimal.server.config.ConfigVersion;
import com.testoptimal.server.controller.helper.CodeAssistMgr;
import com.testoptimal.server.controller.helper.SessionMgr;
import com.testoptimal.server.security.UserMgr;
import com.testoptimal.stats.StatsMgr;
import com.testoptimal.util.StringUtil;
import com.testoptimal.util.misc.SerialNum;

/**
 * SpringBoot application startup.  The config.properties file is read from user's home directory if found,
 * else fall back to the project resource folder.  
 * 
 * You may change the "config.properties" file name to have your app name in it to make it distinctive.
 * 
 * @author yxl01
 *
 */
@ComponentScan
@SpringBootApplication
public class Application extends SpringBootServletInitializer implements CommandLineRunner {
	private static Logger logger = LoggerFactory.getLogger(Application.class);

	@Autowired
	public Environment environment;
	
	private static Application app;
	public static int getPort() {
		return app.port;
	}
	
	private static long startMS = System.currentTimeMillis();
	private static boolean toReady = false;
	public static boolean isToReady() { return toReady; }
	
	public static String Root;
	public static final Date startDT = new Date();
	public static Date getStartDT() {
		return startDT;
	}
	
    @Value("${server.port}")
    private int port;
    
    @Value("${server.ssl.enabled}")
    private boolean sslEnabled;
    public static boolean isSslEnabled() { return app.sslEnabled; }
    public static String genURL (String host_p, int port_p) { 
    	return app.sslEnabled?"https://":"http://" + host_p + ":" + port_p; 
    }

    @Value("${CONFIG.UserMgr}")
    private String ClassUserMgr;
    
    @Value("${CONFIG.SessionMgr}")
    private String ClassSessionMgr;
    
    @Value("${CONFIG.Sequencers}")
    private String ClassSequencersMgr;
    
    @Value("${CONFIG.StatsMgr}")
    private String ClassStatsMgr;
    
    @Value("${CONFIG.RequirementMgr}")
    private String ClassRequirementMgr;
    
    @Autowired
    Config config;
    
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Application.class);
        application.run(args);
    }

	@Override
	public void run(String... args) throws Exception {
		app = this;
		System.out.println("port: " + this.port);
		
		String graphvizDOT = environment.getProperty("GRAPHVIZ_DOT");
		if (!StringUtil.isEmpty(graphvizDOT)) {
			logger.info("environment.GRAPHVIZ_DOT: " + graphvizDOT);
			System.setProperty("GRAPHVIZ_DOT", graphvizDOT);
		}
		this.init();
		this.startup();;
	}
    

	private void init() throws Exception {
		try {
			logger.info("Initializing TestOptimal Server...");
			logger.info("Checking Config ...");
		}
	    catch (Exception e) {
	    	logger.error("Error reading config.properties file at " + Config.getRootPath(), e);
	    	System.exit(0);
	    }
		
	    UserMgr.instantiate (ClassUserMgr);
	    SessionMgr.instantiate(ClassSessionMgr);
	    String [] seqList = ClassSequencersMgr.split(",");
	    Arrays.asList(seqList).stream().forEach(s -> {
	    	String[] s2 = s.split(":");
    		try {
    			logger.info("loading sequencer " + s);
    			Navigator.addSequencer(s2[0], s2[1]);
    		}
    		catch (Exception e) {
    			logger.warn("Error loading sequencer " + s + " - " + e.toString());
    		}
	    });
	    
	    PluginMgr.init();
	    
	    StatsMgr.instantiate(ClassStatsMgr);
	    RequirementMgr.instantiate (ClassRequirementMgr);
	}
	
    private void startup () throws Exception {
	   	logger.info((new java.util.Date()).toString());
 	   	logger.info ("Loading config from: " + Config.getConfigPath());
 	   	
 	   	logger.info ("Model-Based Testing and Process Automation, copyright 2008 - 2020, all rights reserved. TestOptimal, LLC");
 	   	logger.info ("Version: " + Config.versionDesc);
 	   	
 	   	System.out.println("***TO Version:" + Config.versionDesc);
 	   	logger.info("***TO Version:" + Config.versionDesc);
 	   	System.out.println("***Build Date:" + ConfigVersion.getReleaseDate());
 	   	logger.info("***Build Date:" + ConfigVersion.getReleaseDate());
 	   	System.out.println("***Edition: " + Config.getProperty("License.Edition"));
 	   	logger.info("***Edition: " + Config.getProperty("License.Edition", "MBT"));
		String ideURL = Application.genURL(Config.getHostName(), port);
		logger.info("IDE Browser: " + ideURL);
		System.out.println("IDE Browser: " + ideURL);
		System.out.println("Sys ID: " + Config.getSysID());
		logger.info("Sys ID: " + Config.getSysID());
		System.out.println("MAC Address: " + SerialNum.getMAC());
		logger.info("MAC Address: " + SerialNum.getMAC());
		System.out.println("Serial #: " + SerialNum.getSerialNum());
		logger.info("Serial #: " + SerialNum.getSerialNum());
		 	   		   
	   	logger.info("java.library.path: " + System.getProperty("java.library.path"));
	   	logger.info("GRAPHVIZ_DOT: " + System.getProperty("GRAPHVIZ_DOT"));
	   	CodeAssistMgr.init();

	   	String readyMsg = "TO Ready: " + (System.currentTimeMillis() - startMS) + "ms";
	   	System.out.println(readyMsg);
	   	logger.info(readyMsg);
	   	toReady = true;
    }
    
//    @Value("${jasypt.encryptor.password}")
//    private String test;
//    
//    @Bean(name="encryptablePropertyResolver")
//    EncryptablePropertyResolver encryptablePropertyResolver(@Value("${jasypt.encryptor.password}") String password) {
//        return new TOEncryptablePropertyResolver(password.toCharArray());
//    }
}
