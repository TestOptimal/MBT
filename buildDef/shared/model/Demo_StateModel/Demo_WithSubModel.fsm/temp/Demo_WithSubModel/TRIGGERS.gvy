// Model's Trigger script
import com.testoptimal.mscript.groovy.TRIGGER
import org.openqa.selenium.By;

// --- Ctrl/Cmd-I to insert a trigger for a state or transition in the model.

@TRIGGER('MBT_START')
def 'MBT_START' () {
	$SELENIUM.setBrowserChrome();
}


@TRIGGER('U474')
def 'Start' () {
	$SELENIUM.getWebDriver().get('http://localhost:' + $UTIL.getPort() + '/DemoApp/DemoWebSite/TestOptimalHome.html');
}


@TRIGGER('U494')
def 'Start: HomePage'() {
	$SELENIUM.getWebDriver().findElement(By.id('menuHome')).click();
}



@TRIGGER('U495')
def 'Start: FeaturePage'() {
	$SELENIUM.getWebDriver().findElement(By.id('menuFeature')).click();
}


@TRIGGER('U496')
def 'Start: DownloadPage'() {
	$SELENIUM.getWebDriver().findElement(By.id('menuDownload')).click();
}


@TRIGGER('U477')
def 'HomePage' () {
    curPageTitle = $SELENIUM.getWebDriver().getTitle();
    $SELENIUM.assist().waitForElement(By.id('menuHome'),1000);
    if (curPageTitle=="Demo WebSite - Home") {
        $SYS.addReqPassed('REQ_HOMEPAGE', 'Homepage check good');
    }
    else {
        $SYS.addReqFailed('REQ_HOMEPAGE', "Homepage check failed" + curPageTitle);
    }
}



@TRIGGER('U476')
def 'FeaturePage' () {
    curPageTitle = $SELENIUM.getWebDriver().getTitle();
    $SELENIUM.assist().waitForElement(By.id('menuFeature'),1000);
    if (curPageTitle=="Demo WebSite - Features") {
        $SYS.addReqPassed('REQ_FEATURES', 'Homepage check good');
    }
    else {
        $SYS.addReqFailed('REQ_FEATURES', "Homepage check failed" + curPageTitle);
    }
}


@TRIGGER('U478')
def 'DownloadPage' () {
    curPageTitle = $SELENIUM.getWebDriver().getTitle();
    $SELENIUM.assist().waitForElement(By.id('menuDownload'),1000);
    if (curPageTitle=="Demo WebSite - Downloads") {
        $SYS.addReqPassed('REQ_DOWNLOADS', 'Homepage check good');
    }
    else {
        $SYS.addReqFailed('REQ_DOWNLOADS', "Homepage check failed" + curPageTitle);
    }
}

