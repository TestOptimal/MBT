// Model Triggers
import com.testoptimal.mscript.groovy.TRIGGER
@TRIGGER('MBT_START')
def 'MBT_START' () {
	>> Open Safari browser
}

@TRIGGER('MBT_END')
def 'MBT_END' () {
	>> Close browser
}

@TRIGGER('MBT_ERROR')
def 'MBT_ERROR' () {
	>> Take a screenshot
	>> Close browser
}


@TRIGGER('MBT_FAIL')
def 'MBT_FAIL' () {
    >> Take a screenshot
    >> Trace last 3 steps
}

@TRIGGER('U1062')
def 'Start' () {
    >> Goto webpage DemoApp/VendingMachine/VendingMachine.html
    >> Click on Cancel button
    >> Assert that balance is set to 0.00
}

@TRIGGER('U1072')
def 'Start: add25'() {
    >> Insert a Quarter
    >> Assert balance is 0.25 for covering requirement REQ-QUARTER
}

@TRIGGER('U1073')
def 'Start: add50'() {
    >> Insert a HalfDollar
    >> Assert balance is 0.50 for covering requirement REQ-HALFDOLLOR
}

@TRIGGER('U1065')
def 'V25Cents: add25'() {
    >> Insert a Quarter
    >> Assert balance is 0.50 for covering requirement REQ-QUARTER
}

@TRIGGER('U1066')
def 'V25Cents: add50'() {
    >> Insert a HalfDollar
    >> Assert balance is 0.75 for covering requirement REQ-HALFDOLLOR
}

@TRIGGER('U1067')
def 'V50Cents: add25'() {
    >> Insert a Quarter
    >> Assert balance is 0.75 for covering requirement REQ-QUARTER
}

@TRIGGER('U1068')
def 'V50Cents: add50'() {
    >> Insert a HalfDollar
    >> Assert balance is 1.00 for covering requirement REQ-HALFDOLLOR
}

@TRIGGER('U1069')
def 'V75Cents: add25'() {
    >> Insert a Quarter
    >> Assert balance is 1.00 for covering requirement REQ-QUARTER
}

@TRIGGER('U1070')
def 'V75Cents: add50'() {
    >> Insert a HalfDollar
    >> Assert balance is 1.25 for covering requirement REQ-HALFDOLLOR
}
@TRIGGER('U1064')
def 'V125Cents: return25'() {
    >> Assert 25 cents is returned
}

@TRIGGER('U1071')
def 'Choose Drink: discharge'() {
    >> Choose a drink
    >> Assert correct drink is dispensed, covering requirement REQ-DRINK
}
