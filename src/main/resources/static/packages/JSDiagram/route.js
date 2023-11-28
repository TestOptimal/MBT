var defaultLabelPosList = {
			"up": {left: 5, top: -25, width: 75, height: 25},
			"down": {left: 5, top: 25, width: 75, height: 25},
			"left": {left: -80, top: -25, width: 75, height: 25},
			"right": {left: 5, top: -25, width: 75, height: 25}
		};

function routeTrans (fromState_p, fromSideID_p, fromDelta_p, toState_p, toSideID_p, toDelta_p) {
	var posList = [];
	var minLength = 25;
	var dA, dB;

	var dirMap = {
		"top": "up",
		"up": "up",
		"bottom": "down",
		"down": "down",
		"left": "left",
		"right": "right"
	}
	var dirIntMap = {
		"up": -1,
		"down": 1,
		"left": -1,
		"right": 1
	}
	
	function route () {
		dA = dirMap[fromSideID_p];
		var da = dirIntMap[dA];
		dB = dirMap[toSideID_p];
		var db = dirIntMap[dB];
		var posA = getStartPos(fromState_p, fromSideID_p, fromDelta_p);
		var posB = getStartPos(toState_p, toSideID_p, toDelta_p);
		posList.push(posA);
		var posC, posD;
		
		// if perpendicular
		if ("updown".indexOf(dA)>=0 && "leftright".indexOf(dB) >= 0 ||
			"updown".indexOf(dB)>=0 && "leftright".indexOf(dA) >= 0) {
			if (("updown".indexOf(dA)>=0 && da * (posB.top - posA.top) > 0 ||
				 "leftright".indexOf(dA)>=0 && da * (posB.left - posA.left) > 0 ) &&
				("updown".indexOf(dB)>=0 && db * (posA.top - posB.top) > 0 ||
				 "leftright".indexOf(dB)>=0 && db * (posA.left - posB.left) > 0)) {
				posC = intersect (posA, dA, posB, dB);
				posList.push(posC);
				posList.push(posB);
			}
			else { // moving away
				var posA2 = movePos (posA, dA, minLength);
				var posB2 = movePos (posB, dB, minLength);
				posC = intersect (posA2, dB, posB2, dA);
				posList.push(posA2);
				posList.push(posC);
				posList.push(posB2);
				posList.push(posB);
			}
		}
		else if (dA == dB) { // parallel same dir
			var posA2 = extendPos (posA, dA, minLength, posB);
			var posB2 = extendPos (posB, dA, minLength, posA);
			posList.push(posA2);
			posList.push(posB2);
			posList.push(posB);
		}
		else { // parallel opposite dir
			if ("updown".indexOf(dA) >= 0 && da * (posB.top - posA.top) > 0 ||
				"leftright".indexOf(dA) >= 0 && da * (posB.left - posA.left) > 0) { 
				var delta = Math.round(calcDelta (posA, dA, posB)/2);
				var posA2 = movePos (posA, dA, delta);
				var posB2 = movePos (posB, dB, delta);
				posList.push(posA2);
				posList.push(posB2);
				posList.push(posB);
			}
			else { // away - construct S route
				var posA2 = movePos (posA, dA, minLength);
				var posB2 = movePos (posB, dB, minLength);
				var segA = new Segment (posA, posA2);
				var segB = new Segment (posB2, posB);
				var dA2 = calcDir (segA, segB);
				var dB2 = calcDir (segB, segA);
				var delta = Math.round(calcDelta (posA, dA2, posB)/2);
				posC = movePos (posA2, dA2, delta);
				var posD = movePos (posB2, dB2, delta);
				posList.push(posA2);
				posList.push(posC);
				posList.push(posD);
				posList.push(posB2);
				posList.push(posB);
			}
		}
	}

	function getStartPos (state_p, sideID_p, delta_p) {
		if ("topup".indexOf(sideID_p)>=0) {
			return {
				left: state_p.position.left + delta_p,
				top: state_p.position.top - 1
			}
		}
		else if ("downbottom".indexOf(sideID_p)>=0) {
			return {
				left: state_p.position.left + delta_p,
				top: state_p.position.top + state_p.position.height - 1
			}
		}
		else if (sideID_p=="left") {
			return {
				left: state_p.position.left - 2,
				top: state_p.position.top + delta_p
			}
		}
		else {
			return {
				left: state_p.position.left + state_p.position.width - 2,
				top: state_p.position.top + delta_p
			}
		}
	}

	function calcDelta (posA_p, dir_p, posB_p) {
		if ("updown".indexOf(dir_p)>=0) {
			return Math.abs(posA_p.top - posB_p.top);
		}
		else {
			return Math.abs(posA_p.left - posB_p.left);
		}
	}

	function extendPos (posA_p, dir_p, minLength_p, posB_p) {
		var newPos = jQuery.extend(true, {},  posA_p);
		if (dir_p=="up") {
			newPos.top = Math.min(posA_p.top, posB_p.top) - minLength_p;
		}
		else if (dir_p=="down") {
			newPos.top = Math.max(posA_p.top, posB_p.top) + minLength_p;
		}
		else if (dir_p=="left") {
			newPos.left = Math.min(posA_p.left, posB_p.left) - minLength_p;
		}
		else {
			newPos.left = Math.max(posA_p.left, posB_p.left) + minLength_p;
		}
		return newPos;
	}

	function intersect (posA_p, dA_p, posB_p, dB_p) {
		var left = 0, top = 0;
		if ("updown".indexOf(dA_p) >= 0) {
			return {left: posA_p.left, top: posB_p.top};
		}
		else {
			return { left: posB_p.left, top: posA_p.top};
		}
	}

	function calcDir (fromSegA_p, toSegB_p) {
		if (Math.abs(fromSegA_p.width) <= 2) { // vertical seg
			if (fromSegA_p.left < toSegB_p.left) {
				return "right";
			}
			else {
				return "left";
			}
		}
		else {
			if (fromSegA_p.top < toSegB_p.top) {
				return "down";
			}
			else {
				return "up";
			}
		}
	}
	
	route ();
	JSDSmoothPosList(posList);
	
	return {
		startDir: dA,
		endDir: dB,
		posList: posList,
		label: defaultLabelPosList[dA]
	}
}

function JSDSmoothPosList (posList_p) {
	// smoothen segs with length <= 5 to zero
	var posA, posB, diff;
	var i = 2;
	posB = posList_p[1];
	while (i < posList_p.length-1) {
		posA = posB;
		posB = posList_p[i];
		diff = posB.left - posA.left;
		if (diff!=0 && Math.abs(diff) < 5) {
			posB.left -= diff;
			if (i < posList_p.length-1) {
				posList_p[i+1].left -= diff;
			}
		}
		diff = posB.top - posA.top;
		if (diff!=0 && Math.abs(diff) < 5) {
			posB.top -= diff;
			if (i < posList_p.length-1) {
				posList_p[i+1].top -= diff;
			}
		}
		i += 1;
	}
}

function Segment (posA_p, posB_p) {
	this.left = 0;
	this.top = 0;
	this.width = posB_p.left - posA_p.left;
	this.height = posB_p.top - posA_p.top;
	this.dir; // left, right, up, down
	if (this.width < 0) {
		this.left = posA_p.left + this.width;
		this.width = -this.width;
		if (this.width<2) this.width = 1;
		this.dir = "left";
	}
	else {
		this.left = posA_p.left;
		if (this.width<2) this.width = 1;
		else {
			this.dir = "right";
		}
	}
	
	if (this.height < 0) {
		this.top = posA_p.top + this.height;
		this.height = -this.height;
		if (this.height<2) this.height = 1;
		this.dir = "up";
	}
	else {
		this.top = posA_p.top;
		if (this.height<2) this.height = 1;
		else {
			this.dir = "down";
		}
	}
}

function movePos (posA_p, dir_p, delta_p) {
	var newPos = jQuery.extend(true, {}, posA_p);
	if (dir_p=="up") {
		newPos.top -= delta_p;
	}
	else if (dir_p=="down") {
		newPos.top += delta_p;
	}
	else if (dir_p=="left") {
		newPos.left -= delta_p;
	}
	else {
		newPos.left += delta_p;
	}
	return newPos;
}

function makeAnchorSegment (startPos_p, startDir_p) {
//	var arrowAdjust = {
//		"up": {left: -1, top: -2},
//		"down": {left: -1, top: 0},
//		"left": {left: -2, top: -1},
//		"right": {left: +2, top: -1}
//	}
	var arrowAdjust = {
		"up": {left: -3, top: -10},
		"down": {left: -3, top: -4},
		"left": {left: -6, top: -6},
		"right": {left: 1, top: -6}
	}
	var seg = jQuery.extend(true, {}, startPos_p);
	var adj = arrowAdjust[startDir_p];
	seg.left += adj.left;
	seg.top += adj.top;
	seg.width = 12;
	seg.height = 16;
	return seg;
}

function makeLabelSegment (startPos_p, startDir_p, labelPos_p) {
	var posA, posB, seg;
	posA = {left: startPos_p.left + labelPos_p.left, top: startPos_p.top + labelPos_p.top};
	posB = movePos (posA, "right", labelPos_p.width);
	seg = new Segment (posA, posB);
	seg.width = labelPos_p.width;
	seg.height = labelPos_p.height;
	return seg;
}

function makeSegList (segInfo_p) {
	var posA, posB, seg, startPos, endPos;
	endPos = segInfo_p.posList[segInfo_p.posList.length-1];
	var segList = [];
	var segEnd = makeAnchorSegment(endPos, segInfo_p.endDir);

	for (var i0 in segInfo_p.posList) {
		if (posA) {
			posB = segInfo_p.posList[i0];
			seg = new Segment (posA, posB);
			if (seg.width > 2) {
				//seg.width += 1;
			}
			if (seg.height > 2) {
				//seg.height += 1;
			}
			segList.push(seg);
			posA = posB;
		}
		else {
			posA = segInfo_p.posList[i0];
			startPos = posA;
		}
	}
	endPos = posB;
	
	if (segInfo_p.label==undefined) {
		segInfo_p.label = defaultLabelPosList[segInfo_p.startDir];
	}
	var segLabel = makeLabelSegment(startPos, segInfo_p.startDir, segInfo_p.label);

	return {
		labelSeg: segLabel,
		endSeg: segEnd,
		segList: segList
	}
}

// move transition start segment when source state moves
function JSDMoveTransStart (trans_p, deltaX_p, deltaY_p) {
	var posInfo = trans_p.posInfo;
	var startPos = posInfo.posList[0];
	var secondPos = posInfo.posList[1];
	startPos.left += deltaX_p;
	startPos.top += deltaY_p;
	if (posInfo.startDir=="up") {
		secondPos.left += deltaX_p;
	}
	else if (posInfo.startDir=="down") {
		secondPos.left += deltaX_p;
	}
	else if (posInfo.startDir=="left") {
		secondPos.top += deltaY_p;
	}
	else {
		secondPos.top += deltaY_p;
	}
}

// move transition end segment/anchor when target state moves
function JSDMoveTransEnd (trans_p, deltaX_p, deltaY_p) {
	var posInfo = trans_p.posInfo;
	var endPos = posInfo.posList[posInfo.posList.length-1];
	var secondPos = posInfo.posList[posInfo.posList.length-2];
	endPos.left += deltaX_p;
	endPos.top += deltaY_p;
	if (posInfo.endDir=="up") {
		secondPos.left += deltaX_p;
	}
	else if (posInfo.endDir=="down") {
		secondPos.left += deltaX_p;
	}
	else if (posInfo.endDir=="left") {
		secondPos.top += deltaY_p;
	}
	else {
		secondPos.top += deltaY_p;
	}
}

// shifting the entire transition (all segments) by specified x/y
function JSDShiftTrans (trans_p, deltaX_p, deltaY_p) {
	var posInfo = trans_p.posInfo;
	for (var i in posInfo.posList) {
		var pos = posInfo.posList[i];
		pos.left += deltaX_p;
		pos.top += deltaY_p;
	}
}

// shift transition mid segments (excluding start and end segments)
// used for moving source or target state
function JSDShiftTransMidSegs (trans_p, deltaX_p, deltaY_p) {
	var posInfo = trans_p.posInfo;
	var pos = posInfo.posList[1];
	if ("left,right".indexOf(pos.dir) >= 0) {
		pos.left += deltaX_p;
	}
	else {
		pos.top += deltaY_p;
	}
	for (var i=2; i<posInfo.posList.length-1; i++) {
		var pos = posInfo.posList[i];
		pos.left += deltaX_p;
		pos.top += deltaY_p;
	}
	
	var pos = posInfo.posList[posInfo.posList.length-1];
	if ("left,right".indexOf(pos.dir) >= 0) {
		pos.left += deltaX_p;
	}
	else {
		pos.top += deltaY_p;
	}
}


// move transition segment vertically or horizontally
function JSDMoveTransSeg (trans_p, segIdx_p, dir_p, delta_p) {
	var posInfo = trans_p.posInfo;
	var movePos = posInfo.posList[segIdx_p];
	if (dir_p=="vertical") {
		movePos.top += delta_p;
		if (segIdx_p < posInfo.posList.length-1) {
			posInfo.posList[segIdx_p+1].top += delta_p;
		}
	}
	else {
		movePos.left += delta_p;
		if (segIdx_p < posInfo.posList.length-1) {
			posInfo.posList[segIdx_p+1].left += delta_p;
		}
	}
	JSDSmoothPosList(posInfo.posList);
}

// determine if user click on the upper, left, right or bottom edge of the state
function JSDResolveClickSide (statePos_p, clickPos_p) {
	var vDiffTop = Math.abs(statePos_p.top - clickPos_p.top);
	var vDiffBottom = Math.abs(statePos_p.top+statePos_p.height - clickPos_p.top);
	var vDiff = Math.min(vDiffTop, vDiffBottom);
	var hDiffLeft = Math.abs(statePos_p.left - clickPos_p.left);
	var hDiffRight = Math.abs(statePos_p.left+statePos_p.width - clickPos_p.left);
	var hDiff = Math.min(hDiffLeft, hDiffRight);
	if (vDiff < hDiff) {
		if (vDiffTop < vDiffBottom) {
			return {
				sideID: "top",
				offset: hDiffLeft
			}
		}
		else {
			return {
				sideID: "bottom",
				offset: hDiffLeft
			}
		}
	}
	else {
		if (hDiffLeft < hDiffRight) {
			return {
				sideID: "left",
				offset: vDiffTop
			}
		}
		else {
			return {
				sideID: "right",
				offset: vDiffTop
			}
		}
	}
}


