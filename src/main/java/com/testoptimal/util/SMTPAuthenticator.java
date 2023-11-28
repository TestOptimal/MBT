package com.testoptimal.util;

import javax.mail.PasswordAuthentication;

import com.testoptimal.server.config.Config;

public class SMTPAuthenticator extends javax.mail.Authenticator {
    public PasswordAuthentication getPasswordAuthentication() {
       String username = Config.getProperty("mail.username");
       String password = Config.getProperty("mail.password");
       return new PasswordAuthentication(username, password);
    }
}