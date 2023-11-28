/*
 * jquery plugin: jTagging.js - copyright TestOptimal LLC, 2013
 * author: yaxiong lin
**/

(function($){
    $.fn.jTagging = function(taggingOn, options) {
        return this.each(function() {
			var tElem = $(this).children(".jTagging");
			if (tElem.length>0) {
				if (taggingOn) {
				}
				else {
					$(this).removeClass("marked");
					$(tElem).remove();
				}
			}
			else {
				if (taggingOn) {
					$(this).addClass("marked");
					if (options.addTag) {
						var tElem = $("<div class='jTagging' jTaggingID='" + $(this).attr("id") + "' style='" + jTagging.tagStyle + "'>" + jTagging.nextSeqNum() + "</div>")
							.appendTo($(this));
					}
/*						
						.bind('contextmenu', function(e) {
						    e.preventDefault();
						    jTagging.arrangeTags();
						    return false;
						})
						.dblclick(function(e) {
						    e.preventDefault();
							alert('dblclick');
							jTagging.setSeqNum(2);
						});
*/					
					
					if (options && options.class) {
						$(tElem).addClass(options.class);
					}
				}
				else {
					$(this).removeClass("marked");
				}
			}
						
			jTagging.timmer = setTimeout(jTagging.delayShow, jTagging.delayMS);
        });
    };
})(jQuery);

jTagging = function () { return this; }
jTagging.tagStyle = "display:none; z-index:999999; position: absolute;";
jTagging.delayMS = 150;

jTagging.curSeqNum = 0;
jTagging.nextSeqNum = function () {
	jTagging.curSeqNum += 1;
	return jTagging.curSeqNum;
}

jTagging.resetSeqNum = function() {
	jTagging.curSeqNum = 0;
}

jTagging.setSeqNum = function (tagElem_p, tagNum_p) {
	$(tagElem_p).html(tagNum_p);
}

jTagging.arrangeTags = function() {
	jTagging.curSeqNum = 0;
	$(".jTagging").sort(function(a, b) {
		return parseFloat($(a).text()) > parseFloat($(b).text()) ? 1 : -1;
	}).each(function() {
		$(this).html(jTagging.nextSeqNum());
	});
}

jTagging.clearMarks = function () {
	var allIDs = jTagging.getMarked();
	
	$(".marked").removeClass("marked");
	$(".jTagging").remove();
	jTagging.curSeqNum = 0;
}

jTagging.getMarked = function () {
	var retIdList = new Array();
	$(".jTagging").sort(function(a, b) {
		return parseFloat($(a).text()) > parseFloat($(b).text()) ? 1 : -1;
	}).each(function() {
		retIdList.push($(this).attr("jTaggingID"));
	});
	
	return retIdList;
}

jTagging.timmer = null;
jTagging.delayShow = function () {
	var tagCnt = $(".jTagging").size();
//	if (tagCnt == 1) {
//	 	$(".jTagging").hide();
//	}
//	else {
		if (tagCnt < jTagging.curSeqNum) {
			jTagging.arrangeTags();
		}
	 	$(".jTagging").show();
//	}
}
