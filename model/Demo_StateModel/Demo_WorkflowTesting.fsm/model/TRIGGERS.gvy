// Trigger script - define one function for each state or transition to be executed when traversed,
// To add a trigger: Ctrl/Cmd-I and select the state or transition.
// Do not remove the following import statement.
import com.testoptimal.mscript.groovy.TRIGGER


@TRIGGER('MBT_START')
def 'MBT_START' () {
    $SEQOUT.setOutputFileName('workflowScenarios.tsv');
    $SEQOUT.writeSetup("Setting up workflow testing");
}


@TRIGGER('MBT_END')
def 'MBT_END' () {
    $SEQOUT.writeTeardown("Cleaning up workflow testing");
}

@TRIGGER('MBT_ERROR')
def MBT_ERROR () {
    $SEQOUT.writeSetup("Errored while generating test cases");
}

@TRIGGER('MBT_FAIL')
MBT_FAIL () {
    $SEQOUT.writeSetup("Warning while generating test cases");
}


@TRIGGER('U590')
def 'Select a Loan  Request' () {
	$SEQOUT.startStep();
	$SEQOUT.writeStepAction('Select a loan request from the list');
	$SEQOUT.writeStepAssert("Loan.List", 'Load List contains at least 5 pending requests', "LOANLIST");   
}


@TRIGGER('U600')
def 'Select a Loan  Request: submitRequest'() {
	$SEQOUT.startStep();
	$SEQOUT.writeStepAction('Click submit button to submit the request for approval');
	$SEQOUT.writeStepAssert("Loan.Submit", 'Check request is submitted', "LOANSUBMIT");   
}
