


MainModule.factory ('APITestSvc', function(SvrRest) {
	var APITestSvc = { 
		base: "/api/v1/apitest/"
	};

	APITestSvc.newAPITest = function (api, successCB, errorCB) {
		var url = APITestSvc.base + "apiset/" + encodeURIComponent(api.apiName);
		SvrRest.post(url, api, successCB, errorCB);
	};

	APITestSvc.saveAPITest = function (apiName, api, successCB, errorCB) {
		var url = APITestSvc.base + "apiset/" + encodeURIComponent(apiName);
		SvrRest.put(url, api, successCB, errorCB);
	};
	
	APITestSvc.renameAPITest = function (path, apiName, newAPIName, successCB, errorCB) {
		var url = APITestSvc.base + "apiset/" + apiName + "/rename?path=" + encodeURIComponent(path) + "&newApiName=" + newAPIName;
		SvrRest.get(url, successCB, errorCB);
	};
	
	APITestSvc.cloneAPITest = function (path, apiName, successCB, errorCB) {
		var url = APITestSvc.base + "apiset/" + encodeURIComponent(apiName) + "/clone?path=" + encodeURIComponent(path);
		SvrRest.get(url, successCB, errorCB);
	};

	APITestSvc.delAPITest = function (path, apiName, successCB, errorCB) {
		var url = APITestSvc.base + "apiset/" + encodeURIComponent(apiName) + "?path=" + encodeURIComponent(path);
		SvrRest.delete(url, successCB, errorCB);
	};
	
	APITestSvc.getAPITest = function (path, apiName, successCB, errorCB) {
		var url = APITestSvc.base + "apiset/" + encodeURIComponent(apiName) + "?path=" + encodeURIComponent(path);
		SvrRest.get(url, successCB, errorCB);
	};
	
	APITestSvc.parseSwagger = function (swaggerDoc, successCB, errorCB) {
		var url = APITestSvc.base + "swagger/parse";
		SvrRest.post(url, swaggerDoc, successCB, errorCB);
	};
	
	APITestSvc.getURL = function (url, successCB, errorCB) {
		SvrRest.get(url, successCB, errorCB);
	};
	
	return APITestSvc;
});


MainModule.factory ('AlmSvc', function(SvrRest) {
	var AlmSvc = { 
		base: "/api/v1/alm/"
	};
	
	AlmSvc.getRequirements = function (modelName, successCB, errorCB) {
		SvrRest.get (AlmSvc.base + modelName + "/requirement", successCB, errorCB);
	};

	AlmSvc.saveRequirements = function (modelName, reqConfig, successCB, errorCB) {
		SvrRest.put (AlmSvc.base + modelName + "/requirement", reqConfig, successCB, errorCB);
	};

	
	return AlmSvc;
});


MainModule.factory ('FileSvc', function(SvrRest) {
	var FileSvc = { 
		base: "/api/v1/file/",
	};

	FileSvc.getFolder = function (path_p, successCB, errorCB) {
		if (path_p.substring(0,1)=='/') path_p = path_p.substring(1);
		var url = FileSvc.base + "folder/" + encodeURIComponent(path_p.replace(/\//g, ':'));
		SvrRest.get(url, successCB, errorCB);
	};

	FileSvc.getModelFolder = function (modelName_p, successCB, errorCB) {
		var url = FileSvc.base + "model/" + encodeURIComponent(modelName_p);
		SvrRest.get(url, successCB, errorCB);
	};

	FileSvc.getFileURL = function (path_p, fileName_p, successCB, errorCB) {
		var url = FileSvc.base + "file/" + encodeURIComponent(path_p.replace(/\//g, ':')) + "/" + encodeURIComponent(fileName_p);
		return url;
	};
	FileSvc.getFile = function (path_p, fileName_p, successCB, errorCB) {
		var url = FileSvc.base + "file/" + encodeURIComponent(path_p.replace(/\//g, ':')) + "/" + encodeURIComponent(fileName_p);
		SvrRest.get(url, successCB, errorCB);
	};

	FileSvc.move = function (fileName, fromFolder, toFolder, successCB, errorCB) {
		var url = FileSvc.base + "move";
		SvrRest.post(url, {fileName: fileName, srcFolder: fromFolder, targetFolder: toFolder}, successCB, errorCB);
	};

	FileSvc.listAllModels = function (successCB, errorCB) {
		var url = FileSvc.base + "model/all";
		SvrRest.get(url, successCB, errorCB);
	};

	FileSvc.newFolder = function (curFolder, newFolder, successCB, errorCB) {
		if (curFolder.substring(0,1)=='/') curFolder = curFolder.substring(1);
		if (curFolder=='') curFolder = ' ';
		var url = FileSvc.base + "folder/" + encodeURIComponent(curFolder.replace(/\//g, ':')) + "/" + encodeURIComponent(newFolder);
		SvrRest.post (url, "", successCB, errorCB);
	};
	
	FileSvc.newModel = function (type, curFolder, modelName, successCB, errorCB) {
		if (curFolder.substring(0,1)=='/') curFolder = curFolder.substring(1);
		if (curFolder=='') curFolder = ' ';
		var url = FileSvc.base + type + "/" + encodeURIComponent(curFolder.replace(/\//g, ':')) + "/" + encodeURIComponent(modelName);
		SvrRest.post (url, "", successCB, errorCB);
	};
	
	
	FileSvc.renameFile = function (curFolder, fromFileName, toFileName, successCB, errorCB) {
		if (curFolder.substring(0,1)=='/') curFolder = curFolder.substring(1);
		var url = FileSvc.base + "folder/" + encodeURIComponent(curFolder.replace(/\//g, ':')) + "/" + encodeURIComponent(fromFilename) + "/rename/" + encodeURIComponent(toFileName);
		SvrRest.get (url, successCB, errorCB);
	};
	
	FileSvc.deleteFile = function (folderPath, fileName, successCB, errorCB) {
		if (folderPath=='') folderPath = ' ';
		var url = FileSvc.base + "file/" + encodeURIComponent(folderPath.replace(/\//g, ':')) + "/" + encodeURIComponent(fileName);
		SvrRest.delete (url, successCB, errorCB);
	};

	FileSvc.getSubmodelList = function (successCB, errorCB) {
		var url = FileSvc.base + "submodel";
		SvrRest.get (url, successCB, errorCB);
	};
	
	FileSvc.getSvrLogUrl = function () {
		return FileSvc.base + "log/tosvr";
	};

	FileSvc.getModelDownloadUrl = function (modelName) {
		return FileSvc.base + "model/" + modelName + "/download";
	};
	
	FileSvc.uploadModel = function (modelName, folderPath, fileContent, successCB, errorCB) {
		SvrRest.postFile (FileSvc.base + "upload/" + modelName, fileContent, {comment: "uploadModel", modelPath: folderPath}, successCB, errorCB);
	};
	
	FileSvc.renameFolder = function (folderPath, folderName, newFolderName, successCB, errorCB) {
		SvrRest.post (FileSvc.base + "folder/" + folderName + "/rename/" + newFolderName, folderPath, successCB, errorCB);
	};
	
	FileSvc.getSvrLog = function (successCB, errorCB) {
		SvrRest.get(FileSvc.base + "log/tosvr", successCB, errorCB);
	};
	
	
	return FileSvc;
})


MainModule.factory ('ModelSvc', function(SvrRest) {
	var ModelSvc = { 
		base: "/api/v1/model/"
	};
	
	ModelSvc.getModel = function (modelName, successCB, errorCB) {
		SvrRest.get (ModelSvc.base + modelName + "/getModel", function(data) {
				data.model = JSON.parse(data.modelJson);
				if (successCB) successCB(data);
			}, function(data) {
				if (errorCB) errorCB(data);
			});
	};

	ModelSvc.closeModel = function (modelName,successCB, errorCB) {
		SvrRest.get (ModelSvc.base + modelName + "/close", successCB, errorCB);
	};

	ModelSvc.saveModel = function (modelName, scxml, successCB, errorCB) {
		SvrRest.post (ModelSvc.base + modelName + "/saveModel", scxml, successCB, errorCB);
	};

	ModelSvc.getScript = function (modelName, successCB, errorCB) {
		SvrRest.get (ModelSvc.base + modelName + "/getScript", successCB, errorCB);
	};

	ModelSvc.saveScript = function (modelName, scriptList, successCB, errorCB) {
		SvrRest.post (ModelSvc.base + modelName + "/saveScript", scriptList, successCB, errorCB);
	};
	
	return ModelSvc;
});


MainModule.factory ('RuntimeSvc', function(SvrRest) {
	var RuntimeSvc = { 
		base: "/api/v1/runtime/"
	};

	RuntimeSvc.listModels = function (successCB, errorCB) {
		SvrRest.get (RuntimeSvc.base + "model/list", successCB, errorCB);
	};

	RuntimeSvc.sessions = function (successCB, errorCB) {
		SvrRest.get (RuntimeSvc.base + "session/list", successCB, errorCB);
	};

	RuntimeSvc.runModel = function (runReq, successCB, errorCB) {
		SvrRest.post (RuntimeSvc.base + "model/run/async", runReq, successCB, errorCB);
	};

	RuntimeSvc.closeModel = function (modelName, successCB, errorCB) {
		SvrRest.get (RuntimeSvc.base + "model/"+ modelName + "/close", successCB, errorCB);
	};

	RuntimeSvc.modelMonitor = function (modelName, successCB, errorCB) {
		SvrRest.get (RuntimeSvc.base + "model/" + modelName + "/monitor", successCB, errorCB);
	};

	RuntimeSvc.getModelLog = function (modelName, successCB, errorCB) {
		SvrRest.get (RuntimeSvc.base + "model/" + modelName + "/log", successCB, errorCB);
	}

	return RuntimeSvc;
});

MainModule.factory ('StatsSvc', function(SvrRest) {
	var StatsSvc = { 
		base: "/api/v1/stats/"
	};

	StatsSvc.modelExecList = function (modelName, successCB, errorCB) {
		SvrRest.get (StatsSvc.base + "exec/" + modelName + "/list", successCB, errorCB);
	};

	StatsSvc.getModelExec = function (modelName, mbtSessID, successCB, errorCB) {
		SvrRest.get (StatsSvc.base + "exec/" + modelName + "/" + mbtSessID, successCB, errorCB);
	};
	
	StatsSvc.setModelExecKeep = function (modelName, mbtSessID, keepFlag, successCB, errorCB) {
		SvrRest.get (StatsSvc.base + "exec/" + modelName + "/" + mbtSessID + "/setKeep/" + keepFlag, successCB, errorCB);
	};

	StatsSvc.delModelExec = function (modelName, mbtSessID, successCB, errorCB) {
		SvrRest.delete (StatsSvc.base + "exec/" + modelName + "/" + mbtSessID, successCB, errorCB);
	};
	
	StatsSvc.modelFailedTC = function (modelName, mbtSessID, successCB, errorCB) {
		SvrRest.get (StatsSvc.base + "model/" + modelName + "/failedTC?mbtSessID=" + mbtSessID, successCB, errorCB);
	};
	
	StatsSvc.dashboard = function (successCB, errorCB) {
		SvrRest.get (StatsSvc.base + "dashboard", successCB, errorCB);
	};
	
	return StatsSvc;
});

MainModule.factory ('SysSvc', function(SvrRest) {
	var SysSvc = { 
		base: "/api/v1/sys/"
	};
	
	SysSvc.getSysInfo = function (successCB, errorCB) {
		SvrRest.get (SysSvc.base + "sysinfo", successCB, errorCB);
	};

	SysSvc.saveConfig = function (configMap, successCB, errorCB) {
		SvrRest.put (SysSvc.base + "config", configMap, successCB, errorCB);
	};
	
	SysSvc.saveShortcuts = function (shortcutKey_p, shortcutList_p, successCB, errorCB) {
		var alist = [];
		for (i in shortcutList_p) {
			alist.push(shortcutList_p[i].name);
		}
		var keySet = {};
		keySet[shortcutKey_p] = alist.join(',');
		SvrRest.put (SysSvc.base + "config", keySet, successCB, errorCB);
	};

	SysSvc.getModelTemplates = function (successCB, errorCB) {
		SvrRest.get (SysSvc.base + "template", successCB, errorCB);
	};

//	SysSvc.getLicDetails = function (successCB, errorCB) {
//		SvrRest.get (SysSvc.base + "sysinfo", successCB, errorCB);
//	};

	SysSvc.saveLic = function (licEmail, licKey, successCB, errorCB) {
		SvrRest.get (SysSvc.base + "lic/save?email=" + licEmail + "&licKey=" + encodeURIComponent(licKey), successCB, errorCB);
	};

	SysSvc.about = function (successCB, errorCB) {
		SvrRest.get (SysSvc.base + "about", successCB, errorCB);
	};

	SysSvc.getCA = function (successCB, errorCB) {
		SvrRest.get (SysSvc.base + "CodeAssist/model", successCB, errorCB);
	};
	
	SysSvc.getCAData = function (successCB, errorCB) {
		SvrRest.get (SysSvc.base + "CodeAssist/data", successCB, errorCB);
	};

	SysSvc.getCAListByExpr = function (pathList, successCB, errorCB) {
		SvrRest.post (SysSvc.base + "CodeAssist/expr", pathList, successCB, errorCB);
	};
	
	return SysSvc;
});




