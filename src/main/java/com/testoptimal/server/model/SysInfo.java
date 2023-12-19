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
	public String licLabel;
	public String licUrl;
	
	
	
	public static SysInfo getSysInfo () throws Exception {
		SysInfo licInfo = new SysInfo ();
		licInfo.licEmail = Config.getProperty("License.Email", "");
//		licInfo.licKey = Config.getProperty("***", "");
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
		licInfo.licUrl = Config.getProperty("License.Agreement.url", "https://github.com/TestOptimal/MBT/blob/main/LICENSE");
		licInfo.licLabel = Config.getProperty("License.Agreement.label", "TestOptimal GPL 3.0");
		return licInfo;
	}
	
}