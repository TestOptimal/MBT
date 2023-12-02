// Model Triggers
import com.testoptimal.mscript.groovy.TRIGGER

// This script output the generated test cases and test steps to a file in "report" folder

@TRIGGER('MBT_START')
def 'MBT_START' () {
	$VAR.outFile = new File ($SYS.getModelMgr().getReportFolderPath() + '/test_out.html');
   $VAR.outFile << '<html><body><H1>Test Output</H1>\n';
   $VAR.outFile << '<ol>\n';
}

@TRIGGER('MBT_END')
def 'MBT_END' () {
	$VAR.outFile << '</ol></body></html>\n';
}

@TRIGGER('U1072')
def 'Start: add25'() {
	$VAR.outFile << '<li><span>Test Case ' + $SYS.getPathName() + '</span>\n<ul>\n';
   $VAR.outFile << '<li>Step: add a Quarter</li>\n';
}

@TRIGGER('U1073')
def 'Start: add50'() {
	$VAR.outFile << '<li><span>Test Case ' + $SYS.getPathName() + '</span>\n<ul>\n';
   $VAR.outFile << '<li>Step: add a Half-Dollar</li>\n';
}

@TRIGGER('U1065')
def 'V25Cents: add25'() {
   $VAR.outFile << '<li>Step: add a Quarter</li>\n';
}

@TRIGGER('U1066')
def 'V25Cents: add50'() {
   $VAR.outFile << '<li>Step: add a Half-Dollar</li>\n';
}

@TRIGGER('U1067')
def 'V50Cents: add25'() {
   $VAR.outFile << '<li>Step: add a Quarter</li>\n';
}

@TRIGGER('U1068')
def 'V50Cents: add50'() {
   $VAR.outFile << '<li>Step: add a Half-Dollar</li>\n';
}

@TRIGGER('U1069')
def 'V75Cents: add25'() {
   $VAR.outFile << '<li>Step: add a Quarter</li>\n';
}

@TRIGGER('U1070')
def 'V75Cents: add50'() {
   $VAR.outFile << '<li>Step: add a Half-Dollar</li>\n';
}

@TRIGGER('U1064')
def 'V125Cents: return25'() {
   $VAR.outFile << '<li>Step: check return change 25 cents</li>\n';
}

@TRIGGER('U1071')
def 'Choose Drink: discharge'() {
   drinkExpected = $DATASET.DrinkChoices.get('DrinkCheckText');
   $VAR.outFile << '<li>Step: select a drink: ' + drinkExpected + ' </li>\n';
	$DATASET.DrinkChoices.next();
}

@TRIGGER('U1061')
def 'End' () {
   $SYS.genMSC('TestCase ' + $SYS.getPathName(), $SYS.getPathName());
   $VAR.outFile << "<li><img src='" + $SYS.getPathName() + ".png'/></li>\n";
   $VAR.outFile << '</ul></li>\n';
}
