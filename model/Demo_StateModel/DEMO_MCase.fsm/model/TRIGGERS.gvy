// Model Triggers
import com.testoptimal.exec.mscript.TRIGGER
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import io.github.bonigarcia.wdm.WebDriverManager;

// This script automates the testing of a webpage (Vending machine) and 
// at the same time outputs a test case report (stored in "report" folder

@TRIGGER('MBT_START')
def 'MBT_START' () {
	ideBrowser = $SYS.getExecSetting().getOption('ideBrowser');
	$SYS.log('current browser is: ' + ideBrowser);
   $SYS.log ('mbt started. ');
	
	// test with current browser, or you can choose to test with specific browser
	switch (ideBrowser) {
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
	
	$VAR.outFile = new File ($SYS.getModelMgr().getReportFolderPath() + '/test_out.html');
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
	$SYS.log('System error detected, model exec aborted')
	try { $VAR.webDriver.close(); } catch (err) { }
	try { $VAR.webDriver.quit(); } catch (err) { }
}

@TRIGGER('U1062')
def 'Start' () {
	$SYS.log('getting url ' + 'http://localhost:' + $UTIL.getPort() + '/DemoApp/VendingMachine/VendingMachine.html');
	$SYS.page('MainPage').perform('go', 'http://localhost:' + $UTIL.getPort() + '/DemoApp/VendingMachine/VendingMachine.html');
   bal = $SYS.page('MainPage').perform('getBalance');
	$SYS.log('bal: ' + bal);
   if (!bal.equals('0.00') && !bal.equals('0')) {
      $SYS.addReqFailed ('Cancel failed to reset balance to 0.00', 'Cancel', 'CANCEL-FAILED');
      $SYS.abort('Cancel does not work!!!');
   }
}

@TRIGGER('U1072')
def 'Start: add25'() {
	$VAR.outFile << '<li><span>Test Case ' + $SYS.getPathName() + '</span>\n<ul>\n';
   $VAR.outFile << '<li>Step: add a Quarter</li>\n';
	
   $SYS.page('MainPage').element('25c').perform('click');
   bal = $SYS.page('MainPage').perform('getBalance');
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
	$VAR.outFile << '<li><span>Test Case ' + $SYS.getPathName() + '</span>\n<ul>\n';
   $VAR.outFile << '<li>Step: add a Half-Dollar</li>\n';
	
   $SYS.page('MainPage').element('25c').perform('click');
   bal = $SYS.page('MainPage').perform('getBalance');
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
   $VAR.outFile << '<li>Step: add a Quarter</li>\n';
   $SYS.page('MainPage').element('25c').perform('click');
   bal = $SYS.page('MainPage').perform('getBalance');
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
   $VAR.outFile << '<li>Step: add a Half-Dollar</li>\n';
   $SYS.page('MainPage').element('50c').perform('click');
   bal = $SYS.page('MainPage').perform('getBalance');
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
   $VAR.outFile << '<li>Step: add a Quarter</li>\n';
   $SYS.page('MainPage').element('25c').perform('click');
   bal = $SYS.page('MainPage').perform('getBalance');
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
   $VAR.outFile << '<li>Step: add a Half-Dollar</li>\n';
   $SYS.page('MainPage').element('50c').perform('click');
   bal = $SYS.page('MainPage').perform('getBalance');
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
   $VAR.outFile << '<li>Step: add a Quarter</li>\n';
   $SYS.page('MainPage').element('25c').perform('click');
   bal = $SYS.page('MainPage').perform('getBalance');
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
   $VAR.outFile << '<li>Step: add a Half-Dollar</li>\n';
   $SYS.page('MainPage').element('50c').perform('click');
   bal = $SYS.page('MainPage').perform('getBalance');
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
   $VAR.outFile << '<li>Step: check return change 25 cents</li>\n';
}

@TRIGGER('U1071')
def 'Choose Drink: discharge'() {
   drinkCodeSelected = $SYS.dataset('DrinkChoices').get('Drink');
   drinkTextExpected = $SYS.dataset('DrinkChoices').get('DrinkCheckText');
   $SYS.page('MainPage').perform('chooseDrink', drinkCodeSelected);
	drinkDispensed = $SYS.page('MainPage').perform('getDrinkDispensed');
   bal = $SYS.page('MainPage').perform('getBalance');
	
   if (drinkDispensed.indexOf(drinkTextExpected)>=0) {
      $SYS.addReqPassed ('DRINK', 'Correct drink has been dispensed: ' + drinkTextExpected);
   }
   else {
      failMsg = 'Incorrect drink was dispensed. Selected ' + drinkTextExpected + ', got this instead: ' + drinkDispensed;
      $SYS.addReqFailed ('DRINK', failMsg, 'Vend-BUG');
   }
   $VAR.outFile << '<li>Step: select a drink: ' + drinkTextExpected + ' </li>\n';
}

@TRIGGER('U1061')
def 'End' () {
   $SYS.genMSC('TestCase ' + $SYS.getPathName(), $SYS.getPathName());
   $VAR.outFile << "<li><img src='" + $SYS.getPathName() + ".png'/></li>\n";
   $VAR.outFile << '</ul></li>\n';
}
