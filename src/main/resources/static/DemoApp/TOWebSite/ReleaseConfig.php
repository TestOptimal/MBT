<?php
	$relMajor = 6;
	$relMinor = 0;
	$relBuild = 2;
	$relDate = "2020-05-15";

    date_default_timezone_set('America/Chicago');

	$relLabel = $relMajor ."." .$relMinor. "." .$relBuild;
	$relBuildSeq = $relMajor * 1000 + $relMinor * 100 + $relBuild;
	$downloadFolder = "https://testoptimal.com/downloads/Rel-" .$relMajor. "." .$relMinor;

	$downloadURL_Win = $downloadFolder."/TestOptimal_".$relLabel."_win.zip";
	$downloadURL_Mac = $downloadFolder."/TestOptimal_".$relLabel."_mac.zip";
	$downloadURL_Linux = $downloadFolder."/TestOptimal_".$relLabel."_linux.zip";
?>
