// Trigger script - define one function for each state or transition to be executed when traversed,
// To add a trigger: Ctrl/Cmd-I and select the state or transition.
// Do not remove the following import statement.
import com.testoptimal.exec.mscript.TRIGGER



@TRIGGER('MBT_START')
def 'MBT_START' () {
	$VAR.path = 0;
}

@TRIGGER('U1466714b')
def 'state name: trans name'() {
	$VAR.path = $VAR.path + 1;
}
