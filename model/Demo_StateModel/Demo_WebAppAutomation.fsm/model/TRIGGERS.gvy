// Model Triggers
import com.testoptimal.mscript.groovy.TRIGGER
import org.openqa.selenium.By;
 
import org.openqa.selenium.WebDriver;
// import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.chrome.ChromeDriver;
// import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
// import com.testoptimal.plugin.selenium.WebDriverAssist;

@TRIGGER('MBT_START')
def 'MBT_START' () {
	$SYS.log('current browser is: ' + $SYS.getExecDir().getExecSetting().getOption('ideBrowser'));
   $SYS.log ('mbt started. ');
// 	$VAR.wd = new HtmlUnitDriver();

// 	WebDriverManager.safaridriver().setup()
	$VAR.wd = new SafariDriver();

// 	WebDriverManager.chromedriver().setup()
// 	$VAR.wd = new ChromeDriver();

// 	WebDriverManager.firefoxdriver().setup()
// 	$VAR.wd = new FirefoxDriver();
	
	$VAR.outFile = new File ($SYS.getModelFolderPath() + '/report/test_out.html');
   $VAR.outFile << '<html><body><H1>Test Output</H1>\n';
   $VAR.outFile << '<ol>\n';
	$SYS.log($DATASET*.key);
	
	$SYS.log('datasets: ' + ($SYS.listDataSets().size()));
	
}


@TRIGGER('MBT_END')
def 'MBT_END' () {
	$VAR.outFile << '</ol></body></html>\n';
	try { $VAR.wd.close(); } catch (err) { }
	try { $VAR.wd.quit(); } catch (err) { }
}

@TRIGGER('MBT_FAIL')
def 'MBT_FAIL' () {
   // Failure detected
	$SYS.log('Failure detected')
// 	WebDriverAssist wdAssist = new WebDriverAssist($VAR.wd);
//    wdAssist.snapScreen($SYS.getModelFolderPath() + '/snapscreen');
	
}

@TRIGGER('MBT_ERROR')
def 'MBT_ERROR' () {
	$SYS.log('System error detected')
	try { $VAR.wd.close(); } catch (err) { }
	try { $VAR.wd.quit(); } catch (err) { }

}

@TRIGGER('U1062')
def 'Start' () {
	$SYS.log('getting url ' + 'http://localhost:' + $UTIL.getPort() + '/DemoApp/VendingMachine/VendingMachine.html');
	$VAR.wd.get('http://localhost:' + $UTIL.getPort() + '/DemoApp/VendingMachine/VendingMachine.html');
   $VAR.wd.findElement(By.id('cancel')).click();
   bal = $VAR.wd.findElement(By.id('amount')).getText();
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
	
   $VAR.wd.findElement(By.id('addQuarter')).click();
   bal = $VAR.wd.findElement(By.id('amount')).getText();
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
	
   $VAR.wd.findElement(By.id('addHalfDollar')).click();
   bal = $VAR.wd.findElement(By.id('amount')).getText();
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
   $VAR.wd.findElement(By.id('addQuarter')).click();
   bal = $VAR.wd.findElement(By.id('amount')).getText();
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
   $VAR.wd.findElement(By.id('addHalfDollar')).click();
   bal = $VAR.wd.findElement(By.id('amount')).getText();
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
   $VAR.wd.findElement(By.id('addQuarter')).click();
   bal = $VAR.wd.findElement(By.id('amount')).getText();
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
   $VAR.wd.findElement(By.id('addHalfDollar')).click();
   bal = $VAR.wd.findElement(By.id('amount')).getText();
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
   $VAR.wd.findElement(By.id('addQuarter')).click();
   bal = $VAR.wd.findElement(By.id('amount')).getText();
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
   $VAR.wd.findElement(By.id('addHalfDollar')).click();
   bal = $VAR.wd.findElement(By.id('amount')).getText();
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
   $VAR.wd.findElement(By.id($DATASET.DrinkChoices.get('Drink'))).click();
   drinkDisplayed = $VAR.wd.findElement(By.id('productName')).getText();
   drinkExpected = $DATASET.DrinkChoices.get('DrinkCheckText');
	
	$DATASET.DrinkChoices.next();
	
   if (drinkDisplayed.indexOf(drinkExpected)>=0) {
      $SYS.addReqPassed ('DRINK', 'Correct drink has been dispensed: ' + drinkExpected);
   }
   else {
      failMsg = 'Incorrect drink was dispensed. Selected ' + drinkExpected + ', got this instead: ' + drinkDisplayed;
      $SYS.addReqFailed ('DRINK', failMsg, 'Vend-BUG');
   }
   $VAR.outFile << '<li>Step: select a drink: ' + drinkExpected + ' </li>\n';
}

@TRIGGER('U1061')
def 'End' () {
   $SYS.genMSC('TestCase ' + $SYS.getPathName(), $SYS.getPathName());
   $VAR.outFile << "<li><img src='" + $SYS.getPathName() + ".png'/></li>\n";
   $VAR.outFile << '</ul></li>\n';
}
