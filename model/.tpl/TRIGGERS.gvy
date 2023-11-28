import com.testoptimal.mscript.groovy.TRIGGER
@TRIGGER('ALL_STATES')
def 'ALL_STATES' () {
	$AGENT.sendCmd('STATE: ' + $SYS.getCurState().getStateID());
	cmdResult = $AGENT.fetchResult($VAR.timeMillis);
	$SYS.log(groovy.json.JsonOutput.toJson(cmdResult));
	if (cmdResult.status) {
		$SYS.addReqPassed(cmdResult.reqTag, cmdResult.result, cmdResult.assertID);
	}
	else {
		$SYS.addReqFailed(cmdResult.reqTag, cmdResult.result, cmdResult.assertID);
	}
}

@TRIGGER('ALL_TRANS')
def 'ALL_TRANS' () {
	$AGENT.sendCmd('TRANS: ' + $SYS.getCurTrans().getEvent());
	cmdResult = $AGENT.fetchResult($VAR.timeMillis);
	$SYS.log(groovy.json.JsonOutput.toJson(cmdResult));
	if (cmdResult.status) {
		$SYS.addReqPassed(cmdResult.reqTag, cmdResult.result, cmdResult.assertID);
	}
	else {
		$SYS.addReqFailed(cmdResult.reqTag, cmdResult.result, cmdResult.assertID);
	}
}

@TRIGGER('MBT_START')
def 'MBT_START' () {
	$VAR.timeMillis = 30000;
}
