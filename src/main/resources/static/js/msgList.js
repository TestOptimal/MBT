// copyright TestOptimal, LLC, all rights reserved.
// msgList.js

var gMsgList = new Array();

var msgIDE = "TestOptimal";

gMsgList["alert"] = {"code": "Alert Message", "desc": "System alert and warning message." };
gMsgList["versionAUT"] = {"code": "AUT Version", "desc": "AUT software version number." };
gMsgList["aut.not.started"] = {"code": "AUT Not Running", "desc": "AUT is not running."};
gMsgList["assertID"] = {"code": "Assert ID", "desc": "Unique id to be assigned to the defect found." };

gMsgList["build"] = {"code": "Model Build Num", "desc": "Model build (archive) number (auto incremented)" };

gMsgList["canvasHeight"] = {"code": "Canvas Height (px)", "desc": "Modeling canvas height in px."};
gMsgList["canvasWidth"] = {"code": "Canvas Width (px)", "desc": "Modeling canvas width in px."};
gMsgList["change.not.allowed"] = {"code": "Change Not Allowed", "desc": "Changes not allowed."};
gMsgList["change.ok2discard"] = {"code": "Change Discard", "desc": "There are pending changes in Property tab, do you wish to discard the changes?"};
gMsgList["change.save.first"] = {"code": "Change Save First", "desc": "Please save the changes first."};
gMsgList["change.pending"] = {"code": "Changes Pending", "desc": "There are pending unsaved changes!"};
gMsgList["char.not.allowed"] = {"code": "Char Not Allowed", "desc": "Character @@ is not allowed for @@. Replaced it with alternative char @@"};
gMsgList["color"] = { "code": "Color", "desc": "Color for the state and transition" };
gMsgList["config"] = { "code": "Config", "desc": "System Info" };
gMsgList["coverageType"] = { "code": "Coverage Type", "desc": "For Optimal sequencers only, to cover all transitions or all paths." };
gMsgList["cssStyle"] = {"code": "CSS Style", "desc": "CSS style string"};
gMsgList["curFolder"] = {"code": "Current Folder", "desc": "The folder path for the current model or new model being created."};

gMsgList["objective"] = { "code": "Objective", "desc": "Optimize on total # of traversals or on total execution elapsed time." };

gMsgList["desc"] = {"code": "Description", "desc": "Enter a description for the model" };
gMsgList["delete.select.file"] = {"code": "Select File to Delete", "desc": "Please select files to delete." };
gMsgList["delete.confirm"] = {"code": "Confirm Delete", "desc": "Do you wish to delete @@?" };
gMsgList["file.delete.confirm"] = {"code": "Confirm Delete", "desc": "Do you wish to delete the following? @@" };
gMsgList["delete.stat.confirm"] = {"code": "Confirm Delete", "desc": "Do you wish to delete state @@?" };
gMsgList["delete.failed"] = {"code": "Unable to delete model", "desc": "Unable to delete the model. System deletes the model by renaming the model folder to _DELETED_*. Use Cleanup to remove all DELTED folders." };
gMsgList["delete.node.confirm"] = {"code": "Confirm Delete Node", "desc": "Deleting a node (state or transition) will result in deletion of the steps referencing this node and all its children states/transitions. Do you wish to continue?" };
gMsgList["delete.demo"] = {"code": "Delete Demo", "desc": "Demo model can not be deleted." };
gMsgList["demo.save"] = {"code": "Save not allowed in demo mode", "desc": "This is a demo server, save not allowed." };
gMsgList["delete.submodel.refd"] = {"code": "Submodel Referenced", "desc": "Submodel is referenced, can not be deleted." };

gMsgList["email"] = {"code": "Email Address", "desc": "Licensed user email address." };
gMsgList["errored"] = {"code": "Errored", "desc": "Errored"};
gMsgList["event.invalid"] = {"code": "Invalid Event", "desc": "Event @@ is not a valid event for state @@"};
gMsgList["event"] = {"code": "Trans. Name", "desc": "Name of the transition or edge."};
gMsgList["exec"] = {"code": "Executing", "desc": "Executing"};
gMsgList["exec.model.changed"] = {"code": "Changes pending", "desc": "There are unsaved changes to the model."};
gMsgList["exec.not.paused"] = {"code": "Exec not paused", "desc": "Execution not paused. Requested action requires execution being paused."};

gMsgList["is.executing"] = {"code": "Executing", "desc": "MBT model is executing."};
gMsgList["field.required"] = {"code": "Required Field", "desc": "Field @@ is required."};
gMsgList["field.not2contain"] = {"code": "Not to Contain", "desc": "Field @@ may not contain @@."};
gMsgList["field.value.not.numeric"] = {"code": "Not Numeric", "desc": "@@ is not a number."};
gMsgList["field.value.min"] = {"code": "Min Value", "desc": "@@ must be at least @@"};
gMsgList["field.value.max"] = {"code": "Max Value", "desc": "@@ must be no more than @@"};
gMsgList["file.missing"] = {"code": "File missing", "desc": "Please enter name of file to import."};
gMsgList["file.deleted"] = {"code": "File Deleted", "desc": "The following file(s) have been deleted: @@"};
gMsgList["file.ok2delete"] = {"code": "Delete File?", "desc": "Do you wish to delete this model: @@"};
gMsgList["file.import"] = {"code": "File Import", "desc": "To import a model, select <b>FileList</b> tab, scroll to the bottom of the list and click on <b>Browse</b> button."};
gMsgList["file.name.illegal.char"] = {"code": "File Name Invalid Char", "desc": "Filename may not contain forward or backward slash."};
gMsgList["file.mscript.corrupted"] = {"code": "mScript file corrupted", "desc": "The mScript file for this model is corrupted.  You must manually edit the mscript.xml in the model folder to correct the error."};
gMsgList["file.rename"] = {"code": "Rename Model", "desc": "Enter new name for the model" };
gMsgList["file.select.first"] = {"code": "Select a file", "desc": "Please select a file or enter a name for the file to import."};
gMsgList["filename"] = {"code": "Model Name", "desc": "Model file name." };
gMsgList["filename.template"] = {"code": "Model Name", "desc": "Please change the model file name." };
gMsgList["folder.new"] = {"code": "New Folder", "desc": "New Folder: please enter folder name."};

gMsgList["graph.find"] = {"code": "Find State/Trans", "desc": "Find states/transitions by tag, state id or transition id." };
gMsgList["graphShowWeight"] = { "code": "Show Weight", "desc": "To show transition weight on graphs." };
gMsgList["guard"] = {"code": "Guard", "desc": "Guard condition boolean expression, e.g. $VAR.myVar > 10" };
gMsgList["guardResolvers"] = {"code": "Guard Resolver(s)", "desc": "UIDs of transitions that can resolve the guard." };

gMsgList["ide.reset.prompt"] = {"code": "Reset IDE Layout?", "desc": "Do you wish to reset IDE screen layout?" };
gMsgList["ipaddress"] = {"code": "IP Address", "desc": "IP address of " + msgIDE + " server." };
gMsgList["isInitial"] = {"code": "Is Initial", "desc": "If this is an intial state / node. Each model and each super state/node must have one initial state/node." };
gMsgList["isFinal"] = {"code": "Is Final", "desc": "If this is a final state / node. Each model or super state/node can have at least one final state/node." };
gMsgList["import.model.open"] = {"code": "Requires Open Model", "desc": "Open an existing model or create a new model first." };
gMsgList["import.merge"] = {"code": "Merge Import with Model", "desc": "You have a model open, do you wish to merge the import file with the model?" };
gMsgList["item.delete.confirm"] = {"code": "Delete?", "desc": "Do you wish to delete @@?" };
gMsgList["item.deleted"] = {"code": "Deleted", "desc": "@@ has been deleted." };

gMsgList["edition"] = {"code": "Edition", "desc": "Licensed edition" };

gMsgList["left"] = {"code": "Node Left (px)", "desc": "Node's left position in pixel" };
gMsgList["top"] = {"code": "Node Top (px)", "desc": "Node's top position in pixel" };
gMsgList["load.no.server"] = {"code": "No Runtime server for LoadTesting", "desc": "Unable to find a Runtime server to execute the model" };
gMsgList["labelSize"] = {"code": "Label Size (px)", "desc": "Size of label field in pixel" };

gMsgList["markedRemoved"] = {"code": "Mark Removed", "desc": "Logically marking the state or transition to be removed from the model before execution." };
gMsgList["maxHistoryStat"] = {"code": "History Stats", "desc": "Number of history stats to be kept for this model." };
gMsgList["maxhistorystatE"] = {"code": "Number of History Stats", "desc": "Number of history stats to be kept for this model." };
gMsgList["maxMillis"] = {"code": "Response Max", "desc": "The maximum number of milliseconds the action must complete.  Action exceeding this limit will be tallied and reported, however they do not cause any exception." };
gMsgList["mbt"] = {"code": "MBT Config", "desc": "MBT execution configuration settings." };
gMsgList["mbt.exec.done"] = {"code": "Mbt Execution Completed", "desc": "Mbt execution @@"};
gMsgList["mbt.exec.warning"] = {"code": "Execution Alert", "desc": "Alert: @@"};
gMsgList["mbt.exec.aborted"] = {"code": "Mbt Execution Aborted", "desc": "Mbt execution stopped with error @@"};
gMsgList["method.param.too.few"] = {"code": "Method Params", "desc": "Missing parameters for mScript method" };
gMsgList["model.already.open"] = {"code": "Model Already Open", "desc": "The model is already open in another session" };

gMsgList["model.ok2delete"] = {"code": "Delete Model?", "desc": "Do you wish to delete this model?"};
gMsgList["model.deleted"] = {"code": "Model Deleted", "desc": "Deleted @@."};
gMsgList["model.not.open"] = {"code": "Model not open", "desc": "No model is currently open."};
gMsgList["model.not.found"] = {"code": "Model not found", "desc": "Unable to open the model."};
gMsgList["model.rename"] = {"code": "Rename Model", "desc": "Rename Model: change model name and click save."};
gMsgList["model.name.required"] = {"code": "Model Name Required", "desc": "Please enter a valid model name."};
gMsgList["model.exists"] = {"code": "Model Already Exists", "desc": "Model already exists."};
gMsgList["model.new.change.pending"] = {"code": "Model Changes Pending", "desc": "There are pending changes to the current model in memory. Please save these changes first."};
gMsgList["model.new"] = {"code": "New Model", "desc": "New Model: please enter a name."};
gMsgList["model.play"] = {"code": "Play/Animiate", "desc": "<em>Play/Animate Model</em><br><br>Enter a delay in milliseconds:"};
gMsgList["model.not.running"] = {"code": "Model is not running", "desc": "Model is not running."};
gMsgList["model.notselected"] = {"code": "Select a model", "desc": "No model is selected for paste/move operation."};
gMsgList["model.move.ok"] = {"code": "Model moved", "desc": "Model has been moved."};
gMsgList["model.copy.ok"] = {"code": "Model copied", "desc": "Model copy is completed."};
gMsgList["modelTreeViewSort"] = {"code": "Sort Model Tree View", "desc": "Sort states and transitions in Model Tree View."};
gMsgList["treeViewSort"] = {"code": "Model TreeView Sort", "desc": "How states and transitions are sorted."};
gMsgList["mScript.changed"] = {"code": "Changes not saved", "desc": "There are changes to the mScript. You must either accept (OK) the changes or cancel the changes before further editing other mScripts."};
gMsgList["model.not.authorized"] = {"code": "Not Authorized", "desc": "Access to the selected model not authorized."};
gMsgList["mScript.exec.not.paused"] = {"code": "Exec MScript", "desc": "mScript can only be executed when model execution is paused."};
gMsgList["mScript.change.not.allowed"] = {"code": "Change mScript", "desc": "Changes cannot be made to archived Model mScript."};
gMsgList["model.started"] = {"code": "Execution Started", "desc": "Model execution has started."};
gMsgList["model.completed"] = {"code": "Execution Completed", "desc": "Model execution has completed."};
gMsgList["mscript.comp.notallowed"] = {"code": "Compile Not Allowed", "desc": "Can not compile with unsaved changes pending."};
gMsgList["mscript.save.prev.error"] = {"code": "Unable to save mScript", "desc": "Unable to save mScript to mscript.xml due to previous parsing error. Delete mscript.xml (save a copy first) and try again. "};
gMsgList["mscript.save.extool"] = {"code": "Unable to save mScript", "desc": "Can not save mScript changes. mScript file has been changed by external tool. Rename mscript.xml file and try to save again."};
gMsgList["MScriptWrap"] = {"code": "MScript Wrap", "desc": "If to turn on word-wrapping in MScript Editor."};
gMsgList["mScriptWrapAttr"] = {"code": "MScript Auto-Format", "desc": "Auto-format MScript to separate attributes to one attribute per line."};
gMsgList["model.missing.initial"] = {"code": "Missing Initial State", "desc": "Model is missing the initial state."};
gMsgList["model.missing.final"] = {"code": "Missing Final State", "desc": "Model is missing final state(s)."};
gMsgList["model.multi.initial"] = {"code": "Too Many Initial States", "desc": "Multiple initial states found. Model can only have one initial state."};
gMsgList["modelType"] = {"code": "Model Notation", "desc": "Model type/notation: choose State Diagram (extended Finite State Machine) or Activity Diagram."};

gMsgList["name"] = {"code": "Name", "desc": "Name"};
gMsgList["not.supported"] = {"code": "Not Supported", "desc": "Not supported."};

gMsgList["osName"] = {"code": "OS Name", "desc": "Operating System ." };
gMsgList["osVersion"] = {"code": "OS Version", "desc": "Operating System Version." };

gMsgList["paused"] = {"code": "Paused", "desc": "Paused"};
gMsgList["pause.invalid"] = {"code": "Pause not allowed", "desc": "Pause action is invalid."};
gMsgList["popup.blocker"] = {"code": "Popup Blocker Enabled", "desc": "Popup blocker is preventing new window from opening." };
gMsgList["prompt.change"] = {"code": "Change", "desc": "Enter domain values separated by | and click OK." };

gMsgList["releaseDate"] = {"code": "Release Date", "desc": "Date when the current release was published." };
gMsgList["rt.error"] = {"code": "Runtime Error", "desc": "Runtime error has occurred, check server log file for details." };
gMsgList["runtime.readonly"] = {"code": "RunTime Edition", "desc": "Change not allowed with Runtime Edition." };
gMsgList["reportindex.updated"] = {"code": "Report Index Updated", "desc": "Report index page has been updated." };
gMsgList["runtime.save.notallowed"] = {"code": "Runtime Alert", "desc": "Changes to model not allowed in Runtime edition." };

gMsgList["save.model.prompt"] = {"code": "Save Model Changes", "desc": "Enter change comment" };
gMsgList["save.model.as"] = {"code": "Save Model As", "desc": "Save current model to a new name, optionally to a specified folder path. Use '/' for folder path." };
gMsgList["savePassed"] = {"code": "Save Passed Stats", "desc": "If to save all positive asserts. This may increase memory and db usage." };
gMsgList["scList"] = {"code": "ShortCut Keys", "desc": "ShortCut keys are defined with keySeq:action;keySeq:action. More info at http://testoptimal.com/wiki search for shortcut." };
gMsgList["scxml"] = {"code": "SCXML Model", "desc": "MBT Model" };
gMsgList["search.not.found"] = {"code": "No found", "desc": "No states/transitions found matching the criteria."};
gMsgList["select.change"] = {"code": "Select", "desc": "Choose from the dropdown list below and click OK." };

gMsgList["snapscreen.file.notfound"] = {"code": "Screenshot not found", "desc": "Screenshot file not found." };
gMsgList["stat.select"] = {"code": "Select a stat first", "desc": "Please select a stat first."};
gMsgList["stat.save.mbt.running"] = {"code": "MBT is running, no stat save", "desc": "Stats can not be saved while Mbt is executing."};
gMsgList["step.invalid"] = {"code": "Debug step not allowed", "desc": "Step over is only valid when model execution is paused."};
gMsgList["step.line"] = {"code": "Step over n mScript lines", "desc": "Resume execution by moving forward n mScript lines (e.g. +3) or pause at a specific mScript line number (e.g. 17)."};
gMsgList["removeFlag"] = {"code": "Mark Removed", "desc": "Flag indicates if the underlying state/transition to be removed before execution."};

gMsgList["serialNum"] = {"code": "Serial Number", "desc": "Computer serial number, used to request license key."};
gMsgList["stopped"] = {"code": "Stopped", "desc": "Stopped"};
gMsgList["state"] = {"code": "State Name", "desc": "Name of the state, unique within the model."};
gMsgList["stateid"] = {"code": "Name", "desc": "Unique name for the state / node."};
gMsgList["state.id.duplicate"] = {"code": "State ID", "desc": "State identifier is already used by another state in the model."};
gMsgList["stateid.illegal"] = {"code": "Illegal State ID", "desc": "State identifier contains reserve keyword or illegal chars."};

gMsgList["state.submodel.imported"] = {"code": "State SubModel Imported", "desc": "Submodel has been imported and added to the super state. Please save the changes and re-open the model to make the changes effective."};
gMsgList["state.submodel.notimported"] = {"code": "State SubModel Imported", "desc": "Submodel has not been imported due to exceptions."};
gMsgList["stat.del.keepit"] = {"code": "Stats can not be deleted", "desc": "Please unset the keep flat for these stats." };

gMsgList["stereotype"] = {"code": "Stereotype", "desc": "Stereotype, used to categorize states and transitions. Stereotype list can be customized by setting stateStereotypeList and transStererotype property in config.properties file."};
gMsgList["subModel"] = {"code": "Sub Model Name", "desc": "Name of the sub model for the state." };
gMsgList["subModel.missing.initialFinal"] = {"code": "Submodel missing initial or final states", "desc": "SubModel requires one initial state and at least 1 final state."};
gMsgList["submodelFinalState"] = {"code": "Submodel Final State", "desc": "Final state/node in the subModel to attach this transition/edge to."};
gMsgList["sync.submitted"] = {"code": "Sync Model Submitted", "desc": "Sync Model request has been submitted. Depending on the number of models and Runtime servers involved, Sync Model may take several minutes to complete."};

gMsgList["tags"] = {"code": "Requirement Tag", "desc": "Requirement tag covered" };
gMsgList["tags.mark"] = {"code": "Mark By Tags", "desc": "Highlight states/transitions by tags using exact match or RegExp" };
gMsgList["targetUID"] = {"code": "Target", "desc": "Target state/node of the transition/edge." };
gMsgList["template"] = {"code": "Model Template", "desc": "Template model to clone to create the new model"};
gMsgList["TestOptimalVersion"] = {"code": "Server Ver.", "desc": "Current server version." };
gMsgList["traverseTimes"] = {"code": "Traversals Required", "desc": "The minimum number of times this transition must be traversed." };
gMsgList["textColor"] = { "code": "Text Color", "desc": "Color for the state name." };

gMsgList["uid"] = {"code": "UID", "desc": "Unique id for each scxml (model) node"};

gMsgList["versionReq"] = {"code": "Req. Version", "desc": "Version number of the requirements specification." };
gMsgList["transition"] = {"code": "Transition", "desc": "A transition that represents a link or button on an AUT page (model state)." };

gMsgList["vrsn"] = {"code": "Model Version", "desc": "Model version number." };
gMsgList["versionTO"] = {"code": "TO Server Ver.", "desc": "Version of " + msgIDE + " this model was last updated with." };

gMsgList["weight"] = {"code": "Weight", "desc": "The weight assigned to this transition to determine the probablity this transition may be chosen among other transitions originating from the same state during the random and greed walk.  A higher value assigned in relation to other transition weights will cause this transition to be chosen relatively more often." };

gMsgList["width"] = {"code": "Node Width (px)", "desc": "Width of state node." };
gMsgList["height"] = {"code": "Node Height (px)", "desc": "Height of state node." };

gMsgList["playModelDelay"] = {"code": "Play Model Delay", "desc": "Number of milliseconds to pause between state/transition traversals"};

gMsgList["swimlaneName"] = {"code": "Set Name", "desc": "Swimlane set description." };
gMsgList["swimlaneCssStyle"] = {"code": "CSS Style", "desc": "Html CSS style to be applied to the swimlane element, e.g. \"background-color: orange;\"" };
gMsgList["laneLabels"] = {"code": "Lane Label List", "desc": "List of labels for the lanes separated by semi-colon. Example: lane 1, lane 2, lane 3" };
gMsgList["swimlaneOrient"] = {"code": "Orientation", "desc": "Swimlane orientation: Horizontal or Vertical" };

gMsgList["hint_addState_state"] = {"code": "add state", "desc": "click on canvas to create a state" };
gMsgList["hint_addState_initial"] = {"code": "add initial state", "desc": "click on canvas to create an initial  state" };
gMsgList["hint_addState_final"] = {"code": "add final state", "desc": "click on canvas to create a final state" };
gMsgList["hint_addSwimlane"] = {"code": "add swimlane", "desc": "click on canvas to create a set of swimlanes" };
gMsgList["hint_addBox"] = {"code": "add box", "desc": "click on canvas to create a box" };
gMsgList["hint_addState_branch"] = {"code": "add branch node", "desc": "click on canvas to create a branch node" };
gMsgList["hint_addState_switch"] = {"code": "add switch node", "desc": "click on canvas to create a switch node" };
gMsgList["hint_addTrans"] = {"code": "add trans", "desc": "click on state edge to select transition source state" };
gMsgList["hint_addTrans_target"] = {"code": "add trans", "desc": "select transition target state" };
gMsgList["hint_mark"] = {"code": "mark states/trans", "desc": "click on states and transitions to mark/unmark or left-mouse drag to mark multiple states." };

function translateMsg (msgKey_p, token1_p, token2_p, token3_p, token4_p, token5_p) {
	var transMsg = gMsgList[msgKey_p];
	if (transMsg==undefined) return msgKey_p; //return decodeURIComponent(msgKey_p);
	transMsg = transMsg.desc;
	if (token1_p==undefined) return transMsg; //decodeURIComponent(transMsg);
	transMsg = transMsg.replace("@@", token1_p);

	if (token2_p==undefined) return transMsg; //decodeURIComponent(transMsg);
	transMsg = transMsg.replace("@@", token2_p);

	if (token3_p==undefined) return transMsg; //decodeURIComponent(transMsg);
	transMsg = transMsg.replace("@@", token3_p);

	if (token4_p==undefined) return transMsg; //decodeURIComponent(transMsg);
	transMsg = transMsg.replace("@@", token4_p);

	if (token5_p==undefined) return transMsg; //decodeURIComponent(transMsg);
	transMsg = transMsg.replace("@@", token5_p);
	
	return transMsg; //decodeURIComponent(transMsg);
	
}
//
//function containsTransMessages() {
//	if (gMsgList==undefined) return false;
//	for (msgKey in gMsgList) {
//		return true;
//	}
//	return false;
//}
//
//function resolveSysMsg (msg_p) {
//	if (curAppState.nodeDataList["scxml"].modelType=="FSM") {
//		return msg_p;
//	}
//	
//	var ret = ActivityDiagramMsgList[msg_p];
//	
//	if (ret) return ret;
//	else return msg_p;
//}

//function isModelCFG() {
//	return curAppState.nodeDataList["scxml"] && curAppState.nodeDataList["scxml"].modelType=="CFG";
//}
//
//function isModelFSM() {
//	return curAppState.nodeDataList["scxml"].modelType=="FSM";
//}
