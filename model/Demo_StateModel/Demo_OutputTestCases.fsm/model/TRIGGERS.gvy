// Model Triggers
import com.testoptimal.mscript.groovy.TRIGGER

// This script output the generated test cases and test steps to a file in "report" folder

@TRIGGER('MBT_START')
def 'MBT_START' () {
	$VAR.outFile = new File ($SYS.getModelMgr().getReportFolderPath() + '/test_out.html');
   $VAR.outFile << '<html><body><H1>Garage Door Test Cases</H1>\n';
   $VAR.outFile << '<ol>\n';
}

@TRIGGER('MBT_END')
def 'MBT_END' () {
	$VAR.outFile << '</ol></body></html>\n';
}


@TRIGGER('Ua1e78163')
def 'Start: start'() {
	$VAR.outFile << '<li><span>Test Case ' + $SYS.getPathName() + '</span>\n<ul>\n';
}


@TRIGGER('U8a0ea761')
def 'Door Up' () {
   $VAR.outFile << '<li>Step: Check door is up</li>\n';
}
@TRIGGER('U476aee1b')
def 'Door Up: controlEvent'() {
   $VAR.outFile << '<li>Step: Push button</li>\n';
}
@TRIGGER('U716f4461')
def 'Door Up: end'() {
   $SYS.genMSC('TestCase ' + $SYS.getPathName(), $SYS.getPathName());
   $VAR.outFile << "<li><img src='" + $SYS.getPathName() + ".png'/></li>\n";
   $VAR.outFile << '</ul></li>\n';
}


@TRIGGER('Ud8774c1f')
def 'Door Closing' () {
   $VAR.outFile << '<li>Step: Check door is closing</li>\n';
}
@TRIGGER('Ua3f8fe7c')
def 'Door Closing: controlEvent'() {
   $VAR.outFile << '<li>Step: Push button</li>\n';
}
@TRIGGER('Uf597a929')
def 'Door Closing: laserBeamCrossed'() {
   $VAR.outFile << '<li>Step: cross laser beam</li>\n';
}
@TRIGGER('U3e21849c')
def 'Door Closing: endOfTrack'() {
   $VAR.outFile << '<li>Step: wait till door fully closed (end of track)</li>\n';
}


@TRIGGER('U10a4a68e')
def 'Door Stopped Closing' () {
   $VAR.outFile << '<li>Step: check door stops closing</li>\n';
}

@TRIGGER('Ua4d9f162')
def 'Door Stopped Closing: controlEvent'() {
   $VAR.outFile << '<li>Step: Push button</li>\n';
}


@TRIGGER('U3dfc21b1')
def 'Door Down' () {
   $VAR.outFile << '<li>Step: check door is down/closed</li>\n';
}
@TRIGGER('U67ca76e5')
def 'Door Down: controlEvent'() {
   $VAR.outFile << '<li>Step: Push button</li>\n';
}

@TRIGGER('U737471bc')
def 'Door Opening' () {
   $VAR.outFile << '<li>Step: check door is openning</li>\n';
}
@TRIGGER('Ucdd53004')
def 'Door Opening: controlEvent'() {
   $VAR.outFile << '<li>Step: Push button</li>\n';
}
@TRIGGER('U363bff9f')
def 'Door Opening: endOfTrack'() {
   $VAR.outFile << '<li>Step: wait for door to fully open (end of track)</li>\n';
}


@TRIGGER('U219d4359')
def 'Door Stopped Openning' () {
   $VAR.outFile << '<li>Step: check door stops openning</li>\n';
}
@TRIGGER('U6bab3b34')
def 'Door Stopped Openning: controlEvent'() {
   $VAR.outFile << '<li>Step: Push button</li>\n';
}


