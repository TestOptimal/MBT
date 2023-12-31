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


function getScopeVar () {
	var scope = angular.element(document.getElementById('Main')).scope();
	return scope;
}

var scope;

var curAppState = {
	auth: {id: '', pwd: ''},
	setToken: function(username, password) {
		scope.setToken(username, password);
	},
	winMgr: {},
	modelState: { running: false },
	edition: "MBT",
	curFolder: {path: "", name: ""},
	getCurFolder: function () {
		if (curAppState.curFolder && curAppState.curFolder.path) {
			return curAppState.curFolder.path + "/" + curAppState.curFolder.name;
		}
		else return curAppState.curFolder.name;
	},
	nodeDataList: new Array(),
	playModelDelay: 100,
	subModelList: null,
	reqList: [],
	modelChangedList: {}, //{Script: false, Model: false },
	setModelChanged: function setModelChanged(appName, changed_p) {
		curAppState.modelChangedList[appName] = changed_p;
		var scope = getScopeVar();
		scope.flags.modelChanged = curAppState.isModelChanged();
		setTimeout(function(){ scope.$apply();},10);
	},
	isModelChanged: function (msgIfChanged) {
		var list = [];
		for (var n in curAppState.modelChangedList) {
			if (curAppState.modelChangedList[n]) {
				list.push(n);
			}
		}
		if (list.length>0 && msgIfChanged) {
			curAppState.addMsg({type: "alert", text: "There are pending changes made to: " + list.join(", ") + ". Please save the changes first.", 
				source: "IDE_Main.isModelChanged"});
		}
		return list.length>0;
	},
	resetModelChanged: function () {
		curAppState.modelChangedList = {};
	},
	addMsg: function (msgObj_p) {
		msgObj_p.text = translateMsg(msgObj_p.text);
		msgObj_p.timestamp = new Date();
		scope.addMsg(msgObj_p);
		setTimeout(function() { scope.$apply();}, 10);
	},
	setModelEditorMode: function(mode_p) {
		scope.flags.modelEditorMode = mode_p;
		scope.flags.seqMode = "Optimal";
		scope.$apply();
	},
	openDialog: function (winName_p) {
		scope.openDialog(winName_p);
		scope.$apply();
	},
	setModelInfo: storeModelInfo,
	selectTab: function (tabID_p) {
		scope.selectTab(tabID_p);
		scope.$apply();
	},
	fileSave: function() {
		for (p in curAppState.modelChangedList) {
			if (curAppState.modelChangedList[p] && 
				(p=="Model" || p=="Script" || p=="Requirement")) {
				curAppState.winMgr.runWinAction(p, "save");
			}
		}
	},
	debug: {
		exprList: [],
		curExpr: { script: "" }
	},
	isCommunity: function() {
		return curAppState.config["License.Edition"]=="MBT";
	},
	FileViewer: {title: ''},
	openSvrLog: function (skipApply_p) {
		curAppState.FileViewer = {type: "ServerLog"};
		scope.openDialog("FileViewer");
		if (!skipApply_p) scope.$apply();
	},
	openModelLog: function (skipApply_p) {
		curAppState.FileViewer = {type: "ModelLog"};
		scope.openDialog("FileViewer");
		if (!skipApply_p) scope.$apply();
	},
	openModelFile: function (folderPath_p, fileName_p) {
		curAppState.FileViewer = {type: "ModelFile", folderPath: folderPath_p, fileName: fileName_p};
		scope.openDialog("FileViewer");
		scope.$apply();
	}	

}

curAppState.winMgr = new WinManager(curAppState);
curAppState.toWS = new TOWS ();

function storeModelInfo (modelInfo_p) {
	curAppState.modelInfo = modelInfo_p;
	curAppState.scxml = modelInfo_p.model;
	var model = curAppState.scxml;
	curAppState.nodeDataList = {};

	curAppState.nodeDataList[model.mbtNode.uid] = model.mbtNode;
	model.mbtNode.typeCode="mbt";
	curAppState.mbtNode = model.mbtNode;
	model.mbtNode.parentuid = model.uid;

	curAppState.miscNode = model.miscNode;
	model.miscNode.parentuid = model.uid;
	
	curAppState.nodeDataList[model.uid] = model;

	if (model.miscNode && model.miscNode.swimlanes) {
		curAppState.nodeDataList[model.miscNode.uid] = model.miscNode;
		for (i=0; i<model.miscNode.swimlanes.length; i++) {
			curAppState.nodeDataList[model.miscNode.swimlanes[i].uid] = model.miscNode.swimlanes[i];
			model.miscNode.swimlanes[i].parentuid=model.miscNode.uid;
		}
	}

	if (model.miscNode && model.miscNode.boxes) {
		curAppState.nodeDataList[model.miscNode.uid] = model.miscNode;
		for (i=0; i<model.miscNode.boxes.length; i++) {
			curAppState.nodeDataList[model.miscNode.boxes[i].uid] = model.miscNode.boxes[i];
			model.miscNode.boxes[i].parentuid=model.miscNode.uid;
		}
	}

	var expandList = new Array();
	expandList[0] = model;
	while (expandList.length>0) {
		expandList = loadStates(expandList);
	}

	var templateList = JSON.parse(modelInfo_p.templateJson);
	curAppState.nodeDataList["template.state"] = templateList.state;
	curAppState.nodeDataList["template.transition"] = templateList.transition;
	curAppState.nodeDataList["template.swimlane"] = templateList.swimlane;
	curAppState.nodeDataList["template.box"] = templateList.box;

	return true;
}

function loadStates (expandList_p) {
	var newExpandList = new Array();
	for (k=0; k<expandList_p.length; k++) {
		for (i=0; i<expandList_p[k].childrenStates.length; i++) {
			curAppState.nodeDataList[expandList_p[k].childrenStates[i].uid] = expandList_p[k].childrenStates[i];
			expandList_p[k].childrenStates[i].parentuid = expandList_p[k].uid;
			for (j=0; j<expandList_p[k].childrenStates[i].transitions.length; j++) {
				curAppState.nodeDataList[expandList_p[k].childrenStates[i].transitions[j].uid]= expandList_p[k].childrenStates[i].transitions[j];
				expandList_p[k].childrenStates[i].transitions[j].parentuid=expandList_p[k].childrenStates[i].uid;
			}
			if (expandList_p[k].childrenStates[i].childrenStates && expandList_p[k].childrenStates[i].childrenStates.length>0) {
				newExpandList[newExpandList.length] = expandList_p[k].childrenStates[i];
			}
		}
	}
	return newExpandList;
}


function closeDialog() {
	getScopeVar().closeDialog(true);
}

$(document).ready(function() {
	window.onunload = function () {
		curAppState.winMgr.closeAllWins();
    }
    
	$(document).keyup(function(event) {
		var moveDelta = 3;
		if (event.keyCode == 27) { // escape
			parent.scope.sysMsg.visible = false;
			parent.scope.$apply();
		}
	});
});


MainModule.controller('mainCtrl', function ($scope, $cookies, $window, SvrRest, SysSvc, FileSvc, ModelSvc, AlmSvc, StatsSvc) {
	parent.scope = $scope;
	$scope.shortcutKey = "IDE.shortcuts.ide";
	$scope.shortcutDefault = "closeModel,ModelProperty,MBTSetting,genTestCase,startExec,startDebug,startPlay,stopExec,pauseExec,stepOver,Monitor,showCoverage,MScriptLog";
	
    $scope.tabList = [];
    $scope.headerHeight = 20;
    $scope.tabLabelLeft = 350;
	$scope.viewHeight = $window.innerHeight;
	$scope.viewWidth = $window.innerWidth;
	$scope.moreMenuID = "";
	$scope.rootCurAppState = curAppState;
	$scope.sysMsg = {
		SeverityList: { "info": 1, "alert": 2, "warn": 3, "error": 4},
		msgList: [],
		infoCount: 0,
		alertCount: 0,
		warnCount: 0,
		errorCount: 0,
		visible: false
	}
	
	$scope.setToken = function (username, password) {
		SvrRest.setToken(username, password);
	}
	
	$scope.shortcutList = [];
	
	$scope.actionList = { // add "tab" to restrict menu /toolbar to a specific tab
		ModelProperty: {menuFunc: "$scope.openDialog('PropModel')", label: "Model Property", toolbar: true, icon: 'glyphicon-th-list', title: "Model properties", hide: "noModelOpen"},
		MBTSetting: {menuFunc: "$scope.openDialog('PropMBT')", label: "Exec Setting", toolbar: true, icon: "glyphicon-wrench", title: "Model execution settings", hide: "noModelOpen"},
		openModel: { menuFunc: "$scope.openDialog('FileList')", classes: "", label: "Open/New Model", title: "Open or create model"},
		saveModel: { menuFunc: "curAppState.fileSave()", classes: "extraPaddingTop", label: "Save", msc: "", hide: "noModelOpen", title: "Save model changes: model diagram, properties and scripts" /*, tabID: "Model" */ },
		saveAs: { menuFunc: "$scope.fileSaveAs()", classes: "", label: "Save As", msc: "", title: "Save this model as another name", hide: "noModelOpen"},
		renameModel: { menuFunc: "$scope.renameModel()", classes: "", label: "Rename Model", msc: "", hide: "noModelOpen", title: "Rename model" },
		closeModel: { menuFunc: "$scope.closeModel()", toolbar: true, icon: "glyphicon-log-out", classes: "", label: "Close", msc: "", title: 'Close model', hide: "noModelOpen"},
		artifact: { menuFunc: "$scope.selectTab('Files')", classes: "", label: "Model Files", msc: "", title: "Open File tab", hide: "noModelOpen"},

		ModelGraph: { menuFunc: "curAppState.winMgr.openWebPage('api/v1/graph/model/' + curAppState.scxml.modelName + '/model', 'ModelGraph')", classes: "", label: "Model Graph", msc: "", title: "Generate and display model graph", hide: "noModelOpen"},

		Monitor: {menuFunc: "$scope.openDialog('Monitor')", label: 'Monitor', classes: "extraPaddingTop", toolbar: true, icon: 'glyphicon-tasks', title: "Model execution monitor", hide: "noModelOpen"},
		runResults: {menuFunc: "$scope.selectTab('Results')", label: 'Results', toolbar: true, icon: 'glyphicon-stats', title: "Model execution test results", hide: "noModelOpen"},
		
		genTestCase: { menuFunc: "$scope.genTestCase()", toolbar: true, icon: "glyphicon glyphicon-play-circle", classes: "extraPaddingTop", label: "Generate", hide: "noModelOpen", title: "Generate test cases/paths (no script execution)", stopOnModelChanged: true},
		startExec: { menuFunc: "$scope.runModel()", toolbar: true, icon: "glyphicon-play", classes: "", label: "Run", hide: "noModelOpen", title: "Start/Resume model execution (generate test case and execute scripts)", stopOnModelChanged: true},
		startPlay: { menuFunc: "$scope.playModel()", toolbar: true, icon: "glyphicon-forward", classes: "", label: "Play", hide: "noModelOpen", title: "Play - execute model in annimation mode", stopOnModelChanged: true},
		stopExec: { menuFunc: "$scope.stopModel()", toolbar: true, icon: "glyphicon-stop", classes: "", label: "Stop", hide: "noModelOpen", title: "Stop current model execution"},
		startDebug: { menuFunc: "$scope.debugModel()", toolbar: true, icon: "glyphicon-step-forward", classes: "extraPaddingTop", label: "Debug", hide: "noModelOpen", title: "Execute model in debug mode", stopOnModelChanged: true},
		pauseExec: { menuFunc: "$scope.pauseModel()", toolbar: true, icon: "glyphicon-pause", classes: "", label: "Pause", hide: "noModelOpen", title: 'Pause model execution'},
		stepOver: { menuFunc: "$scope.stepModel()", toolbar: true, icon: "glyphicon-share-alt", classes: "", label: "Step Over", hide: "noModelOpen", title: 'Step over while in Debug execution mode'},
		playback: { menuFunc: "openPlayback()", classes: "extraPaddingTop", label: "Play Recording", title: "Playback screen recording. Screen recording is started by MScript function $startScreenRecording(...)."},
		showCoverage: { menuFunc: "curAppState.winMgr.raiseEvent('showCoverage')", toolbar: true, icon: "glyphicon-tint", classes: "", label: "Show Coverage", hide: "noModelOpen", title: "Show model execution coverage"},

		help: { menuFunc: "curAppState.winMgr.openWin ('Help')", classes: "", title: "TestOptimal online help documentation/wiki", label: "Online Wiki", helpMenu: true},
		forum: { menuFunc: "curAppState.winMgr.openWin ('Forum')", classes: "", label: "Community Forum", title: "TestOptimal Community Discussion Forum"},
		support: { menuFunc: "curAppState.winMgr.openWin ('Support');", classes: "", label: "Contact Us", title: "submit sales-questions or for ProMBT users support-tickets"},
		sysInfo: { menuFunc: "$scope.openDialog('SysInfo')", classes: "extraPaddingTop", label: "System Info", title: "System information"},
		serverLog: { menuFunc: "curAppState.openSvrLog(true)", classes: "extraPaddingTop", toolbar: true, icon: "glyphicon-alert", label: "Server Log", title: "Server Log window"},
		MScriptLog: {menuFunc: "curAppState.openModelLog(true)", label: 'Model Log', classes: "", toolbar: true, icon: 'glyphicon-warning-sign', title: "Model script log file", hide: "noModelOpen"},
		resetShortcuts: {menuFunc: "$scope.initShortcuts()", label: 'Reset Shortcuts', classes: "extraPaddingTop", title: "Resets toolbar"},
		swaggerUI: {menuFunc: "curAppState.winMgr.openWebPage('swagger')", label: 'REST API', classes: "", title: "TestOptimal REST API - Swagger UI"},
		toggleFS: { menuFunc: "toggleFullScreen()", classes: "", title: "Toggle fullscreen", label: "Toggle Fullscreen"},
		logout: { menuFunc: "$scope.logout()", classes: "", title: "Logout", label: "Logout"},
		welcome: { menuFunc: "$scope.openDialog('Welcome');", classes: "", title: "open Welcome", label: "Welcome"},
	};
	
	$scope.logout = function () {
		$cookies.put(location.hostname + "_" + location.port + "_totoken", "", {domain: location.hostname, samesite: 'strict'});
		$.ajax("/logout");
		document.location = '/IDE_Login.html';	
	}
	
	$scope.menuFile ={ 
		name: "FILE", label: "FILE", 
    	items: [
    		$scope.actionList.openModel,
    		$scope.actionList.renameModel,
    		$scope.actionList.saveModel,
    		$scope.actionList.saveAs,
    		$scope.actionList.closeModel,
    		$scope.actionList.resetShortcuts,
			$scope.actionList.logout
    	  ]
    	};
	$scope.menuModel = {
    	name: "MODEL", label: "MODEL",
    	items: [
	    	$scope.actionList.ModelProperty,
	    	$scope.actionList.ModelGraph,
    		$scope.actionList.artifact
    	  ]
    	};
	$scope.menuRun = {
		name: "RUN", label: "RUN",
    	items: [
	    	$scope.actionList.MBTSetting,
		  	$scope.actionList.genTestCase,
		  	$scope.actionList.startExec,
		  	$scope.actionList.startPlay,
		  	$scope.actionList.stopExec,
		  	$scope.actionList.startDebug,
		  	$scope.actionList.pauseExec,
		  	$scope.actionList.stepOver,
		  	$scope.actionList.Monitor,
		  	$scope.actionList.showCoverage,
		  	$scope.actionList.runResults,
		  	$scope.actionList.MScriptLog
	      ]
		};
	$scope.menuHelp = { 
	  name: "HELP", label: "HELP",
	  items: [
		  $scope.actionList.welcome,
		  $scope.actionList.help,
		  $scope.actionList.forum,
		  $scope.actionList.support,
		  $scope.actionList.sysInfo,
	      $scope.actionList.toggleFS,
	      $scope.actionList.serverLog,
	      $scope.actionList.swaggerUI
	  ]
	};

	$scope.menuList = [ $scope.menuFile, $scope.menuModel, $scope.menuRun ];
	
	$scope.guideList = [
		{ name: 'ide', title: "IDE Intro", label: "IDE Intro"},
		{ name: 'model', label: 'MODEL Intro', hide: "", requireModel: true, title: 'Quick intro of Model editor'},
		{ name: 'script', label: 'SCRIPT Intro', hide: "", requireModel: true, title: 'Quick intro of Script editor'},
		{ name: 'execModel', label: 'Execute Model', hide: "", requireModel: true, title: 'How to generate test cases and run model'},
		{ name: 'trackReq', label: 'Track Requirement', hide: "", requireModel: true, title: 'How to track requirements in script'},
	];

	$scope.startGuide = function (item) {
		if (item.requireModel && !$scope.flags.isModelOpen) {
			alertDialog("Guide '" + item.title + "' requires a model opened.");
		}
		else startGuide(item.name);
	}	
	
	$scope.initShortcuts = function (shortcuts) {
		$scope.shortcutList = [];
		if (shortcuts==undefined || shortcuts=='') {
			$scope.config[$scope.shortcutKey] = $scope.shortcutDefault;
		}
		else {
			$scope.config[$scope.shortcutKey] = shortcuts;
		}
		$scope.config[$scope.shortcutKey].split(",").forEach(function (key) {
			var scObj = $scope.actionList[key];
			if (scObj) {
				$scope.shortcutList.push(scObj);
			}
		});
		$scope.saveShortcuts();
	}
	
	$scope.dialog = {
		visible: false,
		title: '',
		frameDefn: {},
		style: {

		},
		frames: {
			Monitor: {
				title: 'Execution Monitor',
				srcFrame: 'IDE_Main/PropertyMonitor.html',
				width: 600,
				height: 500
			},
			SysInfo: {
				title: 'System Info',
				srcFrame: 'SysInfo.html',
				width: 500,
				height: 450,
			},
			FileList: {
				title: 'Model List',
				srcFrame: 'IDE_Main/PropertyFileList.html',
				width: 800,
				height: 600,
			},
			PropMBT: {
				title: 'Execution Settings',
				srcFrame: 'IDE_Main/PropertyMbtSetting.html',
				width: 400,
				height: 480,
			},
			PropModel: {
				title: 'Model Properties',
				srcFrame: 'IDE_Main/PropertyModel.html',
				width: 580,
				height: 550,
			},
			PropRequirement: {
				title: 'Model Requirements',
				srcFrame: 'IDE_Main/PropertyRequirement.html',
				width: 800,
				height: 600,
			},
			InfoScript: {
				title: 'Script Editor',
				srcFrame: 'IDE_Main/InfoScript.html',
				width: 600,
				height: 650
			},
			InfoModel: {
				title: 'Model Editor',
				srcFrame: 'IDE_Main/InfoModel.html',
				width: 600,
				height: 650
			},
			InfoRequirement: {
				title: 'Requirement Editor',
				srcFrame: 'IDE_Main/InfoRequirement.html',
				width: 600,
				height: 450
			},
			Dashboard: {
				title: 'Dashboard',
				srcFrame: 'Dashboard.html',
				width: 600,
				height: 350
			},
			Welcome: {
				title: 'TestOptimal Studio for Model-Based Test Designer and Automation Engineers',
				srcFrame: 'IDE_Main/Welcome.html',
				width: 600,
				height: 475
			},
			FileViewer: {
				title: 'File Viewer',
				srcFrame: 'FileViewer.html',
				width: '75%',
				height: '85%'
			}
		}
	};

	$scope.openDialog = function (frameID_p) {
		$scope.dialog.visible = true;
		$scope.dialog.frameDefn = $scope.dialog.frames[frameID_p];
		if (isNaN($scope.dialog.frameDefn.width)) {
			$scope.dialog.style.width = $scope.dialog.frameDefn.width;
			var pct = parseInt ($scope.dialog.frameDefn.width.substring(0, $scope.dialog.frameDefn.width.length-1));
			$scope.dialog.style.left = ($scope.viewWidth * (100-pct)/100) /2;
		}
		else {
			$scope.dialog.style.width = Math.min($scope.dialog.frameDefn.width, $scope.viewWidth);
			$scope.dialog.style.left = ($scope.viewWidth - $scope.dialog.style.width) /2;
		}
		if (isNaN($scope.dialog.frameDefn.height)) {
			$scope.dialog.style.height = $scope.dialog.frameDefn.height;
			var pct = parseInt ($scope.dialog.frameDefn.height.substring(0, $scope.dialog.frameDefn.height.length-1));
			$scope.dialog.style.top = Math.max(20, ($scope.viewHeight * (100-pct)/100) /2 - 50);
		}
		else {
			$scope.dialog.style.height = Math.min($scope.dialog.frameDefn.height, $scope.viewHeight-55);
			$scope.dialog.style.top = Math.max(20, ($scope.viewHeight - 55 - $scope.dialog.style.height)/2);
		}
	}
	
	$scope.closeDialog = function (externCall_p) {
		$scope.dialog.visible = false;
		$scope.dialog.frameDefn = $scope.dialog.frames.Blank;
		if (externCall_p) $scope.$apply();
	}

	$scope.optionalTabList = {
			Model: {tabID: "Model", tabLabel: "MODEL", frameSrc: "IDE_Main/Tab_Model.html", enabled: false},
			Script: {tabID: "Script", tabLabel: "SCRIPT", frameSrc: "IDE_Main/Tab_ScriptEditor.html", enabled: false},
			Results: {tabID: "Results", tabLabel: "RESULT", frameSrc: "IDE_Main/Tab_Result.html", enabled: false},
			Files: {tabID: "Files", tabLabel: "ARTIFACT", frameSrc: "IDE_Main/Tab_ModelFiles.html", enabled: false},
	}

    $scope.flags = {
    	showNewFeature: false,
    	isModelOpen: false,
    	noModelOpen: true,
    	modelChanged: false,
    	selectedTabID: "",
    	syncTimerOn: true,
		modelEditorMode: 'edit'
    }
    
	$scope.isMenuDisabled = function (item) {
		return $scope.flags[item.hide] || (item.tab && item.tab != $scope.flags.selectedTabID);
	}

    $scope.menuClicked = function (item) {
    	if ($scope.isMenuDisabled(item) ||
    		item.stopOnModelChanged && $scope.rootCurAppState.isModelChanged(true)) {
    		return;
    	} 
    	eval(item.menuFunc); //.apply($scope, [item]);
    	if (item.tabID) {
    		$scope.selectTab(item.tabID);
    	}
    }
    
	$scope.toggleShortcut = function (scObj) {
		var idx = $scope.shortcutList.indexOf(scObj);
		if (idx>=0) {
			$scope.shortcutList.splice(idx,1);
		}
		else {
			$scope.shortcutList.push(scObj);
		}
		$scope.saveShortcuts();
	}
	
	$scope.saveShortcuts = function () {
		curAppState.toSvc.SysSvc.saveShortcuts($scope.shortcutKey, $scope.shortcutList);
	}

	$scope.markCurSession = function (modelName) {
		if ($scope.rootCurAppState.scxml) {
			$scope.menuRun.items.forEach(function(m) {
				m.classes = (m.modelName == modelName)?"selectedSession":"";
			});
		}
	}

	$scope.selectTab = function (tabID) {
		if (tabID) {
			$scope.flags.selectedTabID = tabID;
			$scope.rootCurAppState.winMgr.raiseEvent("selectTab", [tabID]);
		}
    };

    $scope.openWin = function (winID) {
    	curAppState.winMgr.openWin (winID);
    }
    
    $scope.setSeqMode = function (seqMode_p) {
    	if (seqMode_p) {
    		$scope.flags.seqMode = seqMode_p; 
    		$scope.$apply(); // from MbtProperty
    	}
    	else {
	    	$scope.rootCurAppState.curSeqMode = $scope.flags.seqMode;
			$scope.rootCurAppState.winMgr.runWinAction("Model", "seqMode", $scope.flags.seqMode);
    	}
    }
    
	$scope.toggleMoreMenu = function (menuID_p) {
		if ($scope.moreMenuID==menuID_p) $scope.moreMenuID = "";
		else $scope.moreMenuID = menuID_p;
	}

	$scope.openModel = function (modelName) {
		$scope.tabList = [];
		$scope.sysMsg.msgList = [];
		$scope.rootCurAppState.toSvc.ModelSvc.getModel(modelName, function (modelInfo) {
			if (modelInfo.valid) {
				$scope.rootCurAppState.winMgr.init();
				$scope.tabList = [];
				$scope.rootCurAppState.modelChangedList = [];
				$scope.flags.modelChanged = false;
				$scope.flags.isModelOpen = true;
				$scope.flags.noModelOpen = false;
				modelInfo.model = JSON.parse(modelInfo.modelJson);
				var scxmlNode = modelInfo.model;
				$scope.rootCurAppState.setModelInfo(modelInfo);
				
				$scope.flags.seqMode = scxmlNode.mbtNode.mode;
				$scope.rootCurAppState.curSeqMode = scxmlNode.mbtNode.mode;
				$scope.tabList.push($scope.optionalTabList.Model);
				$scope.tabList.push($scope.optionalTabList.Script);
				$scope.tabList.push($scope.optionalTabList.Requirement);
				$scope.tabList.push($scope.optionalTabList.Results);
				$scope.tabList.push($scope.optionalTabList.Files);
				$scope.selectTab("Model");
				
				if (scxmlNode.showNotepad) {
					alertDialog (scxmlNode.notepad);
				}
			}
			else {
				$scope.tabList = [];
				$scope.flags.isModelOpen = false;
				$scope.flags.noModelOpen = true;
			}
		});
		$scope.rootCurAppState.toSvc.AlmSvc.getRequirements (modelName, function(reqList_p) {
			$scope.rootCurAppState.reqList = reqList_p;
		});
	}
	
    $scope.init = function () {
    	$scope.rootCurAppState.toSvc = {
    		SysSvc: SysSvc, 
    		FileSvc: FileSvc, 
    		ModelSvc: ModelSvc,
    		AlmSvc: AlmSvc,
    		StatsSvc: StatsSvc,
    		SvrRest: SvrRest
    	};
    	
		angular.forEach ($scope.actionList, function (act, key) {
    		act.name = key;
    	});

    	SysSvc.getSysInfo (function(data) {
    		$scope.config = data;
    		
    		$scope.rootCurAppState.config = $scope.config;
    		$scope.initWS();
    		
    		$scope.flags.showNewFeature = $scope.config.showNewReleaseFeatures;
    		
    		$scope.shortcutList.length = 0;
    		$scope.initShortcuts($scope.config[$scope.shortcutKey]);
    		
			// initMenu
	    	$scope.flags.edition = $scope.config.licEdition;
			angular.forEach($scope.menuList, function(menu) {
				angular.forEach(menu.items, function(item) {
					item.title += " (" + menu.name + "/" + item.label + ")";
				});
			});

			if (getRequestParam("model")) {
				setTimeout(function() { $scope.openModel(getRequestParam("model"));}, 50);
			}
    	});
    	
    }
    
    $scope.getModelNameShort = function () {
    	if ($scope.rootCurAppState.scxml) {
        	var mname = $scope.rootCurAppState.scxml.modelName;
        	if (mname.length > 20) {
        		mname = mname.substring(0,20) + "...";
        	}
        	return mname + " - ";
    	}
    	return "TestOptimal IDE";
    }

    $scope.getModelNameFull = function () {
    	if ($scope.rootCurAppState.scxml) {
    		return $scope.rootCurAppState.scxml.modelName + " - ";
    	}
    	else return 'TestOptimal IDE';
    }

    $scope.initWS = function () {
		// setup websocket connection
		$scope.rootCurAppState.toWS.wsSubscribe("alert", function (packet) {
        	var msg = JSON.parse(packet.body);
        	if (!msg.type) {
        		msg = { type: "alert", text: msg};
        	}
        	$scope.addMsg(msg);
        	$scope.$apply();
		}, "IDE_Main");
		
		$scope.rootCurAppState.toWS.wsSubscribe("renamed", function(packet) {
			$scope.rootCurAppState.scxml.modelName = packet.body;
			$scope.addMsg({type: "alert", text: "Model renamed to " + packet.body, source: "IDE_Main.renamed"});
			$scope.$apply();
		}, "IDE_Main");

		$scope.rootCurAppState.toWS.wsSubscribe("saved", function (packet) {
			$scope.addMsg({type: "alert", text: "Changes to " + packet.body + " have been saved."});
			$scope.$apply();
		}, "IDE_Main");

		$scope.rootCurAppState.toWS.wsSubscribe("model.created", function (packet) {
			$scope.openModel(packet.body);
		}, "IDE_Main");

		$scope.rootCurAppState.toWS.wsSubscribe("model.saveas.done", function (packet) {
			$scope.openModel(packet.body);
		}, "IDE_Main");

		$scope.rootCurAppState.toWS.wsSubscribe('model.paused', function (packet) {
			//if (packet.body.indexOf("{")>=0) {
			//	var p = JSON.parse(packet.body);
			//	$scope.addMsg({type: "alert", text: "Paused at " + p.type + ': ' + p.desc + ' (' + p.uid + ')', source: "IDE_Main"});
			//}
			if ($scope.rootCurAppState.playModel) {
				setTimeout (function() {
					$scope.rootCurAppState.toWS.wsSendModel('stepOver','');
					}, $scope.rootCurAppState.playModelDelay
				);
			}
			$scope.$apply();
		}, "IDE_Main");

		$scope.rootCurAppState.toWS.wsSubscribe('model.ended', function (packet) {
			$scope.selectTab("Results");
		}, "IDE_Main");

    	if ($scope.config.alertMsg) {
    		$scope.rootCurAppState.toSvc.SysSvc.saveConfig({"alertMsg": null});
    		alertDialog ($scope.config.alertMsg);
    	}
    	if (!$scope.config.welcomed) {
    		$scope.rootCurAppState.toSvc.SysSvc.saveConfig({"welcomed": new Date()});
			$scope.openDialog('Welcome');
    	}
    }

    
    $scope.addMsg = function(msgObj_p) {
    	if (!msgObj_p.timestamp) {
    		msgObj_p.timestamp = new Date();
    	}
		$scope.sysMsg.msgList.unshift(msgObj_p);
		$scope.tallyMsgList();
	}

	$scope.tallyMsgList = function () {
		$scope.sysMsg.msgGroups = $scope.sysMsg.msgList.reduce((acc, m) => {
			m.type = m.type || '';
			if (!acc[m.type]) {
				acc[m.type] = [];
			}
			acc[m.type].push(m);
			return acc;
		}, {});
		$scope.sysMsg.mostSevereType = "info";
		if ($scope.sysMsg.msgGroups.alert?.length>0) $scope.sysMsg.mostSevereType = "alert";
		if ($scope.sysMsg.msgGroups.warn?.length>0) $scope.sysMsg.mostSevereType = "warn";
		if ($scope.sysMsg.msgGroups.error?.length>0) $scope.sysMsg.mostSevereType = "error";
	}
	
	$scope.clearMsg = function () {
		$scope.sysMsg.visible=false; 
		$scope.sysMsg.msgList = [];
		$scope.tallyMsgList();
	}
	
	$scope.checkModelEditorMode = function () {
		$scope.flags.modelEditorMode = $scope.rootCurAppState.winMgr.runWinAction("Model", "modelEditorMode");
	}
	
	$scope.closeModel = function() {
		if ($scope.rootCurAppState.isModelChanged(false)) {
			confirmDialog("Do you wish to discard changes?", function() {
				$scope.rootCurAppState.toSvc.ModelSvc.closeModel($scope.rootCurAppState.scxml.modelName, function() {
					$scope.modelClosed($scope.rootCurAppState.scxml.modelName);
				});
			})

		}
		else {
			$scope.rootCurAppState.toSvc.ModelSvc.closeModel($scope.rootCurAppState.scxml.modelName, function() {
				$scope.modelClosed($scope.rootCurAppState.scxml.modelName);
			});
		}
	}
	
	$scope.modelClosed = function (modelName) {
		$scope.tabList = [];
		$scope.flags.isModelOpen = false;
		$scope.flags.noModelOpen = true;
		$scope.rootCurAppState.resetModelChanged();
		$scope.rootCurAppState.scxml = undefined;
		$scope.closeDialog();
	}
	
	$scope.renameModel = function () {
		if ($scope.rootCurAppState.scxml) {
			promptDialog ("file.rename", $scope.rootCurAppState.scxml.modelName, 
				function () {
					var modelName2 = $("#promptField").val().replace( /[\n\r'"=]+/g, "");
					if (modelName2=="") return;
					if (modelName2==$scope.rootCurAppState.scxml.modelName) return;
					$scope.rootCurAppState.toWS.wsSendModel("rename", modelName2);
				});
		}
	}

	$scope.fileSaveAs = function () {
		if (curAppState.isModelChanged(true)) {
			return;
		} 
		
		promptDialog("save.model.as", "", function(){
			var asModelName = $("#promptField").val();
			if (asModelName=="" || asModelName=="/") return;
			$scope.rootCurAppState.toWS.wsSendModel("saveAs", asModelName);
		});
	}
	
	$scope.runModel = function () {
		$scope.rootCurAppState.playModel = false;
		$scope.clearMsg();
		//if ($scope.rootCurAppState.modelState.running) {
		//	$scope.rootCurAppState.toWS.wsSendModel("resumeModel", $scope.rootCurAppState.scxml.modelName);
		//}
		//else {
			$scope.rootCurAppState.toWS.wsSendModel("startModel", $scope.getRunRequest(false));
		//}
	}

	$scope.genTestCase = function () {
		$scope.rootCurAppState.playModel = false;
		$scope.clearMsg();
		var runReq = $scope.getRunRequest(false);
		runReq.options.generateOnly = true;
		//if ($scope.rootCurAppState.modelState.running) {
		//	$scope.rootCurAppState.toWS.wsSendModel("resumeModel", $scope.rootCurAppState.scxml.modelName);
		//}
		//else {
			$scope.rootCurAppState.toWS.wsSendModel("startModel", runReq);
		//}
	}

	$scope.debugModel = function () {
		$scope.rootCurAppState.playModel = false;
		$scope.sysMsg.msgList = [];
		//if ($scope.rootCurAppState..runn.running) {
		//	$scope.rootCurAppState.toWS.wsSendModel("resumeDebug", $scope.rootCurAppState.scxml.modelName);
		//}
		//else {
			$scope.rootCurAppState.toWS.wsSendModel("debugModel", $scope.getRunRequest(true));
		//}
	}

	$scope.playModel = function () {
		promptDialog("Animation delay (milliseconds)", $scope.rootCurAppState.playModelDelay, function () {
			$scope.rootCurAppState.playModelDelay = parseInt(getPromptVal());
			$scope.rootCurAppState.playModel = true;
			$scope.sysMsg.msgList = [];
//			if ($scope.rootCurAppState.modelState.running) {
//				$scope.rootCurAppState.toWS.wsSendModel("stepOver", $scope.rootCurAppState.scxml.modelName);
//			}
//			else {
				$scope.rootCurAppState.toWS.wsSendModel("debugModel", $scope.getRunRequest(true));
//			}
		});
		$scope.rootCurAppState.selectTab("Model");
	}
	
	$scope.getRunRequest = function (debugMode) {
		var runParams = {};
		runParams["debug"] = debugMode; 
		runParams["ideBrowser"] = (new UAParser()).getResult().browser.name;
		
		var markedUIDs = $scope.rootCurAppState.winMgr.runWinAction("Model", "getAllMarkedUID");
		if (markedUIDs && markedUIDs.length > 0) {
			runParams ["markList"] = markedUIDs;
		}
		runParams.breakpoints = $scope.rootCurAppState.winMgr.runWinAction("Model", "getBreakpoints");
		
		return {
			modelName: $scope.rootCurAppState.scxml.modelName, 
			mbtMode: $scope.flags.seqMode,
			statDesc: "IDE_Run_" + (new Date()).toLocaleString(), 
			options: runParams
		}
	}

	$scope.pauseModel = function () {
		$scope.rootCurAppState.playModel = false;
		$scope.rootCurAppState.toWS.wsSendModel("pauseModel", $scope.rootCurAppState.scxml.modelName);
	}

	$scope.stepModel = function () {
		$scope.rootCurAppState.toWS.wsSendModel("stepOver", $scope.rootCurAppState.scxml.modelName);
	}

	$scope.stopModel = function () {
		$scope.rootCurAppState.toWS.wsSendModel("stopModel", $scope.rootCurAppState.scxml.modelName);
	}

	$scope.init();
});


//from http://blog.pothoven.net/2006/07/get-request-parameters-through.html
function getRequestParam ( parameterName ) {
	var queryString = window.top.location.search.substring(1);
	// Add "=" to the parameter name (i.e. parameterName=value)
	var parameterName = parameterName + "=";
	if ( queryString.length > 0 ) {
	// Find the beginning of the string
		begin = queryString.indexOf ( parameterName );
		// If the parameter name is not found, skip it, otherwise return the value
		if ( begin != -1 ) {
			// Add the length (integer) to the beginning
			begin += parameterName.length;
			// Multiple parameters are separated by the "&" sign
			end = queryString.indexOf ( "&" , begin );
			if ( end == -1 ) {
				end = queryString.length
			}
			// Return the string
			return unescape ( queryString.substring ( begin, end ) );
		}
		// Return "null" if no parameter has been found
		return "null";
	}
}

function toggleFullScreen() {
  if (!document.fullscreenElement &&    // alternative standard method
      !document.mozFullScreenElement && !document.webkitFullscreenElement && !document.msFullscreenElement ) {  // current working methods
    if (document.documentElement.requestFullscreen) {
      document.documentElement.requestFullscreen();
    } else if (document.documentElement.msRequestFullscreen) {
      document.documentElement.msRequestFullscreen();
    } else if (document.documentElement.mozRequestFullScreen) {
      document.documentElement.mozRequestFullScreen();
    } else if (document.documentElement.webkitRequestFullscreen) {
      document.documentElement.webkitRequestFullscreen(Element.ALLOW_KEYBOARD_INPUT);
    }
  } else {
    if (document.exitFullscreen) {
      document.exitFullscreen();
    } else if (document.msExitFullscreen) {
      document.msExitFullscreen();
    } else if (document.mozCancelFullScreen) {
      document.mozCancelFullScreen();
    } else if (document.webkitExitFullscreen) {
      document.webkitExitFullscreen();
    }
  }
}

var guideLoaded = false;
function startGuide (name) {
	if (guideLoaded) {
		startTour(name);
	}
	else {
	  	loadScript("IDE_Main/TourIDE.js");
		guideLoaded = true;
		setTimeout('startTour("' + name + '")', 100);
	}
}	



