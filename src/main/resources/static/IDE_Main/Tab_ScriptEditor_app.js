// copyright 2008 - 2020, TestOptimal, LLC, all rights reserved.
// Tab_ScriptEditor.js
//
// Ctrl-Space/Cmd-Space: code assist. Enter $
// Ctrl-F / Cmd-F: Start searching
// Ctrl-G / Cmd-G: Find next
// Shift-Ctrl-G / Shift-Cmd-G: Find previous
// Shift-Ctrl-F / Cmd-Option-F: Replace
// Shift-Ctrl-R / Shift-Cmd-Option-F:  Replace all
// Alt-G: Jump to line 
// Ctrl-/ / Cmd-/: toggle comment
// Ctrl-R: requirement list or show req details if text selected.
// Ctrl-I: add a State/transition trigger
// Ctrl-Z: undo
// Ctrl-Y: redo
// Shift-Ctrl-Z: redo


var editor = null;
var parentWinObj = parent;
var changesNotified = false;
var MScriptHintList = {};

parentWinObj.curAppState.winMgr.addWin("Script", window);

$(document).ready(function() {
	window.onresize = adjustHeight;
});

function adjustHeight() {
	var editorHeight = $(window).height() - 25;
	$(".CodeMirror-scroll").css("height", editorHeight).css("width", $(window).width());
}


function getStateTransList () {
	var stateList = parentWinObj.curAppState.scxml.childrenStates;
	retList = [
		{displayText: "MBT_START", text: "\n@TRIGGER('MBT_START')\ndef 'MBT_START' () {\n\n}\n"},
		{displayText: "MBT_END", text: "\n@TRIGGER('MBT_END')\ndef 'MBT_END' () {\n\n}\n"},
		{displayText: "ALL_STATES", text: "\n@TRIGGER('ALL_STATES')\ndef 'ALL_STATES' () {\n\n}\n"},
		{displayText: "ALL_TRANS", text: "\n@TRIGGER('ALL_TRANS')\ndef 'ALL_TRANS' () {\n\n}\n"},
		{displayText: "MBT_FAIL", text: "\n@TRIGGER('MBT_FAIL')\ndef 'MBT_FAIL' () {\n\n}\n"},
		{displayText: "MBT_ERROR", text: "\n@TRIGGER('MBT_ERROR')\ndef 'MBT_ERROR' () {\n\n}\n"},
	];
	for ( var i in stateList) {
		var state = stateList[i];
		retList.push ({ displayText: state.stateID, 
			text: "\n@TRIGGER('" + state.uid + "')\ndef '" + state.stateID + "' () {\n\n}\n", 
			className: "caItem"});
		for (var j in state.transitions) {
			var trans = state.transitions[j];
			retList.push ({ displayText: " - " + trans.event, 
				text: "\n@TRIGGER('" + trans.uid + "')\ndef '" + state.stateID + ": " + trans.event + "'() {\n\n}\n", 
				className: "caItem"});
		}
	}
	return retList;
}


function execExpr (cm) {
	parentWinObj.curAppState.openDialog('Monitor');
	parentWinObj.curAppState.debug.curExpr.script = cm.getSelection();
}

// show requirement description if selected tag, otherwise show hint of req.
function showReqList (cm) {
	var selectRange = { from: cm.getCursor(true), to: cm.getCursor(false) }
	var cursor = cm.getCursor(), lineText = cm.getLine(cursor.line);
	var tagList = Stream(parentWinObj.curAppState.reqList).map(function(tagObj) {
			return { displayText: (tagObj.priority||'?').substring(0,1).toUpperCase() +": " + tagObj.name + " - " + tagObj.desc, text: tagObj.name, className: "caItem"};
		}).toArray();	
	if (selectRange.from.ch == selectRange.to.ch) {
//		var cursor = cm.getCursor();
		var hOpt = { line: cursor.line, replaceStart: cursor.ch, replaceEnd: cursor.ch };
		while (hOpt.replaceStart && /\w/.test(lineText.charAt(hOpt.replaceStart - 1))) --hOpt.replaceStart;
		while (hOpt.replaceEnd < lineText.length && /\w/.test(lineText.charAt(hOpt.replaceEnd))) ++hOpt.replaceEnd;
		hOpt.filterWord = lineText.slice(hOpt.replaceStart, cursor.ch);
		hOpt.hintList = tagList;
		cm.setOption("hintOptions", hOpt);
		CodeMirror.showHint(cm, MScriptHint);
	}
	else {
		var tag = lineText.slice(selectRange.from.ch, selectRange.to.ch);
		for (var i in tagList) {
			var tagObj = tagList[i];
			if (tagObj.text==tag) {
				alertDialog (tagObj.displayText);
			}
		}
	}
}


function showStateTransList (cm) {
	var cursor = cm.getCursor(), lineText = cm.getLine(cursor.line);
	var stateList = getStateTransList();
	var hOpt = { line: cursor.line, replaceStart: 0, replaceEnd: 1 };
	hOpt.filterWord = "";
	hOpt.hintList = stateList;
	cm.setOption("hintOptions", hOpt);
	CodeMirror.showHint(cm, MScriptHint);
}

function alertDialog(msg) {
	parentWinObj.alertDialog(msg);
}
	
MainModule.controller('mainCtrl', function ($scope) {
	$scope.scriptList = [];
	$scope.scriptIndentNum = 3;
	$scope.curScriptName;
	$scope.curScriptDisplayed;
	$scope.viewDataList = [];
	$scope.reservedScript = ["TRIGGERS","PAGES","STEPS","MCASES"];
	
	$scope.init = function () {
		if (parentWinObj.curAppState.scxml.modelName) {
    		$scope.loadScriptList();
    		$scope.loadCAList();
    	}
	}
	
	$scope.loadCAList = function () {
		parentWinObj.curAppState.toSvc.SysSvc.getCA (function(caList) {
			MScriptHintList = caList;
			for (var k in MScriptHintList) {
				var list = MScriptHintList[k];
				for (var d in list) {
					list[d].className = "caItem";
				}
			}
		});
	}
	
	$scope.loadScriptList = function () {
		parentWinObj.curAppState.toSvc.ModelSvc.getScript(parentWinObj.curAppState.scxml.modelName, function(scriptList_p) {
			$scope.changesNotified = false;
			$scope.scriptList = scriptList_p;
			angular.forEach($scope.scriptList, function(scriptObj) {
				scriptObj.reserved = $scope.reservedScript.indexOf(scriptObj.scriptName)>=0;
			});
			$scope.selectScript($scope.scriptList[0]);
			parentWinObj.curAppState.scriptList = $scope.scriptList;
			$scope.$apply();
		});
	}
	
	$scope.addScript = function () {
		var scriptObj = {
			modelName: parentWinObj.curAppState.scxml.modelName,
			scriptName: "SCRIPT_" + $scope.scriptList.length,
			title: "User script",
			loadSeqNum: $scope.scriptList.length + 1,
			reserved: false,
			script: "// Script\n"
		}
		$scope.scriptList.push(scriptObj);
		$scope.selectScript(scriptObj);
	}
	
	$scope.removeScript = function (scriptObj) {
		var elem = $scope.editor.getWrapperElement();
		elem.parentNode.removeChild(elem);
		var idx = $scope.scriptList.indexOf(scriptObj);
		$scope.scriptList.splice(idx,1);
		$scope.editor = undefined;
		$scope.selectScript($scope.scriptList[0]);
		$scope.setChanged();
	}

	$scope.setChanged = function () {
		if (!$scope.changesNotified) {
			$scope.changesNotified = true;
			parentWinObj.curAppState.setModelChanged("Script", true);
		}
	}
	
	$scope.selectScript = function (scriptObj) {
		if (scriptObj.readonly) return;
		
		if ($scope.editor) {
			$scope.curScript.script = $scope.editor.getValue();
			var edElem = $scope.editor.getWrapperElement();
			edElem.parentNode.removeChild(edElem);
		}
		$scope.viewVisible = false;
		$scope.curScript = scriptObj;
		$scope.editor = initEditor (scriptObj.script, 
			function(cm) { // change handler
				$scope.setChanged();
			}, function(cm) { // focus handler
				$scope.viewVisible = false;
			}, $scope.scriptIndentNum, parentWinObj.curAppState.fileSave);
		$scope.editor.setOption("extraKeys", {
			"Cmd-Space": showHintGroovy, 
			"Ctrl-Space": showHintGroovy,
			"Ctrl-/": toggleComment,
			"Cmd-/": toggleComment,
			"Ctrl-I": showStateTransList,
			"Cmd-I": showStateTransList,
			"Ctrl-R": showReqList,
			"Cmd-R": showReqList,
			"Ctrl-E": execExpr,
			"Cmd-E": execExpr
		});
		adjustHeight();
	}
	
	$scope.toggleView = function () {
		$scope.viewVisible = !$scope.viewVisible;
		if ($scope.viewVisible) {
			if ($scope.curScript.scriptName=="TRIGGERS") {
				$scope.viewDataList = parseTriggerScript($scope.curScript.script.split("\n"));
				$scope.viewVisible = true;
			}
			else {
				$scope.viewDataList = [];
				$scope.viewVisible = false;
			}
		}
	}

	$scope.findLine = function (levelObj) {
	    var t = $scope.editor.charCoords({line: levelObj.lineIdx, ch: 0}, "local").top; 
	    var middleHeight = $scope.editor.getScrollerElement().offsetHeight / 2; 
	    $scope.editor.scrollTo(null, t - middleHeight - 5); 
	    $scope.editor.setCursor(levelObj.lineIdx, 0);
	}

	$scope.scrollToTrigger = function (uid_p) {
		var cursor = $scope.editor.getSearchCursor(uid_p);
    	if (cursor.findNext()) {
			$scope.editor.setCursor(cursor.from(), 0);
			return true;
		}
		else {
			return false;
		}
	}	
	
	$scope.findTriggerUID = function (regExp) {
		var splitRegExp = /@TRIGGER[ ]*\([ ]*['"]/;
		var retUIDList = Stream($scope.scriptList).filter(function(s) {
				return s.scriptName=="TRIGGERS";
			}).flatMap(function(s) {
				return Stream(s.script.split(splitRegExp))
					.filter(function(line) {
						return regExp.exec(line);
					})
					.map(function(line) {
						return line.split(/['"]/)[0];
					}).toArray();
			}).toArray();
		return retUIDList;
	}
	
	$scope.save = function () {
		$scope.curScript.script = $scope.editor.getValue();
		parentWinObj.curAppState.toSvc.ModelSvc.saveScript(parentWinObj.curAppState.scxml.modelName, $scope.scriptList, function(data) {
			parentWinObj.curAppState.setModelChanged("Script", false);
			$scope.changesNotified = false;
		});
	}
	
	$scope.renameScript = function (scriptObj) {
		parentWinObj.promptDialog("Enter new script name (uppercase)", scriptObj.scriptName, function() {
				var newName = parentWinObj.getPromptVal().toUpperCase();
				if (newName.length>3) {
					scriptObj.scriptName = newName;
					$scope.setChanged();
					$scope.$apply();
				}
			});
	}
	  
	$scope.toggleCommentScript = function () {
	    toggleComment($scope.editor);
	}      

	$scope.editorCmd = function (cmd) {
	    $scope.editor.execCommand(cmd);
	}

	$scope.addMsg = function(msgObj_p) {
		parentWinObj.curAppState.addMsg(msgObj_p);
	}

	setTimeout($scope.init, 10);
});


//callback from IDE_Main
function mainCallbackFunc(action_p, params_p) {
	var scope = $("body").scope();
	if (scope) {
		if (action_p=="save") {
			scope.save();
			scope.$apply();
		}
		else if (action_p=="findUID") {
			return scope.findTriggerUID(params_p);
		}
		else if (action_p=="gotoTrigger") {
			if (!scope.scrollToTrigger(params_p.uid)) {
				parentWinObj.confirmDialog("Trigger for " + params_p.name + " not found, do you wish to create it?", function() {
					var scriptText = "\n@TRIGGER('" + params_p.uid + "')\ndef '" + params_p.name + "'() {\n\n}\n";
					scope.editor.replaceRange(scriptText, {line: 99999999});
					scope.scrollToTrigger(params_p.uid);
				});
			}
		}
	}
}
