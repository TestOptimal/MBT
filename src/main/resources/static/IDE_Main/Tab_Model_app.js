var curAppState = {};
var parentWinObj = parent;
var scope;

$(document).ready(function() {
	parentWinObj.curAppState.winMgr.addWin("Model", window);
	curAppState = parentWinObj.curAppState;
	
	$(document).keyup(function(event) {
		var moveDelta = 3;
		if (event.ctrlKey) moveDelta = 1;
		if (event.keyCode == 27) { // escape
			if (scope) {
				scope.reset();
				scope.$apply();
			}
		}
		else if (event.keyCode == 46) { // delete key
			scope.deleteMarkedStateTrans();
			event.preventDefault();
			event.stopPropagation();
			if (scope) {
				scope.$apply();
			}
		}
	});
});


MainModule.controller("mainCtrl", function ($scope) {
	$scope.moreMenuID = "";
	$scope.FloatViewID = "";
	$scope.workPanePct = 75;
	$scope.palletVisible = true;
	$scope.FloatViewVisible = false;
	
	$scope.scxmlNode;
	$scope.reqList = parentWinObj.curAppState.reqList;
	$scope.changesPending = false;
	$scope.curStatsExecID = 0;
	
	$scope.curPropNodeData = {};
	$scope.diagram;
	$scope.diagramMode = "edit";
	$scope.stateList = [];
	$scope.sysMsgList = [];
	$scope.searchOpt = {
		uid: false,
		name: true,
		desc: true,
		showState: true,
		showTrans: true
	};
	
	$scope.reqmtViewOpt = {
		syncMode: false
	};
	
	$scope.setCurPropNodeData = function (nodeData_p) {
		$scope.curPropNodeData = {
			curNodeData: nodeData_p,
			changed: false
		}
	}
	
	$scope.saveCurProp = function () {
		$scope.setModelChanged();
		if ($scope.FloatViewID=="CanvasSettingView") {
			$scope.diagram.setCanvasSettings($scope.scxmlNode.miscNode);
		}
		else {
			if ($scope.curPropNodeData.curNodeData.typeCode=="state") {
				if ($scope.curPropNodeData.changed_subModel) {
					$scope.curPropNodeData.curNodeData.childrenStates = [];
					$scope.addMsg({type: "alert", text: "Save the changes and re-open the model to bring in the sub-model."}, true);
					$scope.curPropNodeData.changed_subModel = false;
				}
				if ($scope.curPropNodeData.changed_hideSubstates) {
					$scope.diagram.setShowHideSubstates($scope.curPropNodeData.curNodeData.uid, $scope.curPropNodeData.curNodeData.hideSubStates);
					$scope.curPropNodeData.changed_hideSubstates = false;
				}
			}
			$scope.diagram.updateObjAttr($scope.curPropNodeData.curNodeData.uid);
		}
	}

	$scope.deleteLane = function (lane_p) {
		$scope.curPropNodeData.changed = true;
		var idx = $scope.curPropNodeData.curNodeData.lanes.indexOf(lane_p);
		if (idx >= 0) {
			$scope.curPropNodeData.curNodeData.lanes.splice(idx, 1);
		}
		$scope.diagram.updateObjAttr($scope.curPropNodeData.curNodeData.uid);
	}

	$scope.addLane = function () {
		$scope.curPropNodeData.changed = true;
		$scope.curPropNodeData.curNodeData.lanes.push({name: "lane text", size: 50});
		$scope.diagram.updateObjAttr($scope.curPropNodeData.curNodeData.uid);
	}

	$scope.openView = function (viewID_p, dataObj_p) {
		if (dataObj_p) {
			$scope.setCurPropNodeData(dataObj_p);
		}
		$scope.FloatViewID = viewID_p;
		$scope.FloatViewVisible = true;
	}

	$scope.toggleFloatView = function () {
		$scope.FloatViewVisible = !$scope.FloatViewVisible;
	}

	$scope.openAnnotView = function (curNodeData) {
		if ($scope.setCurPropNodeData(curNodeData)) {
			$("#mdd_editor").val($scope.curPropNodeData.curNodeData.annot);
			$("#mdd_editor").focus();
			$scope.openView ("AnnotView");
		}
	}
	
	$scope.annotSave = function () {
		var mdText = $("#mdd_editor").val();
		$scope.curPropNodeData.curNodeData.annot = mdText;
		$scope.saveCurPro();
	}
	
	$scope.annotCancel = function () {
		$scope.cancelCurPropNodeData();
	}

	
	$scope.toggleMoreMenu = function (menuID_p) {
		if ($scope.moreMenuID==menuID_p) $scope.moreMenuID = "";
		else $scope.moreMenuID = menuID_p;
	}

	$scope.reset = function () {
		$scope.diagram.clearMarked();
		$scope.setMode ('edit');
		$scope.diagram.reset ();
	}
	
	$scope.setMode = function (mode_p) {
		$scope.diagramMode = mode_p;
		$scope.diagram.setMode (mode_p);
		parentWinObj.curAppState.setModelEditorMode(mode_p);
	}

	$scope.genUID = function () {
		var uuid = Math.random().toString(16).slice(2, 10);
		return "U" + uuid;
	}
	
	$scope.randID = function () {
		var uuid = Math.random().toString(16).slice(2, 10);
		return uuid;
	}
	
	// callback from diagram
	$scope.callbackAction = function (action_p, param_p) {
		if (action_p == "addState") {
			var name = "state name";
			var state = jQuery.extend(true, {}, parentWinObj.curAppState.nodeDataList["template.state"]);
			state.uid = $scope.genUID();
			state.stateID = name;
			state.parentuid = param_p.uid;
			state.desc = '';
			state.nodeType = param_p.type.toUpperCase();
			if (state.nodeType == "FINAL") {
				state.isFinal = true;
				state.stateID = "End"
			}
			else if (state.nodeType == "INITIAL") {
				state.isInitial = true;
				state.stateID = "Start";
			}
			state.css = {classes: "name-top"};
			parentWinObj.curAppState.nodeDataList[state.uid] = state;
			setTimeout($scope.forceRefreshStateList, 50);
			return state;
		}
		else if (action_p == "addTrans") {
			var name = "trans name";
			var trans = jQuery.extend(true, {}, parentWinObj.curAppState.nodeDataList["template.transition"]);
			trans.targetUID = param_p.toState.uid;
			trans.parentuid = param_p.fromState.uid;
			trans.event = name;
			trans.desc = '';
			trans.uid = $scope.genUID();
			parentWinObj.curAppState.nodeDataList[trans.uid] = trans;
			setTimeout($scope.forceRefreshStateList, 50);
			return trans;
		}
		else if (action_p == "addBox") {
			var name = "box name";
			if (name != null) {
				var box = jQuery.extend(true, {}, parentWinObj.curAppState.nodeDataList["template.box"]);
				box.uid = $scope.genUID();
				box.name = name;
				box.desc = '';
				box.color = '#F0F0F0';
				box.css = {classes: "name-top"};
				parentWinObj.curAppState.nodeDataList[box.uid] = box;
				return box;
			}
		}
		else if (action_p == "addSwimlaneH") {
			var swimlane = jQuery.extend(true, {}, parentWinObj.curAppState.nodeDataList["template.swimlane"]);
			swimlane.uid = $scope.genUID();
			swimlane.name = "swimlane name";
			swimlane.desc = '';
			swimlane.color = '#dff9fb';
			swimlane.css = {classes: "name-top"};
			swimlane.orient = "horizontal";
			swimlane.lanes = [{name: "lane 1", size: 50}, {name: "lane 2", size: 50}]
			parentWinObj.curAppState.nodeDataList[swimlane.uid] = swimlane;
			return swimlane;
		}
		else if (action_p == "addSwimlaneV") {
			var swimlane = jQuery.extend(true, {}, parentWinObj.curAppState.nodeDataList["template.swimlane"]);
			swimlane.uid = $scope.genUID();
			swimlane.name = "swimlane name";
			swimlane.desc = '';
			swimlane.color = '#dff9fb';
			swimlane.css = {classes: "name-top"};
			swimlane.orient = "vertical";
			swimlane.lanes = [{name: "lane 1", size: 50}, {name: "lane 2", size: 50}]
			parentWinObj.curAppState.nodeDataList[swimlane.uid] = swimlane;
			return swimlane;
		}
		else if (action_p == "needRefresh") {
			$scope.setModelChanged();
			$scope.$apply();
		}
		else if (action_p == "changed") {
			$scope.setModelChanged();
		}
		else if (action_p=="reset") {
			$scope.reset();
			$scope.$apply();
		}
		else if (action_p=="stateCreated") {
			$scope.refreshStateList();
			$scope.$apply();
		}
		else if (action_p=="transCreated") {
			$scope.addMsg({type: "info", text: ""}, true);
			$scope.$apply();
		}
		else if (action_p=="transRerouted") {
			$scope.reset();
			$scope.$apply();
		}
		else if (action_p=="startedLasso") {
			$scope.diagramMode = 'mark';
			$scope.$apply();
		}
		else if (action_p=="transFromSelected") {
			$scope.addMsg({type: "info", text: $scope.gMsgList["hint_addTrans_target"].desc}, true);
			$scope.$apply();
		}
		else if (action_p == "error" || action_p=="alert") {
			$scope.addMsg({type: action_p, text: param_p}, true);
			$scope.$apply();
		}
		else if (action_p == "message") {
			$scope.addMsg(param_p, true);
			$scope.$apply();
		}
		else if (action_p == "dblclickCanvas") {
			$scope.hiLiteStateTrans([]);
			$scope.reset();
			$scope.$apply();
		}
		else if (action_p == "clickObj") {
			$scope.hiLiteStateTrans([]);
			if (param_p.typeCode=="breakpoint") {
				$scope.diagram.removeBreakpoint(param_p.uid);
				$scope.sendBreakpoints();
			}
			else if ($scope.FloatViewVisible) {
				var aViewID = $scope.FloatViewID;
				if ($scope.FloatViewID.indexOf("Prop")>0) {
					if (param_p.typeCode=="state") {
						aViewID = "StateProp";
					}
					else if (param_p.typeCode=="transition") {
						aViewID = "TransProp";
					}
					else if (param_p.typeCode=="box") {
						aViewID = "BoxProp";
					}
					else if (param_p.typeCode=="swimlane") {
						aViewID = "SwimlaneProp";
					}
					$scope.openView(aViewID, param_p);
				}
				$scope.$apply();
			}
		}
		else if (action_p == "dblclickObj") {
			var viewID = "";
			if (param_p.typeCode=="state") viewID = "StateProp";
			else if (param_p.typeCode=="transition") viewID = "TransProp";
			else if (param_p.typeCode=="swimlane") viewID = "SwimlaneProp";
			else if (param_p.typeCode=="box") viewID = "BoxProp";
			if (viewID!="") {
				$scope.openView(viewID, param_p);
				$scope.$apply();
			}
		}
		else if (action_p == "clickGuard") {
			$scope.openView("TransProp", param_p);
			$scope.$apply();
		}
	}
	
	$scope.addMsg = function(msgObj_p) {
		parentWinObj.curAppState.addMsg(msgObj_p);
	}
	
	$scope.addNode = function (mode_p, hintKey_p) {
		$scope.diagram.clearMarked();
		$scope.setMode(mode_p);
		$scope.addMsg({type: "info", text: $scope.gMsgList[hintKey_p].desc});
	}

	$scope.setMarkMode = function () {
		$scope.diagram.clearMarked();
		$scope.setMode("mark");
		$scope.addMsg({type: "info", text: $scope.gMsgList['hint_mark'].desc});
	}
	
	$scope.markAll = function () {
		$scope.setMarkMode();
		var uidList = Stream($scope.stateList).map(function(st) {
			return st.uid;
		}).toArray();
		$scope.diagram.hiLiteObj(uidList, true, "mark", true, "");
	}
	
	$scope.addTrans = function (hintKey_p) {
		$scope.diagram.clearMarked();
		$scope.setMode("addTrans");
		$scope.addMsg({type: "info", text: $scope.gMsgList[hintKey_p].desc}, true);
	}

	$scope.showJson = function () {
		var json = JSON.stringify($scope.scxmlNode, null, 4);
		alert(json);
	}

	
	$scope.refreshStateList = function () {
		$scope.stateList = $scope.makeStateList($scope.scxmlNode.childrenStates);
	}
	

	$scope.showPath = function (uidPath) {
		$scope.diagram.hiLiteObj(uidPath, clear, "trailing", false, "");
	}

	$scope.makeStateList = function (stateList_p) {
		var sList = [].concat(stateList_p);
		for (var i in stateList_p) {
			var state = stateList_p[i];
			var s = state.childrenStates;
			if (s && s.length > 0) {
				sList = sList.concat($scope.makeStateList(s));
			}
			state.transitions.sort(function(t1, t2) {
				var textA = t1.event.toUpperCase();
				var textB = t2.event.toUpperCase();
				return (textA < textB) ? -1 : (textA > textB) ? 1 : 0;
			});
		}
		sList.sort(function(s1,s2){
			var textA = s1.stateID.toUpperCase();
			var textB = s2.stateID.toUpperCase();
			return (textA < textB) ? -1 : (textA > textB) ? 1 : 0;
		})
		return sList;
	}
	
	
	$scope.menuCallback = function(id, event, clickObj){
		if (id=="StateProp" || id=="TransProp" || id=="BoxProp" || id=="SwimlaneProp") {
			$scope.openView(id, clickObj);
			$scope.$apply();
		}
//		else if (id=="StateAnnot" || id=="TransAnnot") {
//			$scope.openView("AnnotView", clickObj);
//			$scope.$apply();
//		}
		else if (id=="StateTrigger") {
			$scope.gotoStateTrigger (clickObj);
		}
		else if (id=="TransTrigger") {
			$scope.gotoTransTrigger (clickObj);
		}
		else if (id=="ReRouteTrans") {
			$scope.setMode("rerouteTrans");
			$scope.diagram.reRouteTrans(clickObj.uid);
			$scope.$apply();
		}
		else if (id=="RefreshTrans") {
			$scope.diagram.refreshTrans(clickObj.uid);
			$scope.$apply();
		}
		else if (id=="RefreshState") {
			$scope.diagram.refreshState(clickObj.uid);
			$scope.$apply();
		}
		else if (id=="DeleteTrans") {
			$scope.diagram.deleteTrans(clickObj.uid);
			$scope.refreshStateList();
			$scope.setModelChanged();
			$scope.$apply();
		}
		else if (id=="DeleteState") {
			$scope.diagram.deleteState(clickObj.uid);
			$scope.refreshStateList();
			$scope.setModelChanged();
			$scope.$apply();
		}
		else if (id=="DeleteSwimlane") {
			$scope.diagram.deleteObj(clickObj.uid);
			$scope.setModelChanged();
		}
		else if (id=="DeleteBox") {
			$scope.diagram.deleteObj(clickObj.uid);
			$scope.setModelChanged();
		}
		else if (id=="StateNeighbors") {
			$scope.diagram.hiLiteStateNeighbors(clickObj.uid);
		}
		else if (id=="OpenSubModel") {
			if (parentWinObj.curAppState.isModelChanged()) {
				parentWinObj.curAppState.addMsg({type: "alert", text: "Please save the model changes."});
				return;
			}
			$scope.addMsg({type: "info", text: "opening submodel ..." + clickObj.subModel}, true);
			parentWinObj.curAppState.winMgr.openWebPage("../IDE_Main.html?model=" + clickObj.subModel); 
		}
		else if (id=="ToggleBreakpoint") {
			$scope.diagram.toggleBreakpoint(clickObj.uid);
			$scope.sendBreakpoints();
		}
		else {
//			alert('menu ' + id);
		}
	}
	
	$scope.sendBreakpoints = function () {
		parentWinObj.curAppState.toWS.wsSendModel('setBreakpoints', 
			{   modelName: parentWinObj.curAppState.scxml.modelName, 
				uidList: $scope.diagram.getBreakpoints()
			});
	}
	

	
	$scope.getStepName = function (stepObj) {
		if (stepObj.stateuid) {
			var stateName = parentWinObj.curAppState.nodeDataList[stepObj.stateuid].stateID;
			var transName = parentWinObj.curAppState.nodeDataList[stepObj.transuid].event;
			return stateName + ":" + transName;
		}
	}
	
	$scope.hiLiteList = function (uidList_p) {
		$scope.diagram.hiLiteObj(uidList_p, true, "mark", true, "");
	}

	$scope.hiLiteStateTrans = function (uid_p) {
		$scope.diagram.hiLiteObj([uid_p], true, "mark", true, "");
	}
	
	$scope.deleteMarkedStateTrans = function () {
		var delUIDs = $scope.diagram.deleteMarked();
		if (delUIDs==undefined || delUIDs.length==0) {
			$scope.addMsg({type: "info", text: "No marked states/transitions"});
		}
		else {
			$scope.refreshStateList();
		}
	}

	$scope.setModelChanged = function () {
		if (!$scope.changesPending) {
			parentWinObj.curAppState.setModelChanged("Model", true);
		}
		$scope.changesPending = true;
	}
	
	$scope.showTestCase = function (tcObj_p) {
		var uidList = Stream(tcObj_p.stepList)
			.map(function(step) {
				return step.UID;
			}).toArray();
		$scope.diagram.hiLiteObj(uidList, true, "mark", false, "");
		$scope.testCaseShown = true;
	}
	
	$scope.showTestStep = function (tcObj_p, step_p) {
		$scope.diagram.hiLiteObj([step_p.UID], $scope.testCaseShown, "mark", false, "");
		$scope.testCaseShown = false;
	}
	$scope.clearMarks = function () {
		$scope.diagram.clearMarked();
		$scope.testCaseShown = false;
	}


	$scope.isMatch = function (nodeData, searchOpt) {
		var searchTextUp = searchOpt.searchText.toUpperCase();
		var matched = false;
		var nodeName = (nodeData.typeCode=='state'? nodeData.stateID: nodeData.event).toUpperCase();
		if (searchOpt.searchText.indexOf("/")==0) {
			var re = new RegExp (searchTextUp.substring(1));
			if (searchOpt.uid && re.test(nodeData.uid) ||
				searchOpt.name && re.test(nodeName) ||
				searchOpt.desc && re.test(nodeData.desc.toUpperCase())) {
				return true;
			}
		}
		else {
			if (searchOpt.uid && nodeData.uid.indexOf(searchOpt.searchText)>=0 ||
				searchOpt.name && nodeName.indexOf(searchTextUp)>=0 ||
				searchOpt.desc && nodeData.desc.toUpperCase().indexOf(searchTextUp) >= 0) {
				return true;
			}
		}
		return false;
	}

	$scope.findStateTrans = function (searchOpt) {
		var resultList = [];
		for (var i in $scope.stateList) {
			var st = $scope.stateList[i];
			if (searchOpt.showState && $scope.isMatch (st, searchOpt)) {
				resultList.push(st);
			}
			
			if (searchOpt.showTrans) {
				for (var j in st.transitions) {
					var trans = st.transitions[j];
					if ($scope.isMatch (trans, searchOpt)) {
						resultList.push(trans);
					}
				}
			}
		}
		return resultList;
	}
	
	$scope.doSearch = function () {
		if ($scope.searchOpt.searchText && $scope.searchOpt.searchText !== "") {
			$scope.searchOpt.resultList = $scope.findStateTrans($scope.searchOpt);
		}
	}
	
	$scope.hiLiteSearch = function () {
		var uidList = [];
		for (var i in $scope.searchOpt.resultList) {
			uidList.push($scope.searchOpt.resultList[i].uid);
		}
		$scope.diagram.hiLiteObj(uidList, true, "mark", true, "");
	}
	
	$scope.hiLiteByTag = function (tag) {
		var regExp = new RegExp(".(assertTrue|assertFailed|addReqPassed|addReqFailed|writeStepAssert)[ ]*\\([ ]*['\"]" + tag + "['\"][ ]*,");
		var uidList = runWinAction("Script", "findUID", regExp);
		$scope.diagram.hiLiteObj(uidList, true, "mark", true, "");
	}
	
	$scope.save = function () {
		var markedUIDList = [];
		if ($scope.diagramMode=="mark") {
			markedUILDList = $scope.diagram.getMarkedUIDs();
		}
		curAppState.scxml.mbtNode.breakpoints = markedUIDList;
		parentWinObj.curAppState.toSvc.ModelSvc.saveModel (parentWinObj.curAppState.scxml.modelName, curAppState.scxml, function(data) {
			$scope.changesPending = false;
			parentWinObj.curAppState.setModelChanged("Model", false);
		});
	}
	
	$scope.isSubModelState = function (stateUID) {
		return (parentWinObj.curAppState.nodeDataList[stateUID].subModel); //parentWinObj.curAppState.nodeDataList[stateUID].subModel.length>0);
	}
	
	$scope.getFinalStates = function (stateUID) {
		if (parentWinObj.curAppState.nodeDataList[stateUID].childrenStates) {
			return parentWinObj.curAppState.nodeDataList[stateUID].childrenStates
			.filter(function(childState) {
				return childState.isFinal;
			});
		}
	}
	
	$scope.gotoStateTrigger = function (stateObj) {
		parentWinObj.curAppState.selectTab("Script");
		runWinAction("Script", "gotoTrigger", {uid: stateObj.uid, name: stateObj.stateID});
	}
	
	$scope.gotoTransTrigger = function (transObj) {
		parentWinObj.curAppState.selectTab("Script");
		var stateObj = parentWinObj.curAppState.nodeDataList[transObj.parentuid];
		runWinAction("Script", "gotoTrigger", {uid: transObj.uid, name: stateObj.stateID + ": " + transObj.event});
	}

	$scope.showCoverage = function () {
		$scope.curMbtSessID;
		parentWinObj.curAppState.toSvc.StatsSvc.getModelExec (parentWinObj.curAppState.scxml.modelName, $scope.curMbtSessID,
			function(execStatsDetails) {
				if (execStatsDetails==="") {
					$scope.addMsg({type: "alert", text: "No current execution stats found."});
					return;
				}
				var uidListCovered = Stream(execStatsDetails.stateTransList)
					.filter (function (s) { return s.failCount == 0 && s.passCount >= s.minTravRequired})
					.map(function (s) { return s.UID; }).toArray();

				var uidListPartial = Stream(execStatsDetails.stateTransList)
					.filter (function (s) { return s.failCount == 0 && s.passCount > 0 && s.passCount < s.minTravRequired})
					.map(function (s) { return s.UID; }).toArray();
				
				var uidListFailed = Stream(execStatsDetails.stateTransList)
					.filter (function (s) { return s.failCount > 0})
					.map(function (s) { return s.UID; }).toArray();

				$scope.diagram.hiLiteObj(uidListCovered, true, "GREEN", true, "");
				$scope.diagram.hiLiteObj(uidListPartial, false, "PALEGREEN", true, "");
				$scope.diagram.hiLiteObj(uidListFailed, false, "ORANGE", true, "");
				setTimeout (function() { parentWinObj.curAppState.selectTab("Model");}, 50);
			});
	}

	$scope.init = function () {
		scope = $scope;
		parentWinObj.curAppState.toWS.wsSubscribe('model.started', function (packet) {
			parentWinObj.curAppState.addMsg({type: "alert", text: "Model execution started", source: "ModelEditor"});
        	parentWinObj.curAppState.modelState = JSON.parse(packet.body);
        	$scope.$apply();
		}, "ModelEditor");

		parentWinObj.curAppState.toWS.wsSubscribe('model.paused', function (packet) {
			
    		if (packet.body.indexOf("{")>=0) {
				pausedAt = JSON.parse(packet.body);
	    		$scope.diagram.hiLiteObj([pausedAt.uid], true, "pausedAt", true, "*");
    		}
    		else {
	    		$scope.diagram.hiLiteObj([], true, "pausedAt", true, "*");
			}
		}, "ModelEditor");

		parentWinObj.curAppState.toWS.wsSubscribe('model.ended', function (packet) {
			parentWinObj.curAppState.addMsg({type: "alert", text: "Model execution ended", source: "ModelEditor"});
        	parentWinObj.curAppState.modelState = JSON.parse(packet.body);
        	$scope.$apply();
		}, "ModelEditor");

		parentWinObj.curAppState.toWS.wsSubscribe('model.state', function (packet) {
        	parentWinObj.curAppState.modelState = JSON.parse(packet.body);
        	$scope.$apply();
		}, "ModelEditor");

    	$scope.scxmlNode = parentWinObj.curAppState.scxml;
		$scope.diagram = JSDiagram.getInstance();

		$scope.gMsgList = parentWinObj.gMsgList;
		$scope.subModelList = parent.curAppState.toSvc.FileSvc.getSubmodelList(function(subList) {
			$scope.subModelList = Stream(subList).filter(function(m) { return m!=$scope.scxmlNode.modelName;}).toArray();
		});
		
		$scope.isEnterprise = parentWinObj.curAppState.config.realprodlevel=="Enterprise";

		$scope.refreshStateList($scope.scxmlNode.childrenStates);

		$scope.diagram.init($scope.scxmlNode.miscNode, $scope.callbackAction, "../img/to.png", 
			"stateMenu", "transMenu", "boxMenu", "swimlaneMenu", $scope.menuCallback);

		$scope.diagram.addModel($scope.scxmlNode);
		$scope.diagram.setMode("edit");
		$scope.diagram.reset();
		parentWinObj.curAppState.toWS.wsSendModel("getModelState", $scope.scxmlNode.modelName);
		parentWinObj.curAppState.winMgr.regEvent("showCoverage", $scope.showCoverage);
		parentWinObj.curAppState.winMgr.regEvent("refreshExec", function(modelName, mbtSessID) {
			$scope.curMbtSessID = mbtSessID;
		});
	}
	
	$scope.init();
});

MainModule.directive("formOnChange", function($parse, $interpolate){
  return {
	    require: "form",
	    link: function(scope, element, attrs, form){
	      var cb = $parse(attrs.formOnChange);
	      element.on("change", function(){
	        cb(scope);
	      });
	    }
	  };
	});
	
function runWinAction (winID_p, action_p, params_p) {
	return parentWinObj.curAppState.winMgr.runWinAction(winID_p, action_p, params_p);
}

// callback from IDE_Main
function mainCallbackFunc(action_p, params_p) {
	if (action_p == "getAllMarkedUID") {
		if (scope.diagramMode=="mark") {
			return scope.diagram.getMarkedUIDs();
		}
		else return [];
	}
	else if (action_p == "getBreakpoints") {
		return scope.diagram.getBreakpoints();
	}
	else if (action_p == "save") {
		scope.save();
	}
	else if (action_p == "highLightState" || action_p == "highLightTrans") {
		scope.hiLiteStateTrans(params_p);
	}
	else if (action_p=="openReqView") {
		scope.openView("ReqmtView");
		scope.reqList = parentWinObj.curAppState.reqList;
		scope.$apply();
	}
	else if (action_p=="modelEditorMode") {
		return scope.diagramMode;
	}
}

