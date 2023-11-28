
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
	  	