package com.testoptimal.util;
import java.io.File;
import java.io.FilenameFilter;

public class MscriptLogFileFilter implements FilenameFilter {
	protected String pattern;
	public MscriptLogFileFilter (String str) {
		pattern = str;
	}
  
	public boolean accept (File dir, String name) {
		return name.toLowerCase().contains(pattern.toLowerCase()) &&
			(name.endsWith(".log") || name.endsWith(".log.lck"));
	}

}
