package com.testoptimal.server.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.testoptimal.exec.FSM.ModelMgr;
import com.testoptimal.server.config.Config;
import com.testoptimal.server.model.ClientReturn;
import com.testoptimal.server.model.FileInfo;
import com.testoptimal.server.model.FolderInfo;
import com.testoptimal.util.FileUtil;
import com.testoptimal.util.ModelFile;
import com.testoptimal.util.ZipUtil;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 
 * 
 * http://localhost:8090/swagger-ui.html
 * 
 * @author yxl01
 *
 */
@RestController
//@Api(tags="File")
@RequestMapping("/api/v1/file")
@JsonIgnoreProperties(ignoreUnknown = true)
@CrossOrigin
public class FileController {
	private static Logger logger = LoggerFactory.getLogger(FileController.class);
	private static Map<String,String> ContentTypeMap = new java.util.HashMap<>();
	static {
		ContentTypeMap.put("png", "image/png");
		ContentTypeMap.put("json", "application/json");
		ContentTypeMap.put("text", "text/plain");
		ContentTypeMap.put("xls", "application/vnd.ms-excel");
		ContentTypeMap.put("html", "text/html");
		ContentTypeMap.put("log", "text/plain");
		ContentTypeMap.put("gvy", "text/plain");
		ContentTypeMap.put("ds", "text/plain");
		ContentTypeMap.put("css", "text/css");
		ContentTypeMap.put("api", "application/json");
	}

	static String getContentType (String ext_p) {
		String type = ContentTypeMap.get(ext_p);
		if (type==null) {
			type = ContentTypeMap.get("text");
		}
		return type;
	}
	
//	@ApiOperation(value = "Folder Info", notes="Folder info")
	@RequestMapping(value = "folder/{folderPath}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<FolderInfo> getFolder(@PathVariable (name="folderPath", required=false) String folderPath) throws Exception {
		if (folderPath==null) {
			folderPath = "";
		}
		else {
			folderPath = folderPath.replace(":", "/");
			folderPath = folderPath.startsWith("/")?folderPath.substring(1):folderPath;
			folderPath = folderPath.trim();
			folderPath = folderPath.equals("/")?"":folderPath;
		}
		FileUtil.sanctionFilePath (folderPath);
		logger.info("FileController.getFolder: " + folderPath);
		FolderInfo f = new FolderInfo(folderPath);
		return new ResponseEntity<>(f, HttpStatus.OK);
	}

//	@ApiOperation(value = "Model Folder Info", notes="Model top folder")
	@RequestMapping(value = "model/{modelName}", method = RequestMethod.GET)
	public ResponseEntity<FolderInfo> modelFileList (@PathVariable ("modelName") String modelName,
			ServletRequest request) throws Exception {
    	String modelPath = FileUtil.findModelPath(modelName);
    	if (modelPath==null) {
    		throw new Exception ("Model.not.found");
    	}
		FolderInfo folderInfo = new FolderInfo (modelPath + modelName);
//		folderInfo.relativePath = modelPath;
		return new ResponseEntity<>(folderInfo, HttpStatus.OK);
	}


//	@ApiOperation(value = "All Model List", notes="All models in nested folders")
	@RequestMapping(value = "model", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<FileInfo>> getAllModels(ServletRequest request) throws Exception {
		File folderReader = new File(Config.getModelRoot(), "");
		List<ModelFile> subList =  ModelFile.getAllModelList(folderReader);
		Principal user = ((HttpServletRequest)request).getUserPrincipal();
		subList.sort((ModelFile f1, ModelFile f2) -> f1.modelName.compareTo(f2.modelName));	
		List<FileInfo> modelList = subList.stream().map(m -> {
				try {
					return new FileInfo(new File(m.absoluteFilePath));
				}
				catch (Exception e) {
					// just skip this folder
					return null;
				}
			})
			.collect(Collectors.toList());
		return new ResponseEntity<>(modelList, HttpStatus.OK);
	}
	
//	@ApiOperation(value = "SubModel List", notes="SubModel list")
	@RequestMapping(value = "submodel", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<String>> getSubModelList(ServletRequest request) throws Exception {
		File folderReader = new File(Config.getModelRoot(), "");
		List<String> subModelList = FileUtil.getModelList(folderReader, true)
			.stream()
			.map( f -> f.endsWith(".fsm")?f.substring(0, f.length()-4):f)
			.collect(Collectors.toList());
		return new ResponseEntity<>(subModelList, HttpStatus.OK);
	}

	/**
	 * moves folder from its current folder to the ide current folder
	 * @param reqInfo_p
	 * @throws Exception
	 */
//	@ApiOperation(value = "New Folder", notes="Create a new folder")
	@RequestMapping(value = "folder/{folderPath}/{fileName}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<FileInfo> newFolder (@PathVariable(name="folderPath", required=true) String folderPath,
			@PathVariable (name="fileName", required=true) String fileName) throws Exception {
		folderPath = folderPath.replace(":", "/");
		String filePath = FileUtil.concatFilePath(Config.getModelRoot(), folderPath, fileName); 
		logger.info("FileController.newFolder: " + filePath);
		FileUtil.sanctionFilePath (filePath);
		if (!FileUtil.createFolder(filePath)) {
			throw new Exception ("Unable to create new folder " + filePath);
		}
		FileInfo f = new FileInfo (new File (filePath));
		return new ResponseEntity<>(f , HttpStatus.OK);
	}
	
//	@ApiOperation(value = "New Model", notes="Create a new model")
	@RequestMapping(value = "model/{modelPath}/{modelName}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<FileInfo> newModel (@PathVariable(name="modelPath", required=true) String modelPath,
		@PathVariable(name="modelName", required=true) String modelName) throws Exception {
		modelPath = modelPath.replace(":", "/");
		if (!modelName.endsWith(".fsm")) {
			modelName += ".fsm";
		}
		String newModelFolder = FileUtil.findModelFolder(modelName);
		if (newModelFolder!=null) {
			throw new Exception ("Model already exists");
		}
		String modelRoot = Config.getModelRoot();
		modelPath = FileUtil.concatFilePath(modelRoot, modelPath, modelName);
		FileUtil.sanctionFilePath(modelPath);
		if (FileUtil.exists(modelPath)) {
			throw new Exception ("Model already exists");
		}
		
		String tplPath = FileUtil.concatFilePath(modelRoot, ".tpl", "fsm.tpl");
		int cnt = FileUtil.copyFolder(tplPath, modelPath, false);
		if (cnt <= 0) throw new Exception ("Model create failed");
		FileInfo f = new FileInfo(new File(modelPath));
		return new ResponseEntity<>(f , HttpStatus.OK);
	}

	/**
	 * Deletes a model (ds or fsm) or a folder.  Can not delete a file or folder within a model,
	 * use modelFileDelete() instead.
	 * 
	 * @param reqInfo_p
	 * @throws Exception
	 */
//	@ApiOperation(value = "Delete Model/Folder", notes="Delete a model (fsm or ds) or a folder")
	@RequestMapping(value = "file/{folderPath}/{fileName}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ClientReturn> deleteFolder (@PathVariable (name="folderPath", required=true) String folderPath,
			@PathVariable (name="fileName", required=true) String fileName) throws Exception {
		folderPath = folderPath.replace(":", "/");
		String fPath = FileUtil.concatFilePath(Config.getModelRoot(), folderPath, fileName);
		FileUtil.sanctionFilePath (fPath);
		logger.info("FileController.deleteFolder: " + fPath);
		if (!FileUtil.deleteOneFile(fPath)) {
			throw new Exception ("Failed to delete " + fileName);
		}
//		if (fileName.endsWith(".fsm")) {
//			ModelCacheMgr.removeModel(fileName.substring(0, fileName.lastIndexOf(".")));
//		}
		return new ResponseEntity<>(ClientReturn.OK() , HttpStatus.OK);
	}
	
//	@ApiOperation(value = "Server Log", notes="Server Log")
	@RequestMapping(value = "log/{logName}", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
	public String getServerLog (@PathVariable (name="logName", required=true) String logName,
		@RequestParam (name="modelName", required=false) String modelName) throws Exception {
		String logFilePath = "log" + File.separator + logName + ".log";
		FileUtil.sanctionFilePath (logFilePath);
		StringBuffer ret = FileUtil.readFile(logFilePath);
		if (Strings.isBlank(modelName)) {
			return logFilePath + "\n\n" + ret;
		}
		else {
			int lastIdx = ret.lastIndexOf("Starting model " + modelName + "\n");
			if (lastIdx>0) {
				return logFilePath + "\n\n" + ret.substring(lastIdx, ret.length());
			}
			else {
				return logFilePath + "\n\n" + ret;
			}
		}
    }
	
	
	// used by IDE_Main/Tab_ModelFiles.html
//	@ApiOperation(value = "Model File", notes="Model file")
	@RequestMapping(value = "file/{folderPath}/{fileName}", method = RequestMethod.GET)
	public ResponseEntity<UrlResource> modelFileOpen (
			@PathVariable ("folderPath") String folderPath,
			@PathVariable ("fileName") String fileName) throws Exception {
		folderPath = folderPath.replace(":", "/");
		String fPath = FileUtil.concatFilePath(Config.getModelRoot(), folderPath, fileName);
		return getFile(fPath, fileName);
	}
	
	public static ResponseEntity<UrlResource> getFile (String fPath, String fileName) throws Exception {
		FileUtil.sanctionFilePath (fPath);
		UrlResource resource = new UrlResource((new File(fPath)).toURI());
		if (resource.exists() || resource.isReadable()) {
			if (fPath.endsWith(".xls")) {
				return ResponseEntity.ok()
						.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
						.body(resource);
			}
			else {
				String fileExt = fileName;
				int idx = fPath.lastIndexOf(".");
				if (idx>=0) {
					fileExt = fPath.substring(idx+1);
				}
				String type = getContentType(fileExt);
				return ResponseEntity.ok()
						.header(HttpHeaders.CONTENT_TYPE, type)
						.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
						.body(resource);
			}
		}
		else throw new Exception ("Unable to read file " + fPath);
    }

	// used by StatsRpt/StatsDetails.html
//	@ApiOperation(value = "Model Articfact", notes="Model articfact/file")
	@RequestMapping(value = "artifact/{modelName}/{folderPath}/{fileName}", method = RequestMethod.GET)
	public ResponseEntity<UrlResource> openModelArtifact (
			@PathVariable ("modelName") String modelName,
			@PathVariable ("folderPath") String folderPath,
			@PathVariable ("fileName") String fileName) throws Exception {
		folderPath = folderPath.replace(":", "/");
		ModelMgr modelMgr = new ModelMgr(modelName);
				
		String fPath = FileUtil.concatFilePath(modelMgr.getFolderPath(), folderPath, fileName);
		return getFile(fPath, fileName);
    }
	
//	@ApiOperation(value = "Model Download", notes="Model download")
	@RequestMapping(value = "model/{modelName}/download", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<UrlResource> modelDownload (@PathVariable (name="modelName", required=true) String modelName,
			ServletRequest request) throws Exception {
		ModelMgr modelMgr = new ModelMgr(modelName);
		String modelFolder = modelMgr.getFolderPath();
		String filterReqExp = Config.getModelFileFilter();
		String archFilePath = Config.getTempPath() + modelName + "_" + System.currentTimeMillis() + ".zip";
		FileUtil.sanctionFilePath (archFilePath);
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(archFilePath)); 
		ZipUtil.zipDir(modelFolder, zos, modelFolder, filterReqExp);
		zos.close();
    	try {
    		UrlResource resource = new UrlResource((new File(archFilePath)).toURI());
    		if (resource.exists() || resource.isReadable()) {
			    return ResponseEntity.ok()
		            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
		            .body(resource);
    		} 
    		else {
    			 throw new RuntimeException("Could not read file: " + archFilePath);
    		}
		 } 
    	 catch (MalformedURLException e) {
			 throw new RuntimeException("Could not read file: " + archFilePath, e);
		 }    	
    }

//	@ApiOperation(value = "Upload model zip", notes="upload model")
	@RequestMapping(value = "upload/{modelName}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ClientReturn> uploadModel (ServletRequest request,
		@PathVariable (name="modelName", required=true) String modelName,
		@RequestPart (name="comment", required=false) String comment,
		@RequestPart (name="modelPath", required=false) String modelPath,
		@RequestPart (name="file", required=true) MultipartFile zipFile) throws Exception {
		logger.info("upload model " + modelName + ", path=" + (modelPath==null?"":modelPath) + ", comment: " + (comment==null?"":comment));
		if (modelName.endsWith(".fsm")) {
			modelName = modelName.substring(0, modelName.length() - 4);
		}
		String modelFolderPath = FileUtil.findModelFolder(modelName);
		if (modelFolderPath != null) {
			return new ResponseEntity<>(ClientReturn.Alert("Model already exists at: " + modelFolderPath), HttpStatus.OK);
		}
		modelFolderPath = FileUtil.concatFilePath(Config.getModelRoot(), modelPath, modelName + ".fsm");
		String fileName = StringUtils.cleanPath(zipFile.getOriginalFilename());
		String archFilePath = Config.getTempPath() + fileName;
		Path path = Paths.get(archFilePath);
		zipFile.transferTo(path);
		File archFile = new File (archFilePath);
		ZipUtil.unzipDir(archFile, new File (modelFolderPath), "");

		FileUtil.deleteOneFile(archFilePath);
		File modelFolderObj = new File (modelFolderPath);
		modelFolderObj.setLastModified(System.currentTimeMillis());
		
		ModelMgr modelMgr = new ModelMgr(modelName); // reload
		modelMgr.getScxmlNode().setBuildNum(0);
		modelMgr.saveScxml();

		logger.info("upload done for model " + modelName);
		return new ResponseEntity<>(ClientReturn.OK(), HttpStatus.OK);
	}
	
//	@ApiOperation(value = "Rename Folder", notes="Rename Folder")
	@RequestMapping(value = "folder/{folderName}/rename/{newFolderName}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ClientReturn> uploadModel (
		@PathVariable (name="folderName", required=true) String folderName,
		@PathVariable (name="newFolderName", required=true) String newFolderName,
		@RequestBody String folderPath,
		ServletRequest request) throws Exception {
		if (folderPath==null) {
			folderPath = "";
		}
		else {
			folderPath = folderPath.startsWith("/")?folderPath.substring(1):folderPath;
			folderPath = folderPath.replace("\"", "").trim();
			folderPath = folderPath.equals("/")?"":folderPath;
		}
		FileUtil.sanctionFilePath (folderPath);
		String modelRootFolder = Config.getModelRoot();
		boolean ret = FileUtil.renameFile(FileUtil.concatFilePath(modelRootFolder, folderPath, folderName), FileUtil.concatFilePath(modelRootFolder, folderPath, newFolderName));
		if (ret) {
			logger.info("Renamed folder from " + folderName + " to " + newFolderName);
			return new ResponseEntity<>(ClientReturn.OK(), HttpStatus.OK);
		}
		else {
			logger.info("Failed to rename folder from " + folderName + " to " + newFolderName);
			return new ResponseEntity<>(ClientReturn.Alert("Rename failed, check folder name"), HttpStatus.OK);
		}
	}
	

//	@ApiOperation(value = "Move File/Folder", notes="Move File/Folder")
	@RequestMapping(value = "move", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ClientReturn> uploadModel (
		@RequestBody Map<String,String> moveOpts,
		ServletRequest request) throws Exception {

		String modelRootFolder = Config.getModelRoot();
		
		String fileName = moveOpts.get("fileName");
		String srcFolderPath = moveOpts.get("srcFolder");
		String srcFilePath = FileUtil.concatFilePath(modelRootFolder, srcFolderPath, fileName);
		String targetFilePath = FileUtil.concatFilePath(modelRootFolder, moveOpts.get("targetFolder"), fileName);
		
		FileUtil.sanctionFilePath (srcFilePath);
		FileUtil.sanctionFilePath (targetFilePath);
		boolean success = false;
		String errMsg = null;
		File f = new File (srcFilePath);
		if (f.isDirectory()) {
			success = FileUtil.moveFolder(srcFilePath, targetFilePath);
		}
		else {
			success = FileUtil.copyFile(srcFilePath, targetFilePath);
			if (!FileUtil.deleteOneFile(srcFilePath)) {
				success = false;
				errMsg = "unable to clean up source file/folder";
			}
		}
		if (success) {
			logger.info("Moved file/folder from " + srcFilePath + " to " + targetFilePath);
			return new ResponseEntity<>(ClientReturn.OK(), HttpStatus.OK);
		}
		else {
			logger.info("Failed to move file/folder from " + srcFilePath + " to " + targetFilePath);
			if (errMsg!=null) {
				logger.warn(errMsg);
			}
			return new ResponseEntity<>(ClientReturn.Alert("Move failed, check SvrLog file for details"), HttpStatus.OK);
		}
	}
//	
////	@ApiOperation(value = "IDE Custom CSS", notes="IDE CSS")
//	@RequestMapping(value = "ideCss", method = RequestMethod.GET, produces = "text/css" )
//	public ResponseEntity<UrlResource> genModelGraph (ServletRequest request) throws Exception {
//		try {
//			String fPath = FileUtil.concatFilePath(Config.getConfigPath(), "ide.css");
//	    	return this.getFile(fPath, "ide.css");
// 		}
//		catch (Exception e) {
//			return null;
//		}
//	}

}
