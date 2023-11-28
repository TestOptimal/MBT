package com.testoptimal.server.model;

import java.io.File;
import java.util.Date;

public class FileInfo {
	public static enum Type {model, dataset, folder, file, api }
	public String name;
	public String ext = "";
	public boolean isFolder;
	public Date lastModifiedDate;
	public Type type = Type.file;
	
	public FileInfo (File file_p) {
		this.name = file_p.getName();
		this.lastModifiedDate = new java.util.Date(file_p.lastModified());
		int idx = this.name.lastIndexOf(".");
		if (idx>=0) {
			this.ext = this.name.substring(idx);
			this.name = this.name.substring(0, idx);
		}
		this.isFolder = file_p.isDirectory();
		if (this.ext.equals(".fsm")) {
			this.type = Type.model;
		}
		else if (this.ext.equals(".ds")) {
			this.type = Type.dataset;
		}
		else if (this.ext.equals(".api")) {
			this.type = Type.api;
		}
		else if (this.isFolder) {
			this.type = Type.folder;
		}
	}
}
