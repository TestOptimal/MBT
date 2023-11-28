// include after main app.js. contains a set of directives used by IDE app
MainModule.factory ('IdeSize', function($rootScope) {
	var IdeSize = { 
			viewWidth: 600,
			viewHeight: 500,
			listeners: []
	};
	
	IdeSize.addListener = function (listener) {
		IdeSize.listeners.push(listener);
	};
	
	IdeSize.paneResized = function (dragScope, stopLeft) {
    	if (stopLeft==0) {
    		if (dragScope.splitPct==0) {
        		dragScope.splitPct = dragScope.preSplitPct;
        	}
    		else {
        		dragScope.preSplitPct = dragScope.splitPct;
        		dragScope.splitPct = 0;
    		}
    	}
    	else {
    		dragScope.splitPct = Math.round(100 * stopLeft / dragScope.viewWidth);
    	}
		if (dragScope.windowResized) dragScope.windowResized();
		dragScope.$apply();
	}
	
	IdeSize.windowResized = function (applyRibbonScope) {
		IdeSize.windowWidth = window.innerWidth;
		IdeSize.windowHeight = window.innerHeight;
		IdeSize.viewWidth = IdeSize.windowWidth;
		for (var i in IdeSize.listeners) {
			try {
				IdeSize.listeners[i].windowResized();
				if (applyRibbonScope) {
					IdeSize.listeners[i].$apply();
				}
			}
			catch (err) {
				alert('failed calling windowResized() on listener ' + i + ': ' + err);
			}
		}
	}

	window.onresize = function() {
		IdeSize.windowResized(true);
	}
	
	return IdeSize;
});


MainModule.factory ('IdeUtil', function($rootScope) {
	var IdeUtil = {}; 
	
	IdeUtil.applyFilter = function (list_p, attrName_p, searchText_p, hideAttrName_p) {
		var regExp = undefined;
		var startsWith = true;
		var endsWith = true;
		searchText_p = searchText_p.trim();
		if (searchText_p.length>0 && searchText_p.substring(0,1)=="/") {
			regExp = new RegExp(searchText_p.substring(1));
		}
		else {
			if (searchText_p.indexOf("*")==0) {
				searchText_p = searchText_p.substring(1);
			}
			else {
				endsWith = false;
			}
			if (searchText_p.length>0 && searchText_p.substring(searchText_p.length-1)=="*") {
				searchText_p = searchText_p.substring(0, searchText_p.length-1);
			}
			else {
				startsWith = false;
			}
			if (!startsWith && !endsWith) {
				startsWith = true;
				endsWith = true;
			}
		}
		for (i in list_p) {
			var checkObj = list_p[i];
			if (checkObj==undefined || checkObj==null) {
				continue;
			}
			var matched = false;
	    	if (regExp) {
	    		matched = regExp.test(checkObj[attrName_p]);
	    	}
	    	else {
	    		// case insensitive search, default contains
	    		var searchText = searchText_p.toUpperCase();
	    		if (searchText=='') {
	    			matched = true;
	    		}
	    		else {
		    		var objText = checkObj[attrName_p].toUpperCase();
		    		var idx = objText.indexOf(searchText);
		    		if (idx<0) {
		    			matched = false;
		    		}
		    		else if (startsWith && endsWith) {
		    			matched = (idx>=0);
			    	}
		    		else if (startsWith) {
		    			matched = (idx==0);
			    	}
			    	else if (endsWith) {
			    		matched = (searchText == objText.substring(objText.length-searchText.length));
			    	}
	    		}
	    	}
			checkObj.hide = !matched;
		}
	}
	
	return IdeUtil;
});


MainModule.directive('ngEnter', function () {
    return function (scope, element, attrs) {
        element.bind("keydown keypress", function (event) {
            if(event.which === 13) {
                scope.$apply(function (){
                    scope.$eval(attrs.ngEnter);
                });

                event.preventDefault();
            }
        });
    };
});

MainModule.directive('ngEscape', function () {
    return function (scope, element, attrs) {
        element.bind("keydown keypress", function (event) {
            if(event.which === 27) {
                scope.$apply(function (){
                    scope.$eval(attrs.ngEscape);
                });

                event.preventDefault();
            }
        });
    };
});

MainModule.directive('stopEvent', function () {
    return {
//        restrict: 'img',
        link: function (scope, element, attr) {
            element.bind('click', function (e) {
                e.stopPropagation();
            });
        }
    };
 });


MainModule.directive('autoFocus', function() {
    return {
        restrict: 'A',
        link: function(_scope, _element, attrs) {
        	if (attrs.autoFocus) {
                _element[0].focus();
        	}
        }
    };
});


MainModule.directive('draggable', function($document, $rootScope, IdeSize) {
	return function(scope, element, attrs) {
		var startX = 0, startY = 0; // start pos
		var dx, dy;
		var eX = 0, eY = 0; // moving pos
		var x = 0, y = 0; // stop pos
		var dragElement = element;
		if (attrs.draggable && attrs.draggable!="") {
			dragElement = element.find(attrs.draggable);
		}
		dragElement.css("cursor","move");
		
		element.css({
			position: 'absolute'
		});
		 
		dragElement.on('mousedown', function(event) {
			// Prevent default dragging of selected content
			dx = 0;
			dy = 0;
			eX = element.position().left;
			eY = element.position().top;
			event.preventDefault();
			startX = event.pageX;// - x;
			startY = event.pageY;// - y;
			$document.on('mousemove', mousemove);
			$document.on('mouseup', mouseup);
		});
		 
		function mousemove(event) {
			dx = event.pageX - startX;
			dy = event.pageY - startY;
			y = eY + dy;
			x = eX + dx;
			
			element.css({
				top: y + 'px',
				left: x + 'px'
			});
		}
		 
		function mouseup(event) {
			$document.unbind('mousemove', mousemove);
			$document.unbind('mouseup', mouseup);
			if (dx==0 && dy==0) {
				x = 0;
				y = 0;
			}
			if (scope.dragStop) {
				scope.dragStop(element, x, y);
			}
			else if (scope.paneDivider) {
				IdeSize.paneResized(scope, x);
			}
		}
	}
});

MainModule.directive('resizable', function($document, $rootScope) {
	return function(scope, element, attrs) {
		var startX = 0, startY = 0; // start pos
		var elementWidth, elementHeight;
		var dx=0, dy=0;
		var minWidth = 50, minHeight = 50, maxWidth = 1000, maxHeight = 800;
		if (attrs.minWidth) {
			minWidth = parseInt(attrs.minWidth);
		}
		if (attrs.maxWidth) {
			maxWidth = parseInt(attrs.maxWidth);
		}
		if (attrs.minHeight) {
			minHeight = parseInt(attrs.minHeight);
		}
		if (attrs.maxHeight) {
			maxHeight = parseInt(attrs.maxHeight);
		}
		var resizers = element.find(attrs.resizer);
		elementWidth = parseInt(attrs.width);
		elementHeight = parseInt(attrs.height);
		element.css({
			left: attrs.left+"px",
			top: attrs.top + "px"
		});
		resizeElements();
		
		resizers.on('mousedown', function(event) {
			// Prevent default dragging of selected content
			event.preventDefault();
			startX = event.pageX;
			startY = event.pageY;
			$document.on('mousemove', mousemove);
			$document.on('mouseup', mouseup);
		});
		
	   	function resizeElements () {
			element.css({
				width: (elementWidth+dx) + "px",
				height: (elementHeight+dy) + "px"
			});
		
			if (attrs.resizable=="view") {
				element.find(".viewArea").css({
					height: (elementHeight+dy) + "px"
				})
			}
	   	}
	   	
		function mousemove(event) {
			event.preventDefault();
			dx = event.pageX - startX;
			dy = event.pageY - startY;
			var x = elementWidth + dx;
			var y = elementHeight + dy;
			
			if (x < minWidth || x > maxWidth ||
				y < minHeight || y > maxHeight) {
				startX = event.pageX;
				startY = event.pageY;
				$document.unbind('mousemove', mousemove);
				$document.unbind('mouseup', mouseup);
				if (x < minWidth) {
					elementWidth = minWidth;
				}
				else if (x > maxWidth) {
					elementWidth = maxWidth;
				}
				if (y < minHeight) {
					elementHeight = minHeight;
				}
				else if (y > maxHeight) {
					elementHeight = maxHeight;
				}
				dx = 0;
				dy = 0;
			}
			resizeElements();
		}
		 
		function mouseup(event) {
			$document.unbind('mousemove', mousemove);
			$document.unbind('mouseup', mouseup);
			elementWidth = elementWidth + dx;
			elementHeight = elementHeight + dy;
			dx = 0;
			dy = 0;
		}
	}
});


MainModule.directive('cusval', function() {
  return {
    require: 'ngModel',
    restrict: "A",
    // scope = the parent scope
    // elem = the element the directive is on
    // attr = a dictionary of attributes on the element
    // ctrl = the controller for ngModel.
    link: function(scope, ele, attrs, ctrl) {
//      scope.$watch(attrs.ngModel, function(oldVal, newVal) {
//    	  if (attrs.domainList=="YesNo") {
//    		  if (newVal!='Y' && newVal!='N') {
//    			  c.$setValidity('YesNoValidate', false);
//    		  }
//    	  }
//      });
    	
    	// add a parser that will process each time the value is 
        // parsed into the model when the user updates it.
        ctrl.$parsers.unshift(function(value) {
            // test and set the validity after update.
        	var valid = checkDomain (value, attrs.domain);
            ctrl.$setValidity(attrs.domain + 'Validate', valid);
            
            // if it's valid, return the value to the model, 
            // otherwise return undefined.
            return valid ? value : undefined;
        });
        
        // add a formatter that will process each time the value 
        // is updated on the DOM element.
        ctrl.$formatters.unshift(function(value) {
            // validate.
            ctrl.$setValidity(attrs.domain + 'Validate', checkDomain(value, attrs.domain));
            
            // return the value or nothing will be written to the DOM.
            return value;
        });    	
    }
  }
});

MainModule.directive('showHint', function() {
    return {
        restrict: 'A',
        link: function(scope, elem, attr) {
       	 	elem.bind('click', function() {
        	     elem.addClass('showhint').siblings().removeClass('showhint'); 
        	        // remove the class from the other sibling divs
        	});

            elem.bind('click', function(e) {
                elem.toggleClass('showhint');
                e.stopPropagation();
            });
            $(document).bind('click', function() {
                elem.removeClass('showhint');
            });
        }
    };
});

MainModule.directive("dynamicName",function($compile){
    return {
        restrict:"A",
        terminal:true,
        priority:1000,
        link:function(scope,element,attrs){
            element.attr('name', scope.$eval(attrs.dynamicName));
            element.removeAttr("dynamic-name");
            $compile(element)(scope);
        }
    };
});


MainModule.directive('paneDivider', function () {
	return {
		scope: false,
		restrict: 'E',
    	transclude: false,
    	replace: true,
    	template: "<div class=\"paneDivider\" ng-init=\"paneDivider=true;\" ng-style=\"{left: splitPct +'%', bottom: 0, top: headerHeight + 'px'}\" draggable title=\"drag divider to resize panes\"></div>"
	}
 });

