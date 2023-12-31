function TOWS () {
	var httpSessID;
	var url = '/to-ws';
	var subDefList = [];
	var subList = [];
	var topicPrefix = '/ide';
	var conCount = 0;
	var stompClient = null;
	
	function wsDoConnect() {
	    var socket = new SockJS (url);
	    stompClient = Stomp.over(socket);
	    stompClient.debug = true;
//	    stompClient.reconnect_delay = 1000;
	    stompClient.connect({reconnect_delay: 1000}, function (frame) {
	        console.log('Connected frame #' + conCount, frame, new Date());
	        wsConnected();
	    }, function (err) {
	        conCount++;
			console.log("Lost ws connection", new Date(), err);
			if (conCount < 200) {
				setTimeout(wsDoConnect, 1000);			
			}
			else {
		    	alertDialog("Lost connection to TestOptimal Server. Check if it's running.");
			}
	    });
	}


	function wsConnected() {
    	stompClient.subscribe(topicPrefix + '/httpSess', function(data) {
			httpSessID = data.body;
			console.log("httpSessID " + httpSessID);
			subList = [];
			Object.keys(subDefList).forEach(function(k) {
				wsDoSubscribe(subDefList[k]);
			});
		});	        

		wsSend ("init", "");
		
		Object.keys(subDefList).forEach(function(k) {
			wsDoSubscribe(subDefList[k]);
		});
	}

	function wsReconnect() {
		console.log("re-connecting");
		wsDisconnect();
		wsDoConnect();
	}

	function wsDisconnect() {
		Object.keys(subList).forEach(function(k) {
			try {
				subList[k].unsubscribe();
			}
			catch (err) {
				// ok
			}
		});
		stompClient.disconnect();
		stompClient = null;
		subList = [];
	}
	
	function wsDoSubscribe (subObj) {
		if (subList[subObj.subID]) {
			subList[subObj.subID].unsubscribe();
		}
		if (httpSessID) {
			console.log("adding sub " + subObj.subID);
			subList[subObj.subID] = stompClient.subscribe(topicPrefix + '/' + httpSessID + '/' + subObj.action, subObj.cb, {id: subObj.subID});
		}		
	}

	function wsSend (action, message) {
		var opt = {}; //  {priority: 9}
		var msg = message;
		if (typeof message !== 'string' && !(message instanceof String)) {
			msg = JSON.stringify(message);
		}
	    stompClient.send("/server/" + action, opt, msg);
	}

	function wsSendModel (action, message) {
		wsSend(curAppState.scxml.modelName + "/" + action, message);
	}

	function wsSubscribe(action, cb, subSrc) {
		var subObj = 
			{	subID: subSrc + "_" + action,
				action: action, 
				cb: cb, 
				subSrc: subSrc
			};
		subDefList[subObj.subID] = subObj;
		wsDoSubscribe(subObj);
	}
	
	wsDoConnect();
	
	return {
		wsSend: wsSend,
		wsSendModel: wsSendModel,
		wsSubscribe: wsSubscribe,
		wsDisconnect: wsDisconnect
	}
}






