function TOWS () {
	var url = '/to-ws';
	var subDefList = [];
	var subList = [];
	var topicPrefix = '/ide';
	var conCount = 0;
	var stompClient = null;
	
	function wsDoConnect() {
	    var socket = new SockJS (url);
	    stompClient = Stomp.over(socket);
//		stompClient = Stomp.over (socket, function(){
//	                                   return new SockJS('/to-ws');
//	                               });
	    stompClient.debug = null;
	    stompClient.reconnect_delay = 1000;
	    
//	    stompClient.heartbeat.outgoing = 10000; // client will send heartbeats every 20000ms
//	    stompClient.heartbeat.incoming = 0; 
	    stompClient.connect({reconnect_delay: 1000}, function (frame) {
	        console.log('Connected frame', frame, new Date());
	        conCount++;
	        wsConnected();
	    }, function (err) {
			if (stompClient) {
				console.log("Lost ws connection", new Date());
		    	alertDialog("Lost connection to TestOptimal Server. Click File/Logout to be reconnected.");
			}
	    });
	}


	function wsConnected() {
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
		if (conCount == 0) {
			// console.log('subscribing before conID is set', subObj);
		}
		else {
			subList[subObj.subID] = stompClient.subscribe(topicPrefix + '/' + subObj.action, subObj.cb, {id: subObj.subID});
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






