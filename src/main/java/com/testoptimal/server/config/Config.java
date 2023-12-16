package com.testoptimal.server.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.testoptimal.util.FileUtil;
import com.testoptimal.util.StringUtil;
import com.testoptimal.util.misc.SerialNum;

/**
 * Config class must not call out other classes that have "static" initializer that may indirectly
 * calls back to Config.
 * 
 * @author yxl01
 *
 */
@Component
public final class Config {
	private static Logger logger = LoggerFactory.getLogger(Config.class);

	public static final String copyright = "Copyright 2008 - 2023 TestOptimal, LLC.  All rights reserved.";
	public final static String versionDesc = ConfigVersion.getReleaseLabel(); // majorVersion + "." + ConfigVersion.minorVersion + "." + ConfigVersion.buildNum;
	
    private static String logPath;
    public static String getLogPath() { return logPath; }
    
    protected static String webRootPath;
    public static String getWebRootPath () { return webRootPath; } 

    private static String ModelRoot;
    public static String getModelRoot() {
    	return ModelRoot; 
    }

    /* mbt server root */
    protected static String root;
    public static String getRootPath () { return root; }
   
   
    private static String configPath;
    public static String getConfigPath () { return configPath; }
    
    private static String tempPath;
    public static String getTempPath () { return tempPath; }
    
    private static String jarPath;
    public static String getJarPath () { return jarPath; }
    
    private static String classPath;
    public static String getClassPath () { return classPath; }

    private static String dashRoot;
    public static String getDashRoot () { return dashRoot; }
    
    private static String hostName;
    public static String getHostName () { return hostName; }
    private static String ipAddress;
    public static String getIpAddress () { return ipAddress; }
    
    public static String sysID;
    public static String getSysID() { return sysID; }
    
	private static String configFile;
	
	private static Properties configProp = new Properties();
    	
	private static void loadConfigProp(String configFilePath_p) throws Exception {
		hostName = InetAddress.getLocalHost().getHostName();
		sysID = SerialNum.getSysID(hostName, 20);
		ipAddress = InetAddress.getLocalHost().getHostAddress();
		
		// Read properties file.
	    configProp = new Properties();
	    try {
	        configProp.load(new FileInputStream(configFilePath_p));
	    } 
	    catch (IOException e) {
	    	System.out.println("Unable to read the config file: " + configFilePath_p);
	    	throw e;
	    }

	    FileUtil.setEncoding(configProp.getProperty("file.encoding"));
	    Config.save();
	}

	public Config() throws Exception {
		root = System.getProperty("user.dir") + File.separator;
		
		logger.info("user.dir: " + root);
		String configFile_p = "config.properties";
		
		webRootPath = root + "www" + File.separator;
		jarPath = root + "lib" + File.separator;
		tempPath = root + "work" + File.separator;
		classPath = root + "build" + File.separator;
	    configPath = root  + "config" + File.separator;
	    logPath = root + File.separator + "log" + File.separator;
		configFile = configFile_p;
	    ModelRoot = root + "model" + File.separator;
	    dashRoot = root + "dashboard" + File.separator;

		// Read properties file.
    	loadConfigProp(configPath + configFile);
	    
	    String tempModel = Config.getProperty("modelFolder");
	    if (!StringUtil.isEmpty(tempModel)) {
	    	ModelRoot = tempModel.trim();
	    	if (!ModelRoot.endsWith("/") && !ModelRoot.endsWith("\\")) {
	    		ModelRoot = ModelRoot + File.separator;
	    	}
	    }
	    
	    if (!FileUtil.exists(tempPath)) {
	    	FileUtil.createFolder(tempPath);
	    }
	    
//    	System.setOut(new CustomPrintStream());
	}

	public static String getMScriptLogFilePath(String modelName_p, long fileIdx_p) {
		return logPath + "mscript_" + modelName_p + "_" + fileIdx_p + ".log";
	}
	
	/**
	 * saves the config to file
	 * @throws Exception
	 */
	public static void save () throws Exception {
	    // Write properties file.
	    try {
	    	Properties tmp = new Properties() {
	    	    @Override
	    	    public synchronized Enumeration<Object> keys() {
	    	        return Collections.enumeration(new TreeSet<Object>(super.keySet()));
	    	    }
	    	};
	    	tmp.putAll(configProp);
	    	tmp.store(new FileOutputStream(configPath + configFile), null);
	    } 
	    catch (IOException e) {
	    	logger.error("Error saving config changes", e);
	    	throw e;
	    }  
	}
	
	public static String getProperty (String name_p, String defaultValue_p) {
		String prop = configProp.getProperty(name_p);
		if (StringUtil.isEmpty(prop)) {
//			Config.setProperty(name_p, defaultValue_p);
//			prop = defaultValue_p;
			return defaultValue_p;
		}
		else return prop.trim();
	}
	
	public String getEdition() {
		return "ProMBT";
	}
	
	public static String getProperty (String name_p) {
		String retVal = Config.getProperty(name_p, null);
		return retVal;
	}
	
	public static void setProperty (String name_p, String value_p) {
		if (value_p==null) configProp.remove(name_p);
		else configProp.setProperty(name_p, value_p);
	}
	
	public static String getOsName() {
		String osName = System.getProperty("os.name");
		if (osName==null) osName = "";
		else {
			try {
				osName = URLEncoder.encode(osName,  FileUtil.getEncoding());
			}
			catch (UnsupportedEncodingException e) {
				// ok
			}
		}
		return osName;
	}
	
	public static String getOsVersion() {
		String osName = System.getProperty("os.version");
		if (osName==null) osName = "";
		else {
			try {
				osName = URLEncoder.encode(osName,  FileUtil.getEncoding());
			}
			catch (UnsupportedEncodingException e) {
				// ok
			}
		}
		return osName;
	}

	public static String getJavaVersion() {
		String osName = System.getProperty("java.version");
		if (osName==null) osName = "";
		else {
			try {
				osName = URLEncoder.encode(osName,  FileUtil.getEncoding());
			}
			catch (UnsupportedEncodingException e) {
				// ok
			}
		}
		
		String bitType = System.getProperty("os.arch");
		if (StringUtil.isEmpty(bitType)) {
			bitType = System.getProperty("sun.arch.data.model");
		}
		
		if (!StringUtil.isEmpty(bitType)) {
			osName = osName + "_" + bitType;
		}
		return osName;
	}

	public static final String userDelimiter = ";";
	
	public static void setAlert(String msg_p) {
		setProperty("alert", msg_p);
	}
	
	/**
	 * returns all properties which have the prefix in the key.
	 * @param keyPrefix_p
	 * @return
	 */
	public static Properties getPropertiesByPrefix (String keyPrefix_p) {
		keyPrefix_p = keyPrefix_p.toUpperCase();
		Properties retProp = new Properties ();
		for (java.util.Map.Entry entry: configProp.entrySet()) {
			String key = (String) entry.getKey();
			if (key.toUpperCase().startsWith(keyPrefix_p)) {
				retProp.put(key, entry.getValue());
			}
		}
		
		return retProp;
	}
	
	public static Properties getConfigFile (String fileName_p) {
	    Properties prop = new Properties();
	    try {
	        prop.load(new FileInputStream(configPath + fileName_p));
	        return prop;
	    }
	    catch (Exception e) {
	    	
	    	return null;
	    }
	}


	public static enum OS {mac, windows, solaris, linux }
	public static OS getOS() {
		String os = System.getProperty("os.name").toLowerCase();
		// windows
		if (os.indexOf("win") >= 0) {
			return OS.windows;
		}
		else if (os.indexOf("mac") >= 0) {
			return OS.mac;
		}
		else if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) {
			return OS.linux;
		}
		else if (os.indexOf("sunos") >= 0) {
			return OS.solaris;
		}
		else {
			return OS.windows;
		}
	}
	
	public static String getModelFileFilter () {
		String filterReqExp = Config.getProperty("Model.arch.includes","");
		if (filterReqExp.equals("")) {
			filterReqExp = ".*\\.(gvy|json|tsv|txt|xls|ds)$";
		}
		return filterReqExp;
	}

}
