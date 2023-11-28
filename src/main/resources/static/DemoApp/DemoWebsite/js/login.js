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

function closeDialog() {

      $.facebox.close(); 
}


function alertDialog(msg_p) {
	if ($("#alertTbl").is(":visible")) {
		$("#alertTbl").append("<tr><td>" + msg_p + "</td></tr>");
	}
	else {
		jQuery.facebox('<table id=alertTbl><tr><td>' + msg_p + '</td></tr></table>');
	}
}


function getConsecBadLogin() {
	var consec = readCookie("consecutiveBadLogin");
	if (consec==undefined || consec=="") consec = 0;
	else consec = parseInt(consec);
	return consec;
}

// return the new consecutive bad login count
function addBadLogin() {
	var consec = readCookie("consecutiveBadLogin");
	if (consec==undefined || consec=="") consec = 1;
	else consec = parseInt(consec) + 1;
	createCookie('consecutiveBadLogin', consec, 1);

	if (getConsecBadLogin() >= maxConsecutiveLoginFailure) {
		$("#alertMsg").html($("#alertMsg").html() + "<br/>Maximum consecutive failed login attempts exceeded.");
		return;
	}

	return consec;
}

function clearBadLogin() {
	createCookie('consecutiveBadLogin', 0, 1);
}


function login() {
	var aUserID = $("#loginSection .UserID").val();
	var aPassword = $("#loginSection .Password").val();
	$("#downloadTrial").attr("disabled", true).addClass("disabled");

	// hardcoded login pwd
	if (aUserID=="goodID1" && aPassword=="goodPwd1") {
		regUser (aUserID, aPassword, "John", "Smith");
	}
	else if (aUserID=="goodID2" && aPassword=="goodPwd2") {
		regUser (aUserID, aPassword, "Sandy", "Lee");
	}
	
	if (aUserID=="" || aPassword=="") {
		$("#alertMsg").html($("#alertMsg").html() + "<br/>User ID/Password required.");
		addBadLogin();
		return;
	}

	var loginPassword = readCookie("TO_PWD_" + aUserID);
	
	if (loginPassword && loginPassword!="") {
		if (loginPassword==aPassword) {
			createCookie("TO_CUR_USERID", aUserID, 2);
			showLogin();
			clearBadLogin();
			$("#loginSection").hide();
			$("#downloadTrial").removeAttr("disabled").removeClass("disabled");
			return;
		}
	}


	addBadLogin();

	$("#alertMsg").html($("#alertMsg").html() + "<br/>Login invalid");
}


function register() {
	var aFirst = $("#registerSection .FirstName").val();

	var aLast = $("#registerSection .LastName").val();
	var aUserID = $("#registerSection .UserID").val();
	var aPassword = $("#registerSection .Password").val();
	var bPassword = $("#registerSection .matchPassword").val();
	
	if (aFirst=="" || aLast == "" || aUserID=="" || aPassword=="" || bPassword=="") {
		alertDialog("All fields are required.");
		return;
	}
	
	if (aPassword!=bPassword) {
		alertDialog("Passwords do not match.");
		return;
	}

	closeDialog();

	regUser (aUserID, aPassword, aFirst, aLast);
//	createCookie("TO_CUR_USERID", aUserID, 2);
//	createCookie("TO_PWD_" + aUserID, aPassword, 2);
//	createCookie("TO_FIRST_" + aUserID, aFirst, 2);
//	createCookie("TO_LAST_" + aUserID, aLast, 2);
	showLogin();

}


function regUser (userID, password, firstName, lastName) {
	createCookie("TO_CUR_USERID", userID, 2);
	createCookie("TO_PWD_" + userID, password, 2);
	createCookie("TO_FIRST_" + userID, firstName, 2);
	createCookie("TO_LAST_" + userID, lastName, 2);
	clearBadLogin();
}

function getUserDisplayName(loginUserID) {
	var displayName = readCookie("TO_FIRST_" + loginUserID) + " " + readCookie("TO_LAST_" + loginUserID);
	return displayName;
}

function showLogin() {
	$("#notLoggedIn").hide();
	$("#loggedIn").show();
	var userID = readCookie("TO_CUR_USERID");
	var displayName = getUserDisplayName(userID);
	$("#loggedIn .displayName").html(displayName);
	$("#loggedIn .UserID").html(userID);
}

function popupLogin() {
	$("#loginSection").css("left", ($(window).width()/2 - 100));
	
	if (getConsecBadLogin() >= maxConsecutiveLoginFailure) {
		$("#alertMsg").html($("#alertMsg").html() + "<br/>Maximum consecutive failed login attempts exceeded.");
		$("#loginSection .field").hide();
		$("#loginSection").show();
		return;
	}

	$("#loginID").val("");
	$("#password").val("");
	$("#alertMsg").html("");
	$("#loginSection .field").show();
	$("#loginSection").show();
}

function closeLogin() {
	$("#alertMsg").html("");
	$("#loginID").val("");
	$("#password").val("");
	$("#loginSection").hide();
}

function popupRegister() {
	var loginHtml = '<table id="registerSection">'
	+'<tr><td>First Name:</td><td><input type=text value="" id="firstName" class="FirstName"/></td></tr>'
	+'<tr><td>Last Name:</td><td><input type=text value="" id="lastName" class="LastName"/></td></tr>'
	+'<tr><td>User ID:</td><td><input type=text value="" id="loginID" class="UserID"/></td></tr>'
	+'<tr><td>Password:</td><td><input type=password value="" id="password" class="Password"/></td></tr>'
	+'<tr><td>Re-Enter Password:</td><td><input type=Password value="" id="matchPassword" class="matchPassword"/></td></tr>'
	+'<tr><td><button id="register" onclick="register()">Register</button></td><td align="right"></td></tr>'
	+'</table>';
	
	alertDialog(loginHtml);
}

function logoff() {
	eraseCookie("TO_CUR_USERID");
	$("#notLoggedIn").show();
	$("#loggedIn").hide();
	$("#downloadTrial").attr("disabled", true).addClass("disabled");

}

function isLoggedIn() {
	var usr = readCookie("TO_CUR_USERID");
	if (usr && usr!="") return true;
	else return false;

}




$(document).ready(function() {

	      $('a[rel*=facebox]').facebox({
	        loading_image : 'js/facebox/loading.gif',
	        close_image   : 'js/facebox/closelabel.gif'
	      })


	if (isLoggedIn()) {
		showLogin();
	}
	else {
		$("#notLoggedIn").show();
	}

});

