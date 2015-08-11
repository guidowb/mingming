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
						var workerDescription = worker.applicationRoute;
							workerDescription += "[" + worker.instanceIndex + "]";
							workerDescription += " -> " + worker.instanceState;
						var index = workerIndex[worker.instanceId];
						if (typeof index != 'undefined') {
							console.log("update existing worker " + workerDescription);
							for (var property in worker) $scope.workers[index][property] = worker[property];
						}
						else {
							console.log("add new worker " + workerDescription);
							$scope.workers.push(worker);
							workerIndex[worker.InstanceId] = $scope.workers.length - 1;
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