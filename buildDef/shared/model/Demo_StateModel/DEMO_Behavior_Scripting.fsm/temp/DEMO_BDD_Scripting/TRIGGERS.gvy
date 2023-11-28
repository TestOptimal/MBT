// Model Triggers
import com.testoptimal.mscript.groovy.TRIGGER

@TRIGGER('MBT_START')
def 'MBT_START' () {
	$STEPS.setBrowserType('Firefox') // Open Firefox browser
}

@TRIGGER('MBT_FAIL')
def 'MBT_FAIL' () {
    $STEPS.takeScreenshot() // Take a screenshot
}

@TRIGGER('MBT_ERROR')
def 'MBT_ERROR' () {
    $STEPS.takeScreenshot() // Take a screenshot
}

@TRIGGER('U1062')
def 'Start' () {
    $STEPS.gotoURL('DemoApp/VendingMachine/VendingMachine.html') // Goto webpage DemoApp/VendingMachine/VendingMachine.html
    $STEPS.clickCancel() // Click on Cancel button
    $STEPS.checkBalance('0.00') // Assert that balance is set to 0.00
}

@TRIGGER('U1072')
def 'Start: add25'() {
    $STEPS.addCoin('Quarter') // Insert a Quarter
    $STEPS.checkBalance('0.25','REQ-QUARTER') // Assert balance is 0.25 for covering requirement REQ-QUARTER
}

@TRIGGER('U1073')
def 'Start: add50'() {
    $STEPS.addCoin('HalfDollar') // Insert a HalfDollar
    $STEPS.checkBalance('0.50','REQ-HALFDOLLOR') // Assert balance is 0.50 for covering requirement REQ-HALFDOLLOR
}

@TRIGGER('U1065')
def 'V25Cents: add25'() {
    $STEPS.addCoin('Quarter') // Insert a Quarter
    $STEPS.checkBalance('0.50','REQ-QUARTER') // Assert balance is 0.50 for covering requirement REQ-QUARTER
}

@TRIGGER('U1066')
def 'V25Cents: add50'() {
    $STEPS.addCoin('HalfDollar') // Insert a HalfDollar
    $STEPS.checkBalance('0.75','REQ-HALFDOLLOR') // Assert balance is 0.75 for covering requirement REQ-HALFDOLLOR
}

@TRIGGER('U1067')
def 'V50Cents: add25'() {
    $STEPS.addCoin('Quarter') // Insert a Quarter
    $STEPS.checkBalance('0.75','REQ-QUARTER') // Assert balance is 0.75 for covering requirement REQ-QUARTER
}

@TRIGGER('U1068')
def 'V50Cents: add50'() {
    $STEPS.addCoin('HalfDollar') // Insert a HalfDollar
    $STEPS.checkBalance('1.00','REQ-HALFDOLLOR') // Assert balance is 1.00 for covering requirement REQ-HALFDOLLOR
}

@TRIGGER('U1069')
def 'V75Cents: add25'() {
    $STEPS.addCoin('Quarter') // Insert a Quarter
    $STEPS.checkBalance('1.00','REQ-QUARTER') // Assert balance is 1.00 for covering requirement REQ-QUARTER
}

@TRIGGER('U1070')
def 'V75Cents: add50'() {
    $STEPS.addCoin('HalfDollar') // Insert a HalfDollar
    $STEPS.checkBalance('1.25','REQ-HALFDOLLOR') // Assert balance is 1.25 for covering requirement REQ-HALFDOLLOR
}
@TRIGGER('U1064')
def 'V125Cents: return25'() {
    $STEPS.checkReturn('25') // Assert 25 cents is returned
}

@TRIGGER('U1071')
def 'Choose Drink: discharge'() {
    $STEPS.chooseDrink() // Choose a drink
    $STEPS.checkDrinkDispensed('REQ-DRINK') // Assert correct drink is dispensed, covering requirement REQ-DRINK
}
