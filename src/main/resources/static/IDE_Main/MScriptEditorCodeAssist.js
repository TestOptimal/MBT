// copyright 2008 - 2018, TestOptimal, LLC, all rights reserved.
// mScriptCodeAssist.js

// var keys = {"uArr": 38, "dArr": 40, "lArr": 37, "rArr": 38, "enter": 13, "tab": 9, "bspace": 8, "keyA": 65, "keyz": 122};
// var keys = {"esc": 27, "space": 32, "enter": 13, "uArr": 38, "dArr": 40, "lArr": 37, "rArr": 38, "enter": 13, "tab": 9, "bspace": 8, "keyA": 65, "keyC": 67, "keyV": 86, "keyz": 122};
// var codeAssistKeys = [keys.uArr, keys.dArr, keys.enter, keys.tab, keys.bspace, keys.lArr];

// search dialog
function initSearch() {
	var btnElem = document.getElementById("nextBtn");
    CodeMirror.on(btnElem, "click", searchNext);
	btnElem = document.getElementById("prevBtn");
    CodeMirror.on(btnElem, "click", searchPrev);
	btnElem = document.getElementById("replaceBtn");
    CodeMirror.on(btnElem, "click", replaceText);
	btnElem = document.getElementById("replaceAllBtn");
    CodeMirror.on(btnElem, "click", replaceAllText);
	btnElem = document.getElementById("closeSrch");
    CodeMirror.on(btnElem, "click", closeSearch);
	btnElem = document.getElementById("srchField");
    CodeMirror.on(btnElem, "click", function(event) {
		CodeMirror.e_stop(event);
    	if ($("#srchField").val()=="search text") {
    		$("#srchField").val("").css("font-color", "#000000");
    	}
    });
    
	btnElem = document.getElementById("replaceField");
    CodeMirror.on(btnElem, "focus", function(event) {
		CodeMirror.e_stop(event);
		$("#searchDialog .replace").show();
    	if ($("#replaceField").val()=="replace text") {
    		$("#replaceField").val("").css("font-color", "#000000");
    	}
    });
    CodeMirror.on(btnElem, "click", function(event) {
		CodeMirror.e_stop(event);
	});
    
	btnElem = document.getElementById("searchDialog");
    CodeMirror.on(btnElem, "click", function(event) {CodeMirror.e_stop(event); closeSearch();});

    var srchField = document.getElementById("srchField");
    CodeMirror.on(srchField, "keydown", function(event) {
        var code = event.keyCode;
        // Enter
        if (code == 13) {searchNext(event);}
        // Escape
        else if (code == 27) {CodeMirror.e_stop(event); closeSearch();}
        else if (code == 38) { searchPrevUpArrow();}
        else if (code == 40) { searchNextDownArrow();}
        else {
        	searchCursor = null;
        	setTimeout(searchNext, 50);
        }
    });	
    
    var srchField = document.getElementById("replaceField");
    CodeMirror.on(srchField, "keydown", function(event) {
        var code = event.keyCode;
        // Enter
        if (code == 13) {replaceText(event);}
        // Escape
        else if (code == 27) {CodeMirror.e_stop(event); closeSearch();}
        else if (code == 38) { searchPrevUpArrow();}
        else if (code == 40) { searchNextDownArrow();}
    });	

    $("#srchField, #replaceField").focus(function() {
    	this.select();
    });
}

function openSearch() {
	$("#searchDialog").show();
	$("#searchDialog .replace").hide();
	$("#searchMsg").text("");
	searchStartPos = editor.getCursor();
	$("#srchField").focus();
	
	var scriptText = editor.getSelection();
	$("#srchField").val(scriptText);
}

function getSearchCursor(searchStartPos) {
	if (searchStartPos || searchCursor==null) {
		var query = $("#srchField").val();
	    var isRE = query.match(/^\/(.*)\/([a-z]*)$/);
	    if (isRE) {
	    	query = new RegExp(isRE[1], isRE[2].indexOf("i") == -1 ? "" : "i");
	    }
	    if (searchStartPos==null) {
		    searchStartPos = editor.getCursor();
		    searchStartPos.ch = 0;
		}
//	 	searchCursor = editor.getSearchCursor(query, searchStartPos, typeof query == "string" && query == query.toLowerCase());
	 }
	 return searchCursor;
}

var searchCursor = null;
var searchStartPos = null;
var lastSearchNext = true;
var lastSearchMatchFrom = undefined;

function searchNext(event) {
	if (event) CodeMirror.e_stop(event);
	var cursor = getSearchCursor();
	var found = cursor.findNext();
	// any key down has no event
	if (found && event && lastSearchMatchFrom &&
		cursor.from().line == lastSearchMatchFrom.line &&
		cursor.from().ch == lastSearchMatchFrom.ch) {
		found = cursor.findNext();
	}
	
	if (found) {
		editor.setSelection(cursor.from(), cursor.to());
		lastSearchMatchFrom = cursor.from();
	}
	lastSearchNext = true;
}
 
function searchPrevUpArrow() {
	searchPrev(true);
}

function searchNextDownArrow() {
	searchNext(true);
}

function searchPrev(event) {
	if (event) CodeMirror.e_stop(event);
	var cursor = getSearchCursor();
	var found = cursor.findPrevious();
	if (found && event && lastSearchMatchFrom && 
		cursor.from().line == lastSearchMatchFrom.line &&
		cursor.from().ch == lastSearchMatchFrom.ch) {
		found = cursor.findPrevious();
	}
	if (found) {
		editor.setSelection(cursor.from(), cursor.to());
		lastSearchMatchFrom = cursor.from();
	}
	lastSearchNext = false;
}

function replaceText(event) {
	if (event) CodeMirror.e_stop(event);
	var cursor = getSearchCursor();
	cursor.replace($("#replaceField").val());
	var dir = "Prev";
	if (lastSearchNext) {
		searchNext();
		dir = "Next";
	}
	else {
		searchPrev();
	}
	
	$("#searchMsg").text("Replaced " + dir + ": 1");
}

function replaceAllText(event) {
	if (event) CodeMirror.e_stop(event);
	var cursor = getSearchCursor({line: 0, ch: 0});
	var replaceString = $("#replaceField").val();
	var replaceCount = 0;
	var foundMatch = cursor.findNext();

	while (foundMatch) {
		cursor.replace(replaceString);
		replaceCount += 1;
		foundMatch = cursor.findNext();
//		if (!foundMatch) break;
	}
	
	$("#searchMsg").text("Replaced " + replaceCount + " occurrence(s)");
	editor.focus();
}

function closeSearch() {
	$("#searchDialog").hide();
	searchCursor = null;
	editor.focus();
}


// requires customization to fold.js: added forceFold, two IFs using forceFold
function foldAll(foldIt_p) {
	var lineText = null;
	var lineIdx = 0;
	var nextLineIdx = 0;
	var foldLineNum = 0;
	var foldEndToken = null;
	while (true) {
		lineText = editor.getLine(nextLineIdx);
		if (lineText==null) break;
		lineIdx = nextLineIdx;
		nextLineIdx += 1;
		var lineTrim = lineText.trim();
		if (lineText==null) break;
		if (foldEndToken==null) {
			if (lineTrim.substring(lineTrim.length-2)=="/>" ||
				lineTrim.indexOf("<mscript")==0) {
				continue;
			}
			var idx = lineText.indexOf("<");
			if (idx<0) continue;
			foldEndToken = lineText.substring(0, idx+1);
			foldLineNum = lineIdx;
		}
		else {
			if (lineText.indexOf(foldEndToken)!=0) continue;

			if (lineText.substring(foldEndToken.length, foldEndToken.length+1)=="/") {
				foldFunc_XML(editor, foldLineNum, foldIt_p);
			}
			foldEndToken = null;
		}
	}
	editor.focus();
}

function mscriptModeImpl (config, parserConfig) {
    var mscriptOverlay = {
	    token: function(stream, state) {
	        var ch;
		    var RE = /[0-9a-zA-Z._ (]/;
	        if (stream.match("\$")) {
			   	var chCount = 0;
			   	var funcName = "";
	        	while ((ch = stream.next()) != null) {
			  		chCount+=1;
				    if (!ch.match(RE)) return null;
				    if (ch == "$") return null;
		            if (ch == "(") {
					    if (chCount<=1) return null;
						break;
				  	}
			  		funcName += ch;
				  	var nextCh = stream.peek();
				  	if (ch==" " && (nextCh!="(" && nextCh!=" ")) return null;
				}
				if (chCount > 0) stream.backUp(1);
				if (isFuncValid(funcName)) {
		        	return "mscriptFunc";
		        }
		        else return null;
		    }
	      	while (stream.next() != null && !stream.match("\$", false)) {}
	      	return null;
	    }
  	};
  	return CodeMirror.overlayMode(CodeMirror.getMode(config, parserConfig.backdrop || "text/html"), mscriptOverlay);
}
