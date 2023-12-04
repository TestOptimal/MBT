// BBD Steps Definition. 
// Call steps with following syntax:
//  >> Take a screenshot
import com.testoptimal.exec.mscript.STEP
import com.testoptimal.exception.MBTException
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import io.github.bonigarcia.wdm.WebDriverManager;

@STEP('Take a screenshot')
def takeScreenshot () {
	$EXEC.log('screenshot not implemented');
//    $VAR.webDriver.snapScreen('');
}

@STEP('Trace last {steps} steps')
def takeTrace (steps) {
   $EXEC.log('Steps to reproduce bug: ' + $EXEC.trace(Integer.parseInt(steps), ';'));
}

@STEP('Close browser')
def close() {
	try { $VAR.webDriver.close(); } catch (err) { }
	try { $VAR.webDriver.quit(); } catch (err) { }
}

@STEP('Open {browserType} browser')
def setBrowserType (String browserType) {
	
		// test with current browser, or you can choose to test with specific browser
	switch (browserType) {
		case 'Safari':
// 			WebDriverManager.safaridriver().setup();
			$VAR.webDriver = new SafariDriver();
			break;
		case 'Chrome':
// 			WebDriverManager.chromedriver().setup();
			$VAR.webDriver = new ChromeDriver();
			break;
		case 'Firefox':
// 			WebDriverManager.firefoxdriver().setup();
			$VAR.webDriver = new FirefoxDriver();
			break;
		default:
			$VAR.webDriver = new HtmlUnitDriver();
	}

}

@STEP('Goto webpage {urlPage}')
def gotoURL (String urlPage) {
	$VAR.webDriver.get('http://localhost:' + $UTIL.getPort() + '/' + urlPage);
}

@STEP('Click on Cancel button')
def clickCancel() {
	$VAR.webDriver.findElement(By.id('cancel')).click()
}

@STEP('Assert that balance is set to {balExp}') 
def checkBalance (balExp) {
    bal = $VAR.webDriver.findElement(By.id('amount')).getText();
    if (!bal.equals(balExp)) {
        $EXEC.getCurTraverseObj().addReqFailed ('Balance check failed: Expecting ' + balExp + ', actual ' + bal, 'Cancel', 'CANCEL-FAILED');
    }
}

@STEP('Insert a {coin}')
def addCoin (String coin) {
	switch(coin.toLowerCase()) {
		case 'quarter':
			 $VAR.webDriver.findElement(By.id('addQuarter')).click();
			break;
		case 'halfdollar':
			 $VAR.webDriver.findElement(By.id('addHalfDollar')).click();
			break;
		default:
			throw MBTException ('Invalid coin ' + coin);
	}
}

@STEP('Assert balance is {balExp} for covering requirement {reqTag}')
def checkBalance (balExp, reqTag) {
    def balActual = $VAR.webDriver.findElement(By.id('amount')).getText();
    if (balActual.equals(balExp)) {
        passMsg = "Requirement " + reqTag + " passed, balance confirmed: " + balExp;
        $EXEC.getCurTraverseObj().addReqPassed(reqTag, passMsg);
    }
    else {
        failMsg = "Requirement " + reqTag + " failed, expecting balance of " + balExp + ", but got " + balActual;
        $EXEC.getCurTraverseObj().addReqFailed(reqTag, failMsg);
    }
}

@STEP('Assert {num} cents is returned')
def checkReturn (num) {
    $EXEC.log('Step: check return change ' + num + ' cents');
}
      
@STEP('Choose a drink')
def chooseDrink () {
	$VAR.webDriver.findElement(By.id($EXEC.dataset('DrinkChoices').get('Drink'))).click();
}

@STEP('Assert correct drink is dispensed, covering requirement {reqTag}')
def checkDrinkDispensed (reqTag) {
    drinkDisplayed = $VAR.webDriver.findElement(By.id('productName')).getText();
    drinkExpected = $EXEC.dataset('DrinkChoices').get('DrinkCheckText');
    if (drinkDisplayed.indexOf(drinkExpected)>=0) {
        $EXEC.addReqPassed (reqTag, 'Correct drink has been dispensed: ' + drinkExpected);
    }
    else {
        failMsg = 'Incorrect drink was dispensed. Expecting ' + drinkExpected + ', got this instead: ' + drinkDisplayed;
        $EXEC.getCurTraverseObj().addReqFailed (reqTag, failMsg);
    }
}
