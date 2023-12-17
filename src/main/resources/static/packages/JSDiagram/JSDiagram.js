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

var JSDiagram = (function () {
	var diagram;
	$(document).keyup(function(event) {
		var moveDelta = 3;
		if (event.ctrlKey) moveDelta = 1;
		if (event.keyCode == 39) { // right arrow
			diagram.moveMarkedStates ({deltaX: moveDelta, deltaY: 0}, true);
			event.preventDefault();
			event.stopPropagation();
		}
		else if (event.keyCode == 37) { // left arrow
			diagram.moveMarkedStates ({deltaX: -moveDelta, deltaY: 0}, true);
			event.preventDefault();
			event.stopPropagation();
		}
		else if (event.keyCode == 38) { // up arrow
			diagram.moveMarkedStates ({deltaX: 0, deltaY: -moveDelta}, true);
			event.preventDefault();
			event.stopPropagation();
		}
		else if (event.keyCode == 40) { // down arrow
			diagram.moveMarkedStates ({deltaX: 0, deltaY: moveDelta}, true);
			event.preventDefault();
			event.stopPropagation();
		}
	});
	
	function create () {
		var scxml;
	  	var objMap = {};
	  	var canvasID = "canvas";
		var mode = "edit"; // edit,mark, addState-state/initial/final/switch/branch/syncbarV/syncbarH, addTrans, addBox, addSwimlaneH, addSwimlaneV, rerouteTrans
		var canvasElem;
		var menuMgr;
		var resizeMgr;
		var moveMgr;
		var canvasTracker;
		var transBuilder;
		var lassoMgr;
		var mouseDownPos;
		var hiLiter;
		
		// stores last trans clicked, used for rerouteTrans mode
		var clickedTransWrapper;
		
		var actionCallback;
		var cursorList = {
				"edit": "default",
				"mark": "pointer",
				"addState-state": "copy",
				"addState-initial": "copy",
				"addState-final": "copy",
				"addState-switch": "copy",
				"addState-branch": "copy",
				"addState-syncbarH": "copy",
				"addState-syncbarV": "copy",
				"rerouteTrans": "alias",
				"addTrans": "alias",
				"addBox": "copy",
				"addSwimlaneH": "copy",
				"addSwimlaneV": "copy"
		}
	
		function setChanged () {
			sendAction("changed");
		}
		
		function setCanvasSettings (misc_p) {
			canvasElem.css({width: misc_p.canvasWidth, height: misc_p.canvasHeight});
			canvasTracker.resetSize({width: misc_p.canvasWidth, height: misc_p.canvasHeight});
		}
		
		// msgObj: {type: '', text: ''}
		function displayMsg (msgObj_p) {
			actionCallback.apply(null, ["message", msgObj_p]);
		}
		
		function sendAction (action_p, param_p) {
			return actionCallback.apply(null, [action_p, param_p]);
		}
		
		function init (misc_p, actionCallback_p, menuIcon_p, stateMenuID_p, transMenuID_p, boxMenuID_p, swimlaneMenuID_p, menuCallback_p) {
			actionCallback = actionCallback_p;
			canvasElem = $("#canvas").css({width: misc_p.canvasWidth, height: misc_p.canvasHeight});
			canvasElem.addClass("JSD-canvas");
			canvasTracker = new CanvasBackdrop(canvasElem, {width: misc_p.canvasWidth, height: misc_p.canvasHeight});
			transBuilder = new TransBuilder();
			transBuilder.init(canvasElem);
			lassoMgr = new LassoMgr(diagram, canvasElem, canvasTracker);
			menuMgr = new CircleMenu (canvasElem, menuCallback_p, menuIcon_p);
			resizeMgr = new Resizer (diagram, canvasElem, canvasTracker);
			moveMgr = new JSDMover (diagram, canvasElem, canvasTracker);
			hiLiter = new JSDHiLiter (diagram, canvasElem, canvasTracker);

  			canvasElem.on("mouseover", function(event) {
  				event.preventDefault();
  				event.stopPropagation();
  				var uid = event.target.id;
  				if (uid==canvasID) {
  					menuMgr.close();
  					resizeMgr.close();
  					moveMgr.close();
					$(".JSDHover").removeClass("JSDHover");
  					return;
  				}
  			});
  			
			canvasElem.on("mouseout", function(event) {
		  		event.preventDefault();
				event.stopPropagation();
				if ($(event.target).prop("id")=="canvas") {
					
				}
			}).on("mousedown", function(event) {
		  		event.preventDefault();
				event.stopPropagation();
				if ($(event.target).prop("id")=="canvas" ||
						$(event.target).hasClass("JSD-box") ||
						$(event.target).hasClass("JSD-swimlane-lane")) {
					mousedownObj (event);
				}
			}).on("mouseup", function(event) {
		  		event.preventDefault();
				event.stopPropagation();
				if ($(event.target).prop("id")=="canvas" ||
						$(event.target).hasClass("JSD-box") ||
						$(event.target).hasClass("JSD-swimlane-lane")) {
					mouseupObj (event);
				}
			}).on("mousemove", function(event) {
		  		event.preventDefault();
				event.stopPropagation();
				if ($(event.target).prop("id")=="canvas" ||
					$(event.target).hasClass("JSD-box") ||
					$(event.target).hasClass("JSD-swimlane-lane")) {
					mousemoveObj (event, "canvas");
				}
			});
			
			canvasElem.on ("dblclick", function(event) {
				sendAction("dblclickCanvas");
			});
			
			canvasElem.on ("click", function(event) {
  		  		event.preventDefault();
  				event.stopPropagation();
				var elemID = $(event.target).prop("id");
				if (elemID!="canvas") {
					return;
				}
				
//				sendAction("clickCanvas");
				
				var scrollOffset = canvasElem.offset();
				var pos = { left: event.pageX - scrollOffset.left, top: event.pageY - scrollOffset.top};
				
				if (mode == "addBox") {
					pos.width = 100;
					pos.height = 75;
					newBox (pos);
					setChanged();
  				}
				else if (mode == "addSwimlaneH" || mode == "addSwimlaneV") {
					pos.width = 100;
					pos.height = 175;
					pos.offset = 0;
					newSwimlane (mode, pos);
					setChanged();
  				}
				else if (mode.indexOf("addState")==0) {
					newState (undefined, pos, mode.substring(9));
  					sendAction("stateCreated");
					setChanged();
  				}
				else if (mode == "mark") {
//  					hiLiter.clear();
  				}
  			});
		}
		
		function mousedownObj (event_p) {
			mouseDownPos = {
				left: event_p.pageX,
				top: event_p.pageY
			}
		}

		function mouseupObj (event_p) {
			mouseDownPos = undefined;
		}

		function mousemoveObj (event_p, uid_p) {
//			if (mode=="mark" && mouseDownPos &&
			if (mouseDownPos &&
				(Math.abs(mouseDownPos.left - event_p.pageX) > 2 ||
				 Math.abs(mouseDownPos.top - event_p.pageY) > 2)) {
				mouseDownPos = undefined;
				var sList = [], newList=[];
				if (uid_p=="canvas") {
					sList = scxml.childrenStates;
				}
				else {
					var wrapper = objMap[uid_p];
					if (wrapper && wrapper.objData.typeCode=="state") {
						sList = wrapper.objData.childrenStates;
					}
					else {
						sList = scxml.childrenStates;
					}
				}
				for (var i in sList) {
					newList.push(objMap[sList[i].uid]);
				}
				if (newList.length>0) {
					lassoMgr.show(newList, event_p);
			  		canvasElem.removeClass(mode);
					mode = 'mark';
			  		canvasElem.addClass(mode);
			  		canvasElem.css({cursor: cursorList[mode]});
			  		sendAction('startedLasso');
				}
			}
		}
		
		function mouseoverObj (objData_p, event_p) {
			if (objData_p.uid==undefined) {
				objData_p = objMap[objData_p].objData;
			}
			var objType = objData_p.typeCode;
			$(".JSDHover").removeClass("JSDHover");
			if (mode=="edit") {
				resetMode();
				
//				if (objData_p.readOnly) return;
				
				var wrapper = objMap[objData_p.uid];
				if (objType=="swimlane") {
					menuMgr.show("swimlaneMenu", 100, wrapper);
					if (!objData_p.readOnly) {
						moveMgr.show(wrapper, event_p);
					}
				}
				else if (objType == "box") {
					menuMgr.show("boxMenu", 100, wrapper);
					if (!objData_p.readOnly) {
						moveMgr.show(wrapper, event_p);
						resizeMgr.show(wrapper);
					}
				}
				else if (objType=="state") {
					menuMgr.show("stateMenu", 100, wrapper);
					if (!objData_p.readOnly) {
						moveMgr.show(wrapper, event_p);
						resizeMgr.show(wrapper);
					}
					wrapper.addClass("JSDHover");
				}
				else if (objType=="transition") {
					var targetElem = $(event_p.target);
					if (!objData_p.readOnly) {
						moveMgr.show(wrapper, event_p);
					}
					if (!targetElem.hasClass("JSD-trans-seg")) {
						menuMgr.show("transMenu", 100, wrapper);
						if (!objData_p.readOnly) {
							resizeMgr.show(wrapper);
						}
					}
					wrapper.addClass("JSDHover");
				}
			}
		}

		function clickObj (objData_p, event_p) {
			var objType = objData_p.typeCode;
			var scrollOffset = canvasElem.offset();
			var pos = 
				{ 
					left: event_p.pageX - scrollOffset.left, 
					top: event_p.pageY - scrollOffset.top
				}
			if (mode == "edit") {
				if (objData_p.readOnly) {
					displayMsg({type: "info", text: "ReadOnly " + objData_p.typeCode});
					return;
				}
				var wrapper = objMap[objData_p.uid];
				sendAction("clickObj", wrapper.objData);
			}
			else if (mode == "mark") {
				if (objData_p.readOnly) {
					displayMsg({type: "info", text: "ReadOnly " + objData_p.typeCode});
					return;
				}
				var wrapper = objMap[objData_p.uid];
				if (wrapper && (objData_p.typeCode=="state" || objData_p.typeCode=="transition")) {
					hiLiter.toggleHiLite(wrapper, "JSDHiLite-mark");
				}
			}
			else if (mode.indexOf("addState")==0) {
				if (objType=="swimlane" || objType=="box") {
					newState (undefined, pos, mode.substring(9));
					setChanged();
				}
			}
			else if (mode == "addTrans" || mode == "rerouteTrans") {
				var stateWrapper = objMap[objData_p.uid];
				if (stateWrapper.objData.typeCode!="state") {
					return;
				}

				var clickInfo = transBuilder.clickState(stateWrapper, event_p, pos);
				if (clickInfo.toStateWrapper) {
  					var posInfo = routeTrans(clickInfo.fromStateWrapper.state, clickInfo.fromSideOffset.sideID, clickInfo.fromSideOffset.offset, 
  							clickInfo.toStateWrapper.state, clickInfo.toSideOffset.sideID, clickInfo.toSideOffset.offset);
	  				var transInfo = {
	  						fromState: clickInfo.fromStateWrapper.state,
	  						toState: clickInfo.toStateWrapper.state,
	  						posInfo: posInfo
	  					}
	  				
	  				var trans;
	  				if (mode == "addTrans") {
	  					// creates new transition to the model
	  					trans = sendAction("addTrans", transInfo);
	  				}
	  				else if (mode == "rerouteTrans" && clickedTransWrapper) {
	  					trans = clickedTransWrapper.trans;
	  					trans.targetUID = transInfo.toState.uid;
	  					trans.parentuid = transInfo.fromState.uid;
	  					deleteTrans(trans.uid);
	  				}
  					if (trans) {
  						trans.posInfo = transInfo.posInfo;
  						var fromStateWrapper = objMap[transInfo.fromState.uid];
  						fromStateWrapper.state.transitions.push(trans);
  						addTrans(fromStateWrapper.getPUID(), fromStateWrapper, trans);
	  					transBuilder.reset();
	  					if (mode == "addTrans") {
		  					sendAction("transCreated");
	  					}
	  					else {
		  					sendAction("transRerouted");
	  					}
  					}
					setChanged();
				}
				else if (clickInfo.fromStateWrapper) {
					sendAction("transFromSelected", clickInfo.fromStateWrapper.objData);
				}
				else if (clickInfo){
					sendAction("alert", clickInfo);
				}
			}
		}

		function doubleClickObj (objData_p) {
			var objType = objData_p.typeCode;
			if (mode == "edit") {
				if (objData_p.readOnly) {
					displayMsg({type: "info", text: "ReadOnly " + objData_p.typeCode});
					return;
				}
				reset();
				var wrapper = objMap[objData_p.uid];
				sendAction("dblclickObj", wrapper.objData);
			}
		}

		function reset () {
			resetMode();
			hiLiter.clear();
		}
		
		function resetMode () {
			resetElem();
			menuMgr.close();
			resizeMgr.close();
			moveMgr.close();
			canvasTracker.stopTrack();
			lassoMgr.close();
		}
		
		function resetElem () {
			$(".JSDGhost-show").removeClass("JSDGhost-show");
			$(".selected").removeClass("selected");
			canvasTracker.stopTrack();
		}
		

	  	function setMode (mode_p) {
	  		resetElem();
	  		canvasElem.removeClass(mode);
	  		mode = mode_p;
	  		canvasElem.addClass(mode_p);
	  		canvasElem.css({cursor: cursorList[mode_p]});
	  		transBuilder.reset();
	  	}

	  	function getMode () {
	  		return mode;
	  	}

		function addModel(scxml_p) {
			scxml = scxml_p;
			regStateMap (canvasID, null, scxml_p.childrenStates);
			regTransMap (canvasID, scxml_p.childrenStates);
			regBoxMap (canvasID, scxml_p.miscNode.boxes);
			regSwimlaneMap (canvasID, scxml_p.miscNode.swimlanes);
			for (var id in objMap) {
				var stateWrapper = objMap[id];
				if (stateWrapper.objData.hideSubstates) {
					stateWrapper.updSubstatesVisibility();
				}
			}
		}
	
		function regStateMap (pUID_p, stateWrapper_p, stateList_p) {
			for (var i in stateList_p) {
				var state = stateList_p[i];
				// backward compatible
				if (state.position==undefined) {
					state.position = {
						left: state.left,
						top: state.top,
						width: state.width,
						height: state.height,
						nameOffset: 5
					}
					state.css = {};
				}
				if (state.isInitial) {
					state.nodeType = "INITIAL";
				}
				else if (state.isFinal) {
					state.nodeType = "FINAL";
				}
				else if (state.nodeType==undefined || state.nodeType=="") {
					state.nodeType = "STATE";
				}
				var stateWrapper = new JSDState(diagram, pUID_p, stateWrapper_p, state);
				objMap[state.uid] = stateWrapper;
				if (state.childrenStates) {
					regStateMap(state.uid, stateWrapper, state.childrenStates);
				}
			}
		}
	
		// create new state to the model
		function newState (parentStateWrap_p, pos_p, type_p) {
			var parentUID = undefined;
			if (parentStateWrap_p) {
				parentUID = parentStateWrap_p.objData.uid;
			}
			var state = sendAction("addState", {type: type_p, uid: parentUID});
			if (state) {
				state.position = pos_p;
				state.position.width = 100;
				state.position.height = 75;
				if (mode == "addState-syncbarH") {
					state.position.height = 10;
  				}
				else if (mode == "addState-syncbarV") {
					state.position.width = 10;
				}
				else if (mode == "addState-initial") {
					state.position.width = 75;
					state.position.height = 50;
					state.position.nameOffset = 15;
				}
				else if (mode == "addState-final") {
					state.position.width = 75;
					state.position.height = 50;
					state.position.nameOffset = 10;
				}

				if (state.nodeType=="BRANCH") {
					state.position.nameOffset = Math.round((state.position.height - 25)/2);
				}
				var pUID = canvasID;
				if (parentStateWrap_p) {
					pUID = parentStateWrap_p.state.uid;
					if (parentStateWrap_p.state.childrenStates==undefined) {
						parentStateWrap_p.state.childrenStates = [];
					}
					parentStateWrap_p.state.childrenStates.push(state);
				}
				else {
					scxml.childrenStates.push(state);
				}
				var stateWrapper = new JSDState(diagram, pUID, parentStateWrap_p, state);
				objMap[state.uid] = stateWrapper;
			}
		}

		// add trans to canvas
		function addTrans (containerID_p, fromStateWrapper_p, trans_p) {
			var toStateWrapper = objMap[trans_p.targetUID];
			var transWrapper = new JSDTrans(diagram, containerID_p, fromStateWrapper_p, toStateWrapper, trans_p);
			objMap[trans_p.uid] = transWrapper;
			fromStateWrapper_p.addOutTrans(transWrapper);
			toStateWrapper.addInTrans(transWrapper);
		}
		
		function regBoxMap (pUID_p, boxList_p) {
			for (var i in boxList_p) {
				var box = boxList_p[i];
				var boxWrapper = new JSDBox(diagram, pUID_p, box);
				objMap[box.uid] = boxWrapper;
			}
		}
	
		function regSwimlaneMap (pUID_p, swimlaneList_p) {
			for (var i in swimlaneList_p) {
				var swimlane = swimlaneList_p[i];
				var swimlaneWrapper = new JSDSwimlane(diagram, pUID_p, swimlane);
				objMap[swimlane.uid] = swimlaneWrapper;
			}
		}

		// add box to the model and canvas
		function newBox (pos_p) {
			var box = sendAction("addBox");
			if (box) {
				box.position = pos_p;
				var pUID = canvasID;
				scxml.miscNode.boxes.push(box);
				var boxWrapper = new JSDBox(diagram, canvasID, box);
				objMap[box.uid] = boxWrapper;
			}
		}

		// add swimlane to the model and canvas
		function newSwimlane (action_p, pos_p) {
			var swimlane = sendAction(action_p);
			if (swimlane) {
				swimlane.position = pos_p;
				scxml.miscNode.swimlanes.push(swimlane);
				var swimlaneWrapper = new JSDSwimlane(diagram, canvasID, swimlane);
				objMap[swimlane.uid] = swimlaneWrapper;
				sendAction("needRefresh");
			}
		}

		// delete trans from the model (and canvas)
		function deleteTrans (transUID_p) {
			var transWrapper = objMap[transUID_p];
			if (transWrapper) {
				var k = transWrapper.fromStateWrapper.state.transitions.indexOf(transWrapper.trans);
				if (k >= 0) {
					transWrapper.fromStateWrapper.state.transitions.splice(k,1);
				}
				transWrapper.fromStateWrapper.removeOutTrans(transWrapper);
				transWrapper.toStateWrapper.removeInTrans(transWrapper);
				transWrapper.remove();
				objMap[transWrapper.transuid] = undefined;
				setChanged();
			}
		}

		// delete state from the model (and canvas)
		function deleteState (stateUID_p) {
			var stateWrapper = objMap[stateUID_p];
			if (stateWrapper) {
				var parentStateWrapper = objMap[stateWrapper.state.parentuid];
				if (parentStateWrapper) {
					var j = parentStateWrapper.state.childrenStates.indexOf(stateWrapper.state);
					if (j >= 0) {
						parentStateWrapper.state.childrenStates.splice(j,1);
					}
				}
				else {
					var j = scxml.childrenStates.indexOf(stateWrapper.state);
					if (j >= 0) {
						scxml.childrenStates.splice(j,1);
					}
				}
				stateWrapper.remove();
				objMap [stateWrapper.state.uid] = undefined;
				setChanged();
			}
		}
		
		function regTransMap (containerID_p, stateList_p) {
			for (var i in stateList_p) {
				var state = stateList_p[i];
				var fromStateWrap = objMap[state.uid];
				if (state.transitions==undefined) {
					state.transitions = [];
				}
				for (var j in state.transitions) {
					var trans = state.transitions[j];
					var toStateWrap = objMap[trans.targetUID];
					if (trans.posInfo==undefined) {
						trans.posInfo = {
							startDir: "right",
							endDir: "left"
						};
						trans.posInfo.posList = routeTrans(fromStateWrap.state, trans.posInfo.startDir, 25, 
								toStateWrap.state, trans.posInfo.endDir, 25).posList;
					}
					addTrans(containerID_p, fromStateWrap, trans);
				}
				if (state.childrenStates) {
					regTransMap(state.uid, state.childrenStates);
				}
			}
		}

		// requires mode be set to rerouteTrans before this call
		function reRouteTrans(uid_p) {
			clickedTransWrapper = objMap[uid_p];
//			setMode ("rerouteTrans");
		}

		// reroute trans using the same start/end and offset
		function refreshTrans(uid_p) {
			clickedTransWrapper = objMap[uid_p];
			var startOffset = 25;
			if ("leftright".indexOf(clickedTransWrapper.trans.posInfo.startDir)>=0) {
				startOffset = clickedTransWrapper.trans.posInfo.posList[0].top - clickedTransWrapper.fromStateWrapper.state.position.top;
			}
			else {
				startOffset = clickedTransWrapper.trans.posInfo.posList[0].left - clickedTransWrapper.fromStateWrapper.state.position.left;
			}
			var endOffset = 25;
			var lastIdx = clickedTransWrapper.trans.posInfo.posList.length-1;
			if ("leftright".indexOf(clickedTransWrapper.trans.posInfo.endDir)>=0) {
				endOffset = clickedTransWrapper.trans.posInfo.posList[lastIdx].top - clickedTransWrapper.toStateWrapper.state.position.top;
			}
			else {
				endOffset = clickedTransWrapper.trans.posInfo.posList[lastIdx].left - clickedTransWrapper.toStateWrapper.state.position.left;
			}
			var posInfo = routeTrans(clickedTransWrapper.fromStateWrapper.state, clickedTransWrapper.trans.posInfo.startDir, startOffset, 
					clickedTransWrapper.toStateWrapper.state, clickedTransWrapper.trans.posInfo.endDir, endOffset);
			var transInfo = {
					fromState: clickedTransWrapper.fromStateWrapper.state,
					toState: clickedTransWrapper.toStateWrapper.state,
					posInfo: posInfo
				}
			clickedTransWrapper.reRoute(transInfo);
			transBuilder.reset();
			sendAction("transRerouted");
			setChanged();
		}

		// refresh all incoming and outgoing trans after state is moved
		function refreshState (stateUID_p) {
			var stateWrapper = objMap[stateUID_p];
			stateWrapper.refreshTrans();
		}
		
		function hiLiteStateNeighbors (stateUID_p) {
			hiLiter.clear();
			var stateWrapper = objMap[stateUID_p];
			if (stateWrapper && stateWrapper.objData.typeCode=="state") {
				stateWrapper.hiLiteNeighbors(hiLiter);
			}
		}
		
		function hiLiteObj (uidList_p, clear_p, type_p, hideSeq_p, seqPreffix_p) {
			if (clear_p) hiLiter.clear();
			var objList = [];
			var typeClass = "JSDHiLite-mark";
			if (type_p) {
				typeClass = "JSDHiLite-" + type_p;
			}
			for (var i in uidList_p) {
				var wrapper = objMap[uidList_p[i]];
				if (wrapper) {
					objList.push(wrapper);
				}
			}
			hiLiter.hiLite(objList, typeClass, hideSeq_p, seqPreffix_p);
		}

		function updateObjAttr (uid_p) {
			var wrapper = objMap[uid_p];
			if (wrapper) {
				wrapper.updProp();
			}
		}

		function getMarkedUIDs () {
			return hiLiter.getUIDs("JSDHiLite-mark");
		}

		function clearMarked() {
			hiLiter.clear();
		}

		function deleteMarked () {
			var uid;
			var uidList = hiLiter.getUIDs("JSDHiLite-mark");
			for (var i in uidList) {
				uid = uidList[i];
				deleteObj(uid);
			}
			hiLiter.clear();
			return uidList;
		}

		function deleteObj (uid_p) {
			var wrapper = objMap [uid_p];
			if (wrapper && wrapper.objData) {
				if (wrapper.objData.typeCode=="state") {
					deleteState(uid_p);
				}
				else if (wrapper.objData.typeCode=="transition") {
					deleteTrans(uid_p);
				}
				else if (wrapper.objData.typeCode=="swimlane") {
					var idx = scxml.miscNode.swimlanes.indexOf(wrapper.objData)
					if (idx>=0) {
						scxml.miscNode.swimlanes.splice(idx,1);
						wrapper.remove();
					}
				}
				else if (wrapper.objData.typeCode=="box") {
					var idx = scxml.miscNode.boxes.indexOf(wrapper.objData)
					if (idx>=0) {
						scxml.miscNode.boxes.splice(idx,1);
						wrapper.remove();
					}
				}
				else {
					wrapper.remove();
				}
				objMap[uid_p] = undefined;
			}
		}
		
		function getMarkedStateWrapList() {
			var uidList = hiLiter.getUIDs("JSDHiLite-mark");
			var stateWrapList = [];
			for (var i in uidList) {
				var stateWrap = objMap[uidList[i]];
				if (stateWrap && stateWrap.objData.typeCode=="state") {
					stateWrapList.push(stateWrap);
				}
			}
			return stateWrapList;
		}

		function alignStatesTop (stateWrapList_p) {
			if (stateWrapList_p.length==0) return;

			var targetPos = 999999;
			var stateWrap;
			for (var i in stateWrapList_p) {
				stateWrap = stateWrapList_p[i];
				targetPos = Math.min(targetPos, stateWrap.state.position.top);
			}
			
			for (var i in stateWrapList_p) {
				stateWrap = stateWrapList_p[i];
				stateWrap.move({deltaX: 0, deltaY: targetPos - stateWrap.state.position.top});
			}
		}

		function alignStatesMiddle (stateWrapList_p) {
			if (stateWrapList_p.length==0) return;

			var stateWrap = stateWrapList_p[0];
			var targetPos = stateWrap.state.position.top + stateWrap.state.position.height / 2;
			
			for (var i in stateWrapList_p) {
				stateWrap = stateWrapList_p[i];
				stateWrap.move({deltaX: 0, deltaY: targetPos - (stateWrap.state.position.top + stateWrap.state.position.height/2)});
			}
		}

		function alignStatesBottom (stateWrapList_p) {
			if (stateWrapList_p.length==0) return;

			var targetPos = 0;
			var stateWrap;
			for (var i in stateWrapList_p) {
				stateWrap = stateWrapList_p[i];
				targetPos = Math.max(targetPos, stateWrap.state.position.top + stateWrap.state.position.height);
			}
			
			for (var i in stateWrapList_p) {
				stateWrap = stateWrapList_p[i];
				stateWrap.move({deltaX: 0, deltaY: targetPos - stateWrap.state.position.top - stateWrap.state.position.height});
			}
		}

		function alignStatesLeft (stateWrapList_p) {
			if (stateWrapList_p.length==0) return;

			var targetPos = 999999;
			var stateWrap;
			for (var i in stateWrapList_p) {
				stateWrap = stateWrapList_p[i];
				targetPos = Math.min(targetPos, stateWrap.state.position.left);
			}
			
			for (var i in stateWrapList_p) {
				stateWrap = stateWrapList_p[i];
				stateWrap.move({deltaX: targetPos - stateWrap.state.position.left, deltaY: 0});
			}
		}
		
		function alignStatesCenter (stateWrapList_p) {
			if (stateWrapList_p.length==0) return;
			
			var stateWrap = stateWrapList_p[0];
			var targetPos = stateWrap.state.position.left + stateWrap.state.position.width/2;
			for (var i in stateWrapList_p) {
				stateWrap = stateWrapList_p[i];
				stateWrap.move({deltaX: targetPos - (stateWrap.state.position.left + stateWrap.state.position.width/2), deltaY: 0});
			}
		}

		function alignStatesRight (stateWrapList_p) {
			if (stateWrapList_p.length==0) return;

			var targetPos = 0;
			var stateWrap;
			for (var i in stateWrapList_p) {
				stateWrap = stateWrapList_p[i];
				targetPos = Math.max(targetPos, stateWrap.state.position.left + stateWrap.state.position.width);
			}
			
			for (var i in stateWrapList_p) {
				stateWrap = stateWrapList_p[i];
				stateWrap.move({deltaX: targetPos - stateWrap.state.position.left - stateWrap.state.position.width, deltaY: 0});
			}
		}

		function alignStates (dir_p) {
			var stateWrapList = getMarkedStateWrapList();
			if (dir_p=="top") alignStatesTop(stateWrapList);
			else if (dir_p=="middle") alignStatesMiddle(stateWrapList);
			else if (dir_p=="bottom") alignStatesBottom(stateWrapList);
			else if (dir_p=="left") alignStatesLeft(stateWrapList);
			else if (dir_p=="center") alignStatesCenter(stateWrapList);
			else alignStatesRight(stateWrapList);
			setChanged();
		}
		
		function moveStates(uidList_p, mouseTrack_p) {
			for (var i in uidList_p) {
				var wrapper = objMap[uidList_p[i]];
				wrapper.move(mouseTrack_p, uidList_p);
				for (var j in wrapper.objData.transitions) {
					var trans = wrapper.objData.transitions[j];
					if (uidList_p.indexOf(trans.targetUID)>=0) {
						var tWrap = objMap[trans.uid];
						JSDShiftTrans(tWrap.objData, mouseTrack_p.deltaX, mouseTrack_p.deltaY);
						tWrap.show();
					}
				}
			}
		}

		function moveMarkedStates (mouseTrack_p, moveMarkers) {
			var stateWrapList = getMarkedStateWrapList();
			var uidList = [];
			for (var i in stateWrapList) {
				uidList.push(stateWrapList[i].objData.uid);
			}
			moveStates(uidList, mouseTrack_p);
			
			if (moveMarkers) {
				var sLeft = $(canvasElem).parent().scrollLeft();
				var sTop = $(canvasElem).parent().scrollTop();
				var elist = $(".JSDHiLite-item");
				for (var i = 0; i < elist.length; i++) {
					var e1 = elist[i];
					var ep = $(e1).position();
					$(e1).css ({ 
					  left: ep.left + mouseTrack_p.deltaX + sLeft, 
					  top: ep.top + mouseTrack_p.deltaY + sTop
					});
				}
			}
		}
		
		function setShowHideSubstates (stateUID_p, visibility_p) {
			var stateWrapper = objMap[stateUID_p];
			if (stateWrapper) {
				stateWrapper.updSubstatesVisibility();
			}
		}
		
		function toggleBreakpoint (uid_p) {
			var wrapper = objMap[uid_p];
			if (wrapper) {
				wrapper.objData.breakpoint = !wrapper.objData.breakpoint;
				wrapper.updProp();
			}
		}
		
		function getBreakpoints () {
			var retList = [];
			$(".JSD-breakpoint:visible").parent().each(function() {
				retList.push($(this).attr("uid"));
			})
			return retList;
		}
		
		function setElemStyle (elem_p, colorAttr_p, fromColorAttr_p, style_p) {
			elem_p.removeClass(function (index, css) {
				return (css.match (/\bclass_\S+/g) || []).join(' '); // removes anything that starts with "class_"
			});
			elem_p.css(colorAttr_p, "");
			if (style_p) {
				if (style_p.indexOf("{")==0) {
					try {
						var styleOpt = JSON.parse(style_p);
						if (styleOpt[fromColorAttr_p] && colorAttr_p != fromColorAttr_p) {
							styleOpt[colorAttr_p] = styleOpt[fromColorAttr_p];
							styleOpt[fromColorAttr_p] = "none";
						}
						elem_p.css(styleOpt);
					}
					catch (err) {
						
					}
				}
				else if (style_p.indexOf("class_")==0) {
					elem_p.removeClass(function (index, css) {
							return (css.match (/\bclass_\S+/g) || []).join(' '); // removes anything that starts with "class_"
						})
						.addClass(style_p);
				}
				else {
					elem_p.css(colorAttr_p, style_p);
				}
			}
		}
		
		return {
			addModel: addModel,
			setMode: setMode,
			reset: reset,
			getMode: getMode,
			deleteState: deleteState,
			deleteTrans: deleteTrans,
			deleteMarked: deleteMarked,
			deleteObj: deleteObj,
			init: init,
			reRouteTrans: reRouteTrans,
			refreshTrans: refreshTrans,
			refreshState: refreshState,
			hiLiteStateNeighbors: hiLiteStateNeighbors,
			hiLiteObj: hiLiteObj,
			getMarkedUIDs: getMarkedUIDs,
			clearMarked: clearMarked,
			updateObjAttr: updateObjAttr,
			alignStates: alignStates,
			setChanged: setChanged,
			mouseoverObj: mouseoverObj,
			mousemoveObj: mousemoveObj,
			mousedownObj: mousedownObj,
			mouseupObj: mouseupObj,
			clickObj: clickObj,
			doubleClickObj: doubleClickObj,
			moveStates: moveStates,
			moveMarkedStates: moveMarkedStates,
			setCanvasSettings: setCanvasSettings,
			displayMsg: displayMsg,
			setShowHideSubstates: setShowHideSubstates,
			sendAction: sendAction,
			toggleBreakpoint: toggleBreakpoint,
			getBreakpoints: getBreakpoints,
			setElemStyle: setElemStyle
  		};
	}

	return {
		getInstance: function() {
			if (!diagram) {
				diagram = create();
			}
			return diagram;
		}
	}; 
})();

// state wrapper object
function JSDState (diagram_p, pUID_p, parentStateWrapper_p, state_p) {
	var diagram = diagram_p;
	var state = state_p;
	var pUID = pUID_p;
	var elem;
	var breakElem;
	var stateList;
	var parentStateWrapper = parentStateWrapper_p;
	var inTransWrapList = [];
	var outTransWrapList = [];

	function show () {
		var breakHtml = "<span class='JSD-breakpoint' title='click to remove breakpoint' style='position:absolute;left:0px;top:-3px;'>&#11044;</span>";
		var labelHtml = "<div class='JSDState-label JSD-theme' uid='" + state.uid + "'>" + breakHtml + "<span class='JSD-label'/>" + "</div>";
		var cntHtml = labelHtml;
		var classes = "JSD-state";
		if (state.readOnly) classes += " JSD-readonly";
		if (state.nodeType=="INITIAL") {
			elem = $("<div/>", 
				{	id: state.uid,
					class: "JSD-state JSDInitial JSD-theme",
					append: labelHtml,
					appendTo: "#" + pUID 
				});
		}
		else if (state.nodeType=="FINAL") {
			elem = $("<div/>", 
				{	id: state.uid,
					class: "JSD-state JSDFinal JSD-theme",
					append: labelHtml,
					appendTo: "#" + pUID 
				});
		}
		else if (state.nodeType=="BRANCH") {
			cntHtml = "<div class='JSDBranch-left'></div>"
				+ "<div class='JSDBranch-right'></div>"
				+ labelHtml; //"<div class='JSDState-label'/>";
			elem = $("<div/>", 
				{	id: state.uid,
					class: "JSD-state JSDBranch",
					append: cntHtml,
					appendTo: "#" + pUID 
				});
		}
		else if (state.nodeType=="SWITCH") {
			elem = $("<div/>", 
				{	id: state.uid,
					class: "JSD-state JSDSwitch JSD-theme",
					append: labelHtml,
					appendTo: "#" + pUID 
				});
		}
		else if (state.nodeType=="SYNCBARH") {
			cntHtml = "<div class='JSDState-label' uid='" + state.uid + "'>" + breakHtml + "</div>";
			elem = $("<div/>", 
				{	id: state.uid,
					class: "JSD-state JSDSyncbarH JSD-theme",
					append: cntHtml,
					appendTo: "#" + pUID 
				});
		}
		else if (state.nodeType=="SYNCBARV") {
			cntHtml = "<div class='JSDState-label' uid='" + state.uid + "'>" + breakHtml + "</div>";
			elem = $("<div/>", 
				{	id: state.uid,
					class: "JSD-state JSDSyncbarV JSD-theme",
					append: cntHtml,
					appendTo: "#" + pUID 
				});
		}
		else {
			elem = $("<div/>", 
				{	id: state.uid,
					class: "JSD-state JSDState JSD-theme",
					append: cntHtml,
					appendTo: "#" + pUID 
				});
		}
		elem.on("mouseover", function (event) {
	  		event.preventDefault();
			event.stopPropagation();
			diagram.mouseoverObj(state, event);
		})
		.on("click", function (event) {
	  		event.preventDefault();
			event.stopPropagation();
			diagram.clickObj(state, event);
		})
		.on("mousemove", function (event) {
	  		event.preventDefault();
			event.stopPropagation();
			diagram.mousemoveObj(event, state.uid);
		})
		.on("mousedown", function (event) {
	  		event.preventDefault();
			event.stopPropagation();
			diagram.mousedownObj(event);
		})
		.on("mouseup", function (event) {
	  		event.preventDefault();
			event.stopPropagation();
			diagram.mouseupObj(event);
		})
		.on("dblclick", function (event) {
	  		event.preventDefault();
			event.stopPropagation();
			diagram.doubleClickObj(state);
		});
		
		if (state.nodeType=="BRANCH") {
			breakElem = elem.children(".JSDState-label").css("position", "absolute").children(".JSD-breakpoint");
		}
		else if (state.nodeType=="SWITCH") {
			breakElem = elem.children(".JSDState-label").children(".JSD-breakpoint");
		}
		else {
			breakElem = elem.children(".JSDState-label").children(".JSD-breakpoint");
		}
		breakElem.on("click", function (event){
	  		event.preventDefault();
			event.stopPropagation();
			state.breakpoint = false;
			updProp();
		});
//		elem.children(".JSDState-label").children(".JSD-notepad").on ("click", function(event) {
//	  		event.preventDefault();
//			event.stopPropagation();
//			diagram.sendAction("clickNotepad", state);
//		})
		
		updProp ();
	}

	function getAbsolutePos () {
		var pos = jQuery.extend(true, {}, state.position);
		if (parentStateWrapper) {
			var pPos = getParentAbsPos ();
			pos.top += pPos.top;
			pos.left += pPos.left;
		}
		return pos;
	}

	function getParentAbsPos () {
		if (parentStateWrapper) {
			var pPos = parentStateWrapper.getAbsolutePos();
			pPos.left += 0.5;
			pPos.top += 0.5;
			return pPos;
		}
		else {
			return {left: 0, top: 0};
		}
	}

	function getMenuPos () {
		var p = getParentAbsPos();
		var pos = {
			left: p.left + state.position.left + Math.round(state.position.width/2) - 13,
			top: p.top + state.position.top - 10
		}
		if (state.nodeType=="SYNCBARH") {
			pos.top -= 13;
		}
		return pos;
	}

	function getResizePos () {
		var p = getParentAbsPos();
		var pos = {
			left: p.left + state.position.left + state.position.width - 10,
			top: p.top + state.position.top + state.position.height - 10
		}
		return pos;
	}

	function getDragPos () {
		var pos = getAbsolutePos();
		return pos;
	}
	
	function updProp () {
		elem.css ({
			left: (state.position.left) + 'px',
			top: (state.position.top) + 'px',
			width: state.position.width + 'px',
			height: state.position.height + 'px'
		});
		
		var offset = 5;
		if (state.position.nameOffset) {
			offset = parseInt(state.position.nameOffset);
		}
		var stateName = state.stateID;
		if (state.subModel.length>0) {
			stateName += ": " + state.subModel;
		}
		else if (state.childrenStates.length>0) {
			stateName += " [ ]";
		}
		if (state.nodeType=="BRANCH") {
			var hw = Math.floor(state.position.width/2);
			var hh = Math.floor(state.position.height/2);
			elem.children(".JSDBranch-left").css("border-width", hh + "px " + hw+ "px " + hh + "px 0px");
			elem.children(".JSDBranch-right").css("border-width", hh + "px 0px " + hh + "px " + hw + "px")
				.css("left", hw);
			if (state.color) {
				elem.children(".JSDBranch-left, .JSDBranch-right").css("border-color", "transparent " + state.color);
			}
			elem.css({"padding-top": offset});
			elem.children(".JSDState-label").children(".JSD-label").html(stateName); 
			diagram.setElemStyle (elem.children(".JSDState-label.JSD-theme"), "color", "color", state.textColor);
		}
		else {
			diagram.setElemStyle (elem, "background", "background", state.color);
			var elems = elem.children(".JSDState-label.JSD-theme");
			diagram.setElemStyle (elems, "color", "color", state.textColor);
			elems.css("padding-top", offset);
			elems.children(".JSD-label").html(stateName);
		}
		if (state.breakpoint) {
			breakElem.css("display", "");
		}
		else {
			breakElem.css("display", "none");
		}
		
//		if (!!state.notepad) {
//			breakElem.siblings(".JSD-notepad").css("display", "");
//		}
//		else {
//			breakElem.siblings(".JSD-notepad").css("display", "none");
//		}
	}

	function move (mouseTrack_p, avoidStateUIDList_p) {
		state.position.left = Math.max(0, state.position.left + mouseTrack_p.deltaX);
		state.position.top = Math.max(0, state.position.top + mouseTrack_p.deltaY);
		if (parentStateWrapper && parentStateWrapper.state) {
			state.position.left = Math.min(parentStateWrapper.state.position.width, state.position.left);
			state.position.top = Math.min(parentStateWrapper.state.position.height, state.position.top);
		}
		diagram.setChanged();
		updProp();

		// move incoming and outgoing trans
		for (var si in inTransWrapList) {
			var transWrap = inTransWrapList[si];
			if (avoidStateUIDList_p==undefined || 
				avoidStateUIDList_p.indexOf(transWrap.fromStateWrapper.objData.uid) < 0) {
				if (transWrap.isSelfLoop()) {
					JSDShiftTrans(transWrap.trans, mouseTrack_p.deltaX, mouseTrack_p.deltaY);
				}
				else {
					JSDMoveTransEnd(transWrap.trans, mouseTrack_p.deltaX, mouseTrack_p.deltaY);
				}
				transWrap.show();
			}
		}
		for (var si in outTransWrapList) {
			var transWrap = outTransWrapList[si];
			if (avoidStateUIDList_p==undefined ||
				avoidStateUIDList_p.indexOf(transWrap.toStateWrapper.objData.uid) < 0) {
				if (!transWrap.isSelfLoop()) {
					JSDMoveTransStart(transWrap.trans, mouseTrack_p.deltaX, mouseTrack_p.deltaY);
				}
				transWrap.show();
			}
		}
	}

	function resize (mouseTrack_p) {
//		if (state.nodeType=="INITIAL" || state.nodeType=="FINAL") {
//			mouseTrack_p.deltaY = mouseTrack_p.deltaX;
//		}
		if (state.nodeType=="SYNCBARH") {
			mouseTrack_p.deltaY = 0;
		}
		else if (state.nodeType=="SYNCBARV") {
			mouseTrack_p.deltaX = 0;
		}
		state.position.width += mouseTrack_p.deltaX;
		state.position.height += mouseTrack_p.deltaY;
		diagram.setChanged();
		updProp();

		// move incoming trans
		for (var si in inTransWrapList) {
			var transWrap = inTransWrapList[si];
			var endOffset = transWrap.getOffset("end");
			if (transWrap.trans.posInfo.endDir=="left") {
				var d = endOffset - state.position.height;
				if (d > 0) {
					JSDMoveTransEnd(transWrap.trans, 0, -d);
					transWrap.show();
				}
			}
			else if (transWrap.trans.posInfo.endDir=="right") {
				var d = endOffset - state.position.height;
				if (d < 0) {
					d = 0;
				}
				JSDMoveTransEnd(transWrap.trans, mouseTrack_p.deltaX, -d);
				transWrap.show();
			}
			else if ("uptop".indexOf(transWrap.trans.posInfo.endDir)>=0) {
				var d = endOffset - state.position.width;
				if (d > 0) {
					JSDMoveTransEnd(transWrap.trans, -d, 0);
					transWrap.show();
				}
			}
			else {
				var d = endOffset - state.position.width;
				if (d < 0) {
					d = 0;
				}
				JSDMoveTransEnd(transWrap.trans, -d, mouseTrack_p.deltaY);
				transWrap.show();
			}
		}
		
		// move outgoing trans
		for (var si in outTransWrapList) {
			var transWrap = outTransWrapList[si];
			var startOffset = transWrap.getOffset("start");
			if (transWrap.trans.posInfo.startDir=="left") {
				var d = startOffset - state.position.height;
				if (d > 0) {
					JSDMoveTransStart(transWrap.trans, 0, -d);
					transWrap.show();
				}
			}
			else if (transWrap.trans.posInfo.startDir=="right") {
				var d = startOffset - state.position.height;
				if (d < 0) {
					d = 0;
				}
				JSDMoveTransStart(transWrap.trans, mouseTrack_p.deltaX, -d);
				transWrap.show();
			}
			else if ("uptop".indexOf(transWrap.trans.posInfo.startDir)>=0) {
				var d = startOffset - state.position.width;
				if (d > 0) {
					JSDMoveTransStart(transWrap.trans, -d, 0);
					transWrap.show();
				}
			}
			else {
				var d = startOffset - state.position.width;
				if (d < 0) {
					d = 0;
				}
				JSDMoveTransStart(transWrap.trans, -d, mouseTrack_p.deltaY);
				transWrap.show();
			}
		}
	}

	// remove state and all of its incoming/outgoing trans from canvas
	function remove () {
		for (var t in inTransWrapList) {
			var trnWrapper = inTransWrapList[t];
			trnWrapper.fromStateWrapper.removeOutTrans(trnWrapper);
			trnWrapper.remove();
		}	
		for (var t in outTransWrapList) {
			var trnWrapper = outTransWrapList[t];
			trnWrapper.toStateWrapper.removeInTrans(trnWrapper);
			trnWrapper.remove();
		}
		elem.remove();
	}
	
	function getPUID() {
		return pUID;
	}
	
	function addClass (class_p) {
		elem.addClass(class_p);
	}
	
	function toggleClass (class_p) {
		elem.toggleClass(class_p);
	}
	
	function addInTrans (transWrapper_p) {
		inTransWrapList.push(transWrapper_p);
	}

	function addOutTrans (transWrapper_p) {
		outTransWrapList.push(transWrapper_p);
	}
	
	function removeInTrans (transWrapper_p) {
		var i = inTransWrapList.indexOf(transWrapper_p);
		if (i >= 0) {
			inTransWrapList.splice(i,1);
		}
	}

	function removeOutTrans (transWrapper_p) {
		var i = outTransWrapList.indexOf(transWrapper_p);
		if (i >= 0) {
			outTransWrapList.splice(i,1);
		}
		var j = transWrapper_p.fromStateWrapper.state.transitions.indexOf(transWrapper_p.trans);
		if (j>=0) {
			transWrapper_p.fromStateWrapper.state.transitions.splice(j,1);
		}
	}
	
	function hiLiteNeighbors (hiLiter_p) {
		var inStateWrapList = [], outStateWrapList = [];
		for (var j in inTransWrapList) {
			inStateWrapList.push(inTransWrapList[j].fromStateWrapper);
		}
		for (var j in outTransWrapList) {
			outStateWrapList.push(outTransWrapList[j].toStateWrapper);
		}
		hiLiter_p.hiLite (inStateWrapList, "JSDHiLite-incoming");
		hiLiter_p.resetSeq();
		hiLiter_p.hiLite (outStateWrapList, "JSDHiLite-outgoing");
	}

	function refreshTrans () {
		for (t in inTransWrapList) {
			diagram.refreshTrans(inTransWrapList[t].trans.uid);
		}
		for (t in outTransWrapList) {
			diagram.refreshTrans(outTransWrapList[t].trans.uid);
		}
	}
	
	function updSubstatesVisibility () {
		var visibility = state.hideSubstates?"none":"";
		elem.children(".JSD-state, .JSD-trans").css("display", visibility);
	}
	
	show();

	return {
		getAbsolutePos: getAbsolutePos,
		updProp: updProp,
		show: show,
		state: state,
		objData: state,
		getDragPos: getDragPos,
		getMenuPos: getMenuPos,
		getResizePos: getResizePos,
		move: move,
		resize: resize,
		getParentAbsPos: getParentAbsPos,
		getPUID: getPUID,
		addClass: addClass,
		addInTrans: addInTrans,
		addOutTrans: addOutTrans,
		removeInTrans: removeInTrans,
		removeOutTrans: removeOutTrans,
		remove: remove,
		refreshTrans: refreshTrans,
		hiLiteNeighbors: hiLiteNeighbors,
		toggleClass: toggleClass,
		updSubstatesVisibility: updSubstatesVisibility
	}
}

// trans wrapper object
function JSDTrans (diagram_p, pUID_p, fromStateWrapper_p, toStateWrapper_p, trans_p) {
	var diagram = diagram_p;
	var fromStateWrapper = fromStateWrapper_p;
	var toStateWrapper = toStateWrapper_p;
	var trans = trans_p;
	if (!trans.posInfo.label) {
		trans.posInfo.label = defaultLabelPosList[trans.posInfo.startDir];
	}
	var transSegInfo;
	var pUID = pUID_p;
	var labelElem;
	var segElemList = [];
	var endAnchorElem;
	var MAX_INT = 999999;
	var arrowList = {
		"down": "&#9650;", //"&uarr;",
		"up": "&#9660;", //"&darr;",
		"right": "&#9664;", //"&larr;",
		"left": "&#9654;" //"&rarr;"
	}

	function show () {
		$(".JSD-trans[id='" + trans.uid + "']").remove();
		segElemList.length = 0;
		transSegInfo = makeSegList (trans.posInfo);
		for (var k in transSegInfo.segList) {
			var seg = transSegInfo.segList[k];
			var segDir = "JSD-vertical";
			if (seg.width > 3) {
				segDir = "JSD-horizontal";
			}
			var segElem = $("<div/>", 
				{	id: trans.uid,
					class: "JSD-trans JSD-trans-seg JSD-theme" + segDir,
					appendTo: "#" + pUID
				}).prop('idx', k);
			segElemList.push(segElem);
			segElem.on("mouseover", function (event) {
		  		event.preventDefault();
				event.stopPropagation();
				diagram.mouseoverObj(trans, event);
			})
			.on("click", function (event) {
		  		event.preventDefault();
				event.stopPropagation();
				diagram.clickObj(trans, event);
			});
		}
		
		endAnchorElem = $("<div/>", 
			{	id: trans.uid,
				class: "JSD-trans JSD-trans-end JSD-theme",
//				append: "<img class='JSD-anchor' src='" + arrowList[trans.posInfo.endDir] + "'/>",
				append: arrowList[trans.posInfo.endDir],
				appendTo: "#" + pUID
			}).prop('idx', 'end');
		var breakptHtml = "<span class='JSD-breakpoint' title='click to remove breakpoint' style='position:absolute;left:-15px;'>&#11044;</span>";
		var guardHtml = "<span class='JSD-tag JSD-guard' title='Guarded transition'>g</span>";
		labelElem = $("<div/>", 
			{	id: trans.uid,
				uid: trans.uid,
				class: "JSD-trans JSD-trans-label JSD-theme",
				append: breakptHtml + "<span class='JSD-label'/>" + guardHtml,
				appendTo: "#" + pUID
			}).prop('idx', 'label');
		labelElem.on("mouseover", function (event) {
	  		event.preventDefault();
			event.stopPropagation();
			diagram.mouseoverObj(trans, event);
		})
		.on("click", function (event) {
	  		event.preventDefault();
			event.stopPropagation();
			diagram.clickObj(trans, event);
		})
		.on("dblclick", function (event) {
	  		event.preventDefault();
			event.stopPropagation();
			diagram.doubleClickObj(trans);
		});
		
		labelElem.children(".JSD-breakpoint").on ("click", function(event) {
	  		event.preventDefault();
			event.stopPropagation();
			trans.breakpoint = false;
			updProp();
		})
		labelElem.children(".JSD-guard").on ("click", function(event) {
	  		event.preventDefault();
			event.stopPropagation();
			diagram.sendAction("clickGuard", trans);
		})

		updProp ();
	}

	function updProp () {
		for (var k in transSegInfo.segList) {
			var seg = transSegInfo.segList[k];
			var elem = segElemList [k];
			elem.css ({
				left: seg.left + 'px',
				top:  seg.top + 'px',
				width: seg.width + 'px',
				height: seg.height + 'px'
			});
			diagram.setElemStyle (elem, "background", "background", trans.color);
		}

		endAnchorElem.css ({
			left: transSegInfo.endSeg.left + 'px',
			top:  transSegInfo.endSeg.top + 'px'
		});
		diagram.setElemStyle (endAnchorElem, "color", "background", trans.color);

		labelElem.css ({
			left: transSegInfo.labelSeg.left + 'px',
			top:  transSegInfo.labelSeg.top + 'px',
			width: transSegInfo.labelSeg.width + 'px',
			height: transSegInfo.labelSeg.height + 'px'
		});
		diagram.setElemStyle (labelElem, "color", "color", trans.textColor);
		

		var tname = trans.event;
		if (tname=="") tname = "&nbsp;"; 
		if (trans.hideName) {
//			labelElem.css("display", "none");
			tname = '';
		}
//		else labelElem.css("display", "");
		labelElem.children(".JSD-label").html(tname);
		
		if (trans.breakpoint) {
			labelElem.children(".JSD-breakpoint").css("display","");
		}
		else {
			labelElem.children(".JSD-breakpoint").css("display","none");
		}
		
		if (trans.guard) {
			labelElem.children(".JSD-guard").css("display", "");
		}
		else {
			labelElem.children(".JSD-guard").css("display", "none");
		}
	}


	function getMenuPos (segElem_p, event_p) {
		var idx = "label";
		if (segElem_p) {
			var idxTemp = segElem_p.prop("idx");
			if (idxTemp) idx = idxTemp;
		}
		var p = getSegPos(idx);
		if (p.width == undefined) {
			p.width = 50;
		}
		if (idx=="label") {
			p.left = p.left + p.width/2 - 10;
			p.top = p.top - 15;

			var parentPos = fromStateWrapper.getParentAbsPos();
			p.left += parentPos.left;
			p.top += parentPos.top;
		}
		else {
			p = undefined;
//			var p2 = fromStateWrapper.getParentAbsPos();
//			p.left = event_p.pageX - p2.left - 10;
//			p.top = event_p.pageY - p2.top - 65;
		}
		return p;
	}

	function getResizePos () {
		var p = fromStateWrapper.getParentAbsPos();
		var ep = getSegPos("label");
		var pos = {
			left: p.left + ep.left + ep.width - 10,
			top: p.top + ep.top + ep.height - 10
		}
		return pos;
	}
	
	function resize (mouseTrack_p) {
		trans.posInfo.label.width += mouseTrack_p.deltaX;
		trans.posInfo.label.height += mouseTrack_p.deltaY;
		diagram.setChanged();
		show();
		updProp();
	}

	function getDragPos (segElem_p) {
		var idx = "label";
		if (segElem_p) {
			var idxTemp = segElem_p.prop("idx");
			if (idxTemp) idx = idxTemp;
		}
		var p = getSegPos(idx);
		if (p.width == undefined) {
			p.width = 50;
		}
		if (idx!="label") {
			if (p.width>p.height) {
				p.height += 4;
			}
			else {
				p.width += 4;
			}
		}
		var parentPos = fromStateWrapper.getParentAbsPos();
		p.left += parentPos.left;
		p.top += parentPos.top;
		return p;
	}
	
	// remove trans from canvas
	function remove() {
		for (k in segElemList) {
			segElemList[k].remove();
		}
		labelElem.remove();
		endAnchorElem.remove();
	}

	function getSegPos (idx_p) {
		if (idx_p=="label") {
			return jQuery.extend(true, {}, transSegInfo.labelSeg);
		}
		if (idx_p>=0) {
			var retPos = jQuery.extend(true, {}, transSegInfo.segList[idx_p]);
			retPos.posLimit = {minDeltaX: -MAX_INT, maxDeltaX: MAX_INT, minDeltaY: -MAX_INT, maxDeltaY: MAX_INT};
			if (retPos.width > 5) {
				retPos.posLimit.minDeltaX = 0;
				retPos.posLimit.maxDeltaX = 0;
			}
			else {
				retPos.posLimit.minDeltaY = 0;
				retPos.posLimit.maxDeltaY = 0;
			}
			return retPos;
		}
	}

	function move (mouseTrack_p) {
		var idx = mouseTrack_p.customAttr.prop("idx");
		if (idx==undefined || idx=="label") {
			moveLabel (mouseTrack_p.deltaX, mouseTrack_p.deltaY);
		}
		else {
			moveSeg (idx, mouseTrack_p.deltaX, mouseTrack_p.deltaY);
		}
		diagram.setChanged();
	}
	

	function moveSeg (segIdx_p, deltaX_p, deltaY_p) {
		segIdx_p = parseInt (segIdx_p);
		var startSeg = transSegInfo.segList[segIdx_p];
		if (startSeg == undefined) return;
		
		var startPos = trans.posInfo.posList[0];
		var startDir = trans.posInfo.startDir;
		var startOffset = 0;
		if ("leftright".indexOf(startDir)>=0) {
			startOffset = startPos.top - fromStateWrapper.objData.position.top;
		}
		else {
			startOffset = startPos.left - fromStateWrapper.objData.position.left;
		}
		var endPos = trans.posInfo.posList[trans.posInfo.posList.length-1];
		var endDir = trans.posInfo.endDir;
		var endOffset = 0;
		if ("leftright".indexOf(endDir)>=0) {
			endOffset = endPos.top - toStateWrapper.objData.position.top;
		}
		else {
			endOffset = endPos.left - toStateWrapper.objData.position.left;
		}
		
		if (Math.abs(startSeg.height) < Math.abs(startSeg.width)) {
			// vertical movement
			if (segIdx_p=="0") { // moving first segment
				startOffset += deltaY_p;
				if (startOffset < 0) { 
					// moving over the top
					startOffset = 0;
					if (fromStateWrapper.objData.nodeType=="INITIAL" || fromStateWrapper.objData.stereotype=="BRANCH") {
						startOffset = fromStateWrapper.objData.position.width/2;
					}
					else if (startDir == "right") {
						startOffset =  fromStateWrapper.objData.position.width;
					}
  					var posInfo = routeTrans(fromStateWrapper.objData, "top", startOffset, 
  							toStateWrapper.objData, endDir, endOffset);
  					trans.posInfo = posInfo;
				}
				else if (startOffset > fromStateWrapper.objData.position.height) {
					// moving past bottom
					startOffset = 0;
					if (fromStateWrapper.objData.nodeType=="INITIAL" || fromStateWrapper.objData.nodeType=="BRANCH") {
						startOffset = fromStateWrapper.objData.position.width/2;
					}
					else if (startDir == "right") {
						startOffset =  fromStateWrapper.objData.position.width;
					}
  					var posInfo = routeTrans(fromStateWrapper.objData, "bottom", startOffset, 
  							toStateWrapper.objData, endDir, endOffset);
  					trans.posInfo = posInfo;
				}
				else {
					JSDMoveTransSeg (trans, segIdx_p, "vertical", deltaY_p);
				}
			}
			else if (segIdx_p==transSegInfo.segList.length-1) { // moving end segment
				endOffset += deltaY_p;
				if (endOffset < 0) {
					// moving over the top
					endOffset = 0;
					if (toStateWrapper.objData.nodeType=="INITIAL" || toStateWrapper.objData.nodeType=="BRANCH") {
						endOffset = toStateWrapper.objData.position.width/2;
					}
					else if (endDir == "right") {
						endOffset =  toStateWrapper.objData.position.width;
					}
  					var posInfo = routeTrans(fromStateWrapper.objData, startDir, startOffset, 
  							toStateWrapper.objData, "top", endOffset);
  					trans.posInfo = posInfo;
				}
				else if (endOffset > toStateWrapper.objData.position.height) {
					// moving past bottom
					endOffset = 0;
					if (toStateWrapper.objData.nodeType=="FINAL" || toStateWrapper.objData.nodeType=="BRANCH") {
						endOffset = toStateWrapper.objData.position.width/2;
					}
					else if (endDir == "right") {
						endOffset =  toStateWrapper.objData.position.width;
					}
  					var posInfo = routeTrans(fromStateWrapper.objData, startDir, startOffset, 
  							toStateWrapper.objData, "bottom", endOffset);
  					trans.posInfo = posInfo;
				}
				else {
					JSDMoveTransSeg (trans, segIdx_p, "vertical", deltaY_p);
				}
			}
			else {
				JSDMoveTransSeg (trans, segIdx_p, "vertical", deltaY_p);
			}
		}
		else {
			// move segment horizontally
			if (segIdx_p=="0") { // moving first segment
				startOffset += deltaX_p;
				if (startOffset < 0) { 
					// moving left past left edge
					startOffset =  0;
					if (fromStateWrapper.objData.nodeType=="INITIAL") {
						startOffset = fromStateWrapper.objData.position.width/2;
					}
					else if (startDir == "down") {
						startOffset = fromStateWrapper.objData.position.height;
					}
  					var posInfo = routeTrans(fromStateWrapper.objData, "left", startOffset, 
  							toStateWrapper.objData, endDir, endOffset);
  					trans.posInfo = posInfo;
				}
				else if (startOffset > fromStateWrapper.objData.position.width) {
					// moving past right edge
					startOffset =  0;
					if (fromStateWrapper.objData.nodeType=="INITIAL") {
						startOffset = fromStateWrapper.objData.position.width/2;
					}
					else if (startDir == "down") {
						startOffset = fromStateWrapper.objData.position.height;
					}
  					var posInfo = routeTrans(fromStateWrapper.objData, "right", startOffset, 
  							toStateWrapper.objData, endDir, endOffset);
  					trans.posInfo = posInfo;
				}
				else {
					JSDMoveTransSeg (trans, segIdx_p, "horizontal", deltaX_p);
				}
			}
			else if (segIdx_p==transSegInfo.segList.length-1) { 
				// moving end segment
				endOffset += deltaX_p;
				if (endOffset < 0) {
					// moving left past left edge
					endOffset =  0;
					if (toStateWrapper.objData.nodeType=="FINAL") {
						endOffset = toStateWrapper.objData.position.width/2;
					}
					else if (endDir == "down") {
						endOffset = toStateWrapper.objData.position.height;
					}
  					var posInfo = routeTrans(fromStateWrapper.objData, startDir, startOffset, 
  							toStateWrapper.objData, "left", endOffset);
  					trans.posInfo = posInfo;
				}
				else if (endOffset > toStateWrapper.objData.position.width) {
					// moving past right edge
					endOffset = 0;
					if (toStateWrapper.objData.nodeType=="FINAL") {
						endOffset = toStateWrapper.objData.position.width/2;
					}
					else if (endDir == "down") {
						endOffset =  toStateWrapper.objData.position.height;
					}
  					var posInfo = routeTrans(fromStateWrapper.objData, startDir, startOffset, 
  							toStateWrapper.objData, "right", endOffset);
  					trans.posInfo = posInfo;
				}
				else {
					JSDMoveTransSeg (trans, segIdx_p, "horizontal", deltaX_p);
				}
			}
			else {
				JSDMoveTransSeg (trans, segIdx_p, "horizontal", deltaX_p);
			}
		}
		show();
	}
	

	function moveLabel (deltaX_p, deltaY_p) {
		trans.posInfo.label.left += deltaX_p;
		trans.posInfo.label.top += deltaY_p;
		show();
	}

	function isSelfLoop () {
		return fromStateWrapper==toStateWrapper;
	}

	function calcDelta (state_p, pos_p, dir_p) {
		if ("updown".indexOf(dir_p)>=0) {
			return pos_p.left - state_p.position.left;
		}
		else {
			return pos_p.top - state_p.position.top;
		}
	}

	function reRoute (transInfo_p) {
		trans.posInfo = transInfo_p.posInfo;
		show();
		diagram.setChanged();
	}
	
	

	function addClass (class_p) {
		labelElem.addClass(class_p);
		for (i in segElemList) {
			segElemList[i].addClass(class_p);
		}
		endAnchorElem.addClass(class_p);
	}

	function removeClass (class_p) {
		labelElem.removeClass(class_p);
		for (i in segElemList) {
			segElemList[i].removeClass(class_p);
		}
		endAnchorElem.removeClass(class_p);
	}
	

	function toggleClass (class_p) {
		if (labelElem.hasClass(class_p)) {
			removeClass(class_p);
		}
		else addClass(class_p);
	}
	
	function getOffset (type_p) {
		if (type_p=="start") {
			if ("uptopdownbottom".indexOf(trans.posInfo.startDir)>=0) {
				return trans.posInfo.posList[0].left - fromStateWrapper.objData.position.left;
			}
			else {
				return trans.posInfo.posList[0].top - fromStateWrapper.objData.position.top;
			}
		}
		else {
			if ("uptopdownbottom".indexOf(trans.posInfo.endDir)>=0) {
				return trans.posInfo.posList[trans.posInfo.posList.length-1].left - toStateWrapper.objData.position.left;
			}
			else {
				return trans.posInfo.posList[trans.posInfo.posList.length-1].top - toStateWrapper.objData.position.top;
			}
		}
	}

	show();
	return {
		updProp: updProp,
		getSegPos: getSegPos,
		trans: trans,
		objData: trans,
		getMenuPos: getMenuPos,
		getDragPos: getDragPos,
		move: move,
		moveSeg: moveSeg,
		remove: remove,
		segInfo: transSegInfo,
		isSelfLoop: isSelfLoop,
		show: show,
		reRoute: reRoute,
		fromStateWrapper: fromStateWrapper,
		toStateWrapper: toStateWrapper,
		addClass: addClass,
		moveLabel: moveLabel,
		toggleClass: toggleClass,
		getOffset: getOffset,
		getResizePos: getResizePos,
		resize: resize
	}
}

function JSDBox (diagram_p, canvasID_p, box_p) {
	var diagram = diagram_p;
	var objData = box_p;
	var elem;

	function show () {
		var cntHtml = "<div class='JSDState-label JSD-theme'><div class='JSD-label'></div></div>";
		elem = $("<div/>", 
			{	id: objData.uid,
				class: "JSD-box JSD-theme",
				append: cntHtml,
				appendTo: "#" + canvasID_p 
			});
		
		elem.on("mouseover", function (event) {
				diagram.mouseoverObj(objData, event);
			})
			.on("click", function (event) {
				diagram.clickObj(objData, event);
			});
		
//		elem.on("dblclick", function (event) {
//	  		event.preventDefault();
//			event.stopPropagation();
//			diagram.doubleClickObj(objData);
//		});

		updProp();
	}

	function getAbsolutePos () {
		var pos = jQuery.extend(true, {}, objData.position);
		return pos;
	}

	function getMenuPos () {
		var pos = {
			left: objData.position.left + objData.position.width/2 - 13,
			top: objData.position.top - 10
		}
		return pos;
	}

	function getResizePos () {
		var pos = {
			left: objData.position.left + objData.position.width - 10,
			top: objData.position.top + objData.position.height - 10
		}
		return pos;
	}

	function getDragPos () {
		var pos = getAbsolutePos();
		return pos;
	}

	function updProp () {
		elem.css ({
			left: (objData.position.left + 2) + 'px',
			top: (objData.position.top + 2) + 'px',
			width: objData.position.width + 'px',
			height: objData.position.height + 'px',
		});
		diagram.setElemStyle (elem.children(".JSDState-label"), "color", "color", objData.textColor);
		diagram.setElemStyle (elem, "background", "background", objData.color);
		var offset = 5;
		if (objData.position.nameOffset) {
			offset = objData.position.nameOffset;
		}
		elem.children(".JSDState-label").text(objData.name);
		elem.children(".JSDState-label").css("padding-top", offset);
	}
	
	function move (mouseTrack_p) {
		objData.position.left = Math.max(0, objData.position.left + mouseTrack_p.deltaX);
		objData.position.top = Math.max(0, objData.position.top + mouseTrack_p.deltaY);
		updProp();
	}

	function resize (mouseTrack_p) {
		objData.position.width += mouseTrack_p.deltaX;
		objData.position.height += mouseTrack_p.deltaY;
		updProp();
	}
	
	function addClass (class_p) {
		elem.addClass(class_p);
	}

	function remove () {
		elem.remove();
	}

	show();
	
	return {
		getAbsolutePos: getAbsolutePos,
		getParentAbsPos: getAbsolutePos,
		updProp: updProp,
		addClass: addClass,
		resize: resize,
		remove: remove,
		objData: objData,
		move: move,
		getMenuPos: getMenuPos,
		getDragPos: getDragPos,
		getResizePos: getResizePos
	}
}


function JSDSwimlane (diagram_p, canvasID_p, swimlane_p) {
	var diagram = diagram_p;
	var objData = swimlane_p;
	var elem;
	
	function show () {
		if (elem) {
			elem.remove();
		}
		
		if (objData.lanes==undefined || objData.lanes.length==0) {
			objData.lanes = [{name: "lane 1", size: 50}, {name: "lane 2", size: 50}]
		}

		var htmlCode = "<table border='1' height='100%' width='100%'>";
		var totalSize = 0;
		if (swimlane_p.orient=="vertical") {
			swimlane_p.position.top = 0;
			htmlCode += "<tr class='JSD-header' style='height: 30px'><td align='center' class='JSD-swimlaneText' colspan=" + swimlane_p.lanes.length + ">" + swimlane_p.name + "</td></tr>"
				+ "<tr class='JSD-swimlaneHeader'>";
			for (var i=0; i<swimlane_p.lanes.length; i++) {
				htmlCode += "<td laneSeq='" + i + "' class='JSD-swimlaneHeader' align='center' height='20' width='" + swimlane_p.lanes[i].size + "' "
					+ " style='background: " + swimlane_p.lanes[i].color + "; color: " + swimlane_p.lanes[i].textColor + "'>" 
					+ swimlane_p.lanes[i].name + "</td>";
				totalSize += parseInt(swimlane_p.lanes[i].size);
			}
			htmlCode += "</tr><tr>";
			for (var i=0; i<swimlane_p.lanes.length; i++) {
				htmlCode += "<td class='JSD-swimlane-lane' height='*' "
					+ " style='background: " + swimlane_p.lanes[i].color + "'></td>";
			}
			htmlCode += "</tr>";
			swimlane_p.position.width = totalSize;
		}
		else {
			swimlane_p.position.left = 0;
			var vText = swimlane_p.name.replace(/ /g, '&nbsp;');
			htmlCode += "<tr laneSeq='1'><td valign='middle' class='JSD-swimlaneHeader rotate' width='1%' rowspan='" + swimlane_p.lanes.length + "'><div class='JSD-swimlaneText vertical'>" + vText + "</div></td>"
				+ "<td class='JSD-swimlaneHeader rotate' valign='middle' align='center' width='1%' style='background: "
				+ swimlane_p.lanes[0].color + "; color: " + swimlane_p.lanes[0].textColor + "'><div class='JSD-swimlaneText'>" + swimlane_p.lanes[0].name.replace(/ /g, '&nbsp;') + "</div></td>"
				+ "<td width='*' class='JSD-swimlane-lane' height='" + swimlane_p.lanes[0].size + "' "
				+ "style='background: " + swimlane_p.lanes[0].color + "'>&nbsp;</td></tr>";
			totalSize += parseInt(swimlane_p.lanes[0].size);
			for (var i=1; i<swimlane_p.lanes.length; i++) {
				htmlCode += "<tr><td laneSeq='" + i + "' class='JSD-swimlaneHeader rotate' valign='middle' "
					+ "style='background: " + swimlane_p.lanes[i].color + "; color: " + swimlane_p.lanes[i].textColor + "'>"
					+ "<div class='JSD-swimlaneText'>" + swimlane_p.lanes[i].name.replace(/ /g, '&nbsp;') + "</div></td>"
					+ "<td class='JSD-swimlane-lane' height='" + swimlane_p.lanes[i].size + "' style='"
					+ "background: " + swimlane_p.lanes[i].color + "'>&nbsp;</td></tr>";
				totalSize += parseInt(swimlane_p.lanes[i].size);
			}
			swimlane_p.position.height = totalSize;
		}
		htmlCode += "</table>";
		elem = $("<div/>", 
			{	id: objData.uid,
				class: "JSD-swimlane " + swimlane_p.orient,
				append: htmlCode,
				appendTo: "#" + canvasID_p
			});
		if (swimlane_p.orient=="horizontal") {
			elem.css("width", "inherit");
		}
		else elem.css("height", "inherit");
		
		elem.on("mouseover", function (event) {
			diagram.mouseoverObj(objData, event);
		})
		.on("click", function (event) {
			diagram.clickObj(objData, event);
		});
//		.on("dblclick", function (event) {
//	  		event.preventDefault();
//			event.stopPropagation();
//			diagram.doubleClickObj(objData);
//		});

		updAttr();
	}

	function updProp () {
		// recreate new swimlane
		show();
	}
	
	function updAttr () {
		elem.css ({
			background: objData.color,
			color: objData.textColor
		});
		
		if (objData.orient=="vertical") {
			elem.css({ "width": objData.position.width,
					   "left": objData.position.left
				});
		}
		else {
			elem.css({ "height": objData.position.height,
					"top": objData.position.top
				});
		}
	}
	
	function getAbsolutePos () {
		if (objData.orient=="vertical") {
			return {
				left: objData.position.left,
				top: objData.position.top,
				width: objData.position.width,
				height: "100%"
			}
		}
		else {
			return {
				left: objData.position.left,
				top: objData.position.top,
				width: "100%",
				height: objData.position.height
			}
		}
	}
	
	function getMenuPos () {
		var pos = {
			left: objData.position.left,
			top: objData.position.top
		}
		if (objData.orient=="vertical") {
			pos.left += objData.position.width/2 - 12;
			pos.top += 50;
		}
		else {
			pos.left += 100;
			pos.top += objData.position.height/2 - 30;
		}
		return pos;
	}
	
	function getDragPos () {
		var pos = getAbsolutePos();
		return pos;
	}

	function move (posDelta_p) {
		if (objData.orient=="vertical") {
			objData.position.left += posDelta_p.deltaX;
		}
		else {
			objData.position.top += posDelta_p.deltaY;
		}
		updProp();
	}
	
	function addClass (class_p) {
		elem.addClass(class_p);
	}

	function remove () {
		elem.remove();
	}


	show();
	
	return {
		getAbsolutePos: getAbsolutePos,
		getParentAbsPos: getAbsolutePos,
		updProp: updProp,
		getMenuPos: getMenuPos,
		remove: remove,
		addClass: addClass,
		objData: objData,
		move: move,
		getDragPos: getDragPos
	}
}


// show new trans build visual
function TransBuilder () {
	var fromStateWrapper;
	var fromSideOffset;
	var startPos = {};
	var arrowCanvas;
	
	function init (canvasElem_p) {
		canvasElem = canvasElem_p;
		arrowCanvas = $('<canvas/>',
			{ id: "arrowCanvas",
			  height: "2",
			  width: "2",
			  class: "transArrow",
			  appendTo: canvasElem_p
			}).css( {left: 0, top:0, position: "absolute", "z-index": 999999}).get(0);
		$(canvasElem_p).on("mousemove", function(event_p) {
			if (startPos) {
				var scrollOffset = $(this).offset();
				toPos = { 
					left: event_p.pageX - scrollOffset.left, 
					top: event_p.pageY - scrollOffset.top
				}
				drawArrow (startPos.left, startPos.top, toPos.left, toPos.top);
			}
		});
		
	}

	// return null object if can not create trans to the end connect bar
	function clickState (stateWrapper_p, event_p, clickPos_p) {
		if (stateWrapper_p.objData.readOnly) {
			return "Can not create transition to/from a readonly states";
		}
		var statePos = stateWrapper_p.getAbsolutePos();
		var sideOffset = JSDResolveClickSide (statePos, clickPos_p);
		$(".JSD-state.selected").removeClass("selected");
		
		// initial state only outgoing trans, final state only incoming trans
		if (fromStateWrapper==undefined) {
			startPos = clickPos_p;

			if (stateWrapper_p.objData.nodeType=="FINAL") {
				return "Final state may not have outgoing transition";
			}
			fromStateWrapper = stateWrapper_p;
			fromSideOffset = sideOffset;
			fromStateWrapper.addClass("selected");
			return {
				fromStateWrapper: fromStateWrapper,
				fromSideOffset: fromSideOffset
			}
		}
		
		if (stateWrapper_p.objData.nodeType=="INITIAL") {
			return "Initial state may not have incoming transition";
		}
		
		if (fromStateWrapper.getPUID()!=stateWrapper_p.getPUID()) {
			return "Source and target states for the transition must share the same parent";
		}
		
		return {
			fromStateWrapper: fromStateWrapper,
			fromSideOffset: fromSideOffset,
			toStateWrapper: stateWrapper_p,
			toSideOffset: sideOffset
		}
	}

	function reset() {
		fromStateWrapper = undefined;
		fromSideOffset = undefined;
		startPos = undefined;
		var arrowCtx = arrowCanvas.getContext("2d");
	  	arrowCtx.clearRect(0, 0, arrowCanvas.width, arrowCanvas.height);
		$(arrowCanvas).css({left: 0, top: 0, width: 1, height: 1});
	  	arrowCanvas.width = 1;
	  	arrowCanvas.height = 1;
	}

	function drawArrow (fromx, fromy, tox, toy) {
		if (tox>fromx) {
			tox -= 5;
		}
		else {
			tox += 10;
		}
		
		if (toy>fromy) {
			toy -= 5;
		}
		else {
			toy += 10;
		}
		
		var sizeWidth = Math.abs(fromx - tox) + 3;
		var sizeHeight = Math.abs(fromy - toy) + 3;
	
		var offsetLeft = Math.min(fromx, tox);
		var offsetTop = Math.min(fromy, toy);
		fromx = fromx - offsetLeft;
		fromy = fromy - offsetTop;
		tox = tox - offsetLeft;
		toy = toy - offsetTop;
		if (tox<fromx) {
			tox += 3;
		}
		
		if (toy < fromy) {
			toy += 3;
		}
		
		var context = arrowCanvas.getContext("2d");
	  	context.clearRect(0, 0, arrowCanvas.width, arrowCanvas.height);

		arrowCanvas.width = Math.max(sizeWidth+1, 5);
		arrowCanvas.height = Math.max(sizeHeight + 1, 5);
		$(arrowCanvas).css({left: offsetLeft, top: offsetTop, width: Math.max(sizeWidth+1, 5), height: Math.max(sizeHeight + 1, 5)});
		context.strokeStyle = "gray";
		context.setLineDash([5,3]);
		context.beginPath();
		var headlen = 10;	// length of head in pixels
		var dx = tox-fromx;
		var dy = toy-fromy;
		var angle = Math.atan2(dy,dx);
		context.moveTo(fromx, fromy);
		context.lineTo(tox, toy);
		context.lineTo(tox-headlen*Math.cos(angle-Math.PI/6),toy-headlen*Math.sin(angle-Math.PI/6));
		context.moveTo(tox, toy);
		context.lineTo(tox-headlen*Math.cos(angle+Math.PI/6),toy-headlen*Math.sin(angle+Math.PI/6));
		context.stroke();
	}
			
	return {
		clickState: clickState,
		reset: reset,
		init: init
	}
}

function CanvasBackdrop (canvasElem_p, canvasSize_p) {
	var canvasElem = canvasElem_p;
	var backdropElem = $("<div/>",
			{  id: "JSD-backdrop",
		   class: "JSD-ghost-elem",
		   appendTo: canvasElem_p
		})
		.css ({ "z-index": 9999, display: "none", position: "absolute", width: canvasSize_p.width, height: canvasSize_p.height});

	var mouseTrack = {
		startX: 0,
		startY: 0,
		deltaX: 0,
		deltaY: 0,
		lastX: 0,
		lastY: 0,
		curX: 0,
		curY: 0,
		stepX: 0,
		stepY: 0
	}
	var canvasElem = canvasElem_p;
	var moveCallback;
	var mouseupCallback;

	function resetSize (cssSize_p) {
		backdropElem.css(cssSize_p);
	}
	
	backdropElem.on("mousemove", function (event) {
		event.preventDefault();
		event.stopPropagation();
		var scrollOffset = canvasElem.offset();
		if (moveCallback) {
			mouseTrack.lastX = mouseTrack.curX;
			mouseTrack.lastY = mouseTrack.curY;
			mouseTrack.curX = event.pageX - scrollOffset.left;
			mouseTrack.curY = event.pageY - scrollOffset.top;
			mouseTrack.stepX = mouseTrack.curX - mouseTrack.lastX;
			mouseTrack.stepY = mouseTrack.curY - mouseTrack.lastY;
			mouseTrack.deltaX = event.pageX - mouseTrack.startX - scrollOffset.left;
			mouseTrack.deltaY = event.pageY - mouseTrack.startY - scrollOffset.top;
			moveCallback.apply(null, [mouseTrack]);
		}
	});
	backdropElem.on("mouseup", function (event) {
  		event.preventDefault();
		event.stopPropagation();
		if (Math.abs(mouseTrack.deltaX) >= 2 || Math.abs(mouseTrack.deltaY)>=2) {
			backdropElem.css("display", "none");
			if (mouseupCallback) {
				if (Math.abs(mouseTrack.deltaX) == 0 && Math.abs(mouseTrack.deltaY) == 0) {
					return;
				}
				mouseupCallback.apply(null, [mouseTrack]);
			}
			stopTrack();
		}
	});
	
	function startTrack (event_p, customAttr_p, moveCallback_p, mouseupCallback_p) {
		var scrollOffset = canvasElem.offset();
		mouseTrack.customAttr = customAttr_p;
		mouseTrack.startX = event_p.pageX - scrollOffset.left;
		mouseTrack.startY = event_p.pageY - scrollOffset.top;
		mouseTrack.deltaX = 0;
		mouseTrack.deltaY = 0;
		mouseTrack.lastX = mouseTrack.startX;
		mouseTrack.lastY = mouseTrack.startY;
		mouseTrack.curX = mouseTrack.startX;
		mouseTrack.curY = mouseTrack.startY;
		mouseTrack.stepX = 0;
		mouseTrack.stepY = 0;
		moveCallback = moveCallback_p;
		mouseupCallback = mouseupCallback_p;
		backdropElem.css("display", "block");
	}
	
	function stopTrack () {
		mouseupCallback = undefined;
		moveCallback = undefined;
		backdropElem.css("display", "none");
	}
	
	return {
		resetSize: resetSize,
		startTrack: startTrack,
		stopTrack: stopTrack
	}
}


function CircleMenu (canvasElem_p, menuCallback_p, menuIcon_p) {
	var menuCallback = menuCallback_p;
	var menuElem;
	var objData;
	var circleElem = $("<div/>",
			{  id: "JSD-menu",
		   class: "JSD-ghost-elem",
		   append: "<img src='" + menuIcon_p + "' width='20px'/>",
		   appendTo: canvasElem_p
		}).on ("click", function(event) {
			toggleOpen();
		});

	function show (menuID_p, size_p, wrapper_p) {
		objData = wrapper_p.objData;
		
		var mPos = wrapper_p.getMenuPos();
//		if (mPos.left < 35) {
//			mPos.left = 35;
//		}
//		if (mPos.top < 35) {
//			mPos.top = 35;
//		}
		//mPos.top -= 30;
		circleElem.css ({
			left: mPos.left,
			top: mPos.top 
		}).css("display","block");

		
		menuElem = $("#" + menuID_p).css({width: size_p, height: size_p});
		var k=0;
		var L = 8;
		menuElem.find("span").each(function(idx) {
			// 50% at the middle of circle width, L number of items, k display index
			//		start at 50% in x axis in the div. 35px inner cicle.
			//		{ left: (50 - 35*Math.cos(-0.5 * Math.PI - 2*(1/L)*k*Math.PI)).toFixed(4) + "%",
			//		  top: (50 + 35*Math.sin(-0.5 * Math.PI - 2*(1/L)*k*Math.PI)).toFixed(4) + "%"
			// 		});
			var hideCond = $(this).attr("hidewhen");
			if (hideCond && eval(hideCond)) {
				$(this).css("display", "none");
			}
			else {
				$(this).css("display", "").css(
						{ left: (50 - 35*Math.cos(2*(1-k/L)*Math.PI)).toFixed(4) + "%",
						  top: (50 + 35*Math.sin(2*(1-k/L)*Math.PI)).toFixed(4) + "%"
						});
					$("#X" + (idx+1)).addClass('show');
					k++; // angle starting 0
			}
		});
		menuElem.css ({
			left: mPos.left - size_p/2 + 13,
			top: mPos.top - size_p/2 + 14 
		})

		menuElem.unbind("click")
			.on("click",function(event) {
		  		event.preventDefault();
				event.stopPropagation();
				close();
				if (menuCallback) {
					var id = $(event.target).prop('id');
					menuCallback.apply(null, [id, event, objData]);
				}
			});
	}
	
	function toggleOpen () {
		if (menuElem) menuElem.toggleClass("open");
	}
	
	function close() {
		if (menuElem) menuElem.removeClass("open");
		if (circleElem) circleElem.css("display","none");
	}
	
	return {
		close: close,
		show: show
	}
}

function Resizer (diagram_p, canvasElem_p, canvasTracker_p) {
	var diagram = diagram_p;
	var wrapper;
	var objData;
	var startPos;

	var resizeElem = $("<div/>",
		{  id: "JSDResizer",
		   class: "JSD-ghost-elem",
		   on: {
				"mousedown": function(event) {
			  		event.preventDefault();
					event.stopPropagation();
					dragElem.addClass("JSDGhost-show").css({cursor: "nwse-resize"});
					canvasTracker_p.startTrack(event, null, resize, resizeEnd);
				}
		   },
		   append: "<div/>",
		   appendTo: canvasElem_p
		});

	var dragElem = $("<div/>", 
		{ id: "JSDResizer-drag",
		   class: "JSD-ghost-elem",
		  appendTo: canvasElem_p
		});

	function resize (mouseTrack_p) {
		dragElem.css(
			{
				width: startPos.width + mouseTrack_p.deltaX,
				height: startPos.height + mouseTrack_p.deltaY
			});
	}
	
	function resizeEnd (mouseTrack_p) {
		close();
		dragElem.removeClass("JSDGhost-show");
		wrapper.resize(mouseTrack_p);
	}
	
	function show (wrapper_p) {
		wrapper = wrapper_p;
		objData = wrapper_p.objData;
		startPos = wrapper_p.getDragPos();
		var resizePos = wrapper_p.getResizePos();
		resizeElem.css(resizePos).addClass("JSDGhost-show");
		dragElem.css (startPos);
	}
	
	function close() {
		dragElem.removeClass("JSDGhost-show");
		resizeElem.removeClass("JSDGhost-show");
	}
	
	
	return {
		close: close,
		show: show
	}	
}

//state ghost and actions
function JSDMover (diagram_p, canvasElem_p, canvasTracker_p) {
	var diagram = diagram_p;
	var canvasTracker = canvasTracker_p;
	var wrapper;
	var dragPos;
	var elemMover;
	var dragElem;
	var targetElem;
	
	function init() {
		elemMover = $("<div/>",
			{  id: "JSDMover",
			   class: "JSD-ghost-elem",
			   on: {
					"mousedown": function(event) {
						elemMover.removeClass("JSDGhost-show");
						dragElem.addClass("JSDGhost-show");
						canvasTracker.startTrack(event, targetElem, move, moveEnd);
					}
			   },
			   append: "&#128070;",
			   appendTo: canvasElem_p
			});

		dragElem = $("<div/>", 
			{ id: "JSDMover-drag",
			   class: "JSD-ghost-elem",
			   on: {
					"mousedown": function(event) {
						elemMover.removeClass("JSDGhost-show");
						dragElem.addClass("JSDGhost-show");
						canvasTracker.startTrack(event, targetElem, move, moveEnd);
					}
			   },
			  appendTo: canvasElem_p
			});
	}
	
	function close() {
		if (dragElem) dragElem.removeClass("JSDGhost-show");
		if (elemMover) elemMover.removeClass("JSDGhost-show");
	}
	// moving ghost elem
	function move (mouseTrack_p) {
		dragElem.css ({ 
			  left: dragPos.left + mouseTrack_p.deltaX, 
			  top: dragPos.top + mouseTrack_p.deltaY
		  });
	}

	// commit state move based on ghost state move delta
	function moveEnd (mouseTrack_p) {
		dragElem.removeClass("JSDGhost-show");
		var uidList = diagram.getMarkedUIDs();
		if (uidList.indexOf(wrapper.objData.uid) >= 0) {
			diagram.moveStates (uidList, mouseTrack_p);
		}
		else {
			wrapper.move(mouseTrack_p);
		}
	}

	function show (wrapper_p, event_p) {
		wrapper = wrapper_p;
		targetElem = undefined;
		if (event_p) {
			targetElem = $(event_p.target);
		}
		dragPos = wrapper.getDragPos(targetElem);
		dragElem.css("cursor", "default")
				.css({left: dragPos.left, top: dragPos.top, width: dragPos.width, height: dragPos.height});
		var moverPos = wrapper.getMenuPos(targetElem, event_p);
		if (moverPos) {
			moverPos.top += 30;
			moverPos.left += 2;
			elemMover.css({left: moverPos.left, top: moverPos.top});
			elemMover.addClass("JSDGhost-show");
		}
		else {
			dragElem.addClass("JSDGhost-show").css("cursor", targetElem.css("cursor"));
		}
	}
	
	init();
	return {
		show: show,
		wrapper: wrapper,
		close: close
	}
}

// manage state/trans highlighting
function JSDHiLiter (diagram_p, canvasElem_p, canvasTracker_p) {
	var diagram = diagram_p;
	var canvasElem = canvasElem_p;
	var canvasTracker = canvasTracker_p;
	var seqNum = 0;
	var dragMode = false;
	var mouseDownPos = {};
	
	function init() {
		//
	}
	
	function clear() {
		seqNum = 0;
		$(".JSDHiLite-item").remove();
	}
	
	function resetSeq() {
		seqNum = 0;
	}

	// moving ghost elem
	function move (mouseTrack_p) {
//		diagram.moveMarkedStates ({deltaX: mouseTrack_p.stepX, deltaY: mouseTrack_p.stepY});
		var elist = $(".JSDHiLite-item");
		var sLeft = $(canvasElem).parent().scrollLeft();
		var sTop = $(canvasElem).parent().scrollTop();
		for (var i = 0; i < elist.length; i++) {
			var e1 = elist[i];
			var ep = $(e1).position();
			$(e1).css ({ 
			  left: ep.left + mouseTrack_p.stepX + sLeft, 
			  top: ep.top + mouseTrack_p.stepY + sTop
			});
		}
	}

	// commit state move based on ghost state move delta
	function moveEnd (mouseTrack_p) {
		diagram.moveMarkedStates ({deltaX: mouseTrack_p.deltaX, deltaY: mouseTrack_p.deltaY}, false);
	}
	
	function hiLite (wrapperList_p, typeClass_p, hideSeqNum_p, seqPreffix_p) {
		for (var i in wrapperList_p) {
			var wrapObj = wrapperList_p[i];
			var dragPos = wrapObj.getDragPos();
			dragPos.left -= 1;
			dragPos.top -= 1;
			var cHtml = "";
			var seqS = '';
			if (!hideSeqNum_p) {
				seqNum += 1;
				seqS = seqNum;
			}
			if (seqPreffix_p) seqS = seqPreffix_p + seqS;
			cHtml = "<div class='JSDHiLite-seq' style='position: absolute; right: 0px; top: 0px; border-bottom-left-radius: 10px; padding-left: 5px; padding-right: 3px;'>" + seqS + "</div>";
			var dragElem = $("<div/>", 
				{ uid: wrapObj.objData.uid,
				  class: "JSDHiLite-item " + typeClass_p,
				  on: {
						"mousedown": function(event) {
							dragMode = true;
							mouseDownPos.left = event.pageX;
							mouseDownPos.top = event.pageY;
						},
						"mouseup": function(event) {
							dragMode = false;
						},
						"mousemove": function(event) {
							if (dragMode && (
									Math.abs(mouseDownPos.left - event.pageX) > 2 ||
									Math.abs(mouseDownPos.top - event.pageY) > 2)) {
								dragMode = false;
								canvasTracker.startTrack(event, dragElem, move, moveEnd);
							}
						},
						"click": function (event) {
							if (!dragMode) {
								$(this).remove();
							}
						}
				   },
				   append: cHtml,
				   css: dragPos,
				   appendTo: canvasElem_p
				});
			dragElem.css({cursor: "default", position: "absolute"});
		}
	}
	
	function getUIDs (typeClass_p) {
		var uidList = [];
		$("." + typeClass_p).each(function(ids) {
			uidList.push($(this).attr("uid"));
		});
		
		return uidList;
	}
	
	function toggleHiLite(wrapper_p, typeClass_p) {
		var item = $(".JSDHiLite-item [uid='" + wrapper_p.objData.uid).remove();
		if (item.length == 0) {
			hiLite ([wrapper_p], typeClass_p);
		}
	}
	
	init();
	return {
		clear: clear,
		resetSeq: resetSeq,
		hiLite: hiLite,
		getUIDs: getUIDs,
		toggleHiLite: toggleHiLite
	}
}

//state ghost and actions
function LassoMgr (diagram_p, canvasElem_p, canvasTracker_p) {
	var diagram = diagram_p;
	var canvasTracker = canvasTracker_p;
	var dragElem;
	var targetElem;
	var targetUID;
	var stateWrapList;
	var dragPos;
	
	function init() {
		dragElem = $("<div/>", 
			{ id: "JSDLasso-drag",
			  class: "JSD-ghost-elem",
			  appendTo: canvasElem_p
			});
	}
	
	function close() {
		if (dragElem) dragElem.removeClass("JSDGhost-show");
	}
	// moving ghost elem
	function move (mouseTrack_p) {
		dragPos = {
			left: mouseTrack_p.startX,
			top: mouseTrack_p.startY
		}
		if (mouseTrack_p.deltaX < 0) {
			dragPos.left += mouseTrack_p.deltaX;
			dragPos.width = -mouseTrack_p.deltaX;
		}
		else {
			dragPos.width = mouseTrack_p.deltaX;
		}
		if (mouseTrack_p.deltaY < 0) {
			dragPos.top += mouseTrack_p.deltaY;
			dragPos.height = -mouseTrack_p.deltaY;
		}
		else {
			dragPos.height = mouseTrack_p.deltaY;
		}
		dragElem.css (dragPos);
	}

	// commit state move based on ghost state move delta
	function moveEnd (mouseTrack_p) {
		dragElem.removeClass("JSDGhost-show");
		dragPos.left2 = dragPos.left + dragPos.width;
		dragPos.top2 = dragPos.top + dragPos.height;
		var uidList = [];
		for (var i in stateWrapList) {
			var s = stateWrapList[i];
			var posInfo = s.getAbsolutePos();
			if (!s.objData.readOnly && posInfo.left >= dragPos.left && posInfo.left + posInfo.width <= dragPos.left2 &&
				posInfo.top >= dragPos.top && posInfo.top + posInfo.height <= dragPos.top2) {
				uidList.push(s.objData.uid);
			}
		}
		close();
		diagram.hiLiteObj (uidList);
	}

	function show (stateWrapList_p, event_p) {
		stateWrapList = stateWrapList_p;
		targetElem = $(event_p.target);
		targetUID = targetElem.prop("id");
		dragPos = {
			left: event_p.pageX,
			top: event_p.pageY,
			width: 2,
			height: 2
		}
		$(".JSDGhost-show").removeClass("JSDGhost-show");
		dragElem.css(dragPos).addClass("JSDGhost-show");
		canvasTracker.startTrack(event_p, targetElem, move, moveEnd);
	}
	
	init();
	return {
		show: show,
		close: close
	}
}



