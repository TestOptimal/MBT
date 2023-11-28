// Model's Trigger script 
import com.testoptimal.mscript.groovy.TRIGGER
import org.openqa.selenium.By;
// --- Ctrl/Cmd-I to insert a trigger for a state or transition in the model.


@TRIGGER('MBT_START')
def 'MBT_START' () {
	if ($SYS.isSubModel()) {
    $SELENIUM.setBrowser ($SYS.getExecDir().getExecSetting().getOption('ideBrowser'));
	}
	$VAR.failCount = 0;
	$VAR.MaxRetryable = 2; 
}

@TRIGGER('U3be2df19')
def 'start' () {
	if ($SYS.isSubModel()) {
      $SELENIUM.getWebDriver().get('http://localhost:' + $UTIL.getPort() + '/DemoApp/DemoWebsite/TestOptimalHome.html');
	}
}


@TRIGGER('U33f038da')
def 'LoginPage' () {
    $SELENIUM.assist().runJS('clearBadLogin()');

    // logoff if already logs in
    if ($SELENIUM.assist().containsText('Welcome!')) {
        $SELENIUM.getWebDriver().findElement(By.id('actionLogoff')).click();
    }

    $SYS.log('loginID visible: ' + ($SELENIUM.getWebDriver().findElement(By.id('loginID'))!=null));

    // open login dialog
    if ($SELENIUM.getWebDriver().findElement(By.id('loginID'))!=null) {
        $SELENIUM.getWebDriver().findElement(By.id('actionLogin')).click();
    }

    // wait for login dialog to show
    if ($SELENIUM.assist().waitForElement(By.id('loginID'),1000)) {
       $SELENIUM.getWebDriver().findElement(By.id('loginID')).clear();
       $SELENIUM.getWebDriver().findElement(By.id('password')).clear();
    }
    else {
        $SYS.log('Login dialog failed to show');
        $SYS.addReqFailed('LoginID', 'Missing loginID field','LOGINID');   
    }
}



@TRIGGER('U9fd69d76')
def 'LoginPage: performValidLogin'() {
    loginID = $SYS.getData('loginID');
    password = $SYS.getData('password');

    $SELENIUM.getWebDriver().findElement(By.id('loginID')).sendKeys(loginID);
    $SELENIUM.getWebDriver().findElement(By.id('password')).sendKeys(password);
    $SELENIUM.getWebDriver().findElement(By.id('loginBtn')).click();

    if ($SELENIUM.assist().containsText('Welcome!')) {
        $SYS.addReqPassed('REQ_VALID_LOGIN', 'Successful login with valid loginID / password');
    }
    else {
        $SYS.addReqFailed('REQ_VALID_LOGIN', 'Unable to login with good loginID / password: ' + $loginID + ' / ' + password);
        $SELENIUM.getWebDriver().findElement(By.id('closeBtn')).click();
    }
}


@TRIGGER('U4255387f')
def 'LoginPage: performInvalidLogin'() {
    loginID = $SYS.getData('loginID');
    password = $SYS.getData('password');

    $SELENIUM.assist().type(By.id('loginID'), loginID, '');
    $SELENIUM.assist().type(By.id('password'), password, '');
    $SELENIUM.getWebDriver().findElement(By.id('loginBtn')).click();

    if ($SELENIUM.assist().containsText('Welcome!')) {
        $SYS.addReqFailed('REQ_INVALID_LOGIN', 'Was able to login with invalid login id/password: ' + loginID + ' / ' + password);
    }
    else {
        $SYS.addReqPassed('REQ_INVALID_LOGIN', 'Unable to login with invalid loginID/pwd: ' + loginID + ' / ' + password);
        $SELENIUM.getWebDriver().findElement(By.id('closeBtn')).click();
    }
}


@TRIGGER('Ube2bc50a')
def 'LoginPage: startInvalidLogin'() {
	$VAR.failCount = 0;
}


@TRIGGER('U9f16f0be')
def 'ConsecutiveInvalidLogin: performInvalidLogin'() {
    $SELENIUM.getWebDriver().findElement(By.id('loginID')).clear();
    $SELENIUM.getWebDriver().findElement(By.id('password')).clear();

    loginID = $SYS.getData('loginID');
    password = $SYS.getData('password');
    $VAR.failCount = $VAR.failCount + 1;
    $SELENIUM.assist().type(By.id('loginID'), loginID,'');
    $SELENIUM.assist().type(By.id('password'), password, '');
    $SELENIUM.getWebDriver().findElement(By.id('loginBtn')).click();

}


@TRIGGER('U9ce6f707')
def 'ConsecutiveInvalidLogin: checkLoginLocked'() {

    if ($SELENIUM.assist().containsText('Maximum consecutive failed login')) {
        $SYS.addReqFailed('REQ_LOGIN_LOCK', 'Fail to trigger login lock after 3 attempts with invalid logins');
    }
    else {
        $SYS.addReqPassed('REQ_LOGIN_LOCK', 'Successfully triggered login lock at 3 invalid login attempt');
    }
    $SELENIUM.getWebDriver().findElement(By.id('closeBtn')).click();

}



@TRIGGER('MBT_FAIL')
def 'MBT_FAIL' () {
	// AUT failure/defect detected
	$SELENIUM.snapScreen('');
	$SYS.log('Defect trace: ' + $SYS.trace());
}


// define user js functions
def userFunc1 (p1, p2) {
    $SYS.log('user function userFunc1 called with params: p1=' + p1 + ', p2=' + p2);   
    // test
}


