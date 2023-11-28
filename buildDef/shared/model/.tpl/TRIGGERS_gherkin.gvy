import com.testoptimal.mscript.groovy.TRIGGER
@TRIGGER('ALL_STATES')
def 'ALL_STATES' () {
	$SYS.log('STATE: ' + $SYS.getCurState().getStateID());
}

@TRIGGER('ALL_TRANS')
def 'ALL_TRANS' () {
	$SYS.log('TRANS: ' + $SYS.getCurTrans().getEvent());
}

@TRIGGER('MBT_START')
def 'MBT_START' () {
    $SEQOUT.setOutputFileXLS('testout');
}
