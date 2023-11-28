// copyright 2008 - 2020, TestOptimal, LLC, all rights reserved.
// dialogUtil.js

// dialog and confirm popup, requires faxbox.js
function alertDialog(msg_p) {
//	if (inIFrame && parentWinObj) {
//		parentWinObj.alertDialog(msg_p);
//		return;
//	}
	
	if (msg_p) {
		msg_p = msg_p.replace(/"/g, "'");
	//	alertDialogBase( msg_p);
		setTimeout (function() { alertDialogBase( msg_p);}, 300);
	}
}


// dialog and confirm popup, requires faxbox.js
function alertDialogBase(msg_p) {
//	if (msg_p=="change.saved" || msg_p=="model.started") {
//		setTimeout ("jQuery(document).trigger('close.facebox')", 1200);
//	}
	msg_p = translateMsg(msg_p);
	if ($("#facebox").is(":visible")) {
		$("#alertMsg").append("<p>" + msg_p+"</p>");
		$(document).trigger('reveal.facebox');
	}
	else {
		jQuery.facebox("<div class=alertTable style='overflow:auto;'><span id=alertMsg><p>" + msg_p + "</p></span>"
	    	+ "<br/><button style='float:right;margin-top: 8px;' id=alertOkBtn onclick='closeAlertDialog();'>OK</button>"
			+ "</div>");
	}
	
	$("#facebox #alertOkBtn").focus();
}

function closeAlertDialog() {
	$.facebox.close();
}

function plainDialog(msg_p, yesFunc, focusFieldID_p, cbFunc) {
	if (inIFrame && parentWinObj) {
		parentWinObj.plainDialog(msg_p, yesFunc, focusFieldID_p, cbFunc);
		return;
	}
	
	mbtYesFunc = yesFunc;
	mbtNoFunc = null;
	if ($("#facebox").is(":visible")) {
		$("#alertMsg").append("<p>" + msg_p+"</p>");
		$(document).trigger('reveal.facebox');
	}
	else {
		jQuery.facebox("<div class=alertTable style='overflow:auto;'><span id=alertMsg><p>" + msg_p + "</p></span><br/><button style='float:right;margin-top: 8px;' id=dialogOkBtn>OK</button></div>");
	}
	if (focusFieldID_p) {
		$("#" + focusFieldID_p).focus().select();
	}
	else {
		$("#dialogOkBtn").focus();
	}

	$("#dialogOkBtn").click(function() { confirmDialogAction(true); });
	if (cbFunc) {
		cbFunc.apply();
	}
}

function getDialogField(fldID_p) {
	var firstChar = fldID_p.substring(0,1);
	if (firstChar!="." && firstChar!="#") {
		fldID_p = "#" + fldID_p; 
	}
	return $(fldID_p);
}

var mbtYesFunc;
var mbtNoFunc;
function confirmDialog(msg_p, yesFunc, noFunc, okLabel, cancelLabel) {
	mbtYesFunc = yesFunc;
	mbtNoFunc = noFunc;
	msg_p = translateMsg(msg_p);
	if (okLabel==undefined || okLabel=="") okLabel = "OK";
	if (cancelLabel==undefined || cancelLabel=="") cancelLabel = "Cancel";
	jQuery.facebox("<div class='alertTable'><span id=alertMsg><p>" + msg_p + "</p></span>"
	    	+ "<br/><div style='float:right;'><button id=okBtn onclick='javascript:confirmDialogAction(true)'>"
	    	+ okLabel + "</button><button id=cancelBtn onclick='javascript:confirmDialogAction(false);'>"
	    	+ cancelLabel + "</button></div></div>");
//	scrollToFacebox();
	$("#cancelBtn").focus();
}

function confirmDialogAction(action_p) {
	$.facebox.close();
	if (action_p==true) {
		if (mbtYesFunc!=undefined) mbtYesFunc.apply();
	}
	else {
		if (mbtNoFunc!=undefined) mbtNoFunc.apply();
	}
//	scrollToFacebox();
}

function promptDialog(msg_p, defaultValue_p, yesFunc) {
	mbtYesFunc = yesFunc;
	mbtNoFunc = null;
	msg_p = translateMsg(msg_p);
	jQuery.facebox("<div class='alertTable'><span id=alertMsg><p>" + msg_p + "</p></span>"
			+ "<input type='text' size='60' id='promptField' value=\"" + defaultValue_p + "\" onkeypress='dialogDefaultAction(event);'/>"
	    	+ "<br/><button style='float:right;margin-top: 8px;' id=dialogOkBtn onclick='javascript:confirmDialogAction(true)'>OK</button></div>");
//	scrollToFacebox();
	$("#promptField").focus();
}

function getPromptVal() {
	return $("#promptField").val();
}

function promptSelectDialog(msg_p, selectLabel_p,  optionList_p, promptLabel_p, defaultValue_p, yesFunc) {
	mbtYesFunc = yesFunc;
	mbtNoFunc = null;
	msg_p = translateMsg(msg_p);
	var selectHtml = "<div class='alertTable'><span id=alertMsg><p>" + msg_p + "</p></span>"
		+ "<table><tr><td>" + selectLabel_p + "</td><td><select id='selectField' onkeypress='dialogDefaultAction(event,\"okBtn\");'>";
	for (var i=0; i<optionList_p.length; i++) {
		var selected = "";
		var optionLabel = optionList_p[i];
		var optionVal = optionLabel;
		if (optionLabel.val) {
			optionVal = optionLabel.val;
			optionLabel = optionLabel.label;
		}
		if (optionVal==defaultValue_p) selected = "selected";
		selectHtml = selectHtml + "<option value=\"" + optionVal + "\" " + selected + ">" 
			+ optionLabel + "</option>";
	}
	selectHtml += "</td></tr><tr><td>" + promptLabel_p + "</td><td><input type='text' size='60' id='promptField' value=\"" + defaultValue_p + "\" onkeypress='dialogDefaultAction(event);'/></td></tr>"
	    	+ "</table><button style='float:right;' id=dialogOkBtn onclick='javascript:confirmDialogAction(true)'>OK</button></div>";
	jQuery.facebox(selectHtml);
//	scrollToFacebox();
	$("#selectField").focus();
//	$("#promptField").focus();
}

function selectDialog(msg_p, optionList_p, defaultValue_p, yesFunc) {
	mbtYesFunc = yesFunc;
	mbtNoFunc = null;
	msg_p = translateMsg(msg_p);
	var selectHtml = "<div class='alertTable'><span id=alertMsg><p>" + msg_p + "</p></span>"
		+ "<br><select id='selectField' onkeypress='dialogDefaultAction(event);'>";
	for (var i=0; i<optionList_p.length; i++) {
		var selected = "";
		var optionLabel = optionList_p[i];
		var optionVal = optionLabel;
		if (optionLabel.val) {
			optionVal = optionLabel.val;
			optionLabel = optionLabel.label;
		}
		if (optionVal==defaultValue_p) selected = "selected";
		selectHtml = selectHtml + "<option value=\"" + optionVal + "\" " + selected + ">" 
			+ optionLabel + "</option>";
	}
	selectHtml = selectHtml + "</select>"
		+ "<br><button style='float:right;' id=dialogOkBtn onclick='javascript:confirmDialogAction(true)'>OK</button></div>"
	
	jQuery.facebox(selectHtml);
//	scrollToFacebox();
	$("#selectField").focus();
}

function getSelectedVal() {
	return $("#selectField option:selected").val();
}

function getSelectedLabel() {
	return $("#selectField option:selected").text();
}

// used by facebox for default action on enter key
function dialogDefaultAction(e) {
  // look for window.event in case event isn't passed in
  if (window.event) { 
  	 e = window.event; 
  }
  if (e.keyCode == 13) {
     $("#dialogOkBtn").click();
  }
} 

function getDisplayTime(time_p) {
	var timeString = "";
	if (time_p.getHours()<10) timeString = "0" + time_p.getHours();
	else timeString = time_p.getHours();
	
	if (time_p.getMinutes()<10) timeString += ":0" + time_p.getMinutes();
	else timeString += ":" + time_p.getMinutes();
	
	if (time_p.getSeconds()<10) timeString += ":0" + time_p.getSeconds();
	else timeString += ":" + time_p.getSeconds();
	return timeString;
}

function clone(obj){
    if(obj == null || typeof(obj) != 'object')
        return obj;

    var temp = new obj.constructor();
    for(var key in obj)
        temp[key] = clone(obj[key]);

    return temp;
}


//replaces the @snapTS:nnnnn@ with hyper link to display screenshot.
function resolveSnapID(msg_p, execID_p) {
	var snapIdx = msg_p.indexOf("@snapTS:");
	var execID = "";
	if (execID_p) {
		execID = "&execID=" + execID_p;
	}
	while (snapIdx>=0) {
		var snapId = msg_p.substring(snapIdx+8);
		var snapIdx2 = snapId.indexOf("@");
		var msgTail = snapId.substring(snapIdx2+1);
		snapId = snapId.substring(0, snapIdx2);
		msg_p = msg_p.substring(0, snapIdx) 
			+ " <a href='app=webmbt&action=getSnapScreen&snapTime=" + snapId 
			+ execID + "&rand=" + Math.random() + "' target='_blank'>ScreenShot</a>" + msgTail;
		snapIdx = msg_p.indexOf("@snapTS:");
	}
	return msg_p;
}

