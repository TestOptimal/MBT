// BBD Steps Definition. 
// Call steps with following syntax:
//  >> Take a screenshot
import com.testoptimal.mscript.groovy.STEP
import org.openqa.selenium.By;
import com.testoptimal.exception.MBTException

@STEP('Take a screenshot')
def takeScreenshot () {
   $SELENIUM.snapScreen('');
}

@STEP('Trace last {steps} steps')
def takeTrace (steps) {
   $SYS.log('Steps to reproduce bug: ' + $SYS.trace(Integer.parseInt(steps), ';'));
}

@STEP('Open {browserType} browser')
def setBrowserType (String browserType) {
	switch(browserType.toLowerCase()) {
		case ['firefox','ff']:
			$SELENIUM.setBrowserFirefox();
			break;
		case ['ie','internetexplorer']:
			$SELENIUM.setBrowserIE();
			break;
		case ['safari']:
			$SELENIUM.setBrowserSafari();
			break;
		case ['htmlunit']:
			$SELENIUM.setBrowserHtmlunit();
			break;
		case ['chrome']:
		default:
			$SELENIUM.setBrowserChrome();
			break;
	}
}

@STEP('Goto webpage {urlPage}')
def gotoURL (String urlPage) {
	$SELENIUM.getWebDriver().get('http://localhost:' + $UTIL.getPort() + '/' + urlPage);
}

@STEP('Click on Cancel button')
def clickCancel() {
	$SELENIUM.getWebDriver().findElement(By.id('cancel')).click()
}

@STEP('Assert that balance is set to {balExp}') 
def checkBalance (balExp) {
    bal = $SELENIUM.getWebDriver().findElement(By.id('amount')).getText();
    if (!bal.equals(balExp)) {
        $SYS.addReqFailed ('Balance check failed: Expecting ' + balExp + ', actual ' + bal, 'Cancel', 'CANCEL-FAILED');
    }
}

@STEP('Insert a {coin}')
def addCoin (String coin) {
	switch(coin.toLowerCase()) {
		case 'quarter':
			 $SELENIUM.getWebDriver().findElement(By.id('addQuarter')).click();
			break;
		case 'halfdollar':
			 $SELENIUM.getWebDriver().findElement(By.id('addHalfDollar')).click();
			break;
		default:
			throw MBTException ('Invalid coin ' + coin);
	}
}

@STEP('Assert balance is {balExp} for covering requirement {reqTag}')
def checkBalance (balExp, reqTag) {
    def balActual = $SELENIUM.getWebDriver().findElement(By.id('amount')).getText();
    if (balActual.equals(balExp)) {
        passMsg = "Requirement " + reqTag + " passed, balance confirmed: " + balExp;
        $SYS.addReqPassed(reqTag, passMsg);
    }
    else {
        failMsg = "Requirement " + reqTag + " failed, expecting balance of " + balExp + ", but got " + balActual;
        $SYS.addReqFailed(reqTag, failMsg);
    }
}

@STEP('Assert {num} cents is returned')
def checkReturn (num) {
    $SYS.addTestOutput('Step: check return change ' + num + ' cents');
}
      
@STEP('Choose a drink')
def chooseDrink () {
	$SELENIUM.getWebDriver().findElement(By.id($SYS.getData('Drink'))).click();
}

@STEP('Assert correct drink is dispensed, covering requirement {reqTag}')
def checkDrinkDispensed (reqTag) {
    drinkDisplayed = $SELENIUM.getWebDriver().findElement(By.id('productName')).getText();
    drinkExpected = $SYS.getData('DrinkCheckText');
    if (drinkDisplayed.indexOf(drinkExpected)>=0) {
        $SYS.addReqPassed (reqTag, 'Correct drink has been dispensed: ' + drinkExpected);
    }
    else {
        failMsg = 'Incorrect drink was dispensed. Expecting ' + drinkExpected + ', got this instead: ' + drinkDisplayed;
        $SYS.addReqFailed (reqTag, failMsg);
    }
}
