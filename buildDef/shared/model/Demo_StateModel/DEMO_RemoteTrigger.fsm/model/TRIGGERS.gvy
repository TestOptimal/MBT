// Trigger script - define one function for each state or transition to be executed when traversed,
// To add a trigger: Ctrl/Cmd-I and select the state or transition.
// Do not remove the following import statement.
import com.testoptimal.mscript.groovy.TRIGGER
@TRIGGER('ALL_STATES')
def 'ALL_STATES' () {
	$AGENT.sendCmd('STATE: ' + $SYS.getCurState().getStateID());
	result = $AGENT.fetchResult($VAR.timeMillis);
}

@TRIGGER('ALL_TRANS')
def 'ALL_TRANS' () {
	$AGENT.sendCmd('TRANS: ' + $SYS.getCurTrans().getEvent());
	result = $AGENT.fetchResult($VAR.timeMillis);
}

@TRIGGER('MBT_START')
def 'MBT_START' () {
	$VAR.timeMillis = 30000;
}


