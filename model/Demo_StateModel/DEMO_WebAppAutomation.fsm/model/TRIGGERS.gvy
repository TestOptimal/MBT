// Model Triggers
import com.testoptimal.exec.mscript.TRIGGER
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

// This script automates the testing of a webpage (Vending machine) and 
// at the same time outputs a test case report (stored in "report" folder

@TRIGGER('MBT_START')
def 'MBT_START' () {
	// change testBrowser to the browser you want to test with, 
	// see setup_webdriver function at the end for browsers supported
 	testBrowser = 'HtmlUnit';
	testBrowser = $EXEC.getExecSetting().getOption('ideBrowser');
	$EXEC.log('testing with browser ' + testBrowser);
	
	$VAR.webDriver = setup_webdriver(testBrowser);
	$VAR.outFile = new File ($EXEC.getReportFolderPath() + '/test_out.html');
	$VAR.outFile.newWriter();
   $VAR.outFile << '<html><body><H1>Test Output</H1>\n';
   $VAR.outFile << '<ol>\n';
}

@TRIGGER('MBT_END')
def 'MBT_END' () {
	$VAR.outFile << '</ol></body></html>\n';
	try { $VAR.webDriver.close(); } catch (err) { }
	try { $VAR.webDriver.quit(); } catch (err) { }
}

@TRIGGER('MBT_ERROR')
def 'MBT_ERROR' () {
	$EXEC.log('System error detected, model exec aborted')
	try { $VAR.webDriver.close(); } catch (err) { }
	try { $VAR.webDriver.quit(); } catch (err) { }
}

@TRIGGER('U1062')
def 'Start' () {
	$EXEC.log('getting url ' + 'http://localhost:' + $UTIL.getPort() + '/DemoApp/VendingMachine/VendingMachine.html');
	$PAGE.getPage('MainPage').perform('go', 'http://localhost:' + $UTIL.getPort() + '/DemoApp/VendingMachine/VendingMachine.html');
   bal = $PAGE.getPage('MainPage').perform('getBalance');
	$EXEC.log('bal: ' + bal);
   if (!bal.equals('0.00') && !bal.equals('0')) {
      $EXEC.getCurTraverseObj().addReqFailed ('Cancel failed to reset balance to 0.00', 'Cancel', 'CANCEL-FAILED');
      $EXEC.abort('Cancel does not work!!!');
   }
}

@TRIGGER('U1072')
def 'Start: add25'() {
	$VAR.outFile << '<li><span>Test Case ' + $EXEC.getPathName() + '</span>\n<ul>\n';
   $VAR.outFile << '<li>Step: add a Quarter</li>\n';
	
   $PAGE.getPage('MainPage').element('25c').perform('click');
   bal = $PAGE.getPage('MainPage').perform('getBalance');
   if (bal.equals('0.25')) {
      passMsg = "Insert one Quarter passed, balance confirmed: " + bal;
      $EXEC.getCurTraverseObj().addReqPassed("Q!", passMsg);
   }
   else {
      failMsg = "Insert one Quarter failed, expecting balance of 0.25, but got " + bal;
      $EXEC.getCurTraverseObj().addReqFailed("Q1", failMsg, 'QUARTER-0-25');
   }
}

@TRIGGER('U1073')
def 'Start: add50'() {
	$VAR.outFile << '<li><span>Test Case ' + $EXEC.getPathName() + '</span>\n<ul>\n';
   $VAR.outFile << '<li>Step: add a Half-Dollar</li>\n';
	
   $PAGE.getPage('MainPage').element('50c').perform('click');
   bal = $PAGE.getPage('MainPage').perform('getBalance');
   if (bal.equals('0.50')) {
      passMsg = "Insert one HalfDollar passed, balance confirmed: " + bal;
      $EXEC.getCurTraverseObj().addReqPassed("Q!", passMsg);
   }
   else {
      failMsg = "Insert one HalfDollar failed, expecting balance of 0.50, but got " + bal;
      $EXEC.getCurTraverseObj().addReqFailed("Q1", failMsg, 'QUARTER-0-50');
   }
}

@TRIGGER('U1065')
def 'V25Cents: add25'() {
   $VAR.outFile << '<li>Step: add a Quarter</li>\n';
   $PAGE.getPage('MainPage').element('25c').perform('click');
   bal = $PAGE.getPage('MainPage').perform('getBalance');
   if (bal.equals('0.50')) {
      passMsg = "Insert one Quarter passed, balance confirmed: " + bal;
      $EXEC.getCurTraverseObj().addReqPassed("Q1", passMsg);
   }
   else {
      failMsg = "Insert one Quarter failed, expecting balance of 0.50, but got " + bal;
      $EXEC.getCurTraverseObj().addReqFailed("Q1", failMsg, 'QUARTER-25-50');
   }
}

@TRIGGER('U1066')
def 'V25Cents: add50'() {
   $VAR.outFile << '<li>Step: add a Half-Dollar</li>\n';
   $PAGE.getPage('MainPage').element('50c').perform('click');
   bal = $PAGE.getPage('MainPage').perform('getBalance');
   if (bal.equals('0.75')) {
      passMsg = "Insert one HalfDollar passed, balance confirmed: " + bal;
      $EXEC.getCurTraverseObj().addReqPassed("H1", passMsg);
   }
   else {
      failMsg = "Insert one HalfDollar failed, expecting balance of 0.75, but got " + bal;
      $EXEC.getCurTraverseObj().addReqFailed("H1", failMsg, 'QUARTER-25-75');
   }
}

@TRIGGER('U1067')
def 'V50Cents: add25'() {
   $VAR.outFile << '<li>Step: add a Quarter</li>\n';
   $PAGE.getPage('MainPage').element('25c').perform('click');
   bal = $PAGE.getPage('MainPage').perform('getBalance');
   if (bal.equals('0.75')) {
      passMsg = "Insert one Quarter passed, balance confirmed: " + bal;
      $EXEC.getCurTraverseObj().addReqPassed("Q1", passMsg);
   }
   else {
      failMsg = "Insert one Quarter failed, expecting balance of 0.75, but got " + bal;
      $EXEC.getCurTraverseObj().addReqFailed("Q1", failMsg, 'QUARTER-50-25');
   }
}

@TRIGGER('U1068')
def 'V50Cents: add50'() {
   $VAR.outFile << '<li>Step: add a Half-Dollar</li>\n';
   $PAGE.getPage('MainPage').element('50c').perform('click');
   bal = $PAGE.getPage('MainPage').perform('getBalance');
   if (bal.equals('1.00')) {
      passMsg = "Insert one HalfDollor passed, balance confirmed: " + bal;
      $EXEC.getCurTraverseObj().addReqPassed("H1", passMsg);
   }
   else {
      failMsg = "Insert one HalfDollar failed, expecting balance of 1.00, but got " + bal;
      $EXEC.getCurTraverseObj().addReqFailed("H1", failMsg, 'QUARTER-0-50');
   }
}

@TRIGGER('U1069')
def 'V75Cents: add25'() {
   $VAR.outFile << '<li>Step: add a Quarter</li>\n';
   $PAGE.getPage('MainPage').element('25c').perform('click');
   bal = $PAGE.getPage('MainPage').perform('getBalance');
   if (bal.equals('1.00')) {
      passMsg = "Insert one Quarter passed, balance confirmed: " + bal;
      $EXEC.getCurTraverseObj().addReqPassed("Q1", passMsg);
   }
   else {
      failMsg = "Insert one Quarter failed, expecting balance of 1.00, but got " + bal;
      $EXEC.getCurTraverseObj().addReqFailed("Q1", failMsg, 'QUARTER-75-25');
   }
}

@TRIGGER('U1070')
def 'V75Cents: add50'() {
   $VAR.outFile << '<li>Step: add a Half-Dollar</li>\n';
   $PAGE.getPage('MainPage').element('50c').perform('click');
   bal = $PAGE.getPage('MainPage').perform('getBalance');
   if (bal.equals('1.25')) {
      passMsg = "Insert one HalfDollar passed, balance confirmed: " + bal;
      $EXEC.getCurTraverseObj().addReqPassed("H1", passMsg);
   }
   else {
      failMsg = "Insert one HalfDollar failed, expecting balance of 1.25, but got " + bal;
      $EXEC.getCurTraverseObj().addReqFailed("H1", failMsg, 'QUARTER-75-50');
   }
}

@TRIGGER('U1064')
def 'V125Cents: return25'() {
   $VAR.outFile << '<li>Step: check return change 25 cents</li>\n';
}

@TRIGGER('U1071')
def 'Choose Drink: discharge'() {
   drinkCodeSelected = $DATA.dataset('DrinkChoices').get('Drink');
   drinkTextExpected = $DATA.dataset('DrinkChoices').get('DrinkCheckText');
	$DATA.dataset('DrinkChoices').next();
	
   $PAGE.getPage('MainPage').perform('chooseDrink', drinkCodeSelected);
	drinkDispensed = $PAGE.getPage('MainPage').perform('getDrinkDispensed');
   bal = $PAGE.getPage('MainPage').perform('getBalance');
	
   if (drinkDispensed.indexOf(drinkTextExpected)>=0) {
      $EXEC.getCurTraverseObj().addReqPassed ('DRINK', 'Correct drink has been dispensed: ' + drinkTextExpected);
   }
   else {
      failMsg = 'Incorrect drink was dispensed. Selected ' + drinkTextExpected + ', got this instead: ' + drinkDispensed;
      $EXEC.getCurTraverseObj().addReqFailed ('DRINK', failMsg, 'Vend-BUG');
   }
   $VAR.outFile << '<li>Step: select a drink: ' + drinkTextExpected + ' </li>\n';
}

@TRIGGER('U1061')
def 'End' () {
   $EXEC.genMSC('TestCase ' + $EXEC.getPathName(), $EXEC.getPathName());
   $VAR.outFile << "<li><img src='" + $EXEC.getPathName() + ".png'/></li>\n";
   $VAR.outFile << '</ul></li>\n';
}

// you may need to play with the web driver options to get it to work in your environment
// add additional browsers support here
def setup_webdriver (testBrowser) {
	switch (testBrowser) {
		case 'Safari':
 			WebDriverManager.safaridriver().setup();
			return new SafariDriver();
		case 'Chrome':
			WebDriverManager.chromedriver().setup();
			ChromeOptions chromeOptions = new ChromeOptions();
//  			chromeOptions.setHeadless(true);
			chromeOptions.addArguments("--remote-allow-origins=*");
			return new ChromeDriver(chromeOptions);
		case 'Firefox':
			WebDriverManager.firefoxdriver().setup();
			FirefoxOptions firefoxOptions = new FirefoxOptions();
// 			firefoxOptions.setHeadless(true);
			firefoxOptions.addArguments("--remote-allow-origins=*");
			return new FirefoxDriver(firefoxOptions);
		case 'Remote':
			ChromeOptions remoteOptions = new ChromeOptions();
			// set gridUrl below to point to your remote webdriver server
    		return new RemoteWebDriver('gridUrl', remoteOptions);
		case 'Edge':
			WebDriverManager.edgedriver().setup();
			EdgeOptions edgeOptions = new EdgeOptions();
			edgeOptions.addArguments("--remote-allow-origins=*");
    		return new EdgeDriver(edgeOptions);
		default:
			wd = new HtmlUnitDriver();
			wd.setJavascriptEnabled(true);
			return wd;
	}	
}
