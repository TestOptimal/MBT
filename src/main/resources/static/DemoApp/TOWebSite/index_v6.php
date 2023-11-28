<html>
<head>
<link rel="icon" href="to.ico" type="image/x-icon" />
<META name="description" content="Model-Based Testing and Proess Automation">
<META name="keywords" content="testing, automation, robotic process automation, RPA, test case design, test data design, test case generation, model-based testing, regression testing, functional testing, requirements traceability, test coverage, data-driven testing, pairwise testing, behavior-driven testing, BDT, MBT">
<script src='js/jquery-3.2.1.min.js'></script>

<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<style>
	html,body {
		margin: 0px;
		heigh: 100%;
		width: 100%;
		overflow: auto;
		font-family: 'Open Sans', sans-serif;
		background: #EAEAEA;
	}
	
	#pageHeader {
		position: fixed;
		top: 0px;
		left: 0px;
		width: 100%;
		height: 40px;
		background: #000000;
		z-index: 1;
	}
	
	#topSec {
		padding-top: 75px;
		width: 100%; 
		height: 225px; 
		color: #FFFFFF;
		text-align: center;
	}
	
	#logo {
		float: left;
		height: 36px;
	}
	
	#tabLabels {
		margin: 15px;
		float: right;
	}
	
	.tabLabel {
		padding: 8px;
		color: #EEEEEE;
		font-style: italic;
	}
	
	.link {
		text-decoration: none;
	}
	
	.link:hover {
		text-decoration: underline;
	}
	
	.tabLabel.selected {
		border: 2px solid #EEEEEE;
	}
	
	.subHeader {
		font-size: large;
		line-height: 1.8em;
		color: #DDDDDD;
	}
	
	#downloadLink {
		margin-top: 15px;
		font-style: italic;
	}
	
	#downloadLink a {
		color: lightcyan;
		font-style: italic;
	}
	
	#benefits {
		color: #24678d;
		margin: 25px;
		text-align: center;
	}
	
	#benefits .content {
		margin-left: 50px;
		margin-right: 50px;
		display: flex;
		justify-content: center;
	}
	
	.benefitItem {
		text-align: center;
		margin: 20px;
		font-size: large;
	}
	
	.benefitItem img {
		height: 100px;
		width: 120px;
		border-radius: 80px;
	}
	
	.benefitItem div {
		margin: 8px;
	}
	
	.benefitItem .subText {
		font-size: medium;
	}
	
	#downloadLink2 {
		text-align: center;
		color: darkblue;
		font-style: italic;
		padding-top: 5px;
		padding-bottom: 10px;
	}
	
	#downloadLink2 a {
		color: darkslateblue;
	}

	#downloadLink2 a:hover {
		color: darkblue;
	}
	
	#footer {
		text-align: center;
		font-size: x-small;
		margin-bottom: 5px;
		border-top: 1px solid #DDDDDD;
		padding-top: 8px;
		font-style: italic;
	}

	#iterativeList li {
	 	line-height: 30px;
	}
</style>
<script>
$(document).ready (function() {
	$( "#downloadRequest" ).submit(function( event ) {
	    $.ajax({
	        data: $(this).serialize(), // get the form data
	        type: $(this).attr('method'), // GET or POST
	        url: $(this).attr('action'), // the file to call
	        success: function(response) { // on success..
	        	var email = $("#email").val();
	    	    alert( "Thank you for requesting download of TestOptimal. The download link has been sent to your email account at " + email + ".");
	        }
	    });
	    return false; // cancel original event to prevent form submitting
	});
})
</script>

</head>
<body>
	<div id="pageHeader">
		<img id="logo" src="img/testoptimal.png"/> <span style="position:relative; top: 8px; font-style: italic; font-size: x-small; color: #FFFFFF;">v6</span>
		<div id="tabLabels">
			<a class="tabLabel link" href="#Download">Download</a>
			<a class="tabLabel link" href="https://testoptimal.com/v6/wiki" target=_blank>Documentation</a>
			<a class="tabLabel link" href="https://testoptimal.com/smf" target=_blank>Community</a>
			<a class="tabLabel link" href="https://testoptimal.com/support" target=_blank>Contact Us</a>
		</div>
	</div>
	<div style="background-image: linear-gradient(#000, #111, #555)">
		<div id="topSec" style="background-image: url(img/tobkg.png); background-repeat: no-repeat;">
		<div><h1>Model-Based Testing & Process Automation</h1></div>
		<div class="subHeader">Intelligent Test Design</div>
		<div class="subHeader">Robust Process & Workflow Automation</div>
		<div class="subHeader">Adaptive to Changes</div>

		<div class="subHeader" id="downloadLink">
			<a class="link" href="#Download">Download Free Community Edition (Windows, Mac, Linux)</a>
		</div>
		</div>
	</div>
	
	<div id="benefits">
		<h2>Super Charge Your Testing & Automation Process</h2>
		<div class="content">
			<div class="benefitItem">
				<div><img src="img/smartCar.png"/></div>
				<div>Intelligent</div>
				<div class="subText">Algorithm-based path generation to test and automate processes</div>
			</div>
			<div class="benefitItem">
				<div><img src="img/easy123.png" style="background: lightsteelblue;"/></div>
				<div>Easy</div>
				<div class="subText">Integrated Development Environment (IDE) with efficient scripting and debugging</div>
			</div>
			<div class="benefitItem">
				<div><img src="img/teamIDE.png"/></div>
				<div>Visual</div>
				<div class="subText">Powerful graphs to visualize test cases and process automation paths</div>
			</div>
			<div class="benefitItem">
				<div><img src="img/reuse.png" style="background: ORANGE;"/></div>
				<div>Reuse</div>
				<div class="subText">Repurpose and aggregate models for workflow automation and load/stress testing</div>
			</div>
		</div>
	</div>
	
	<div style="background: #FFFFFF; text-align: center; color: #575354;">
		<h2 style="padding-top: 25px; color: #666666;">Agile Iterative Testing & Process Automation</h2>
		<div style="display: flex; width: 100%; justify-content: center; align">
			<div style="padding-top: 20px; font-size: large;">
				<ul id="iterativeList" style="text-align: left;">
					<li>Incrementally build up models from user stories in each iteration</li>
					<li>Algorithm-based path generation directly from models:
						<ul>
							<li>achieve different test coverage</li>
							<li>perform targeted scenario testing</li>
							<li>construct process path to automate</li>
						</ul>
					</li>
					<li>Track requirement coverage and passed/failed test cases</li>
					<li>Model changes are automatically incorporated into test cases</li>
					<li>Automatic data collection for dashboard and KPI reporting</li>
				</ul>
			</div>
			<img src="img/MBT_IterativeProcess.png" style="align-self: center; width: 400px; height: 300px; margin-left: 100px;"/>
		</div>
	</div>
	<div style="background: #FAFAFA; text-align: center; color: #575354; padding-bottom: 15px;">
		<a name="Download">
			<h2 style="padding-top: 25px; color: DARKSLATEBLUE;">Download TestOptimal</h2>
		</a>
		<div style="display: flex; width: 100%; justify-content: center; align">
			<form id="downloadRequest" name="downloadRequest" action="v6/DownloadRequest.php" method="post">
			<table>
				<tr class="entryrow"><td>Email</td>
					<td><input type=text class="field" name="email" required id="email" size="50"/></td>
				</tr>
				<tr class="entryrow">
					<td algin="right" nowrap>First Name</td>
					<td><input type=text class="field" name="firstName" required id="firstName" size="50"/></td>
				</tr>
				<tr class="entryrow">
					<td algin="right" nowrap>Last Name</td>
					<td><input type=text class="field" name="lastName" required id="lastName" size="50"/></td>
				</tr>
				<tr class="entryrow">
					<td>Company</td>
					<td><input type=text class="field" name="company" required id="company" size="50"></td>
				</tr>
				<tr>
					<td>
					</td>
					<td style="line-height: 35px;">
						<input type="radio" name="platform" value="Win" checked/> Windows
						<input style="margin-left: 35px;" type="radio" name="platform" value="Mac"/> Mac / Linux
						<input type="submit" style="float: right; padding: 5px; border-radius: 3px; position: relative; top: 3px;" value="SUBMIT"/>
					</td>
			</table>
			</form>
		</div>
	</div>
	<div id="footer">
		Copyright © 2020 TestOptimal, LLC. All rights reserved.
	</div>
	
</body>
</html>