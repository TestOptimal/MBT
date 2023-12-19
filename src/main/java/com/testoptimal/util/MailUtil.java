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

package com.testoptimal.util;
import java.lang.reflect.Constructor;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.testoptimal.server.config.Config;

public class MailUtil {

	public static void sendMail (String fromAddress, String toAddress, 
			String subject, String mailMsg) throws Exception {
		Properties props = Config.getPropertiesByPrefix("mail.");

		// create authenticator if specified
	    Authenticator auth = null;
	    String mailAuthClass = Config.getProperty("mail.auth.class");
	    if (!StringUtil.isEmpty(mailAuthClass)) {
			Class theClass = Class.forName(mailAuthClass);
			Constructor constructor = theClass.getConstructor();
			auth = (Authenticator) constructor.newInstance();
	    }
	       
		// Get session
		Session session = Session.getDefaultInstance(props, auth);

		// Define message
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(fromAddress));
		message.addRecipient(Message.RecipientType.TO, 
		  new InternetAddress(toAddress));
		message.setSubject(subject);
		message.setText(mailMsg);
//		message.setContent("<h1>Hello world</h1>", "text/html");

		// Send message
		Transport.send(message);
	}
	

}
