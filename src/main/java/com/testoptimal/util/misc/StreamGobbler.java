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
class StreamGobbler extends Thread {
	private static Logger logger = LoggerFactory.getLogger(StreamGobbler.class);

    InputStream is;
    boolean errStream = true;
    
    StreamGobbler(InputStream is_p, boolean errStream_p) {
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
