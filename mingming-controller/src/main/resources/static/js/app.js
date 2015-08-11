angular.module('mingming', [ 'ngRoute', 'ngResource', 'ngAnimate', 'angular.filter' ]).config(
		function($routeProvider) {

			$routeProvider.otherwise('/');
			$routeProvider.when('/', {
				templateUrl : 'views/home.html',
				controller : 'home'
			}).when('/workers', {
				templateUrl : 'views/workers.html',
				controller : 'workers'
			});

}).controller('home', function() {
}).controller('workers', function($rootScope, $scope, $http, $timeout, $animate) {

	function workerDescription(worker) {
		var description = worker.applicationRoute;
		description += "[" + worker.instanceIndex + "]";
		description += " -> " + worker.instanceState;
		return description;
	}

	$scope.timestamp = 0;
	$scope.backoff = 50;
	$scope.workers=[];
	$scope.listen = function() {
		$http.get('/workers/events?since=' + $scope.timestamp).success(function(notification) {
			$scope.backoff = 50;
			$scope.timestamp = notification.timestamp;
			for (var e = 0; e < notification.events.length; e++) {
				var event = notification.events[e];
				if (event.eventType == "refresh") {
					console.log("refresh");
					$scope.workers = event.workers;
				}
				else if (event.eventType == "update") {
					var workerIndex = {};
					for (var i = 0; i < $scope.workers.length; i++) workerIndex[$scope.workers[i].instanceId] = i;
					for (var w = 0; w < event.workers.length; w++) {
						var worker = event.workers[w];
						var index = workerIndex[worker.instanceId];
						if (typeof index != 'undefined') {
							if (worker.instanceState == 'gone') $scope.workers[index].instanceState = "gone";
							else {
								console.log("update existing worker " + workerDescription(worker));
								for (var property in worker) $scope.workers[index][property] = worker[property];
							}
						}
						else {
							console.log("add new worker " + workerDescription(worker));
							$scope.workers.push(worker);
							workerIndex[worker.InstanceId] = $scope.workers.length - 1;
						}
					}
					for (var w = $scope.workers.length - 1; w >= 0; w--) {
						if ($scope.workers[w].instanceState == 'gone') {
							console.log("delete gone worker " + workerDescription($scope.workers[w]));
							$scope.workers.splice(w, 1);
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