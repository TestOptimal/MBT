// state/transition trigger scripts 
import com.testoptimal.mscript.groovy.TRIGGER

@TRIGGER('MBT_START')
def MBT_START () {  
    $SEQOUT.setOutputFileTXT('testout');
    $SEQOUT.writeSetup("Plugin the powercord into outlet.");
    $SEQOUT.writeSetup("Press Cancel to reset vending machine");
}

@TRIGGER('MBT_END')
def MBT_END () {
    $SEQOUT.writeTeardown("Unplugin the powercord from outlet.");
    $SEQOUT.writeTeardown("Return drinks back into the machine.");
}

@TRIGGER('MBT_ERROR')
def MBT_ERROR () {
    $SEQOUT.writeSetup("Errored while generating test cases");
}

@TRIGGER('MBT_FAIL')
MBT_FAIL () {
    $SEQOUT.writeSetup("Warning while generating test cases");
}


@TRIGGER('U1072')
def start_25 () {
    $SEQOUT.startStep();
    $SEQOUT.writeStepAction('Insert one Quarter');
	$SEQOUT.writeStepAssert("Q1", 'Check: Deposited Amount must be 0.25', "BUG_0_25");   
}

@TRIGGER('U1073')
def start_50 () {
    $SEQOUT.startStep();
    $SEQOUT.writeStepAction('Insert one Half-Dallor');
    $SEQOUT.writeStepAssert("H1", "Check: Deposit Amount must be 0.50", "BUG_0_50");
}

@TRIGGER('U1065')
def v25_25 () {
	$SEQOUT.startStep();
    $SEQOUT.writeStepAction('Insert one Quarter');
    $SEQOUT.writeStepAssert("Q1", 'Check: Deposited Amount must be 0.50', "BUG_25_25");   
}

@TRIGGER('U1066')
def v25_50 () {
    $SEQOUT.startStep();
    $SEQOUT.writeStepAction('Insert one Half-Dallor');
    $SEQOUT.writeStepAssert("H1", "Check: Deposit Amount must be 0.75", "BUG_25_50");
}

@TRIGGER('U1067')
def v50_25 () {
    $SEQOUT.startStep();
    $SEQOUT.writeStepAction('Insert one Quarter');
    $SEQOUT.writeStepAssert("Q1", 'Check: Deposited Amount must be 0.75', "BUG_50_25");    
}

@TRIGGER('U1068')
def v50_50 () {
    $SEQOUT.startStep();
    $SEQOUT.writeStepAction('Insert one Half-Dallor');
    $SEQOUT.writeStepAssert("H1", "Check: Deposit Amount must be 1.00", "BUG_50_50");
}

@TRIGGER('U1069')
def v75_25 () {
    $SEQOUT.startStep();
    $SEQOUT.writeStepAction('Insert one Quarter');
    $SEQOUT.writeStepAssert("Q1", 'Check: Deposited Amount must be 1.00', "BUG_75_25");
}

@TRIGGER('U1070')
def v75_50 () {
    $SEQOUT.startStep();
    $SEQOUT.writeStepAction('Insert one Half-Dallor');
    $SEQOUT.writeStepAssert("H1", "Check: Deposit Amount must be 1.25", "BUG_75_50");
}

@TRIGGER('U1063')
def v100_toDrink () {
    $SEQOUT.startStep();
    $SEQOUT.writeSuiteSetup('Exact change used for this test case');
    $SEQOUT.writeStepAction("Check for coins returned");
    $SEQOUT.writeStepAssert("R2", "No change should be returned", "BUG_RET0");
}

@TRIGGER('U1064')
def v125_return25() {
    $SEQOUT.startStep();
    $SEQOUT.writeSuiteSetup('Over payment test case');
    $SEQOUT.writeStepAction("Check for coins returned");
    $SEQOUT.writeStepAssert("R1", 'Check: 25 cents is returned', "BUG_R25");    
}

@TRIGGER('U1071')
def chooseDrink_discharge () {
    $SEQOUT.startStep();
    $SEQOUT.writeStepAction("Select " + $SYS.getDataSet('DrinkChoices').getData('Drink'));
    $SEQOUT.writeStepAssert("V1", "Check " + $SYS.getDataSet('DrinkChoices').getData('Drink') + " is discharged.", "BUG_VEND");
}

