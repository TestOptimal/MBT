// copyright 2008 - 2020, TestOptimal, LLC, all rights reserved.
// winUtil.js

function WinManager (curAppState_p) {
	var winRegList = {};
	var winEventList = {};
	var rootCurAppState = curAppState_p;
	
	function getWin (winName_p) {
		var winObj = winRegList[winName_p];
		if (winObj==undefined) {
			winObj = {
				name: winName_p
			}
			winRegList[winName_p] = winObj;;
		}
		return winObj;
	}
		
	function regWin (winName_p, url_p) {
		var winObj = getWin(winName_p);
		winObj.url = url_p;
	}
	
	function addWin ( winName_p, windowObj_p) {
		var winObj = getWin(winName_p);
		winObj.window = windowObj_p;
		if (windowObj_p.mainCallbackFunc) {
			winObj.cb = windowObj_p.mainCallbackFunc;
		}
		if (windowObj_p.focus) {
			windowObj_p.focus();
		}
	}
	
	function regCB (winName_p, cb_p) {
		var winObj = getWin(winName_p);
		winObj.cb = cb_p;
	}
	
	function runWinAction(winName_p, action_p, params_p) {
		console.log("runWinAction: " + winName_p + ", " + action_p + ", " + params_p);
		var ret;
		if (winName_p=="*") {
			for (var i in winRegList) {
				var winObj = winRegList[i];
				if (winObj.cb) {
					try {
						ret = winObj.cb.apply(this, [action_p, params_p]);
					}
					catch (err) {
						//
					}
				}
			}
		}
		else {
			var winObj = getWin(winName_p);
			if (winObj.cb) {
				try {
					ret = winObj.cb.apply(this, [action_p, params_p]);
				}
				catch (err) {
					//
				}
			}
		}
		
		return ret;
	}
	
	function closeWin (winName_p) {
		var winObj = getWin(winName_p);
		if (winObj.window) {
			try {
				winObj.window.close();
			}
			catch (err) {
				//
			}
			winObj.window = undefined;
		}
		return winObj;
	}

	function closeAllWins () {
		for (var winName in winRegList) {
			closeWin(winName);
		}
	}

	function openWin (winName_p) {
		var winObj = closeWin(winName_p);
		var winURL = winObj.url;
		if (winURL) {
			if (winURL.indexOf("http")!=0) {
				winURL = "/MbtSvr/" + winObj.url;
			}
			winObj.window = window.open(winURL, "_blank"); // + winID_p);
			return winObj;
		}
	}
	
	function openWebPage (url_p, winName_p) {
		var winObj = closeWin(winName_p);
		winObj.window = window.open(url_p, winName_p);
		if (winObj.window) {
			winObj.window.focus();
		}
		return winObj;
	}
	
	function regEvent (name, cb) {
		var list = winEventList [name];
		if (list==undefined) {
			list = [];
			winEventList[name] = list;
		}
		list.push(cb);
	}
	
	function raiseEvent (name, params) {
		var cbList = winEventList[name];
		for (var i in cbList) {
			var cb = cbList[i];
			try {
				cb.apply(this, params);
			}
			catch (err) {
				curAppState.addMsg ({text: err, type: "error"});
			}
		}
	}
	
	function refresh () {
		window.location = location.protocol + "//" + window.location.host + "/ide";
	}
	
	function init() {
		winRegList = {};
		winEventList = {};
		regWin("Help", 'https://testoptimal.com/v6/wiki');
		regWin("Support", 'https://testoptimal.com/support');
		regWin("Dashboard", '../Dashboard_Main.html');
		regWin("Forum", "https://testoptimal.com/forum");
		regWin("APIDOCS", "https://testoptimal.com/v6/apidocs/");
		regWin("Download", "https://testoptimal.com/#Download");
	}


	init();
	return {
		runWinAction: runWinAction,
		regCB: regCB,
		addWin: addWin,
		closeWin: closeWin,
		closeAllWins: closeAllWins,
		openWebPage: openWebPage,
		openWin: openWin,
		regEvent: regEvent,
		raiseEvent: raiseEvent,
		init: init,
		refresh: refresh
	};
}

