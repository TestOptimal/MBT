// PAGE Script
// Create UI page to be referenced in TRIGGER to perform automation.
// An UI page is a collection of elements.  An element has a locator
// and one or many actions.  For example, a login page has 3 elements
//   username field, password field and a Login button.
// Each of the element has a locator to find them respectively. 
// An element can have one or more actions. For example, username
// and password elements have a "set" action to enter values into the
// field and Login button has a "click" action to click on the button.
//
// The advantage of using UI Page is to de-couple the automation script
// from the UI layout and how to interact with UI to minimize the impact
// to the automation script when UI layout is changed. 
//
// In this model, we will create one UI page and 4 elements each representing
// a button, field and a group of drink selection buttons on the screen.

import org.openqa.selenium.By;

def addVendingMachinePage() {
	mainPage = $PAGE.addPage('MainPage');
	mainPage.addAction('go', { page, params ->
		$VAR.webDriver.get(params[0]);
	});
	mainPage.addAction('getDrinkDispensed', { page, params -> 
		$VAR.webDriver.findElement(By.id('productName')).getText();
	});
	mainPage.addAction('getBalance', { elem, params -> 
		$VAR.webDriver.findElement(By.id('amount')).getText();
	});
	

	elemAdd25c = mainPage.addElement('25c', By.id('addQuarter'));
	elemAdd25c.addAction('click', { elem, params -> 
		$VAR.webDriver.findElement(elem.locator).click();
	});

	elemAdd50c = mainPage.addElement('50c', By.id('addHalfDollar'));
	elemAdd50c.addAction('click', { elem, params -> 
		$VAR.webDriver.findElement(elem.locator).click();
	});

	elemCancel = mainPage.addElement('cancelBtn', By.id('cancel'));
	elemCancel.addAction('click', { elem, params -> 
		$VAR.webDriver.findElement(elem.locator).click();
	});
	
	
	// the drink name will be passed as parameter to the click action
	elemDrinks = mainPage.addElement('drinkChoices', null);
	mainPage.addAction ('chooseDrink', { elem, params ->
		$VAR.webDriver.findElement(By.id(params[0])).click();
	});
}
												
addVendingMachinePage();
												
