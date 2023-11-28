
// disable right-mouse menu
window.addEventListener('contextmenu', function(e) {
		e.preventDefault();
	}, false);

function loadScript(filePath_p) {
	var fileref=document.createElement('script');
    fileref.setAttribute("type","text/javascript");
    fileref.setAttribute("src", filePath_p);
    document.getElementsByTagName("head")[0].appendChild(fileref);
}

function shiftGuideStep (stepNum_p, top_p, left_p, animateDur_p) {
	var curStepElem = $('#step-' + stepNum_p);
	var curPos = curStepElem.position();
	curPos.left += left_p;
	curPos.top += top_p;
	if (animateDur_p) {
		curStepElem.animate(curPos, animateDur_p);
	}
	else curStepElem.offset(curPos);
}

function frameAdjust(tour, deltaTop_p, deltaLeft_p) {
	var deltaTop = 40, deltaLeft = 0;
	if (deltaTop_p) deltaTop += deltaTop_p;
	if (deltaLeft_p) deltaLeft += deltaLeft_p;
	var stepNum = tour.getCurrentStep();
	shiftGuideStep(stepNum, deltaTop, deltaLeft);
}

function startTour (name) {
	var tour = new Tour({
		debug: false,
		backdrop: false,
		storage: false
	});
	var steps = guidedTourList[name]();
	var stepCount = steps.length;
	$.each(steps, function(i, step){
	  step['title'] += '<span class="pull-right">'+(i+1)+'/'+steps.length+'</span>';
	  var percent = parseInt(((i+1) / steps.length) * 100);
	  step['content'] = '<div class="pbar_wrapper"><hr class="pbar" style="width:'+percent+'%;"></div>' + step['content'];
	});	
	
	tour.addSteps(steps);
	tour.init();
	tour.start();
}
