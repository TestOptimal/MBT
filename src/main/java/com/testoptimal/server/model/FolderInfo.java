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

package com.testoptimal.server.model;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.testoptimal.server.config.Config;

public class FolderInfo {
	private static String IgnoreFileExts = ".git,.svn,.deleted,.project,.setting,.tpl,.DS_Store";
	
	public String fileName; // includes file extension
	public String relativePath; // from model root
	public List<FileInfo> fileList = new java.util.ArrayList<>();
	
    /**
     * Folder and its files. Nested folders are not loaded.
     * 
     * @param path_p relative path from model root
     */
    public FolderInfo (String path_p) throws Exception {
    	this.relativePath = path_p==null? "":path_p;
    	
    	String modelRoot = Config.getModelRoot();
    	File f = new File (modelRoot + this.relativePath);
    	this.fileName = f.getName();
    	this.fileList = new java.util.ArrayList<>();
   	    if (f.isDirectory()) {
   	    	this.fileList = Arrays.asList(f.listFiles()).stream()
   	    		.map(file -> {
	   	    		return new FileInfo(file);
	   	    	})
   	    		.filter(finfo -> 
   	    			finfo.ext.equals("") || !IgnoreFileExts.contains(finfo.ext)
   	    		)
   	    		.collect(Collectors.toList());
   	    }
    }

}
