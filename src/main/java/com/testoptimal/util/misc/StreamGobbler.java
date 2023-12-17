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

package com.testoptimal.util.misc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * consumes the process input and output stream and prints them to log file.
 * 
 * @author yxl01
 *
 */
public class StreamGobbler extends Thread {
	private static Logger logger = LoggerFactory.getLogger(StreamGobbler.class);

	InputStream is;
    boolean errStream = true;
    
    public StreamGobbler(InputStream is_p, boolean errStream_p) {
        this.is = is_p;
        this.errStream = errStream_p;
    }
    
    public void run() {
        try {
        	Thread.currentThread().setName("StreamGobbler-" + (this.errStream?"Err":"Out"));
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ( (line = br.readLine()) != null) {
            	if (this.errStream) {
            		logger.error(line);
            	}
            	else {
            		logger.debug(line);
            	}
            }
        }
        catch (IOException ioe) {
        	logger.error("StreamGobbler.run", ioe);
        }
    }
}
