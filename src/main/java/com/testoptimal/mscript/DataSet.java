package com.testoptimal.mscript;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.testoptimal.mscript.groovy.GroovyScript;
import com.testoptimal.util.FileUtil;

public class DataSet {

	public String dsName;
	public String filePath;
	public List<String> colList;
	public List<String []> rows = new ArrayList();
	public int idx = 0;

	public static List<DataSet> readModelDataSets (String datasetFolderPath_p) throws Exception {
		File [] datasetFileList = FileUtil.getFileList(datasetFolderPath_p);
		List<DataSet> retList = new ArrayList<>();
		for (File f: datasetFileList) {
			try {
				retList.add(new DataSet(f.getAbsolutePath()));
			}
			catch (Exception e) {
				throw new Exception("Failed to load dataset " + f.getName(), e);
			}
		}
		return retList;
	}
	
	public DataSet (String filePath_p) throws Exception {
		File f = new File (filePath_p);
		this.dsName = f.getName();
		if (this.dsName.endsWith(".tsv")) {
			this.dsName = this.dsName.substring(0, this.dsName.length()-4);
		}
		this.dsName = this.dsName.replace(".", "_");

		this.filePath = filePath_p;
		if (this.filePath.endsWith(".tsv")) {
			List<String> rowList = FileUtil.readFileIntoArray(filePath_p, true);
			if (rowList.isEmpty()) throw new Exception ("Dataset file is empty: " + this.filePath);
			this.colList = Arrays.asList(rowList.get(0).split("\t"));
			rowList.stream().skip(1).forEach(r -> this.rows.add(r.split("\t")));
		}
		else if (this.filePath.endsWith(".ds")) {
//			Gson gs = new Gson();
//			ds = (CombDataSet) gs.fromJson(dsString, CombDataSet.class);
			throw new Exception (".ds dataset not supported: " + this.filePath);
		}
	}
	
	public int next() { 
		this.idx = ++this.idx >= rows.size()? 0: this.idx;
		return this.idx;
	}
	public int prev() {
		this.idx = --this.idx < 0? rows.size()-1: this.idx;
		return this.idx;		
	}
	public String get(String colName_p) {
		int colIdx = this.colList.indexOf(colName_p);
		if (this.idx >= this.rows.size() || colIdx < 0) return null;
		String ret = this.rows.get(this.idx)[colIdx];
		return ret;
	}
}
