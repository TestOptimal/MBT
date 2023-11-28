package com.testoptimal.server.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.testoptimal.util.FileUtil;

public class RequirementConfig {
	private static Logger logger = LoggerFactory.getLogger(RequirementConfig.class);

	public Map<String, String> params;
	public Date updateDT = new java.util.Date();

	/**
	 * ALM.'source'.class=com.testoptimal.alm.jiraConnect
	 * ALM.'source'....
	 * 
	 * In model RequirementConfig, set "source" to this source to use this requirement config
	 */
	public String source;
	public List<Requirement> reqList = new java.util.ArrayList<>();

	/**
	 * reads requirement config from model dataset folder, if not found, return a new/empty object.
	 * @param reqFolderPath_p
	 * @return
	 * @throws Exception
	 */
	public static RequirementConfig getReqConfig(String reqFolderPath_p) {
		String filePath = reqFolderPath_p + "requirement.json";
		try {
			if (FileUtil.exists(filePath)) {
				StringBuffer buf = FileUtil.readFile(filePath);
				Gson gson = new Gson();
				return gson.fromJson(buf.toString(), RequirementConfig.class);
			}
		}
		catch (Exception e) {
			logger.error("Error encountered reading requirements from " + filePath);
		}
		return new RequirementConfig();
	}
	
	public void saveReqConfig (String reqFolderPath_p) throws Exception {
		String reqFilePath = reqFolderPath_p + "requirement.json";
		FileUtil.deleteOneFile(reqFilePath);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String outJson = gson.toJson(this);
		FileUtil.writeToFile(reqFilePath, new StringBuffer(outJson));
	}
}
