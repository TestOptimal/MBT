var module = angular.module("lvl.directives.dragdrop", ['lvl.services']);

module.directive('lvlDraggable', ['$rootScope', 'uuid', function($rootScope, uuid) {
    return {
        restrict: 'A',
        link: function(scope, el, attrs, controller) {
            angular.element(el).attr("draggable", "true");
            var id = angular.element(el).attr("id");
            if (!id) {
                id = uuid.new()
                angular.element(el).attr("id", id);
            }
            
            el.bind("dragstart", function(e) {
                e.dataTransfer.setData('text', id);
                $rootScope.$emit("LVL-DRAG-START");
            });
            
            el.bind("dragend", function(e) {
                $rootScope.$emit("LVL-DRAG-END");
            });
        }
	}
}]);

module.directive('lvlDropTarget', ['$rootScope', 'uuid', function($rootScope, uuid) {
    return {
        restrict: 'A',
        scope: {
            onDrop: '&'
        },
        link: function(scope, el, attrs, controller) {
            var id = angular.element(el).attr("id");
            if (!id) {
                id = uuid.new()
                angular.element(el).attr("id", id);
            }
                       
            el.bind("dragover", function(e) {
              if (e.preventDefault) {
                e.preventDefault(); // Necessary. Allows us to drop.
              }
              
              e.dataTransfer.dropEffect = 'move';  // See the section on the DataTransfer object.
              return false;
            });
            
            el.bind("dragenter", function(e) {
              // this / e.target is the current hover target.
              angular.element(e.target).addClass('lvl-over');
            });
            
            el.bind("dragleave", function(e) {
              angular.element(e.target).removeClass('lvl-over');  // this / e.target is previous target element.
            });
            
            el.bind("drop", function(e) {
              if (e.preventDefault) {
                e.preventDefault(); // Necessary. Allows us to drop.
              }

              if (e.stopPropogation) {
                e.stopPropogation(); // Necessary. Allows us to drop.
              }
            	var data = e.dataTransfer.getData("text");
                var dest = document.getElementById(id);
                var src = document.getElementById(data);

                angular.element(e.target).removeClass('lvl-over');  // this / e.target is previous target element.
                
                scope.onDrop({dragEl: src, dropEl: dest});
            });

            $rootScope.$on("LVL-DRAG-START", function() {
                var el = document.getElementById(id);
                angular.element(el).addClass("lvl-target");
            });
            
            $rootScope.$on("LVL-DRAG-END", function() {
                var el = document.getElementById(id);
                angular.element(el).removeClass("lvl-target");
                angular.element(el).removeClass("lvl-over");
            });
        }
	}
}]);

try {
    module = angular.module('lvl.services');  
} catch (e) {
    module  = angular.module('lvl.services', []);
}

module.factory('uuid', function() {
    var svc = {
        new: function() {
            function _p8(s) {
                var p = (Math.random().toString(16)+"000000000").substr(2,8);
                return s ? "-" + p.substr(0,4) + "-" + p.substr(4,4) : p ;
            }
            return _p8() + _p8(true) + _p8(true) + _p8();
        },
        
        empty: function() {
          return '00000000-0000-0000-0000-000000000000';
        }
    };
    
    return svc;
});