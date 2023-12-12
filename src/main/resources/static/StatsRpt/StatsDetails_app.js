MainModule.controller("detailsCtrl", function ($scope) {
	$scope.stateTransFilter = "";
	$scope.rspBarWidth = 100;
	$scope.rspBarHeight = 10; // should be kept in sync with parent.curAppState.execID

	$scope.modelName = "";
	$scope.mbtSessID = undefined;
	$scope.curExecStatDetail = {};
	
	$scope.refresh = function () {
		$scope.curExecStatDetail = undefined;
		$scope.tcShowState=true; 
		$scope.tcShowTrans=true;
		
		$scope.reqExpanded = true;	
		parent.curAppState.toSvc.StatsSvc.getModelExec ($scope.modelName, $scope.mbtSessID,
			function(returnData) {
				if (returnData==="") {
					$scope.curExecStatDetail = null;
					$scope.$apply();
					return;
				}
				
				$scope.curExecStatDetail = returnData;
				if ($scope.curExecStatDetail.execSummary.startDT) {
					$scope.curExecStatDetail.execSummary.startDT = new Date($scope.curExecStatDetail.execSummary.startDT).toLocaleString();
				}
				if ($scope.curExecStatDetail.execSummary.endDT) {
					$scope.curExecStatDetail.execSummary.endDT = new Date($scope.curExecStatDetail.execSummary.endDT).toLocaleString();
				}
				
				$scope.stateTransMap = Object.assign({}, $scope.curExecStatDetail.stateMap, $scope.curExecStatDetail.transMap);
				$scope.stateTransList = Object.values($scope.stateTransMap).sort(function(a,b) {
					return ((a.stateName < b.stateName) ? -1 : ((a.stateName > b.stateName) ? 1 : 
							((a.transName || '') < (b.transName || '')?-1:(a.transName || '') > (b.transName || '')?1:0  )
						));
				});
				for (var s in $scope.stateTransMap) {
					var stat = $scope.stateTransMap[s];
					$scope.stateTransMap[s] = stat;
					millisWindow = (stat.maxMillis || 0) - (stat.minMillis || 0);
					if (millisWindow > 0) {
						stat.histoMidPos = Math.round(($scope.rspBarWidth-2) * (stat.avgMillis-stat.minMillis)/millisWindow);
					}
					else {
						stat.histoMidPos = Math.round(($scope.rspBarWidth-2)/2);
					}
				}
				var sMap = Stream($scope.curExecStatDetail.tcList).flatMap(function(tcObj){
						return tcObj.stepList;
					}).groupBy(function (step) {
						return step.UID;
					});
				for ( var k in  sMap) {
					$scope.stateTransMap[k].msgMap = Stream(sMap[k])
						.flatMap(function(step) {
							return step.itemList; 
						})
						.filter(function(item) { 
							return !item.passed;
						})
						.groupBy(function(item) { 
							return "failed";
						});
				}
				
				$scope.curExecStatDetail.execSummary.stateCovPct = Math.round($scope.curExecStatDetail.execSummary.stateCovered * 100 / $scope.curExecStatDetail.execSummary.stateNum);
				$scope.curExecStatDetail.execSummary.transCovPct = Math.round($scope.curExecStatDetail.execSummary.transCovered * 100 / $scope.curExecStatDetail.execSummary.transNum);
				if ($scope.curExecStatDetail.execSummary.reqNum>0) {
					$scope.curExecStatDetail.execSummary.tagCovPct = Math.round($scope.curExecStatDetail.execSummary.reqCovered * 100 / $scope.curExecStatDetail.execSummary.reqNum);
				}
				
				angular.forEach($scope.curExecStatDetail.tcList, function(tcObj) {
					var s1 = tcObj.ReqStatusList = Stream(tcObj.stepList).flatMap(function (step) {
						return step.itemList;
					}).groupBy(function(item) {
						return item.reqTag;
					});
					for ( var k in  s1) {
						s1[k] = (Stream(s1[k]).anyMatch(function(item) {
								return item.status=='failed';
							}))?'failed':'passed';
					}
				});
				
				Stream($scope.curExecStatDetail.tcList).forEach(function(tcObj) {
					Stream(tcObj.stepList).forEach(function(stepObj) {
						var t = $scope.stateTransMap[stepObj.UID];
						stepObj.type = t.type;
						stepObj.stateName = t.stateName;
						stepObj.transName = t.transName;
					});
				});
				
				$scope.$apply();
			});
	}

	$scope.showTagPassMsg = function (tagStat) {
		var msgList = Stream($scope.curExecStatDetail.tcList).flatMap(function (tcObj) {
			return tcObj.stepList;
		}).flatMap(function(step) {
			return step.itemList;
		}).filter(function(item) {
			return item.reqTag===tagStat.reqTag && item.status==="passed";
		}).toArray();
		$scope.showTagMsg(tagStat.reqTag + ": Passed Traversals", "State.Transition", msgList);
	}
	
	$scope.showTagFailMsg = function (tagStat) {
		var msgList = Stream($scope.curExecStatDetail.tcList).flatMap(function (tcObj) {
			return tcObj.stepList;
		}).flatMap(function(step) {
			return step.itemList;
		}).filter(function(item) {
			return item.reqTag===tagStat.reqTag && item.status==="failed";
		}).toArray();
		$scope.showTagMsg(tagStat.reqTag + ": Failed Traversals", "State.Transition", msgList);
	}

	$scope.openDialog = function (winName_p) {
		parent.curAppState.openDialog (winName_p);
	}
	
	$scope.showTagMsg = function (title_p, columnLabel_p, msgList_p) {
		var msgText = "<div id=msgDialog><div class='header'>" + title_p + "</div>" +
			"<div><ol>";
		for (var i in msgList_p) {
			var msgTag = msgList_p[i];
			
			msgText += "<li>";
			if (msgTag.stateName) {
				msgText +=  msgTag.stateName;
				if (msgTag.transName) {
					msgText += "." + msgTag.transName;
				}
				if (msgTag.assertCode!="") {
					msgText += " (" + msgTag.assertCode + ")"; 
				}
				msgText += ": ";
			}
			msgText += msgTag.checkMsg + "</li>";
		}
		msgText += "</ol></div></div>";
		parent.alertDialog(msgText);
	}

	$scope.showPerfMsg = function (stat) {
		parent.alertDialog("Number of slow traversals: " + stat.slowCount);
	}

	$scope.showTravExceptMsg = function (stat) {
		var msgList = stat.msgMap.failed;
		var hcode = "<ol>";
		for (i in msgList) {
			hcode += "<li>"+ msgList[i].checkMsg + "</li>";
		}
		hcode += "</ol>";
		parent.alertDialog(hcode);
	}

	$scope.showExecSettings = function () {
		var msgList = [];
		angular.forEach($scope.curExecStatDetail.execOptions, function (v,k) {
			if (v && k) {
				msgList.push(k + ": " + v);
			}
		});

		var hcode = "<div><b>Model Execution Settings</b></div><ul><li>" + msgList.join("</li><li>") + "</li></ul>";
		parent.alertDialog(hcode);
	}
	
	$scope.toggleKeep = function () {
		if ($scope.curExecStatDetail.keepIt==undefined) $scope.curExecStatDetail.keepIt=false;
		var newKeep = !$scope.curExecStatDetail.keepIt;
		parent.curAppState.toSvc.StatsSvc.setModelExecKeep ($scope.modelName, $scope.mbtSessID, newKeep, 
			function(returnData) {
				$scope.curExecStatDetail.keepIt = newKeep;
				$scope.$apply();
			});
	}
	
	$scope.deleteModelExec = function () {
		$scope.$parent.deleteModelExec($scope.modelName, $scope.mbtSessID);
	}

	$scope.showTestCase = function(tcObj_p) {
		$scope.curTestCase = tcObj_p; 
		$scope.reqExpanded = false;
		$scope.tcExpanded = true;
	}
	

	$scope.openModelGraph = function () {
		parent.curAppState.winMgr.openWebPage ("../api/v1/graph/model/" + $scope.modelName + "/model", "ModelGraph");
	}

	$scope.openTravGraph = function () {
		parent.curAppState.winMgr.openWebPage ("../api/v1/graph/model/" + $scope.modelName + "/sequence?mbtSessID=" + $scope.mbtSessID, "SequenceGraph");
	}

	$scope.openCoverageGraph = function () {
		parent.curAppState.winMgr.openWebPage ("../api/v1/graph/model/" + $scope.modelName + "/coverage?mbtSessID=" + $scope.mbtSessID, "CoverageGraph");
	}

	$scope.openTravMSC = function () {
		parent.curAppState.winMgr.openWebPage ("../api/v1/graph/model/" + $scope.modelName + "/msc?mbtSessID=" + $scope.mbtSessID, "ModelMSC"); 
	}
	
	$scope.init = function () {
		parent.curAppState.winMgr.regEvent("refreshExec", function(modelName, mbtSessID) {
			$scope.modelName = modelName;
			$scope.mbtSessID = mbtSessID;
			$scope.refresh();
		});

	}
	
	$scope.init();
});
