package com.testoptimal.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.testoptimal.exec.FSM.ModelMgr;
import com.testoptimal.server.config.Config;

public class ModelFile {
	private static final String modelLockFile = "model.lock";
	private static Logger logger = LoggerFactory.getLogger(ModelFile.class);

	public String modelName;
	public java.sql.Timestamp modifiedDate;
	public String absoluteFilePath;

	public static ModelFile toModelFile(ModelMgr modelMgr_p) throws Exception {
		String modelPath = modelMgr_p.getFolderPath();
		File modelFileObj = new File (modelPath);
		if (!modelFileObj.exists()) return null;
		return toModelFile(modelFileObj);
	}
	
	public static ModelFile toModelFile(File fileObj_p) {
		if (fileObj_p==null) return null;
		String fileNameTemp = fileObj_p.getName();
		if (fileNameTemp.startsWith("_DELETED_") || fileNameTemp.endsWith(".svn")) {
			return null;
		}
		
		if (!isModelFolder(fileObj_p)) return null;
		
		ModelFile ret = new ModelFile(fileObj_p);
		return ret;
	}
	
	private ModelFile(File fileObj_p) {
		this.modelName = fileObj_p.getName();
		this.absoluteFilePath = fileObj_p.getAbsolutePath();

		File mbtFile = new File (FileUtil.concatFilePath(this.absoluteFilePath, "model.scxml"));
		long modLong = Math.max(mbtFile.lastModified(), fileObj_p.lastModified());
		this.modifiedDate = new java.sql.Timestamp(modLong);
	}
		
	/**
	 * returns true if the folder path passed in is a model folder (folder
	 * contains the model.scxml file).
	 * 
	 * @param folderPath_p
	 *            absolute folder path to the model
	 * @return
	 */
	public static boolean isModelFolder(String folderPath_p) {
		if (StringUtil.isEmpty(folderPath_p))
			return false;
		return isModelFolder(new File(folderPath_p));
	}

	public static boolean isModelFolder(File folder_p) {
		return folder_p.getName().endsWith(".fsm");
//		if (folder_p == null || folder_p.isFile())
//			return false;
//		if (new File(folder_p, "model/model.json").exists())
//			return true;
//		else
//			return false;
	}
	
	
	
	/**
	 * returns a list of names of all models.
	 */
	public static List<ModelFile> getAllModelList(File srchFolder_p) {
		List<ModelFile> subList = new java.util.ArrayList<ModelFile>();
		String[] fileList = srchFolder_p.list();
		for (int i = 0; fileList != null && i < fileList.length; i++) {
			if (fileList[i].equals(".svn") || fileList[i].startsWith("_DELETED_"))
				continue;
			File tempFile = new File(srchFolder_p, fileList[i]);
			if (ModelFile.isModelFolder(tempFile)) {
				// if (hideDemoApp && fileList[i].startsWith("DEMO_")) continue;
				File fileReader = new File(srchFolder_p, fileList[i]);
				if (!fileReader.isDirectory()) {
					continue;
				}
				ModelFile modelFile = ModelFile.toModelFile(fileReader);
				if (modelFile==null) continue;
				subList.add(modelFile);
			} else {
				if (tempFile.isDirectory()) {
					subList.addAll(getAllModelList(tempFile));
				}
			}
		}
		return subList;
	}


	/**
	 * returns the licensed email which has placed a lock on the model.
	 * @param modelName_p
	 * @return licensed email address which has the lock on the model. 
	 */
	public static String getModelLock(String modelFolderPath_p) throws Exception {
		try {
			String modelLockFilePath = FileUtil.concatFilePath(modelFolderPath_p, modelLockFile);
			StringBuffer lockContent = FileUtil.readFile(modelLockFilePath);
			if (lockContent==null || Strings.isBlank(lockContent.toString())) return null;
			String lockString = lockContent.toString().replace("\n", "");
			return lockString;
		}
		catch (FileNotFoundException ne) {
			return null;
		}
		catch (Exception e) {
			throw new Exception ("Unable to determine model lock for model " + modelFolderPath_p + ": " + e.getMessage());
		}
	}
	
	
//	/**
//	 * place a lock on the model by creating model.lock file with the licensed email.
//	 * @param modelName_p
//	 * @return null if lock is acquired or email of the user who already has the lock.
//	 */
//	public static String lockModel(String modelFolderPath_p, boolean forceLock_p) throws Exception {
//		String lockUser = getModelLock(modelFolderPath_p);
//		if (StringUtil.isEmpty(lockUser) || forceLock_p) {
//			try {
//				String modelLockFilePath = FileUtil.concatFilePath(modelFolderPath_p, modelLockFile);
//				FileUtil.writeToFile(modelLockFilePath, "License.getLicEmail()");
//			}
//			catch (Exception e) {
//				throw new Exception ("Failed to lock model " + modelFolderPath_p + ": " + e.getMessage());
//			}
//			return null;
//		}
//		else {
//			return lockUser.equalsIgnoreCase("License.getLicEmail()")? null: lockUser;
//		}
//	}
	
	
	/**
	 * removes the model.lock if current user has the lock. Otherwise return
	 * the email of the user who has the lock.
	 * @param modelName_p
	 * @return
	 */
	public static String unlockModel(String modelFolderPath_p) throws Exception {
		try {
			String modelLockFilePath = FileUtil.concatFilePath(modelFolderPath_p, modelLockFile);
			File file = new File (modelLockFilePath);
			if (file.exists()) {
				if (!file.delete()) {
					Config.setAlert("Unable to delete model.lock for model " + modelFolderPath_p);
				}
			}
		}
		catch (Exception e) {
			throw new Exception ("Failed to delete model.lock for model " + modelFolderPath_p + ": " + e.getMessage());
		}
		return null;
	}
}
