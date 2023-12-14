import com.testoptimal.exec.mscript.TRIGGER;

@TRIGGER('MBT_START')
def 'MBT_START' () {
	$VAR.failCount = 0;
}


@TRIGGER('U9f16f0be')
def 'ConsecutiveInvalidLogin: performInvalidLogin'() {
	$VAR.failCount = $VAR.failCount + 1;
}
