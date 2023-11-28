/*
 * jquery plugin: flexTooltip.js - copyright TestOptimal LLC, 2013
 * author: yaxiong lin
**/

(function($){
    $.fn.flexTooltip = function(options) {
        return this.each(function() {
	    	flexTooltip.initialize(options);
	    	var title = $(this).attr("title");
	    	if (title) {
	    		$(this).attr("title2", title).attr("title", null);
	    	}
        	$(this).hover(
        		function() {
        			flexTooltip.triggerTooltip(this);
        		},
        		function() {
        			flexTooltip.signalClose();
        		}
        	); 
        });
    };
})(jQuery);

flexTooltip = function () { 
	return this; 
}

flexTooltip.tooltipElem;
flexTooltip.tooltipElemMS;		
flexTooltip.showTimer;
flexTooltip.closeTimer;
flexTooltip.popupElem;
flexTooltip.hideDelayMS = 100;
flexTooltip.delayMS = 1250;
flexTooltip.inPopup = false;
flexTooltip.css = {"z-index": 999999, "background-color": "#BDBDBD", 
	opacity: 1.0,
	"border-radius": "8px", padding: "5px", border: "4px double orange"};
flexTooltip.tooltipKeys = ["title", "url"];

flexTooltip.initialize = function(options) {
	if (options) {
		if (options.delayMS>0) {
			flexTooltip.delayMS = options.delayMS;
		}
		
		if (options.css) {
			// set each css attr
		}
		
		if (options.tooltipKeys) {
			flexTooltip.tooltipKeys = options.tooltipKeys;
		}
	}
	this.popupElem = $("<div id='flexTooltipPopup' style='display:none;position:absolute;'><div class='flexTooltipHeader'></div><hr/><div class='flexTooltipBody'></div></div>").appendTo("body");
	$(this.popupElem).css(flexTooltip.css).hover(
		function() { flexTooltip.inPopup = true; },
		function() { flexTooltip.inPopup = false; $(this).hide();}
	);	
	
	this.popupHeader = $(this.popupElem).find(".flexTooltipHeader");
	this.popupBody = $(this.popupElem).find(".flexTooltipBody");
	
}

flexTooltip.triggerTooltip = function (elem) {
	if (flexTooltip.closeTimer) {
		clearTimeout(flexTooltip.closeTimer);
		flexTooltip.closeTimer = undefined;
	}

	if (flexTooltip.showTimer) {
		clearTimeout(flexTooltip.showTimer);
		flexTooltip.showTimer = undefined;
	}
	flexTooltip.tooltipElem = elem;
	flexTooltip.cancelMS = (new Date()).getMilliseconds() + flexTooltip.delayMS; 
	if (flexTooltip.delayMS>0) {
		flexTooltip.showTimer = setTimeout('flexTooltip.showTooltip()', flexTooltip.delayMS);
	}
	else {
		flexTooltip.showTooltip();
	}
}

flexTooltip.showTooltip = function() {
	var popupContinue = flexTooltip.setupTooltip(flexTooltip.tooltipElem);
	if (popupContinue) {
		$(this.popupElem).show();
	}
}

flexTooltip.signalClose = function() {
	if (flexTooltip.closeTimer) {
		clearTimeout(flexTooltip.closeTimer);
		flexTooltip.closeTimer = undefined;
	}
	flexTooltip.closeTimer = setTimeout('flexTooltip.hideTooltip()', flexTooltip.hideDelayMS);
}

flexTooltip.hideTooltip = function() {
	if (!flexTooltip.inPopup) {
		if (flexTooltip.showTimer) {
			clearTimeout(flexTooltip.showTimer);
		}
		$(this.popupElem).hide();
	}
}

flexTooltip.setupTooltip = function(elem) {
	var fieldList = new Array();
	for (i in flexTooltip.tooltipKeys) {
		var tooltipKey = flexTooltip.tooltipKeys[i];
		var lookupKey = tooltipKey;
		if (tooltipKey=="title") {
			lookupKey = "title2";
		}
		var val = $(elem).attr(lookupKey);
		if (val==undefined || val=="") continue;
		
		if (tooltipKey=="href" || val.indexOf("http://")==0) {
			var linkName = val;
			var idx = val.indexOf("|");
			if (idx>0) {
				linkName = val.substring(idx+1);
				val = val.substring(0, idx);
			}
			
			val = "<a href=\"" + val + "\" target=_blank>" + linkName + "</a>";
		}
		fieldList.push ( tooltipKey + ": " + val);
	}

	if (fieldList.length<=0) {
		flexTooltip.hideTooltip();
		return false;
	}
	
	var html = "<ul style='list-style: none; padding-left: 2px; margi-left: 0px;'><li>" + fieldList.join("</li><li>") + "</li></ul>";
	
	$(this.popupHeader).html($(elem).html());
	var position = $(elem).offset();
	$(this.popupBody).html(html);
	$(this.popupElem).css(position);
	var color = flexTooltip.getElemBackGroundColor(elem);
	$(this.popupElem).css("background-color", color);
	
	return true;	
}

flexTooltip.getElemBackGroundColor = function(elem) {
	while (elem!=undefined) {
		var bgColor = $(elem).css("backgroundColor");
		if (bgColor!="transparent") {
			return bgColor;
		}
		elem = $(elem).parent();
	}
	return "#BEBEBE";
}

