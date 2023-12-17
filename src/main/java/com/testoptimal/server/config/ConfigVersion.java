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


package com.testoptimal.server.config;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigVersion {
	private static Logger logger = LoggerFactory.getLogger(ConfigVersion.class);
	
	private static int majorVersion;
	private static int minorVersion; 
	private static int buildNum; 
	public static final String DateFormat = "yyyy-MM-dd";
	private static java.util.Date releaseDate = null;

	static {
		try {
			Properties prop;		
			InputStream inputStream = ConfigVersion.class.getClassLoader().getResourceAsStream("TO.properties");
			prop = new Properties ();
			prop.load(inputStream);
			String[] vlist = prop.getProperty("to.version").split("\\.");
	        majorVersion = Integer.parseInt(vlist[0]);
	        minorVersion = Integer.parseInt(vlist[1]);
	        buildNum = Integer.parseInt(vlist[2]);
	        SimpleDateFormat formatter = new SimpleDateFormat(DateFormat);
	        releaseDate = formatter.parse(prop.getProperty("build.date"));
		}
		catch (Exception e) {
			System.out.println("Unable to initialize version info");
			logger.error("Unable to initialize version info", e);
			System.exit(0);
		}
	}
	
	public static String getReleaseLabel () {
		return ConfigVersion.majorVersion + "." + ConfigVersion.minorVersion + "." + ConfigVersion.buildNum;
	}
	
	public static Date getReleaseDate() {
		return ConfigVersion.releaseDate;
	}
	
	public static int getMajorVersion () {
		return majorVersion;
	}

	public static int getMinorVersion () {
		return minorVersion;
	}

	public static int getBuildNum () {
		return buildNum;
	}
}
	  	