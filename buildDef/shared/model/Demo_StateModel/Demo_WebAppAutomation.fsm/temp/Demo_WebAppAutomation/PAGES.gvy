// PAGE Script
import org.openqa.selenium.By;

mainPage = $SYS.addPage('MainPage')
	.addAction ('getBodyText', { page ->
		 $SELENIUM.getWebDriver().findElement(By.tagName('body')).getText();
	})

mainPage.addElement('Balance', By.id('amount'))
	.addAction('getText', { elem, params ->
		 return $SELENIUM.getWebDriver().findElement(elem.locator).getText();
	})

mainPage.addElement('Quarter', By.id('addQuarter'))
	.addAction('click', { elem, params ->
		 $SELENIUM.getWebDriver().findElement(elem.locator).click();
	})

mainPage.addElement('HalfDollar', By.id('addHalfDollar'))
	.addAction('click', { elem, params ->
		 $SELENIUM.getWebDriver().findElement(elem.locator).click();
	})

mainPage.addElement('Cancel', By.id('cancel'))
	.addAction('click', { elem, params ->
		 $SELENIUM.getWebDriver().findElement(elem.locator).click();
	})

mainPage.addElement('DrinkPepsi', By.id('Pepsi'))
	.addAction('click', { elem, params ->
		 $SELENIUM.getWebDriver().findElement(elem.locator).click();
	})

mainPage.addElement('DrinkMountainDew', By.id('MountainDew'))
	.addAction('click', { elem, params ->
		 $SELENIUM.getWebDriver().findElement(elem.locator).click();
	})

mainPage.addElement('DrinkCoke', By.id('Coke'))
	.addAction('click', { elem, params ->
		 $SELENIUM.getWebDriver().findElement(elem.locator).click();
	})

mainPage.addElement('DrinkSprite', By.id('Sprite'))
	.addAction('click', { elem, params ->
		 $SELENIUM.getWebDriver().findElement(elem.locator).click();
	})

elem = mainPage.addElement('DrinkWater', By.id('Water'))
elem.addAction('click', { elem, params ->
    $SELENIUM.getWebDriver().findElement(elem.locator).click();
})

mainPage.addElement('DrinkDisplay', By.id('productName'))
	.addAction('getText', { elem, params ->
		 $SELENIUM.getWebDriver().findElement(elem.locator).getText();
	})

mainPage.addElement('ProductName', By.id('productName'))
	.addAction('getText', { elem, params ->
		 $SELENIUM.getWebDriver().findElement(elem.locator).getText();
	})
