// Trigger script - define one function for each state or transition to be executed when traversed,
// To add a trigger: Ctrl/Cmd-I and select the state or transition.
// Do not remove the following import statement.
import com.testoptimal.mscript.groovy.TRIGGER
@TRIGGER('ALL_STATES')
def 'ALL_STATES' () {
	$AGENT.sendReceive('STATE: ' + $SYS.getCurState().getStateID(), $VAR.timeMillis);
}

@TRIGGER('ALL_TRANS')
def 'ALL_TRANS' () {
	$AGENT.sendReceive('TRANS: ' + $SYS.getCurTrans().getEvent(), $VAR.timeMillis);
}

@TRIGGER('MBT_START')
def 'MBT_START' () {
	$VAR.timeMillis = 30000;
}


