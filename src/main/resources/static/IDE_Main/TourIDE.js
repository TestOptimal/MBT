
var guidedTourList = {};

guidedTourList.ide = function () {
	return [
		{ title: "Studio/IDE Intro", 
		  orphan: true,
		  content: "We will go over common features of TestOptimal Studio / IDE", 
		  onShown: function(tour) {
			curAppState.addMsg({type:"info", text:"Welcome to IDE Guided Tour"});
		  }
		},
		{ title: "Application Header", 
		  element: '#ideHeader',
		  placement: "bottom",
		  content: "Application Header consists of:<li>Application Menu / Toolbar<li>Tab Selection<li>Guided Tour/Help Menu", 
		},
		{ title: "Application Menu", 
		  element: '#appMenu', 
		  content: "<p>Hover menus File, Model and Run.</p><p>Go ahead and move mouse over <img src='img/to.png' style='width: 20px;'/> to show menus.</p><p>Mouse over menu name to show menu items.</p><p>Some menu items are only available when a model is open.</p><p>Menu items can be added to Menu Toolbar by clicking on icon to the right,</p>", 
		  placement: "bottom",
		  onShown: function(tour) {
			var stepNum = tour.getCurrentStep();
			setTimeout ('shiftGuideStep(' + stepNum + ', 250, 0, 1000)', 1000); 
			setTimeout('$("#appMenu").addClass("tourHover")', 500);
			setTimeout('$("#appMenu #appMenuContent .hoverDropdown:nth-child(1)").addClass("tourHover")', 1500);
			setTimeout('$("#appMenu #appMenuContent .hoverDropdown:nth-child(1)").removeClass("tourHover")', 2500);
			setTimeout('$("#appMenu #appMenuContent .hoverDropdown:nth-child(2)").addClass("tourHover")', 2500);
			setTimeout('$("#appMenu #appMenuContent .hoverDropdown:nth-child(2)").removeClass("tourHover")', 3500);
			setTimeout('$("#appMenu #appMenuContent .hoverDropdown:nth-child(3)").addClass("tourHover")', 3500);
		  },
		  onHide: function(tour) {
			$("#appMenu #appMenuContent .hoverDropdown:nth-child(3)").removeClass("tourHover");
			$("#appMenu").removeClass("tourHover");
		  }
		},
		{ title: "Menu Toolbar", 
		  element: '#appMenuSC', 
		  content: "<p>Quick access to application menu item</p><p>Most of the menu items are only be visible when a model is open.</p>", 
		  placement: "bottom"
		},
		{ title: "Tab Selecion", 
		  element: "#tabHeader",
		  placement: "bottom",
		  content: "This section shows model name and tabs available for the model type", 
		},
		{ title: "Model Name", 
		  element: $('#modelName'), 
		  content: "<p>Will show model name when a model is open.</p><p>First 20 chars of model name are shown.</p><p>Click to show complete model name or rename model.</p>", 
		  placement: "bottom", 
		},
		{ title: "Tabs", 
		  element: $('#tabHeader'), 
		  content: "<p>Tab labels will be displayed when a model is open.</p><p>State Model: <li>MODEL<li>SCRIPT<li>REQUIREMENT<li>RESULT<li>ARTIFACT</p><p>Combinatorial Model: <li>DEFINE<li>GENERATE<li>SCRIPT<li>EXECUTE</li></ul>", 
		  placement: "bottom", 
		},
		{ title: "Dashboard", 
		  element: $('#dashboardBtn'), 
		  content: "<p>Quick access to Dashboard.</p>", 
		  placement: "bottom", 
		},
		{ title: "Help", 
		  element: '#helpMenu',
		  content: "<p>Helpful resources including Manage License, check for software updates, server log file, and more.</p>", 
		  placement: "left", 
			onShown: function(tour) {
				var stepNum = tour.getCurrentStep();
				setTimeout('$("#helpMenu").addClass("tourHover")', 500);
				setTimeout('shiftGuideStep(' + stepNum + ', 100, -100, 1000)', 1000);
			},
			onHide: function(tour) {
				$("#helpMenu").removeClass("tourHover");
			}
		},
		{ title: "Guided Tours", 
		  element: '#tourMenu',
		  content: "<p>Hover over for more guided tours like this one.</p><p>Most guided tours are available when there is a model open.</p>", placement: "left", 
			onShown: function(tour) {
				var stepNum = tour.getCurrentStep();
				setTimeout('$("#tourMenu").addClass("tourHover")', 500);
				setTimeout('shiftGuideStep(' + stepNum + ', 25, -75, 1000)', 1000);
			},
			onHide: function(tour) {
				$("#tourMenu").removeClass("tourHover");
			}
		},
		{ title: "System Message Bar", 
		  orphan: true,
		  content: "Displays system messages, alerts and errors at the bottom of the screen.", 
		},
		{ title: "Message Indicator", 
		  element: '#msgExpander', 
		  content: "<p>Color coded indicator to show last message severity: <li style='background: gray'>gray normal</li><li style='background: ORANGE'>orange: warning</li><li style='background: RED'>red: error</li></p><p>Clicking it to expand System Message Bar</p>", 
		  placement: "top",
		  onShown: function(tour) {
			var stepNum = tour.getCurrentStep();
			shiftGuideStep(stepNum, -20, 0, 1000);
			setTimeout('$("#msgExpander").trigger("click")', 2000);
		  }
		},
		{ title: "Message Bar", 
		  element: '#msgBar', 
		  content: "<p>Latest messages are displayed on the top.</p><p>Message Bar automatically expands when new messages are posted and hides after 10 seconds.</p>", 
		  placement: "top",
		  onShown: function(tour) {
			var stepNum = tour.getCurrentStep();
			shiftGuideStep(stepNum, -100, 0, 1000);
			setTimeout('curAppState.addMsg({type:"info", text:"this is info message"})', 1000);
			setTimeout('curAppState.addMsg({type:"warn", text:"this is warning message"})', 1000);
			setTimeout('curAppState.addMsg({type:"error", text:"this is error message"})', 1000);
		  }
		},
		{ title: "Message Bar Action", 
		  element: '#msgBarActions', 
		  content: "<p>Messages can be cleared and closed</p>", 
		  placement: "left",
		  onShown: function(tour) {
			setTimeout('$("#msgBarActions .glyphicon-erase").trigger("click")', 2000);
			setTimeout('$("#msgBarActions .glyphicon-chevron-down").trigger("click")', 6000);
		  }
		},
		{ title: "Open Model", 
		  orphan: true, 
		  content: "<p>Our next step is to find a model to open.</p>", 
	
		},
		{ title: "Open Model", 
		  element: '.glyphicon-folder-open', 
		  content: "<p>Click <span class='glyphicon glyphicon-folder-open' style='color: yellow; margin-right: 3px;'></span> to open Model List dialog window.</p>", 
		  placement: "bottom",
		  onShown: function(tour) {
			setTimeout('$(".glyphicon-folder-open").trigger("click")', 1000);
		  }
		},
		{ title: "Model List", 
		  element: "#dialog", 
		  content: "<p>Demo models are organized in two folders:</p><ul><li>Demo_CombinatorialTesting<li>Demo_StateModel</ul></p><p><span class='glyphicon glyphicon-thumbs-up'/> Go ahead and click on these two folders to review the DEMO models in each folder.</p><p>To open a model, just click on the model name. But do it after this tutorial is completed.</p><p>", 
		  placement: "right",
		  onShown: function(tour) {
				var stepNum = tour.getCurrentStep();
				shiftGuideStep(stepNum, 0, -375);
				setTimeout('shiftGuideStep(' + stepNum + ', 0, 200, 1000)', 1000); 
		  }
		},
		{ title: "Model Actions", 
		  element: "#dialog", 
		  content: '<li><img src="/img/folder.png"/> new folder<li><img src="/img/model.png"/> new state model<li><span class="icon-ctrl glyphicon glyphicon-import"/> import a model<li><span class="icon-ctrl glyphicon glyphicon-repeat"/> refresh folder', 
		  placement: "right",
		  onShown: function(tour) {
				var stepNum = tour.getCurrentStep();
				shiftGuideStep(stepNum, -230, -540);
		  }
		},
		{ title: "What's Next", 
		  orphan: true,
		  content: "<p>Recommend to explore DEMO models and other guided tours like this", 
		  onShown: function(tour) {
			setTimeout('$(".dialogClose").trigger("click")', 1000);
		  }
		},
		
	  ];
}


guidedTourList.model = function() {
	return [
		{ title: "MODEL Tab Infro", 
		  orphan: true, 
		  content: "<p>Graphical modeling of state models</p><p>Editor has 3 modes:</p><ul><li><em>edit</em> - modify and layout states/transitions</li><li><em>add</em> - add states/transitions</li><li><em>mark</em> - select states/transitions for set operation or partial model execution</li></ul><p>States and transitions have properties</p><p>And they can also be styled</p>",
		  onShown: function(tour) {
			setTimeout("curAppState.selectTab('Model')", 50);
		  }
		},
		{ title: "Toolbar", 
		  element: '#toolPallet', 
		  content: "Toolbar is divided into 4 groups separated by "|" reflecting editor modes.", 
		  placement: "bottom",
		  onShown: frameAdjust
		},
		{ title: "Edit Mode", 
		  element: $("#tab_Model").contents().find('#resetBtn'), 
		  content: "Main functions of <em>edit</em> mode:</p><ul><li>review model</li><li>re-arrange state / transitions</li><li>delete unwanted states / transitions</li>model execution & animation</li><li>edit state / transition properties</li></ul>", 
		  placement: "bottom", 
		  onShown: frameAdjust
		},
		{ title: "Add Mode", 
		  element: $("#tab_Model").contents().find('#addBtnGroup'),
		  content: "<p>Main functions of <em>add</em> mode:</p><ul><li>add states (including initial and final states)</li><li>add transitions</li><li>add other nodes</li></ul><p>To add a state, mouse click on canvas.</p><p>To add a transition, click the sides of source and target states. We will arrange transition routing (segments) later in this guided tour.</p><p>ESC key selects <em>edit</em> mode.</p><p><span class='glyphicon glyphicon-thumbs-up'/> Go ahead and  create some states and transitions.</p><p>Notice <em>add</em> mode has blueish background and a distinctive mouse cursor.</p><p>Default state/transition name can be changed in Properties view at next step.</p>",
		  placement: "bottom", 
		  onShown: frameAdjust
		},
		{ title: "Context Menus and Properties", 
		  orphan: true,
		  content: "<p>Available in <em>edit</em> mode.</p><p>To open context menu, mouse over state or transition label and click on <img src='/img/to.png' style='width: 20px;'/></p><p>Properties view can be opened from context menu <span style='color: orange;' class='glyphicon glyphicon glyphicon-th-list '/> or double click on state/transition label.</p><p><span class='glyphicon glyphicon-thumbs-up'/> Go ahead and open Properties view to rename states and transitions.</p>", 
		},
		{ title: "View", 
		  element: $("#tab_Model").contents().find('#viewExpender'),
		  content: "<p>View can be closed or opened by clicking here.</p><p><span class='glyphicon glyphicon-thumbs-up'/> Go ahead and close the view if it is still open..</p><p>More on view later.</p>", 
		  placement: 'left',
		  onShown: function(tour) { frameAdjust(tour, -10, -10);}
		},
		{ title: "Move and Resize", 
		  orphan: true,
		  content: "<p>Available in <em>edit</em> mode.</p><p>State and Transition Label:</p><ul><li>move - drag on &#128070;</li><li>resize - drag on resizer</li></ul><p>Transition Routing:<p><ul><li>move - drag on transition segment</li><li>move anchor - move start/end segment pass state border boundary to anchor transition on adjacent state border</li></ul><p><span class='glyphicon glyphicon-thumbs-up'/> Go ahead and try to move states and transitioin around. Make sure you are in <em>edit</em> mode.</p>", 
		},
		{ title: "Mark Mode", 
		  element: $("#tab_Model").contents().find('#markBtnGroup'), 
		  content: "Main functions of <em>mark</em> mode  <span class='glyphicon glyphicon-star-empty' style='color: yellow; margin-right: 3px;'/>:</p><ul><li>set operation on states and transitions: move, delete and align</li><li>model execution to cover targeted area of the model</li></ul><p><em>mark</em> mode has pinkish background</p><p>Double clicking on canvas to switch to <em>edit</em> mode.</p>", 
		  placement: "bottom", 
		  onShown: frameAdjust
		},
		{ title: "Search and Views",
		  element: $("#tab_Model").contents().find('#viewMenu'), 
		  content: "<p>Search and views provides a quick way to locate and mark states/transitions in the model.</p><ul><li>Search - find states transitions by name and decription</li><li>Canvas Settings - set canvas size</li><li>Tree View - tree view of states/transitions</li><li>Requirements - highlight requirement coverage in model</li><li>Defects - highlight defects in the model found during model execution</li></ul><p>State and transition properties are also views.</li>", 
		  placement: "bottom", 
		  onShown: function(tour){
			frameAdjust(tour);
			var stepNum = tour.getCurrentStep();
			setTimeout('$("#tab_Model").contents().find("#viewMenu").addClass("tourHover")', 500);
			setTimeout('shiftGuideStep(' + stepNum + ', 105, 0, 1000)', 800);
		  },
		  onHide: function(tour) {
			$("#tab_Model").contents().find("#viewMenu").removeClass("tourHover");
		  }
		},	
		{ title: "Execute Model", 
		  element: '#appMenuSC', 
		  content: "<p>When done with model, save changes and execute model to generate test cases with menu toolbar</p>", 
		  placement: "bottom"
		},
		{ title: "What's Next?", 
		  orphan: true,
		  content: "<p>Next step is to write automation scripts.</p>", 
		},
	  ];
}

guidedTourList.script = function() {
	return [
		{ title: "SCRIPT Tab Infro", 
		  element: ".tabLabel:nth-child(2)", 
		  content: "<p>SCRIPT tab contains automation scripts (<a href='https://en.wikipedia.org/wiki/Apache_Groovy' target='_blank'>Groovy</a>) called by model execution.</p><p>We will cover main features of Script Editor.</p>",
		  placement: "bottom",
		  onShown: function(tour) {
			setTimeout("curAppState.selectTab('Script')", 50);
		  }
		},
		{ title: "Script Types", 
		  element: $("#tab_Script").contents().find("#tabLabelList"), 
		  content: "<p>These scripts are automatically created:<ul><li>TRIGGERS - script for states and transitions</li><li>PAGES - define page objects</li><li>STEPS - step definition for high-level natural language scripting</li><li>MCASES - custom test cases</li></ul></p><p>More on these script types later.</p>",
		  placement: "bottom",
		  onShown: frameAdjust
		},
		{ title: "Editor Toolbar", 
		  element: $("#tab_Script").contents().find("#actionBtn"), 
		  content: "<p>Toolbar provides quick access to some of the editor functions divided in three groups.</p><ul><li>Find/Replace - search for selected text or by regExp</li><li>Comment/Undo/Redo - toggle code comment and undo/redo las changes</li><li>Req - manage requirements</li></ul></p><p>Recommended reading on Script Editor <span class='glyphicon glyphicon-info-sign'/>.</p>",
		  placement: "bottom",
		  onShown: frameAdjust
		},
		{ title: "TRIGGER Script", 
		  element: $("#tab_Script").contents().find("#tabLabelList .tabLabel:nth-child(1)"), 
		  content: "<p>Write script for states, transitions and the following system TRIGGERs:<ul><li>MBT_START</li><li>MBT_END</li><li>ALL_STATES</li><li>ALL_TRANS</li><li>MBT_FAIL</li><li>MBT_ERROR</li></ul></p><p>To add a TRIGGER script, press Ctrl-I</p><p><span class='glyphicon glyphicon-thumbs-up'/> Go ahead and create TRIGGER script for a state, a transition and MBT_START.</p><p>Notice the TRIGGER script is just a groovy function with annotation mapping to the state/transition/system TRIGGER type.</p>",
		  onShown: function(tour) {
			frameAdjust(tour, -10, 10);
			var stepNum = tour.getCurrentStep();
			setTimeout('shiftGuideStep(' + stepNum + ', 100, 400, 1000)', 800);
		  }
		},
		{ title: "Code Assist", 
		  orphan: true,
		  content: "<p>Ctrl-Space opens code assist (hint) list.</p><p>Code assist list is context sensitive:</p>on a blank line or after dollar-sign ($)<ul><li>$EXEC - access to model execution</li><li>$UTIL - utility functions</li><li>$VAR - define/access user variables</li><li>Req - manage requirements (more on this later)</li></ul></p><p>after character dot (.): lists java functions.</p><span class='glyphicon glyphicon-thumbs-up'/> Go ahead and try to enter following using code assist: <pre>$VAR.os = $UTIL.getOsName();if ($VAR.os == 'windows') {\n  $SYS.log('Successfully detected Windows OS');\n}\nelse {\n  $SYS.log('Detected non-Windows OS: ' + $UTIL.camelCase($VAR.os));\n}</pre></li></p>",
		},
		{ title: "Trigger Listing", 
		  element: $("#tab_Script").contents().find("#viewExpender"), 
		  content: "<p>Trigger Listing shows trigger scripts in tree view.</p><p>Click on <span style='font-size: x-large; font-weight: bold'>⟨</span>  to open the view.</p><span class='glyphicon glyphicon-thumbs-up'/> Go ahead and try it.</p><p>Click on script name to scroll editor to the script.</p>",
		  placement: "left",
		  onShown: function(tour) {
			frameAdjust(tour, -10, 0);
			var stepNum = tour.getCurrentStep();
			setTimeout('shiftGuideStep(' + stepNum + ', 0, -150, 1000)', 800);
		  }
		},
		{ title: "Requirements", 
		  element: $("#tab_Script").contents().find("#reqBtn"), 
		  content: "<p>Requirements popup allows you to define/view requirements to be referenced in MScript.</p><p><span class='glyphicon glyphicon-thumbs-up'/> Go ahead and click on button 'Req' to open Requirement popup.</p>",
		  placement: "left",
		  onShown: function(tour) {
			frameAdjust(tour, -10, -10);
		  }
		},
		{ title: "Requirement Code Assist", 
		  orphan: true, 
		  content: "<p>Requirements are automatically added to the code assist list Ctri-R / Cmd-R.</p><p><span class='glyphicon glyphicon-thumbs-up'/> Go ahead and bring up code assist.</p>",
		},
		{ title: "What's Next?", 
		  orphan: true,
		  content: "<p>Next step is to test your scripts by running the model.</p><p>Check out Guide on Execute Model if you need help on that.</p>", 
		},
	  ];
}




guidedTourList.execModel = function() {
	return [
		{ title: "Execute Model", 
		  element: ".tabLabel:nth-child(1)", 
		  content: "<p>We will run the model from MODEL tab, but you can do it from any TABs.</p></p>Options to run a model:<ul><li>Generate - generate test cases only (script will not be executed)</li><li>Execute - generate test cases and execute scripts</li><li>Debug - execute model in debug mode</li><li>Play - execute model with animation</li></ul></p><p>We will cover not cover Debug in this guided tour.</p>",
		  placement: "bottom",
		  onShown: function(tour) {
			setTimeout("curAppState.selectTab('Model')", 50);
		  }
		},
		{ title: "Run Buttons", 
		  element: "#appMenuSC", 
		  content: "<p>We will conduct our model execution using toolbar:<ul><li>Generate <span class='glyphicon glyphicon-play-circle'/><li>Execute <span class='glyphicon glyphicon-play'/></li><li>Play <span class='glyphicon glyphicon-forward'/></li></ul></p><p>If these buttons are missing, you can restore them with 'Reset Shortcuts' from File menu.</p><p>Typically you would start with Generate first and review test cases generated.</p>",
		  placement: "right",
		},
		{ title: "MBT Modes - Sequencers", 
		  element: "#mbtModeSelect",
		  content: "<p>Sequencers drives the test generation from the model.</p>The sequencer must be selected for all modes of model executions.</p><p>The default is Optimal, which generates test cases with minimum # of steps to cover the entire model.</p><p>For more information about sequencers and which sequencer to use, please refer to <a href='https://testoptimal.com/v6/wiki/doku.php?id=sequencers' target=_blank'>Sequencers</a>.</p>",
		  placement: "bottom",
		  onShown: function(tour) {
		    frameAdjust (tour, -20,0);
		  }
		},
		{ title: "Run Model", 
		  element: "#appMenuSC", 
		  content: "<p>To run model in Generate mode, click on <span class='glyphicon glyphicon-play-circle'/>.</p><p><span class='glyphicon glyphicon-thumbs-up'/> Go ahead and run model.</p><p>You should see progress message displayed in the System Message area.</p><p>When model execution is completed, it will automatically take you to RESULT tab.</p>",
		  placement: "bottom",
		},
		{ title: "Review Model Coverage", 
		  element: $("#tab_Results").contents().find("#modelCov"), 
		  content: "<p>Review model coverage (state and transition coverage)</p>",
		  placement: "top",
		  onShow: function(tour) {
			frameAdjust(tour);
			curAppState.selectTab('Results');
		  }
		},
		{ title: "Review Test Cases", 
		  element: $("#tab_Results").contents().find("#testCases"), 
		  content: "<p>Review test cases generated.</p>",
		  placement: "top",
		  onShow: function(tour) {
			frameAdjust(tour);
			curAppState.selectTab('Results');
		  }
		},
		{ title: "Review Results in Graphs", 
		  element: $("#tab_Results").contents().find("#headerLinks"), 
		  content: "<p>Review coverage and test cases in graphs:<ul><li>Traversal - test paths over the model</li><li>Coverage - identify uncovered transitions</li><li>Test Case - test cases in easy to read Message Sequence Chart (MSC)</li></ul></p>",
		  placement: "left",
		  onShow: function(tour) {
			curAppState.selectTab('Results');
		  }
		},
		{ title: "Execute and Play", 
		  orphan: true, 
		  content: "<p>Steps to execute model and review the results works the same way as generate except that scripts will be executed and thus additional requirements coverage and traceability matrix will be collected in RESULT tab.</p><p><span class='glyphicon glyphicon-thumbs-up'/> Go ahead and give it a run.</p>",
		},
	  ];
}


guidedTourList.trackReq = function() {
	return [
		{ title: "Track Requirement", 
		  orphan: true, 
		  content: "<p>This tour shows how to track requirement and identify requirement coverage in the model, assuming your model already has a few requirements added to REQUIREMENT tab.</p><p>Specifically:<ul><li>Perform requirement check in script</li><li>Locate state/transition covering a specific requirement</li></ul></p>",
		},
		{ title: "Requirement Check in Script", 
		  orphan: true, 
		  content: "<p>Requirements are validated in states and transitions TRIGGER script.</p><p>Your script will determine if the requirement has passed or failed and will add the validation result to the model execution results. $EXEC.getCurTraverseObj() provides a set of functions to record test pass/failure.</p></ul><li>addReqPassed(...)</li><li>addReqFailed(...)</li></ul></p><p>The script would look like:<pre>if ( check requirement passed ) {\n  addReqPassed('tag', 'msg');\n}\nelse {\n  addReqFailed('tag','msg');\n}</pre></p><p><span class='glyphicon glyphicon-thumbs-up'/> Go ahead and try it on a state or transition TRIGGER.</p>",
		  onShow: function (tour) {
			curAppState.selectTab("Script");
		  }
		},
		{ title: "Code Assist on Requirement", 
		  orphan: true, 
		  content: "<p>To enter the requirement tag, use code assist Ctrl-R, which lists the requirements defined in REQUIREMENT tab.</p><p><span class='glyphicon glyphicon-thumbs-up'/> Go ahead and try it.</p><p>Note: Ctrl-R will not work if there is no requirements.</p>",
		  onShow: function (tour) {
			curAppState.selectTab("Script");
		  }
		},
		{ title: "State/Transition Pass/Fail", 
		  orphan: true,
		  content: "<p>Calling addReqFailed(...) will cause the corresponding state/transition to fail while absence of calls to addReqFailed(...) is treated as passed.</p><p>A state or transition can be traversed (and hence state/transition TRIGGER) several times, each time will determine the pass/fail for that specific state/transition traversal and ultimately determining the pass/fail for the test case.</p><p><span class='glyphicon glyphicon-thumbs-up'/> Go ahead and execute the model and to test your requirement script.</p>",
		},
		{ title: "Requirement Traceability Matrix", 
		  element: $("#tab_Results").contents().find("#reqCov"), 
		  content: "<p>Coverage of the requirement by each test cases are tracked and tallied in the matrix below.</p><p>A check mark indicating the requirement is covered by the test case (row) while an x indicates the requirement was checked and failed.</p><p>The test case details will show exactly which test step the requirement check failed.</p><p><span class='glyphicon glyphicon-thumbs-up'/> Go ahead and click on a test case that has a reqirement check or x on it.</li></p>",
		  placement: "top",
		  onShow: function(tour) {
			frameAdjust(tour);
			curAppState.selectTab('Results');
		  }
		},
		{ title: "Requirement Check in Test Csae", 
		  element: $("#tab_Results").contents().find("#testCases"), 
		  content: "<p>Scroll through the test step to find the requirement check message and the status of the check.</p>",
		  placement: "top",
		  onShow: function(tour) {
			frameAdjust(tour);
			curAppState.selectTab('Results');
		  }
		},
		{ title: "Open Requirements View", 
		  element: $("#tab_Model").contents().find("#viewMenu"), 
		  content: "<p>At times you may want find where in the model a specific requirement is being covered (checked).</p><p>You can certainly do that by searching the script.</p><p>Alternatively you can use Requirement View to just that.</p><p><span class='glyphicon glyphicon-thumbs-up'/> Go ahead and open Requirement View.</p>",
		  placement: "bottom",
	  	  onShow: function (tour) {
			curAppState.selectTab('Model');
		  },
		  onShown: function(tour){
			frameAdjust(tour);
			var stepNum = tour.getCurrentStep();
			setTimeout('$("#tab_Model").contents().find("#viewMenu").addClass("tourHover")', 500);
			setTimeout('shiftGuideStep(' + stepNum + ', 105, 0, 1000)', 800);
		  },
		  onHide: function(tour) {
			$("#tab_Model").contents().find("#viewMenu").removeClass("tourHover");
		  }
		},
		{ title: "Requirements View", 
		  orphan: true,
		  content: "<p>Reuirements View lists all requirements defined in REQUIREMENT tab.</p><p>Clicking on a requirement highlights the states and transitions that have scripts checking the requirement.</li><p><span class='glyphicon glyphicon-thumbs-up'/> Go ahead and try it.</p>",
		},
	  ];
}
