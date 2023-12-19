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
