package com.testoptimal.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.testoptimal.server.config.Config;

public class ScriptLogger {
	private static Logger logger = LoggerFactory.getLogger(ScriptLogger.class);
	
	private String logFilePath;
	private BufferedWriter outWriter;
	private Calendar cal = Calendar.getInstance();
	private int lastDay = 0;
	private boolean writeTimestamp = true;

	public String getLogFilePath () {
		return this.logFilePath;
	}
	
	public ScriptLogger (String logFilePath_p) throws IOException {
		this.logFilePath = logFilePath_p;
		File file = new File(this.logFilePath);
		if (file.exists()) {
			FileUtil.deleteOneFile(this.logFilePath);
		}
		file.createNewFile();
		FileWriter fw = new FileWriter(file);
		this.outWriter = new BufferedWriter(fw);
		this.writeTimestamp = StringUtil.isTrue(Config.getProperty("log.model.timestamp", "true"));
	}
	
	public void log (String msg_p) { 
        StringBuffer logMsg = new StringBuffer();
        if (this.writeTimestamp) {
    		this.cal.setTimeInMillis(System.currentTimeMillis());
    		int today = this.cal.get(Calendar.DATE);
            if (this.lastDay!=today) {
                logMsg.append(">>> Date: ").append(cal.get(Calendar.YEAR)).append("-").append(cal.get(Calendar.MONTH)+1).append("-")
    		      	  .append(cal.get(Calendar.DATE)).append(" ").append("\r\n"); 
                this.lastDay = today;
            }
            logMsg.append(cal.get(Calendar.HOUR_OF_DAY))
            	  .append(":").append(cal.get(Calendar.MINUTE))
            	  .append(":").append(cal.get(Calendar.SECOND)).append(".").append(cal.get(Calendar.MILLISECOND))
            	  .append(" "); 
        }
        logMsg.append(msg_p).append("\r\n"); 
        try {
        	this.outWriter.write(logMsg.toString());
            this.outWriter.flush();
        }
        catch (Exception e) {
        	logger.info(logMsg.toString());
        }
	} 
	
	public void setWriteTimestamp (boolean opt_p) {
		this.writeTimestamp = opt_p;
	}
	
	public void close () {
		try {
			this.outWriter.close();
		}
		catch (Exception e) {
			
		}
	}
}
