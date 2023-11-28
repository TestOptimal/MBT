// Model Triggers
import com.testoptimal.mscript.groovy.TRIGGER

@TRIGGER('MBT_START')
def 'MBT_START' () {
    $SELENIUM.setBrowser ($SYS.getExecDir().getExecSetting().getOption('ideBrowser'));
    $SYS.addTestOutput('<html><body><H1>Test Output</H1>');
    $SYS.addTestOutput('<ol>');\
    $SYS.log ('mbt started. ');
}

@TRIGGER('MBT_END')
def 'MBT_END' () {
    $SYS.addTestOutput('</ol></body></html>');
    $SYS.saveTestOutput('testOutput.html');
}

@TRIGGER('MBT_FAIL')
def 'MBT_FAIL' () {
    // Failure detected
    $SELENIUM.snapScreen('');
}

@TRIGGER('MBT_ERROR')
def 'MBT_ERROR' () {
}

@TRIGGER('U1062')
def 'Start' () {
    $SELENIUM.getWebDriver().get('http://localhost:' + $UTIL.getPort() + '/DemoApp/VendingMachine/VendingMachine.html');
    $SYS.page('MainPage').element('Cancel').perform('click');
    bal = $SYS.page('MainPage').element('Balance').perform('getText');
    if (!bal.equals('0.00')) {
        $SYS.addReqFailed ('Cancel failed to reset balance to 0.00', 'Cancel', 'CANCEL-FAILED');
        $SYS.abort('Cancel does not work!!!');
    }
}

@TRIGGER('U1072')
def 'Start: add25'() {
    $SYS.addTestOutput('<li><span>Test Case '  + $SYS.getPathName() + '</span><ul>');

    $SYS.addTestOutput('<li>Step: add a Quarter</li>');
    $SYS.page('MainPage').element('Quarter').perform('click');
    bal = $SYS.page('MainPage').element('Balance').perform('getText');
    if (bal.equals('0.25')) {
        passMsg = "Insert one Quarter passed, balance confirmed: " + bal;
        $SYS.addReqPassed("Q!", passMsg);
    }
    else {
        failMsg = "Insert one Quarter failed, expecting balance of 0.25, but got " + bal;
        $SYS.addReqFailed("Q1", failMsg, 'QUARTER-0-25');
    }
}

@TRIGGER('U1073')
def 'Start: add50'() {
    $SYS.addTestOutput('<li><span>Test Case '  + $SYS.getPathName() + '</span><ul>');

    $SYS.addTestOutput('<li>Step: add a Half-Dollar</li>');
    $SYS.page('MainPage').element('HalfDollar').perform('click');
    bal = $SYS.page('MainPage').element('Balance').perform('getText');
    if (bal.equals('0.50')) {
        passMsg = "Insert one HalfDollar passed, balance confirmed: " + bal;
        $SYS.addReqPassed("Q!", passMsg);
    }
    else {
        failMsg = "Insert one HalfDollar failed, expecting balance of 0.50, but got " + bal;
        $SYS.addReqFailed("Q1", failMsg, 'QUARTER-0-50');
    }
}

@TRIGGER('U1065')
def 'V25Cents: add25'() {
    $SYS.addTestOutput('<li>Step: add a Quarter</li>');
    $SYS.page('MainPage').element('Quarter').perform('click');
    bal = $SYS.page('MainPage').element('Balance').perform('getText');
    if (bal.equals('0.50')) {
        passMsg = "Insert one Quarter passed, balance confirmed: " + bal;
        $SYS.addReqPassed("Q1", passMsg);
    }
    else {
        failMsg = "Insert one Quarter failed, expecting balance of 0.50, but got " + bal;
        $SYS.addReqFailed("Q1", failMsg, 'QUARTER-25-50');
    }
}

@TRIGGER('U1066')
def 'V25Cents: add50'() {
    $SYS.addTestOutput('<li>Step: add a Half-Dollar</li>');
    $SYS.page('MainPage').element('HalfDollar').perform('click');
    bal = $SYS.page('MainPage').element('Balance').perform('getText');
    if (bal.equals('0.75')) {
        passMsg = "Insert one HalfDollar passed, balance confirmed: " + bal;
        $SYS.addReqPassed("H1", passMsg);
    }
    else {
        failMsg = "Insert one HalfDollar failed, expecting balance of 0.75, but got " + bal;
        $SYS.addReqFailed("H1", failMsg, 'QUARTER-25-75');
    }
}

@TRIGGER('U1067')
def 'V50Cents: add25'() {
    $SYS.addTestOutput('<li>Step: add a Quarter</li>');
    $SYS.page('MainPage').element('Quarter').perform('click');
    bal = $SYS.page('MainPage').element('Balance').perform('getText');
    if (bal.equals('0.75')) {
        passMsg = "Insert one Quarter passed, balance confirmed: " + bal;
        $SYS.addReqPassed("Q1", passMsg);
    }
    else {
        failMsg = "Insert one Quarter failed, expecting balance of 0.75, but got " + bal;
        $SYS.addReqFailed("Q1", failMsg, 'QUARTER-50-25');
    }
}

@TRIGGER('U1068')
def 'V50Cents: add50'() {
    $SYS.addTestOutput('<li>Step: add a Half-Dollar</li>');
    $SYS.page('MainPage').element('HalfDollar').perform('click');
    bal = $SYS.page('MainPage').element('Balance').perform('getText');
    if (bal.equals('1.00')) {
        passMsg = "Insert one HalfDollor passed, balance confirmed: " + bal;
        $SYS.addReqPassed("H1", passMsg);
    }
    else {
        failMsg = "Insert one HalfDollar failed, expecting balance of 1.00, but got " + bal;
        $SYS.addReqFailed("H1", failMsg, 'QUARTER-0-50');
    }
}

@TRIGGER('U1069')
def 'V75Cents: add25'() {
    $SYS.addTestOutput('<li>Step: add a Quarter</li>');
    $SYS.page('MainPage').element('Quarter').perform('click');
    bal = $SYS.page('MainPage').element('Balance').perform('getText');
    if (bal.equals('1.00')) {
        passMsg = "Insert one Quarter passed, balance confirmed: " + bal;
        $SYS.addReqPassed("Q1", passMsg);
    }
    else {
        failMsg = "Insert one Quarter failed, expecting balance of 1.00, but got " + bal;
        $SYS.addReqFailed("Q1", failMsg, 'QUARTER-75-25');
    }
}

@TRIGGER('U1070')
def 'V75Cents: add50'() {
    $SYS.addTestOutput('<li>Step: add a Half-Dollar</li>');
    $SYS.page('MainPage').element('HalfDollar').perform('click');
    bal = $SYS.page('MainPage').element('Balance').perform('getText');
    if (bal.equals('1.25')) {
        passMsg = "Insert one HalfDollar passed, balance confirmed: " + bal;
        $SYS.addReqPassed("H1", passMsg);
    }
    else {
        failMsg = "Insert one HalfDollar failed, expecting balance of 1.25, but got " + bal;
        $SYS.addReqFailed("H1", failMsg, 'QUARTER-75-50');
    }
}

@TRIGGER('U1064')
def 'V125Cents: return25'() {
    $SYS.addTestOutput('<li>Step: check return change 25 cents</li>');
}

@TRIGGER('U1071')
def 'Choose Drink: discharge'() {
    $SYS.page('MainPage').element('Drink' + $SYS.getData('Drink')).perform('click');
    drinkDisplayed = $SYS.page('MainPage').element('DrinkDisplay').perform('getText');
    drinkExpected = $SYS.getData('DrinkCheckText');
    if (drinkDisplayed.indexOf(drinkExpected)>=0) {
        $SYS.addReqPassed ('DRINK', 'Correct drink has been dispensed: ' + drinkExpected);
    }
    else {
        failMsg = 'Incorrect drink was dispensed. Selected ' + drinkExpected + ', got this instead: ' + drinkDisplayed;
        $SYS.addReqFailed ('DRINK', failMsg, 'Vend-BUG');
    }
    $SYS.addTestOutput('<li>Step: select a drink: ' + drinkExpected + ' </li>');
}

@TRIGGER('U1061')
def 'End' () {
    $SYS.genMSC('TestCase ' + $SYS.getPathName(), $SYS.getPathName());
    $SYS.addTestOutput("<li><img src='" + $SYS.getPathName() + ".png'/></li>");
    $SYS.addTestOutput('</ul></li>');
}


