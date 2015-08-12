angular.module('mingming', [ 'ngRoute', 'ngResource', 'ngAnimate', 'angular.filter' ]).config(
		function($routeProvider) {

			$routeProvider.otherwise('/canaries');
			$routeProvider.when('/canaries', {
				templateUrl : 'views/canaries.html',
				controller : 'canaries'
			});

}).controller('canaries', function($rootScope, $scope, $http, $timeout, $animate) {

	function canaryDescription(canary) {
		var description = canary.applicationRoute;
		description += "[" + canary.instanceIndex + "]";
		description += " -> " + canary.instanceState;
		return description;
	}

	$scope.timestamp = 0;
	$scope.backoff = 50;
	$scope.canaries=[];
	$scope.listen = function() {
		$http.get('/canaries/events?since=' + $scope.timestamp).success(function(notification) {
			$scope.backoff = 50;
			$scope.timestamp = notification.timestamp;
			for (var e = 0; e < notification.events.length; e++) {
				var event = notification.events[e];
				if (event.eventType == "refresh") {
					console.log("refresh");
					$scope.canaries = event.canaries;
				}
				else if (event.eventType == "update") {
					var canaryIndex = {};
					for (var i = 0; i < $scope.canaries.length; i++) canaryIndex[$scope.canaries[i].instanceId] = i;
					for (var w = 0; w < event.canaries.length; w++) {
						var canary = event.canaries[w];
						var index = canaryIndex[canary.instanceId];
						if (typeof index != 'undefined') {
							if (canary.instanceState == 'gone') $scope.canaries[index].instanceState = "gone";
							else {
								console.log("update existing canary " + canaryDescription(canary));
								for (var property in canary) $scope.canaries[index][property] = canary[property];
							}
						}
						else {
							console.log("add new canary " + canaryDescription(canary));
							$scope.canaries.push(canary);
							canaryIndex[canary.InstanceId] = $scope.canaries.length - 1;
						}
					}
					for (var w = $scope.canaries.length - 1; w >= 0; w--) {
						if ($scope.canaries[w].instanceState == 'gone') {
							console.log("delete gone canary " + canaryDescription($scope.canaries[w]));
							$scope.canaries.splice(w, 1);
						}
					}
				}
			}
			$scope.listen();
		}).error(function() {
			if ($scope.backoff < 20000) $scope.backoff = $scope.backoff * 2;
			$timeout($scope.listen, $scope.backoff);
		});
	}
	$scope.canaryClass = function(canary) {
		return canary.instanceState;
	}
	$scope.listen();
});