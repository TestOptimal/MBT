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

package com.testoptimal.exec.FSM;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.testoptimal.exec.mscript.groovy.GroovyScript;
import com.testoptimal.scxml.ScxmlNode;
import com.testoptimal.scxml.StateNode;
import com.testoptimal.server.config.Config;
import com.testoptimal.server.model.Requirement;
import com.testoptimal.server.model.RequirementConfig;
import com.testoptimal.util.FileUtil;
import com.testoptimal.util.ModelFile;
import com.testoptimal.util.StringUtil;

public class ModelMgr {
	private static Logger logger = LoggerFactory.getLogger(ModelMgr.class);
	
	private String modelName;
	private String folderPath;
	private String relativePath;

	private ScxmlNode scxmlNode;
	private transient Map<StateNode, ScxmlNode> subModelMapList;
	private transient List<String> exceptionList;
	

	public String getModelName() { 
		return this.modelName; 
	}
	
	// path to the current model
	public String getFolderPath() {
		return this.folderPath;
	}
	
	// 'model' folder path within current model
	public String getModelFolderPath() {
		return FileUtil.concatFilePath(this.folderPath, "model") + File.separator;
	}
	
	public String getTempFolderPath() {
		return FileUtil.concatFilePath(this.folderPath, "temp") + File.separator;
	}
	
	public String getReportFolderPath () {
		return FileUtil.concatFilePath(this.folderPath, "report") + File.separator;
	}

	public String getStatsFolderPath () {
		return FileUtil.concatFilePath(this.folderPath, "stats") + File.separator;
	}
	
	public String getDatasetFolderPath () {
		return FileUtil.concatFilePath(this.folderPath, "dataset") + File.separator;
	}
	
	public ScxmlNode getScxmlNode() { 
		return this.scxmlNode; 
	}

	/**
	 * normal model open
	 * @param modelName_p
	 * @throws Exception
	 */
	public ModelMgr(String modelName_p) throws Exception {
		this.modelName = modelName_p;
		this.relativePath = FileUtil.findModelPath(modelName_p);
		if (!StringUtil.isEmpty(this.relativePath)) {
			this.relativePath += "/";
		}
		this.relativePath += this.modelName + ".fsm";
		this.folderPath = FileUtil.concatFilePath(Config.getModelRoot(), this.relativePath);
		this.loadScxml();
	}

	public List<ScxmlNode> getSubModelList() {
		return this.subModelMapList.entrySet().stream()
				.map(e -> 
					e.getValue()
				)
				.collect(Collectors.toList());
	}
	
	public ScxmlNode getSubModelScxml (StateNode stateNode_p) {
		return this.subModelMapList.get(stateNode_p);
	}
	
	public Map<StateNode, ScxmlNode> getSubModelStateMap () {
		return this.subModelMapList;
	}
	
	public List<String> getExceptionList() {
		return this.exceptionList;
	}
	
	public void addException (String msg_p) {
		if (msg_p!=null && !msg_p.contentEquals("")) this.exceptionList.add(msg_p);
	}
	
	public void resetException() {
		this.exceptionList.clear();
	}
	
	/**
	 * returns the model relative path from model root folder.
	 * @return
	 * @throws Exception
	 */
	public String getRelativePath() {
		return this.relativePath;
	}
	
//	/**
//	 * returns true if the user has lock on this model.
//	 * @return
//	 */
//	public boolean hasLock() {
//		try {
//			String lockEmail = ModelFile.getModelLock(this.folderPath);
//			return lockEmail!=null && lockEmail.equalsIgnoreCase();
//		}
//		catch (Exception e) {
//			
//			return false;
//		}
//	}
//
//	/**
//	 * returns true if model is locked by another user
//	 * @return
//	 */
//	public boolean isLocked() {
//		try {
//			String lockEmail = ModelFile.getModelLock(this.folderPath);
//			return lockEmail!=null && !lockEmail.equalsIgnoreCase("License.getLicEmail()");
//		}
//		catch (Exception e) {
//			logger.error(e.getMessage());
//			return false;
//		}
//	}
//
//	public String getLockUser() throws Exception {
//		return ModelFile.getModelLock(this.folderPath);
//	}
//
//	/**
//	 * lock model if it's not currently locked
//	 * @return
//	 */
//	public String lockModel() throws Exception {
//		return ModelFile.lockModel(this.folderPath, false);
//	}
//	
//	/**
//	 * force lock on the model
//	 */
//	public void forceLock() throws Exception {
//		ModelFile.lockModel(this.folderPath, true);
//	}
//	
//	/**
//	 * unlock this model if the user has the lock on this model.
//	 * return true if unlock is performed. Otherwise return false
//	 * @return
//	 */
//	public boolean unlock() throws Exception {
//		String lockEmail = ModelFile.unlockModel(this.folderPath);
//		return lockEmail==null;
//	}

	
	// recuvsively load model and its submodels
	private ScxmlNode readModel (String modelName_p) throws Exception {
		if (modelName_p.endsWith(".fsm")) {
			modelName_p = modelName_p.substring(0, modelName_p.lastIndexOf("."));
		}
		Gson gson = new Gson();
		String modelRelativePath = FileUtil.findModelPath(modelName_p);
		if (modelRelativePath==null) {
			throw new Exception ("Model not found: " + modelName_p);
		}
		String modelFilePath = FileUtil.concatFilePath(Config.getModelRoot(), modelRelativePath, modelName_p + ".fsm", "model", "model.json");
		this.checkModelFolder(FileUtil.concatFilePath(Config.getModelRoot(), modelRelativePath, modelName_p) + ".fsm");
		
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(modelFilePath));
			ScxmlNode scxmlNode = gson.fromJson(br, ScxmlNode.class);
			scxmlNode.prepScxml();
			
			List<StateNode> aList = scxmlNode.getAllStates();
	        for (StateNode stateNode: aList) {
				if (stateNode.isSubModelState()) {
					String subModelName = stateNode.getSubModel();
					try {
						if (modelName_p.equals(subModelName)) {
							throw new Exception ("recursive submodels");
						}
						ScxmlNode subModelScxml = this.readModel(subModelName);
						subModelScxml.setReadOnly(true);
						stateNode.getChildrenStates().addAll(subModelScxml.getChildrenStates());
						this.subModelMapList.put(stateNode, subModelScxml);
					}
					catch (Exception e) {
						logger.warn(e.getMessage());
						this.exceptionList.add("Error loading subModel state " + stateNode.getStateID() + " from model " + subModelName + ": " + e.getMessage());
					}
				}
	        }
			return scxmlNode;
		}
		finally {
			if (br!=null) {
				br.close();
			}
		}
	}

	private void checkModelFolder (String modelFolderPath_p) {
		if (!modelFolderPath_p.endsWith("/") && !modelFolderPath_p.endsWith("\\")) {
			modelFolderPath_p += File.separator;
		}
		for (String name: new String[] {"model", "dataset", "report", "temp", "stats"}) {
			File file = new File (modelFolderPath_p + name);
			if (!file.exists()) {
				file.mkdir();
			}
		}
	}
	
	public void loadScxml () throws Exception {
		this.subModelMapList = new java.util.HashMap<>();
		this.exceptionList = new java.util.ArrayList<>();
		this.scxmlNode = this.readModel (this.modelName);
		if (!this.scxmlNode.getChildrenStates().isEmpty()) {
			this.exceptionList.addAll(this.scxmlNode.validateModel());
			this.scxmlNode.sortChildStates();
		}
		
		// make sure scxmlNode has the model name matches the model folder (modelMgr)
		if (!this.modelName.equals(this.scxmlNode.getModelName())) {
			this.scxmlNode.setModelName(this.modelName);
			this.saveScxml();
		}
	}
	
	public void saveScxml () throws Exception {
		this.scxmlNode.save(this.getModelFolderPath());
	}
	
	/**
	 * return list of mscripts for current model and all of its submodels (marked as readonly).
	 * @return
	 * @throws Exception
	 */
	public List<GroovyScript> getScriptList () throws Exception {
//		return GroovyScript.readModelScript(this.modelName, this.getModelFolderPath());
		List<GroovyScript> retList = GroovyScript.readModelScript(this.modelName, this.getModelFolderPath());
		for (ScxmlNode scxml: this.getSubModelList()) {
			String subModelName = scxml.getModelName();
			String subModelFolder = FileUtil.findModelFolder(subModelName);
			if (subModelFolder==null) throw new Exception ("Submodel not found: " + subModelName);
			retList.addAll(GroovyScript.readModelScript(subModelName, subModelFolder + "model" + File.separator));
		}
		return retList;
	}
	
	public List<DataSet> getDataSetList () throws Exception {
		List<DataSet> dsList = DataSet.readModelDataSets(this.getDatasetFolderPath());
		Map<String, DataSet> dsMap = new java.util.HashMap<>();
		dsList.stream().forEach(d -> dsMap.put(d.dsName, d));
		for (ScxmlNode scxml: this.getSubModelList()) {
			String subModelName = scxml.getModelName();
			String subModelFolder = FileUtil.findModelFolder(subModelName);
			if (subModelFolder==null) throw new Exception ("Submodel not found: " + subModelName);
			List<DataSet> subList = DataSet.readModelDataSets(subModelFolder + "dataset");
			List<DataSet> dupList = subList.stream().filter(d -> dsMap.containsKey(d.dsName)).collect(Collectors.toList());
			List<DataSet> newList = subList.stream().filter(d -> !dsMap.containsKey(d.dsName)).collect(Collectors.toList());
			dsList.addAll(newList);
			newList.stream().forEach(d -> {
				dsList.add(d);
				dsMap.put(d.dsName, d);
			});
			dupList.stream().forEach(d -> dsMap.get(d.dsName).rows.addAll(d.rows));
		}
		return dsList;
	}

	public List<GroovyScript> getScriptForExec (String mbtSessID_p) throws Exception {
		String scriptTempFolder = this.getTempFolderPath() + mbtSessID_p;
//		File f = new File (scriptTempFolder);
//		f.mkdir();
		List<GroovyScript> retList = this.getScriptList();
		for (GroovyScript gScript: retList) {
			String outFolder = FileUtil.concatFilePath(scriptTempFolder, gScript.getModelName());
			FileUtil.createFolder(outFolder); // create if doesn't exist
			gScript.saveScript(outFolder);
		}
		return retList;
	}
	
	public void saveScriptList (List<GroovyScript> scriptList_p) throws Exception {
		String outFolder = this.getModelFolderPath();
		FileUtil.deleteFilesByRegExpr(outFolder, "\\.gvy$");
		for (GroovyScript script: scriptList_p) {
			script.setModelName(modelName);
			script.saveScript(outFolder);
		}
	}
	
	public RequirementConfig getReqConfig () {
		return RequirementConfig.getReqConfig(this.getDatasetFolderPath());
	}
	
	/**
	 * return list of Requirement for current model and submodel's. Use this for model execution.
	 * @return
	 */
	public List<Requirement> getRequirementList () {
		List<Requirement> retList = new java.util.ArrayList<>();
		retList.addAll(this.getReqConfig().reqList);
		
		for (ScxmlNode scxml: this.getSubModelList()) {
			String subModelName = scxml.getModelName();
			RequirementConfig reqConfig = RequirementConfig.getReqConfig(FileUtil.findModelFolder(subModelName) + "dataset" + File.separator);
			retList.addAll(reqConfig.reqList);
		}
		
		// remove duplicates
		HashSet<String> seen=new HashSet<>();
		retList.removeIf(e->!seen.add(e.name));
		return retList;
	}
	
	public void saveReqConfig (RequirementConfig reqConfig_p) throws Exception {
		reqConfig_p.saveReqConfig(this.getDatasetFolderPath());
	}
	
}
