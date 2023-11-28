<html>
<body>
<?php
	include "ReleaseConfig.php";
	$platform = $_REQUEST["platform"];
	$email = $_REQUEST["email"];
	$firstName = $_REQUEST["firstName"];
	$lastName = $_REQUEST["lastName"];
	$company = $_REQUEST["comopany"];
	$ipAddress = $_SERVER['REMOTE_ADDR'];
	$hostName = gethostbyaddr($ipAddress);

	$downloadURL = $downloadURL_Win;
	if ($platform=="Mac") {
	    $downloadURL = $downloadURL_Mac;
	}

	$nowDate = date("Y-m-d H:i:s", time());
	$message = $nowDate."\t".$email."\t".$company."\t".$firstName."\t".$lastName."\t".$ipAddress."\t".$hostname."\t".$platform."\r\n";
	$fh = fopen("data/download.list", 'a');
	fwrite($fh, $message);
	fclose($fh);
		
    echo "Thank you for requesting download of TestOptimal. The download link has been sent to your email account at ".$email.".";
    
    $from = "support@testoptimal.com";
	$headers = "From: TestOptimal Support<$from>\n";
	$headers .= "MIME-Version: 1.0\n";
	$headers .= "Content-type: text/html; charset=iso 8859-1";
	$headers .= "Reply-To: ".($from) . "\n";
	$headers .= "X-Priority: 3\n";
	
    $subject = "TestOptimal Trial License";
	$message = "Dear ".$firstName.":<br/>
<p>Thank you for requesting download of TestOptimal.</p>

<p>Please download the software by clicking <a href='".$downloadURL."'>here</a> and follow the installation instruction at: <a href='http://testoptimal.com/v6/wiki'>TestOptimal Wiki</a>.</p>

<p>If you have any question or need further assistance, please contact us at http://testoptimal.com/support.</p>
<br/>
Sincerely,<br/>
<br/>
Technical Support<br/>
TestOptimal.com<br/>
";
	mail($email, $subject, $message, $headers);  

?>
</body>
</html>