package com.testoptimal.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

import com.testoptimal.util.misc.SysLogger;



public class RuntimeErrorStreamGobbler extends Thread {
    InputStream is;
    Level logLevel;
    
    public RuntimeErrorStreamGobbler(InputStream is, Level level_p) {
        this.is = is;
        this.logLevel = level_p;
    }
    
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ( (line = br.readLine()) != null) {
                SysLogger.logDebug(line);
            }
        }
        catch (IOException ioe) {
            ioe.printStackTrace();  
        }
    }
}
