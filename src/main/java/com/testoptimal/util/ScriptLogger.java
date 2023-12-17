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
