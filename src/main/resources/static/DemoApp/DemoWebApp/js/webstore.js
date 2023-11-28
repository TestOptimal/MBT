
// cookies functions: 0 days  - good until browser closes, -1 days - trashed right after creation
var maxConsecutiveLoginFailure = 3;

function createCookie(c_name,value,exdays)
{
var exdate=new Date();
exdate.setDate(exdate.getDate() + exdays);
var c_value=escape(value) + ((exdays==null) ? "" : "; expires="+exdate.toUTCString());
document.cookie=c_name + "=" + c_value;
}

function readCookie(c_name)
{
var i,x,y,ARRcookies=document.cookie.split(";");
for (i=0;i<ARRcookies.length;i++)
{
  x=ARRcookies[i].substr(0,ARRcookies[i].indexOf("="));
  y=ARRcookies[i].substr(ARRcookies[i].indexOf("=")+1);
  x=x.replace(/^\s+|\s+$/g,"");
  if (x==c_name)
    {
    return unescape(y);
    }
  }
}

function eraseCookie(name) {
	createCookie(name,"",-1);
}

function setCurPage(curPage_p) {
	var lastPage = readCookie("lastPageURL");
	$("#lastPage .pageURL").text(lastPage);
	createCookie("lastPageURL", curPage_p, 1);
}

var ProdList = {PicFrame: {name: "Picture Frame", code: "PicFrame", price: 49, desc: "MCS Classic Format frames are simple to use and easy to hang. To put your picture in the thin edged frame , you just pop out the smooth trimmed glass, insert you picture and pop the glass back in the frame. The frame has molded openings in the back so you can mount the frame either vertical or horizontal. Our most popular picture frame of all time, the format frame is always in fashion and is always the right price."},
				IPad: {name: "iPad 3", code: "IPad", price: 499, desc: "Streamline your daily business tasks with apps that eliminate paper-based processes, give you realtime information, and improve efficiency of common office activities. Explore the tasks below to learn how iPad can change the way you work."},
				UsbMem: {name: "USB ThumbDrive 32GB", code: "UsbMem", price: 39, desc: "With USB ports popping up everywhere you look, you need a simple and reliable way to store and share your photos, videos and music on the fly. The SanDisk Cruzer USB Flash Drive is your answer. Because it works with virtually any computer or electronic device with a USB slot, this flash drive lets you leave your laptop at home but still take your vast amounts of content with you to share with family and friends. And to make sure all your important files are protected, SanDisk offers the free SecureAccess software download, which adds a private, password-protected folder to your drive. Whether you need to transport your latest MP3 downloads, vacation videos, or your college thesis, the San Disk Cruzer USB Flash Drive offers secure, portable storage that you can trust."},
				Toaster: {name: "Toaster 100", code: "Toaster", price: 19, desc: "850 watts of power with one-touch functions for bagels, defrost and cancel with blue LED indicators"}
			}


var ShoppingCartList = new Array();

$(document).ready(function() {
	var shopCart = readCookie("demoShoppingCart");
	if (shopCart) {
		ShoppingCartList = shopCart.split("|");
	}
	
	if (ShoppingCartList.length>0) {
		$("#cartItems").text(ShoppingCartList.length + " item(s)");
	}
	else {
		$("#cartItems").text("is empty");
	}
	init();
});

function saveShoppingCart() {
	createCookie('demoShoppingCart', ShoppingCartList.join("|"), 1);
}

function addItem(itemCode_p) {
	ShoppingCartList.push(itemCode_p);
	saveShoppingCart();
}

function removeItem(itemCode_p) {
	var newList = new Array();
	var removed = false;
	for (var i in ShoppingCartList) {
		var item = ShoppingCartList[i];
		if (!removed && item==itemCode_p) {
			ShoppingCartList[i] = undefined;
			removed = true;
		}
		else {
			newList.push(item);
		}
	}
	
	ShoppingCartList = newList;
	saveShoppingCart();
}

function hasItem(itemCode_p) {
	for (var i in ShoppingCartList) {
		if (ShoppingCartList[i]==itemCode_p) {
			return true;
		}
	}
	return false;
}

function getItemDesc(itemCode_p) {
	return ProdList[itemCode_p];
}


function gotoPage(url_p) {
	window.location = url_p;

}



// from http://blog.pothoven.net/2006/07/get-request-parameters-through.html
function getRequestParam ( parameterName ) {
	var queryString = window.top.location.search.substring(1);
	// Add "=" to the parameter name (i.e. parameterName=value)
	var parameterName = parameterName + "=";
	if ( queryString.length > 0 ) {
	// Find the beginning of the string
		begin = queryString.indexOf ( parameterName );
		// If the parameter name is not found, skip it, otherwise return the value
		if ( begin != -1 ) {
			// Add the length (integer) to the beginning
			begin += parameterName.length;
			// Multiple parameters are separated by the "&" sign
			end = queryString.indexOf ( "&" , begin );
			if ( end == -1 ) {
				end = queryString.length
			}
			// Return the string
			return unescape ( queryString.substring ( begin, end ) );
		}
		// Return "null" if no parameter has been found
		return "null";
	}
}

