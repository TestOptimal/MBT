/**
 * (c) 2020 TestOptimal LLC, http://testoptimal.com
 * 
 * TOServer is used to perform client side web application testing by having
 * models built in TestOptimal to send test cases in a series of keyword to execute.  
 * 
 * TOServer requires access to TestOptimal server, trial download at:
 *     http://testoptimal.com/downloads
 *     
 * Below is a list of methods offered by TOServer, note all methods returns
 * Promise object which you will need to invoke .then(func) invoke the operation
 * and/or receive response from the server:
 * 
 * 1. execModel (execReq)
 *    starts model execution, model is specified in execReq parameter.
 * 	execReq: {
 * 		modelName: "DEMO_RemoteAgent",
 * 		statDesc: "my model run" 
 * 	}
 * 	
 * 2. closeModel (modelName) 
 *    closes model, if model is running, it will be stopped
 *   
 * 3. getSummary (modelName)
 *    retrieves the execution summary of current execution
 *    
 * 4. nextCmd (cmdCB, millisecond) 
 *    retrieves the next remote command from the model execution and 
 *    automatically calls cmdCB if specified.
 *    cmdCB (rmtCmd)
 * 
 * 5. setResult (status, result) {
 *    sets the command execution status to be sent back to the model
 *    success: true or false
 *    result: any result string you wish to pass back to TestOptimal to be recorded with model execution
 */   

function TOServer (svrURL_p) {
	var debugCB;
	var debugLevel = 1;
	var svrURL = svrURL_p?svrURL_p: "localhost:8080";
	var conHeader = {
		 	"Content-type": "application/json"
		};
	
	var pendingResult = false;
	var self = this;

	function setDebugCB (debugCB_p, debugLevel_p) {
		debugCB = debugCB_p;
		if (debugLevel_p > 0) {
			debugLevel = debugLevel_p;
		}
		return toserver;
	}
	
	function setAuth (auth_p) {
		conHeader.Authorization = auth_p;
	}	
	
	function login (userName_p, password_p) {
		var p = new Promise ((resolve, reject) => {
			var url = svrURL + "/api/v1/sec/check";
			var headers = {
					"Content-type": "application/x-www-form-urlencoded",
					username: userName_p, 
					password: password_p
				};
			postURL (url, headers, null,
				function(rsp) {
					if (rsp.status=="OK") {
						var auth = "Basic " + btoa(userName_p + ":" + password_p);
						setAuth.call(self, auth);
						resolve(rsp);
					}
					else {
						if (debugCB) {
							debugCB(rsp);
						}
						reject (rsp);
					}
				}, reject);
		});
		return p;
	}
	
	function openModel (modelName_p) {
		return new Promise ((resolve, reject) => {
			
		});
	}
	
	function execModel (execReq_p, successCB_p, errorCB_p) {
		return new Promise((resolve, reject) => {
			pendingResult = false;
			if (!execReq_p.statDesc) {
				execReq_p.statDesc = "Agent_Exec_" + (new Date());
			}	
			var url = svrURL + "/api/v1/runtime/model/run/async";
			postURL(url, conHeader, execReq_p, resolve, reject);			
		});
	};

	// stop model execution
	function stopModelExec (mbtSessID_p) {
		return new Promise((resolve, reject) => {
			var url = svrURL + "/api/v1/runtime/session/" + mbtSessID_p + "/stop";
			getURL(url, conHeader, resolve, reject);
		});
	};

	// close the model
	function closeModel (modelName_p) {
		return new Promise((resolve, reject) => {
			var url = svrURL + "/api/v1/runtime/model/" + encodeURIComponent(modelName_p) + "/close"; 
				+ encodeURIComponent(modelName_p);
			getURL(url, conHeader, resolve, reject);			
		});
	};

	// retrieve execution summary
	function getSummary (modelName_p) {
		return new Promise((resolve, reject) => {
			var url = svrURL + "/api/v1/stats/exec/" + encodeURIComponent(modelName_p) + "/-1";
			getURL(url, conHeader, resolve, reject);
		});
	};

	function regAgent (modelName_p, agentID_p) {
		return new Promise ((resolve, reject) => {
			var url = svrURL + "/api/v1/agent/register/" + encodeURIComponent(modelName_p) + "/" + agentID_p;
			getURL(url, conHeader, resolve, reject);
		});
	};

	// retrieve next command from the model execution
	function nextCmd (agentID_p, timeoutMillis_p) {
		return new Promise((resolve, reject) => {
			var nextCmdURL = svrURL + "/api/v1/agent/nextCmd/" + agentID_p;
			if (timeoutMillis_p) {
				nextCmdURL += "?timeoutMillis=" + timeoutMillis_p;
			}
			if (pendingResult) {
				var errMsg = "Pending SetResult for last command received";
				reject (errMsg);
			}
			else {
				pendingResult = true;
				getURL (nextCmdURL, conHeader, resolve, reject); 
			}
		});
	};

	// 	cmdResult: {status: true/false, result: text, reqTag: text, assertID: text, moreAttrs: {} };
	function setResult (agentID_p, cmdResult_p) {
		return new Promise((resolve, reject) => {
			pendingResult = false;
			var resultURL = svrURL + "/api/v1/agent/setResult/" + agentID_p;
			postURL (resultURL, conHeader, cmdResult_p, resolve, reject);
		});
	};


	// graphType: model, sequence, msc, coverage
	function getGraphURL (modelName_p, graphType_p) {
		return "/api/v1/graph/" + encodeURIComponent(modelName_p) + "/" + graphType_p; 
	}
	
	function uploadModel (model_p) {
		return new Promise((resolve, reject) => {
			try {
				model_p = JSON.parse(model_p);
			}
			catch (err) { }
			postURL ("/api/v1/client/model/upload", conHeader, model_p, resolve, reject);
		});
	}


	function genPaths (modelName_p, transList_p, timeoutMillis_p, successCB_p, errorCB_p) {
		return new Promise ((resolve, reject) => {
			var req = {modelName: modelName_p, mbtMode: optimal_p?'MarkedOptimal':'MarkedSerial', options: {}};
			if (timeoutMillis_p > 0) {
				req.options["timeoutMillis"] = timeoutMillis_p;
			}
			if (transList_p && transList_p.length > 0) {
				req.options ["markList"] = transList_p.map(function(t) { return t.getUid(); });				
			}
			postURL ("/api/v1/client/model/gen", conHeader, req, resovle, reject);
		});
	}

	function uploadDataSet (dataSet_p) {
		return new Promise ((resolve, reject) => {
			try {
				dataSet_p = JSON.parse(dataSet_p);
			}
			catch (err) { }
			putURL ("/api/v1/datadesign/dataset/" + dataSet_p.getName(), conHeader, dataSet_p, resolve, reject); 
		});
	}
	
	function genDataTable (dsName_p, algorithm_p) {
		return new Promise ((resolve, reject) => {
			if (dsName_p instanceof DataSet) {
				dsName_p = dsName_p.getName();
			}
			algorithm_p = resolveCombAlg(algorithm_p); //pairWise, threeWise, fourWise, fiveWise, sixWise
			var req = { path: 'client', dsName: dsName_p, algorithm: algorithm_p};
			postURL ("/api/v1/client/dataset/gen", conHeader, req, resolve, reject); 
		});
	}

	function fetchURL (url_p, conf_p, successCB_p, errorCB_p) {
		if (debugCB) {
			debugCB(conf_p.method + ": " + url_p);
		}
		
		fetch (url_p, conf_p)
			.then(data => {
				if (data.ok) {
					return data.text();
				}
				else {
					data.text().then(console.log);
					throw ('HTTP Error Status: ' + data.status);
				}
			})
			.then(resp => {
				if (debugCB && debugLevel > 1) {
					debugCB (resp);
				}
				if (successCB_p) {
					var respObj = resp;
					if (resp) {
						try {
							respObj = JSON.parse(resp);
						}
						catch (err) {
							throw Error (err + ": " + resp);	
						}
					}
					successCB_p (respObj);
				}
			})
			.catch(err => {
				if (debugCB) {
					debugCB(err);
				}
				if (errorCB_p) {
					errorCB_p (err);
				}
			});
	}
	
	function getURL (url_p, conHeader_p, successCB_p, errorCB_p) {
		fetchURL (url_p, { headers: conHeader_p, method: "GET"}, successCB_p, errorCB_p);
	};

	function postURL (url_p, conHeader_p, data_p, successCB_p, errorCB_p) {
		var djson = JSON.stringify(data_p);
		fetchURL (url_p, { headers: conHeader_p, method: "POST", body: djson}, successCB_p, errorCB_p);
	};
	
	function putURL (url_p, conHeader_p, data_p, successCB_p, errorCB_p) {
		var djson = JSON.stringify(data_p);
		fetchURL (url_p, { headers: conHeader_p, method: "PUT", body: djson}, successCB_p, errorCB_p);
	};

	toserver = {
		login: login,
		openModel: openModel,
		closeModel: closeModel,
		execModel: execModel,
		stopModelExec: stopModelExec,
		uploadModel: uploadModel,
		uploadDataSet: uploadDataSet,
		genPaths: genPaths,
		getSummary: getSummary,
		regAgent: regAgent,
		nextCmd: nextCmd,
		setResult: setResult,
		getGraphURL: getGraphURL,
		setDebugCB: setDebugCB,
		genDataTable: genDataTable
	}
	return toserver;
}

class Model {
	typeCode = "scxml";
	childrenStates = [];
	pluginList = ['AGENT'];
	modelName;
	uid;
	
	constructor (modelName_p) {
		this.modelName = modelName_p;
		this.uid = modelName_p.replace(/ /g, '_');
	}
	
	getModelName () {
		return this.modelName;
	}
	
	addState (stateName_p) {
		var state = new State (stateName_p);
		this.assignPosition(state);
		this.childrenStates.push(state);
		return state;
	}

	assignPosition (state_p) {
		// assign default position so that IDE can display it
		var canvasWidth = 1500, canvasHeight = 1000, offsetLeft = 100, offsetTop = 100, offsetRight = 100, offsetBottom = 100;
		var spacingHorizontal = 300, spacingVertical = 200;
		state_p.setPosition(offsetLeft + (spacingHorizontal * this.childrenStates.length) % canvasWidth, 
			offsetTop + (spacingHorizontal * this.childrenStates.length / canvasWidth) * spacingVertical, 75, 50);
	}
	
	addStateInitial (stateName_p) {
		var state = new State (stateName_p);
		state.setIsInitial(true);
		this.assignPosition(state);
		this.childrenStates.push(state);
		return state;
	}

	addStateFinal (stateName_p) {
		var state = new State (stateName_p);
		state.setIsFinal(true);
		this.assignPosition(state);
		this.childrenStates.push(state);
		return state;
	}

	findState (stateName_p) {
		for (s in this.childrenStates) {
			if (s.getName() == stateName_p) {
				return s;
			}
		}
		return null;
	}
}

class State {
	uid = create_UUID();
	stateID;
	isFinal = false;
	isInitial = false;
	position = {};
	transitions = [];

	constructor (stateName_p) {
		this.stateID = stateName_p;		
	}
	
	getUid() {
		return this.uid;
	}	
	
	getStateID () {
		return this.stateID;
	}

	getIsFinal() {
		return this.isFinal;
	}
	
	getIsInitial () {
		return this.isInitial;
	}
	
	setIsInitial () {
		this.isInitial = true;
	}
	
	setIsFinal () {
		this.isFinal = true;
	}
	
	addTrans (transName_p, targetState_p) {
		var trans = new Transition (transName_p, targetState_p);
		this.transitions.push(trans);
		return this;
	}
	
	setPosition(left_p, top_p, width_p, height_p) {
		this.position.left = left_p;
		this.position.top = top_p;
		this.position.width = width_p;
		this.position.height = height_p;
	}
	
	findTrans (transName_p) {
		for (t in this.transitions) {
			if (t.getTransName() == transName_p) {
				return t;
			}
		}
		return null;
	}
}

class Transition {
	uid = create_UUID();
	targetState;
	targetUID;
	event;
	weight = 5;
	traverseTimes = 1;
	
	constructor (transName_p, targetState_p)  {
		this.event = transName_p;
		this.setTargetState (targetState_p);
	}
	
	setTargetState (targetState_p) {
		this.targetState = targetState_p;
		this.targetUID = targetState_p.getUid();
		return this;
	}
	
	setWeight (weight_p) {
		this.weight = weight_p;
		return this;
	}

	setTraverseTimes (traverseTimes_p) {
		this.traverseTimes = traverseTimes_p;
		return this;
	}
	
	getTargetState () {
		return this.targetState;
	}
	
	getTransName () {
		return this.event;
	}
}


class DataSet {
	dsName;
	path = "client";
	pluginList = [];
	fieldList = []; 
	overallStrength = 'pairWise';
	ruleList = [];
	relationList = [];
	script = "";
	scriptAux = "";
	moreAttrs = {};
	
	constructor (dsName_p) {
		this.dsName = dsName_p;
	}
	
	getName () {
		return this.dsName;
	}
	
	addAttr (name_p, value_p) {
		this.moreAttrs [name_p] = value_p;
	}
	
	addField (name_p, dataType_p, domainList_p, groupCode_p, derived_p) {
		var field = new Field(name_p, dataType_p, domainList_p, groupCode_p, derived_p);
		this.fieldList.push(field);
		return field;
	}
	
	addRule (ifExpr_p, thenExpr_p) {
		var rule = new Constraint(ifExpr_p, thenExpr_p);
		this.ruleList.push(rule);
		return rule;
	}
	
	addRelation (name_p, fieldNameList_p, strength_p) {
		var rel = new Relation(name_p, fieldNameList_p, strength_p);
		this.relationList.push(rel);
		return rel;
	}
}

class Field {
	fieldName;
	dataType = 'text'; //bool, text, intNum
	derived = false;
	groupCode = "";
	domainList = [];
	
	constructor (name_p, dataType_p, domainList_p, groupCode_p, derived_p) {
		this.fieldName = name_p;
		this.groupCode = groupCode_p;
		this.dataType = 'text';
		var dt = dataType_p.charAt(0);
		if (dt=='I' || dt=='i') {
			this.dataType = 'intNum';
		}
		else if (dt=='B' || dt=='b' ) {
			this.dataType = 'bool';
		}
		else {
			this.dataType = 'text';
		}
		this.derived = derived_p;
		this.domainList = domainList_p;
	}
}

class Constraint {
	ruleExprIF;
	ruleExprTHEN;
	
	constructor (ifExpr_p, thenExpr_p) {
		this.ruleExprIF = ifExpr_p;
		this.ruleExprTHEN = thenExpr_p;
	}
}

class Relation {
	relName;
	relationStrength = 'pairWise';
	fieldNameList = [];
	
	constructor (name_p, fieldNameList_p, strength_p) {
		this.name = name_p;
		this.relationStrength = strength_p;
		this.fieldNameList = fieldNameList_p;
	}
}

function resolveCombAlg (algString_p) {
	if (algString_p) {
		var algUp = algString_p.toUpperCase();
		var algChar = algUp.charAt(0);
		if (algUp=='PAIRWISE' || algChar=='2') {
			return 'pairWise';
		}
		else if (algUp=='THREEWise' || algChar=='3') {
			return 'pairWise';
		}
		else if (algUp=='FOURWISE' || algChar=='4') {
			return 'fourWise';
		}
		else if (algUp=='FIVEWISE' || algChar=='5') {
			return 'fiveWise';
		}
		else if (algUp=='SIXWISE' || algChar=='6') {
			return 'sixWise';
		}
		else return 'pairWise'
	}
	else return 'pairWise';
}

function create_UUID(){
    var dt = new Date().getTime();
    var uuid = 'xxxxxxxxxxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        var r = (dt + Math.random()*16)%16 | 0;
        dt = Math.floor(dt/16);
        return (c=='x' ? r :(r&0x3|0x8)).toString(16);
    });
    return uuid;
}
