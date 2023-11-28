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
