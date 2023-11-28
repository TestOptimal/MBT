<?php
	include "ReleaseConfig.php";
	
	$edition = $_REQUEST["edition"];
	$email = $_REQUEST["email"];
	$userMajor = $_REQUEST["major"];
	$userMinor = $_REQUEST["minor"];
	$userBuild = $_REQUEST["build"];
	$host = $_REQUEST["host"];
	$port = $_REQUEST["port"];
	$ip = $_REQUEST["ip"];
	$serialNum = $_REQUEST["serial"];
	$java = $_REQUEST["java"];
	$nowDate = date("Y-m-d H:i:s", time());

	// generate response, separator \r\n
	$newLine = "\r\n";

	// signal the start of release message. Anything before this will be discarded by client.
	$userBuildSeq = ((int)$userMajor)*1000 + ((int)$userMinor)*100 + (int)$userBuild;
	echo "Current release build is ".$relLabel.$newLine;
	echo "You are running release build: ".$userMajor.".".$userMinor.".".$userBuild.$newLine;

    $status = "";
	if ($relBuildSeq > $userBuildSeq) {
		echo 'Updates available.'.$newLine;
		$status = "UPDATES AVAILABLE";
	}
	else {
		echo 'There are no updates available.'.$newLine;
		$status = "UP_TO_DATE";
	}
	
	$fhCheckUpdates = fopen("data/checkUpdates.log", 'a') or die("can't open file");
// time email lic edition major minor build host port ip java status
	$logMsg = $nowDate
		."\t".$email
		."\t".$edition
		."\t".$userMajor
		."\t".$userMinor
		."\t".$userBuild
		."\t".$host
		."\t".$port
		."\t".$ip
		."\t".$java
		."\t".$status
		."\r\n";

	fwrite($fhCheckUpdates, $logMsg);
	fclose($fhCheckUpdates);
?>