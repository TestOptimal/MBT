function initEditor (scriptText, changeHandler, focusHandler, scriptIndentNum, fileSaveCB) {
	var txtArea = document.getElementById("scriptEditor");
	if (txtArea) {
		txtArea.value = scriptText;
	    editor = CodeMirror.fromTextArea(txtArea, {
		    lineNumbers: true,
			indentUnit: scriptIndentNum,
			tabSize: scriptIndentNum,
			autofocus: true,
			autoRefresh: true,
			fixedGutter: true,
			highlightSelectionMatches: {showToken: /\w/, annotateScrollbar: true},
			styleActiveLine: true,
			matchBrackets: true,
			mode: {name: "text/x-groovy", globalVars: true},
			gutters: ["CodeMirror-lint-markers", "CodeMirror-foldgutter"],
			lint: true,
			indentWithTabs: true,
			foldGutter: true,
			gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
		});

		editor.setOption("theme", "to");
		editor.on("change", changeHandler);
		CodeMirror.commands.save = function(insance) { fileSaveCB(); };
		editor.on("focus", focusHandler);
		return editor;
	}
}

function toggleComment(cm) {
    cm.toggleComment();
}      

function traceAttr (cm) {
	var cursor = cm.getCursor(), lineText = cm.getLine(cursor.line);
	var ret = { line: cursor.line, replaceStart: cursor.ch, replaceEnd: cursor.ch };

	// find replacement string by looking forward and backward
	while (ret.replaceStart && /\w/.test(lineText.charAt(ret.replaceStart - 1))) --ret.replaceStart;
	while (ret.replaceEnd < lineText.length && /\w/.test(lineText.charAt(ret.replaceEnd))) ++ret.replaceEnd;
	ret.filterWord = lineText.slice(ret.replaceStart, cursor.ch);
	ret.prevChar = lineText.charAt(ret.replaceStart - 1);
	
	// abort if not dot notation
	if (ret.prevChar != '.') {
		if (ret.prevChar=='$') {
			ret.replaceStart--;
		}
		return ret;
	}
	
	// find call function stack before "."
	var pos = ret.replaceStart - 2;
	ret.prevAttrs = [];
	ret.prevAttrs = findTrace(cm, ret.line, pos);
    
    // trace to top CA key through var references
    var key = ret.prevAttrs[0];
	var vL = ret.line;
	while ( --vL > 0) {
		var lineText = cm.getLine(vL);
		var regExpr = new RegExp ('^([\\s\\t]*)(var )*(\\s*)' + key + '(\\s*)=');
		if (regExpr.test(lineText)) {
			// find end of expr
			var exprLineNum = vL;
			while (true) {
				if (/^.*[;][\b\t]*$/.test(lineText)) break;
				lineText = cm.getLine(exprLineNum+1);
				if (/^[\b\t]*\.(.)*$/.test(lineText)) exprLineNum++;
				else break;
			}

			ret.prevAttrs.splice(0,1);
			lineText = cm.getLine(exprLineNum);
			ret.prevAttrs = findTrace(cm, exprLineNum, lineText.lastIndexOf(')')).concat(ret.prevAttrs);
			key = ret.prevAttrs[0];
		}
	}

	// handle the top/first key referencing imported class
	vL = 0;
	var lineText;
	var importRegExp = new RegExp("^import (.)*." + ret.prevAttrs[0] + "(;)?( )*$");
	while (true) {
		var lineText = cm.getLine(vL++).trim();
		if (lineText.length == 0 || lineText.indexOf("//")==0) continue;
		if (lineText.indexOf("import ")!=0) break;
		if (importRegExp.test(lineText)) {
			ret.prevAttrs[0] = lineText.substring(7);
			var idx = ret.prevAttrs[0].indexOf(";");
			if (idx>0) {
				ret.prevAttrs[0] = ret.prevAttrs[0].substring(0,idx);
			}
		}
	}

    return ret;
}

function findTrace (cm, line, pos) {
	var lineText = cm.getLine(line);
	while (line >=0 && /^[\b\t]*\.(.)*/.test(lineText)) {
		var prevLine = cm.getLine(--line).trim();
		var dotIdx = lineText.indexOf(".");
		lineText = prevLine + lineText.substring(dotIdx); 
		pos += prevLine.length - dotIdx;
	}
	var prevAttrs = [];
	var prevPos = pos;
	while (lineText.charAt(pos) == ')') {
		prevPos = matchOpenParan(cm, lineText, pos);
		if (prevPos < 0) {
			break;
		}
		pos = prevPos--;
		while (prevPos >= 0 && /[a-zA-Z_0-9]/.test(lineText.charAt(prevPos))) --prevPos;
		prevAttrs.unshift(lineText.slice(prevPos+1, pos));
		pos = prevPos - 1;
		if (pos <= 0 || lineText.charAt(prevPos) != '.') {
			break;
		}
	}
	if (prevPos > 0) {
		var funcChar = /[a-zA-Z0-9._$]/;
		while (prevPos >= 0 && funcChar.test(lineText.charAt(prevPos))) --prevPos;
		prevAttrs.unshift(lineText.slice(prevPos+1, pos+1));
	}
    return prevAttrs;
}

// chIdx is the position of the ")" to match
function matchOpenParan(cm, lineText, chIdx) {
    var stack = 0;
	for (; chIdx >= 0; chIdx--) {
		var ch = lineText.charAt(chIdx);
		if (/[()]/.test(ch)) {
			if (ch== ")") stack++; // found another ")"
			else if (stack <= 1) return chIdx;
			else stack--;
		}
    }
    return -1;
}

function showHintGroovy (cm) {
	var selectRange = { from: cm.getCursor(true), to: cm.getCursor(false) }
	var cursor = cm.getCursor(), lineText = cm.getLine(cursor.line);

	var hOpt = traceAttr(cm);
	if (hOpt) {
		if ("$'\"+-*/,(>=<[ \t".indexOf(hOpt.prevChar) >=0 ) {
			hOpt.hintList = MScriptHintList.TOPLEVEL;
			cm.setOption("hintOptions", hOpt);
			CodeMirror.showHint(cm, MScriptHint);
		}
		else if (hOpt.prevAttrs) {
			parent.curAppState.toSvc.SysSvc.getCAListByExpr (hOpt.prevAttrs, function(caList_p) {
				hOpt.hintList = caList_p;
				cm.setOption("hintOptions", hOpt);
				CodeMirror.showHint(cm, MScriptHint);
			});
		}
	}
}


// possible to combine MScriptHint and showList by moving the logic to determine the
//   srchList out to specific CTRL-key function.
function MScriptHint(cm, option) {
	return new Promise(function(accept) {
		setTimeout(function() {
			var hintList = [];
			var srchList = cm.options.hintOptions.hintList;
			var word = cm.options.hintOptions.filterWord;
			for (var i in srchList) {
				if (srchList[i].displayText.indexOf(word) == 0) {
					hintList.push(srchList[i]);
				}
			}
		
			return accept({list: hintList,
					from: CodeMirror.Pos(cm.options.hintOptions.line, cm.options.hintOptions.replaceStart),
					to: CodeMirror.Pos(cm.options.hintOptions.line, cm.options.hintOptions.replaceEnd)});
				}, 10);
		})
}


// @TRIGGER('uid')
function parseTriggerScript (scriptLines) {
	const triggerExpr = new RegExp("\\@TRIGGER[ ]*\\([ ]*['\"](.+?)['\"][ ]*\\).*");
	var nodeDataList = parentWinObj.curAppState.nodeDataList;
	var triggerMap = {};
	var lineIdx = 0;
	var retList = [];
	scriptLines.forEach(function (line) {
		if ((groups = triggerExpr.exec(line))) {
			var uid = groups[1];
			var nodeData = nodeDataList[uid];
			trigger = {
				lineIdx: lineIdx,
				level: 1,
				name: uid,
				desc: uid,
				nodeData: nodeData,
				children: []
			}
			if (!nodeData) {
				retList.push(trigger);
				triggerMap[uid] = trigger;
			}
			else if (nodeData.typeCode=="state") {
				trigger.desc = nodeData.stateID;
				retList.push(trigger);
				if (triggerMap[uid]) {
					trigger.children = triggerMap[uid].children;
				}
				triggerMap[uid] = trigger;
			}
			else if (nodeData.typeCode=="transition"){
				var parentTrigger = triggerMap[nodeData.parentuid];
				if (!parentTrigger) {
					// out of sync or state doesn't have trigger
					parentNode = nodeDataList[nodeData.parentuid];
					parentTrigger = {
						lineIdx: lineIdx,
						level: 1,
						name: parentNode.uid,
						desc: parentNode.stateID,
						nodeData: parentNode,
						children: [],
						filler: true
					}
					triggerMap[nodeData.parentuid] = parentTrigger;
					retList.push(parentTrigger);
				}
				trigger.desc = nodeData.event;
				parentTrigger.children.push(trigger);
			}
		}
		lineIdx++;
	});
	return retList;
}

