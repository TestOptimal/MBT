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

package com.testoptimal.exec.mscript;

import com.testoptimal.server.Application;
import com.testoptimal.server.config.Config;
import com.testoptimal.server.config.Config.OS;
import com.testoptimal.util.MailUtil;

public class Util {
	private static RandPlugin randPlugin = new RandPlugin();

	
	/**
	 * send mail.  Requires javaMail setup in system config (mail.*) for javaMail prop
	 * (see https://docs.genesys.com/Documentation/ESEmail/latest/Admin/EmSJavaMail)
	 * 
	 * @param fromAddress from address, if blank passed, it will use the license email address
	 * @param toAddress to address
	 * @param subject subject text
	 * @param messageText message body text
	 */
	public static void sendMail(String fromAddress, String toAddress, String subject, String messageText) 
		throws Exception {
		MailUtil.sendMail(fromAddress, toAddress, subject, messageText);
	}
	
	/**
	 * returns the admin login email
	 * @return
	 */
	public static String getAdminEmail() {
		return Config.getProperty("License.Email");
	}
	
	/**
	 * returns current TestOptimal server host name
	 * @return
	 */
	public static String getServerName() {
		return Config.getHostName();
	}
	
	/**
	 * returns OS name of this TestOptimal server.
	 * @return
	 */
	public static String getOsName() {
		OS os = Config.getOS();
		return os.name();
	}
	
	/**
	 * returns java version number.
	 * @return
	 */
	public static String getVersionJava() {
		String jv = Config.getJavaVersion();
		return jv;
	}
	
	/**
	 * returns version number of TestOptimal release build.
	 * @return
	 */
	public static String getVersionTO() {
		String jv = Config.versionDesc;
		return jv;
	}

	/**
	 * returns the port number used by this TestOptimal server.
	 * @return
	 */
	public static int getPort() {
		return Application.getPort();
	}
	
	public static RandPlugin getRandom() {
		return randPlugin;
	}
	
}
