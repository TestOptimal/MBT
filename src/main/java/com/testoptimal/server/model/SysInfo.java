package com.testoptimal.server.model;


import java.util.Date;
import java.util.List;

import com.testoptimal.server.Application;
import com.testoptimal.server.config.Config;
import com.testoptimal.server.config.ConfigVersion;
import com.testoptimal.util.DateUtil;

public class SysInfo {
	public String licEmail;
	public String licKey;
	public String licStatus;
	public Date expireDate;
	public List<String> exceptions;
	public String hostport;
	public String ipaddress;
	public String sysID;
	public String osName;
	public String osVersion;
	public String javaVersion;
	public String TestOptimalVersion;
	public String releaseDate;
	public String edition;
	public boolean expired;
	
	
	
	public static SysInfo getSysInfo () throws Exception {
		SysInfo licInfo = new SysInfo ();
		licInfo.licEmail = Config.getProperty("License.Email", "");
		licInfo.licKey = Config.getProperty("***", "");
		licInfo.licStatus = "Valid";
		licInfo.hostport = Config.getHostName()+":"+Application.getPort();
		licInfo.ipaddress = Config.getIpAddress();
		licInfo.sysID = Config.getSysID();
		licInfo.osName = Config.getOsName();
		licInfo.osVersion = Config.getOsVersion();
		licInfo.javaVersion = Config.getJavaVersion();
		licInfo.TestOptimalVersion = Config.versionDesc;
		licInfo.releaseDate = DateUtil.dateToString(ConfigVersion.getReleaseDate(), ConfigVersion.DateFormat);
		licInfo.edition = Config.getProperty("License.Edition", "MBT");
		return licInfo;
	}
	
}