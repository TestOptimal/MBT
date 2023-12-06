package com.testoptimal.exec.FSM;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.testoptimal.server.controller.DemoController;
import com.testoptimal.util.FileUtil;



public class DataSet {
	private static Logger logger = LoggerFactory.getLogger(DataSet.class);

	public String dsName;
	public String filePath;
	public List<String> colList;
	public List<String []> rows = new ArrayList<>();
	public int idx = 0;

	public static List<DataSet> readModelDataSets (String datasetFolderPath_p) throws Exception {
		File [] datasetFileList = FileUtil.getFileList(datasetFolderPath_p);
		List<DataSet> retList = new ArrayList<>();
		for (File f: datasetFileList) {
			try {
				retList.add(new DataSet(f.getAbsolutePath()));
			}
			catch (Exception e) {
				logger.warn("Failed to load dataset " + f.getName(), e);
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
		else if (this.dsName.endsWith(".ds")) {
			this.dsName = this.dsName.substring(0, this.dsName.length()-3);			
		}
		this.dsName = this.dsName.replace(".", "_");

		this.filePath = filePath_p;
		if (this.filePath.endsWith(".ds")) {
			logger.info("Converting old dataset file " + this.filePath);
			Gson gs = new Gson();
			DataSetDef ds = (DataSetDef) gs.fromJson(FileUtil.readFile(filePath_p).toString(), DataSetDef.class);
			if (ds.fieldList==null) {
				ds.fieldList = new ArrayList<Field>();
			}
			if (ds.dataRows==null) {
				ds.dataRows = new ArrayList<Map<String, Object>>();
			}
			this.colList = ds.fieldList.stream().map(n -> n.fieldName).collect(Collectors.toList());
			this.rows = ds.dataRows.stream().map(r -> 
				this.colList.stream().map(c -> r.get(c).toString())
				.collect(Collectors.toList()).toArray(new String[0]) // toArray(String[] ::new)
			)
			.collect(Collectors.toList());
			String filePath_old = this.filePath;
			this.filePath = this.filePath.substring(0, this.filePath.length()-3) + ".tsv";
			this.save();
			FileUtil.deleteOneFile(filePath_old);
		}

		if (this.filePath.endsWith(".tsv")) {
			List<String> rowList = FileUtil.readFileIntoArray(filePath_p, true);
			if (rowList.isEmpty()) throw new Exception ("Dataset file is empty: " + this.filePath);
			this.colList = Arrays.asList(rowList.get(0).split("\t"));
			rowList.stream().skip(1).forEach(r -> this.rows.add(r.split("\t")));
		}
	}

	public void save() throws Exception {
		StringBuffer buf = new StringBuffer();
		buf.append(String.join("\t", this.colList)).append("\n")
		   .append(this.rows.stream().map(r -> String.join("\t", r)).collect(Collectors.joining("\n")));
		FileUtil.writeToFile(this.filePath, buf.toString());
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

	public class DataSetDef {
		private String dsName;
		private List<Field> fieldList;
		private String overallStrength;
		private List<Map<String, Object>> dataRows;
		
		private DataSetDef () {		}
	}
	public class Field {
		public String fieldName;
		public String dataType;
		public boolean derived;
		public String groupCode;
		public String[] domainList;
	}
}
