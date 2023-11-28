// MODEL groovy script
import com.testoptimal.mscript.groovy.TRIGGER

@TRIGGER('MBT_START')
def 'MBT_START' () {
    $SELENIUM.setBrowser ($SYS.getExecDir().getExecSetting().getOption('ideBrowser'));
    $SYS.log("mbt starting");
    $SYS.addTestOutput ("<h3>Test Case Report for [ModelName]</h3>");
    $SYS.addTestOutput ("<div style='border: 1px solid green'>");
    $SYS.addTestOutput ("<div>Execution Date / Time: " + (new Date()) + "</div>");
    $SYS.addTestOutput ("<div>This is a report generated from the mscript comments.</div>");
    $SYS.addTestOutput ("<div>You can output test results directly to this reoprt with as much details as you want.</div>");
    $SYS.addTestOutput ("<div>You may also use this report to generate automation scripts for execution on external automation tools too.</div>");
    $SYS.addTestOutput ("</div>");
    $SYS.addTestOutput ("<hr/>");
    $VAR.ShoppingCartItemCount = 0;
}

@TRIGGER('MBT_END')
def 'MBT_END' () {
    $SYS.log('ENDEND');
    $SYS.addTestOutput ("<hr/>");
    $SYS.addTestOutput ("<h3 style='font-size:medium'>Execution Summary</h3>");
    $SYS.addTestOutput ("<pre>");
    $SYS.addTestOutput ($SYS.getStats());
    $SYS.addTestOutput ("</pre>");
    $SYS.saveTestOutput();
}

@TRIGGER('MBT_FAIL')
def 'MBT_FAIL' () {
//    $SYS.addTestOutput ($SYS.trace(3));
    $SYS.log("Taking snapshot, file at: " + $SELENIUM.snapScreen('failure_'));
    $SYS.addTestOutput ("<div>Failure detected</div>");
    $SYS.addTestOutput ("<div>Path: <img src='" + $SYS.genPath('errPath_' + $RAND.randNum(1000,9999))+ "'/></div>");
    $SYS.addTestOutput ("<div>Sequence Diagram: <a style='font-color:red;' href='" + $SYS.genMSC('Failure Detected', 'errMSC_' + $RAND.randNum(1000,9999)) + "'>seqGraph</a></div>");
}

@TRIGGER('U1418')
def 'Start: start'() {
    $SELENIUM.getWebDriver().get('http://localhost:' + $UTIL.getPort() + '/DemoApp/DemoWebApp/ProductList.html');
}

@TRIGGER('U1411')
def 'ProductList: AddItem'() {
    $SYS.log("Adds Item to Shopping Cart");
    prodCode = $RAND.randFromList('IPad|PicFrame|UsbMem');
    $SYS.page('ProductList').element('AddItemByProdCode').perform('click', prodCode);
    $SYS.page('ShoppingCart').element('TotalCost').waitUntil('getText', 1000);
    curItemCount = $SYS.page('ShoppingCart').element("OrderItems").perform("countItems");
    itemCountExpected = $VAR.ShoppingCartItemCount + 1;
    $SYS.addTestOutput ("Item Added: " + prodCode);
    if (curItemCount == itemCountExpected) {
        $SYS.addReqPassed('ADD-ITEM', 'Item successfully added');
    }
    else {
        failMsg = "Failed to add product " + prodCode + ", expecting " +  itemCountExpected + " items, instead it had " + curItemCount + " items";
        $SYS.addReqFailed('ADD-ITEM', failMsg, 'AddItemBug');
    }
    $VAR.ShoppingCartItemCount = curItemCount;
}
@TRIGGER('U1413')
def 'ProductList: ShowShoppingCart'() {
    if ($SYS.page('ProductList').element('ShoppingCart').perform('isPresent')) {
        $SYS.page('ProductList').element('ShoppingCart').perform('click');
    }
}
@TRIGGER('U1414')
def 'ProductList: ViewDetail'() {
    prodCode = $RAND.randFromList('IPad|PicFrame|UsbMem');
    $SYS.page('ProductList').element('DetailItemByProdCode').perform('click', prodCode);
    pageText = $SYS.page('ItemDetails').perform('getBodyText');
    if (pageText.indexOf(" ( " + prodCode + " )")>=0) {
        $SYS.addReqPassed("SHOW-DETAILS", 'Show item details passed for item: ' + prodCode);
    }    
    else {
        $SYS.addReqFailed ("SHOW-DETAILS", "Planted bug found: missing a space around paranthesis: ( " + prodCode + " )",
                           "SHOW-DETAILS-BUG");
    }
}
@TRIGGER('U1401')
def 'ProductDetail' () {
    expTitle = "WebStore - Product Details";
    title = $SYS.page('ItemDetails').perform('getTitle');
    if (title.equals(expTitle)) {
        $SYS.addReqPassed('ITEMDETAILS-PAGE', 'Page title checked passed');
    }
    else {
        $SYS.addReqFailed('ITEMDETAILS-PAGE', "Page title mismatch, expecting [" + expTitle + "], got [" + title + "]", "ITEMDETAILS-PAGE-NOTLOADED");
    }
}
@TRIGGER('U1409')
def 'ProductDetail: ContinueShopping'() {
    $SYS.page('ItemDetails').element('ProdList').perform('click');
}
@TRIGGER('U1410')
def 'ProductDetail: ShowShoppingCart'() {
    $SYS.page('ItemDetails').element('ShoppingCart').perform('click');
}
@TRIGGER('U1403')
def 'ShoppingCart' () {
    expTitle = "WebStore - Shopping Cart";
    title = $SYS.page('ShoppingCart').perform('getTitle');
    if (title.equals(expTitle)) {
        $SYS.addReqPassed('SHOPPINGCART-PAGE', 'Page title checked passed');
    }
    else {
        $SYS.addReqFailed('SHOPPINGCART-PAGE', "Page title mismatch, expecting [" + expTitle + "], got [" + title + "]", "SHOPPINGCART-PAGE-NOTLOADED");
    }
	
	
    OrderProdCodeList = $SYS.page('ShoppingCart').element('OrderItems').perform('getProdCodes')
    totalCosts = 0
    itemIndex = 0
    for (String prodCode: OrderProdCodeList) {
        prodPrice = $SYS.getDataSet('ProductList').getData(prodCode);
        totalCosts += prodPrice;
    }
    displayTotalCosts = $SYS.page('ShoppingCart').element('TotalCost').perform('getText');
    if (('$' + Math.round(totalCosts)).equals(displayTotalCosts)) { 
        $SYS.addReqPassed('COST-CALC', 'Item cost calculation passed');
    }
    else {
        $SYS.addReqFailed('COST-CALC', 'Item cost calculation failed, expecting $' + totalCosts + ', got ' + displayTotalCosts, 'CALC-ERROR');
    }
}
@TRIGGER('U1415')
def 'ShoppingCart: ContinueShopping'() {
    $SYS.page('ShoppingCart').element('ProdList').perform('click');
	
    expTitle = "WebStore - Product List";
    title = $SYS.page('ShoppingCart').perform('getTitle');
    if (title.equals(expTitle)) {
        $SYS.addReqPassed("PRODUCTLIST-PAGE", 'Page title checked passed');
    }
    else {
        $SYS.addReqFailed("PRODUCTLIST-PAGE", "Page title mismatch, expecting [" + expTitle + "], got [" + title + "]", "TITLE-SHOPPINGCART-BUG");
    }
}
@TRIGGER('U1417')
def 'ShoppingCart: RemoveItem'() {
    $SYS.addTestOutput("<div>Removing an item from the shopping cart,</div>");
    $SYS.addTestOutput("<div><img style='border:1px solid orange;' src='" + $SYS.genPath('removeItemPath_' + $RAND.randNum(1000,9999)) + "'/>");
    $SYS.addTestOutput("<div>Sequence Diagram: <a href='" + $SYS.genMSC('Remove Item from Shopping Cart', 'removeItemMSC_' + $RAND.randNum(1000,9999)) + "' target=_blank>TestCase MSC</a>");
    itemCountExpected = $SYS.page('ShoppingCart').element("RemoveLinks").perform('countItems') - 1;
    idx = $SYS.page('ShoppingCart').element("RemoveLinks").perform('clickOne');
    shoppingCartItemCount = $SYS.page('ShoppingCart').element("RemoveLinks").perform('countItems');
    if (itemCountExpected == shoppingCartItemCount) {
        $SYS.addReqPassed("REMOVE-ITEM", "Removed one item from shopping cart passed");
    }
    else {
        prodCodeRemoved = $SYS.page('ShoppingCart').element("OrderItems").perform("getProdCode");
        msg = "Failed to remove product " + prodCodeRemoved + ", item count expected after removal is " + itemCountExpected +", but page showed " + shoppingCartItemCount;
        $SYS.addReqFailed("REMOVE-ITEM", msg, "REMOVE-ITEM-BUG");
    }
    $VAR.ShoppingCartItemCount = shoppingCartItemCount;
}
@TRIGGER('U1416')
def 'ShoppingCart: Order'() {
    $SYS.page('ShoppingCart').element('OrderBtn').perform('click');
}
@TRIGGER('U1399')
def 'Checkout' () {
    expTitle = "WebStore - CheckOut";
    title = $SYS.page('Checkout').perform('getTitle');
    if (title.equals(expTitle)) {
        $SYS.addReqPassed('CHECKOUT-PAGE', 'Page title checked passed');
    }
    else {
        $SYS.addReqFailed('CHECKOUT-PAGE', "Page title mismatch, expecting [" + expTitle + "], got [" + title + "]", "CHECKOUT-PAGE-NOTLOADED");
    }

	$SYS.page('ShoppingCart').element('OrderBtn').waitUntil('isDisplayed', 500);
    $SYS.addTestOutput("<div>Performed One Test Case (Path)</div>");
    $SYS.addTestOutput("<div>Path: <a href='" + $SYS.genPath('TestPathPath_' + $RAND.randNum(1000,9999)) + "'>SequenceGraph</a></div>");
    $SYS.addTestOutput("<div><img style='color: orange' src='" + $SYS.genMSC('CheckOut Scenario', 'TestPathMSC_' + $RAND.randNum(1000,9999)) + "'/></div>");
}
@TRIGGER('U1408')
def 'Checkout: ShowShoppingCart'() {
    $SYS.page('Checkout').element('ShoppingCart').perform('click');
}
@TRIGGER('U1406')
def 'Checkout: ContinueShopping'() {
    $SYS.page('Checkout').element('ContinueShopping').perform('click');
}
@TRIGGER('U1407')
def 'Checkout: Pay'() {
    $SYS.page('Checkout').element('PaymentType').perform('click', $SYS.getData('PaymentType'),'true');
    $SYS.page('Checkout').element('CreditCardNum').perform('type', $SYS.getData('CreditCardNum'));
    $SYS.page('Checkout').element('ShippingMethod').perform('select', $SYS.getData('ShippingMethod'));
    $SYS.page('Checkout').element('ReceiveEmail').perform('set', $SYS.getData('DeliveryEmail'));
    $SYS.page('Checkout').element('ReceiveNewsLetter').perform('set', $SYS.getData('Newsletter'));
    $SYS.page('Checkout').element('PlaceOrder').perform('click');
    $VAR.ShoppingCartItemCount = 0;

    $SYS.addTestOutput("<div>Payment Type: " + $SYS.getData('PaymentType') + "</div>");
    $SYS.addTestOutput("<div>Credit Card #: " + $SYS.getData('CreditCardNum') + "</div>");
    $SYS.addTestOutput("<div>Shipping Method: " + $SYS.getData('ShippingMethod') + "</div>");
}
@TRIGGER('U1405')
def 'ThankYou' () {
    pageText = $SYS.page('ThankYou').perform("getBodyText");
    if (pageText.indexOf('Thank you for your business') >= 0) {
        $SYS.addReqPassed('CHECKOUT-THANKYOU', 'Thank You page displayed after checkout');
    }
    else {
        $SYS.addReqFailed('CHECKOUT-THANKYOU', 'Thank You page not displayed after checkout', 'CHECKOUT-THANKYOU-BUG');
    }
}
@TRIGGER('U1419')
def 'ThankYou: ContinueShopping'() {
    $SYS.page('ThankYou').element('ContinueShopping').perform('click');
}

