MainModule.factory ('SvrRest', function($http, $cookies) {
	var SvrRest = { 
    	curSession: null,
    	token: $cookies.get(location.hostname + "_" + location.port + "_totoken")
	}
	
	SvrRest.setToken = function (username, password) {
		SvrRest.token = btoa(username + ":" + password);
	}

	SvrRest.getHeaders = function () {
		var headers = {
	            "Content-Type":"application/json",
	        }
		if (SvrRest.token) {
            headers.Authorization = SvrRest.token;	
		}
		return headers;
	}
	
	SvrRest.get = function (url, successCB, errorCB) {
		$http ({
				method: "GET",
				url: url,
				headers: SvrRest.getHeaders()
			})
			.then (function(ret) {
				if (successCB) {
					successCB(ret.data, ret);
				}
			})
			.catch(function(ret, status) {
				handleError(ret, status, errorCB);
			});
	};

	function handleError (ret, status, errorCB) {		
		var data = ret.message?ret.message: (ret.data? (ret.data.message?ret.data.message:ret.data): ret);
		if (errorCB) {
			errorCB(data);
		}
		var msgObj = {text: data, type: 'error'};
		if (ret.config) {
			if (ret.config.url) {
				msgObj.source = ret.config.url;	
			}
			else if (ret.config.path) {
				msgObj.source = ret.config.path;
			}
		}
		if (status) {
			msgObj.text += ", status=" + status;
		}
		curAppState.addMsg(msgObj);
		console.log("ajax error", ret, status);
	}

	SvrRest.post = function (url, data, successCB, errorCB) {
		var djson = JSON.stringify(data);
		$http ({
				method: "POST",
				url: url,
				data: djson,
				headers: SvrRest.getHeaders()
			})
			.then (function (ret) {
				if (successCB) {
					successCB(ret.data);
				}
			})
			.catch(function(ret, status) {
				handleError(ret, status, errorCB);
			});
	};

	SvrRest.put = function (url, data, successCB, errorCB) {
		$http ({
				method: "PUT",
				url: url, 
				data: data,
				headers: SvrRest.getHeaders()
			})
			.then (function (ret) {
				if (successCB) {
					successCB(ret.data);
				}
			})
			.catch(function(ret, status) {
				handleError(ret, status, errorCB);
			});
	};

	SvrRest.delete = function (url, successCB, errorCB) {
		$http ({
				method: "DELETE",
				url: url,
				headers: SvrRest.getHeaders()
			})
			.then (function (ret) {
				if (successCB) {
					successCB(ret.data);
				}
			})
			.catch(function(ret, status) {
				handleError(ret, status, errorCB);
			});
	};
	
	SvrRest.postFile = function (url, fileContent, attrs, successCB, errorCB) {
		var fd = new FormData();
        fd.append('file', fileContent);
        for (var a in attrs) {
        	fd.append(a, attrs[a]);
        }
        $http ({
        	method: "POST",
        	url: url, 
        	data: fd,
            transformRequest: angular.identity,
            headers: {
            	'Content-Type': undefined,
	            "Authorization" : SvrRest.token
            } 
        }) 
		.then (function (ret) {
			if (successCB) {
				successCB(ret.data);
			}
		})
		.catch(function(ret, status) {
			handleError(ret, status, errorCB);
		});
	};
	


	return SvrRest;
});

MainModule.factory ('IdeSize', function() {
	var IdeSize = { 
			windowWidth: 0,
			windowHeight: 0,
			ribbonTabVisible: true,
	    	ribbonShortHeight: 48,
	    	ribbonTallHeight: 119,
			ribbonHeight: 33,
			viewWidth: 600,
			viewHeight: 500,
			listeners: []
	};
	
	IdeSize.addListener = function (listener) {
		IdeSize.listeners.push(listener);
	};
	
	IdeSize.setRibbonState = function (ribbonTabVisible) {
		IdeSize.ribbonTabVisible= ribbonTabVisible;
		IdeSize.windowResized();
	};
	
	IdeSize.paneResized = function (dragScope, stopLeft) {
    	if (stopLeft==0) {
    		if (dragScope.splitPct==0) {
        		dragScope.splitPct = dragScope.preSplitPct;
        	}
    		else {
        		dragScope.preSplitPct = dragScope.splitPct;
        		dragScope.splitPct = 0;
    		}
    	}
    	else {
    		dragScope.splitPct = Math.round(100 * stopLeft / dragScope.viewWidth);
    	}
		dragScope.windowResized();
		dragScope.$apply();
	}
	
	IdeSize.windowResized = function (applyRibbonScope) {
		IdeSize.windowWidth = window.innerWidth;
		IdeSize.windowHeight = window.innerHeight;
		IdeSize.viewWidth = IdeSize.windowWidth;
		if (IdeSize.ribbonTabVisible) {
			IdeSize.ribbonHeight = IdeSize.ribbonTallHeight;
		}
		else {
			IdeSize.ribbonHeight = IdeSize.ribbonShortHeight;
		}
		IdeSize.viewHeight = IdeSize.windowHeight - IdeSize.ribbonHeight;

		for (var i in IdeSize.listeners) {
			try {
				IdeSize.listeners[i].windowResized();
				if (applyRibbonScope) {
					IdeSize.listeners[i].$apply();
				}
			}
			catch (err) {
				alert('failed calling windowResized() on listener ' + i + ': ' + err);
			}
		}
	}

	window.onresize = function() {
		IdeSize.windowResized(true);
	}
	
	return IdeSize;
});

