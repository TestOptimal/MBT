package com.testoptimal.util;
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
		String host = Config.getProperty("javaMailHost", null);

		// Get system properties
		Properties props = Config.getPropertiesByPrefix("mail.");
		
		// Setup mail server
		if (!StringUtil.isEmpty(host)) props.put("mail.smtp.host", host);

//		if (Util.isEmpty(props.getProperty("mail.smtp.host"))) throw new Exception ("error: mail.host not defined.");


		// create authenticator if specified
	    Authenticator auth = null;
	    String mailAuthClass = Config.getProperty("mail.auth.class");
	    if (!StringUtil.isEmpty(mailAuthClass)) {
			Class theClass = Class.forName(mailAuthClass);
			auth = (Authenticator) theClass.newInstance();
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
