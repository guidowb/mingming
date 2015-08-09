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

}).run(function($rootScope, $window, $interval) {
	$window.onfocus = function() {
		if (!$rootScope.updater) return;
		if ($rootScope.updaterPromise) return;
		$rootScope.updater();
		$rootScope.updaterPromise = $interval($rootScope.updater, 1000)
		$rootScope.updating = true;
		$rootScope.$apply();
	};
	$window.onblur = function() {
		if ($rootScope.updaterPromise) {
			$interval.cancel($rootScope.updaterPromise);
			$rootScope.updaterPromise = null
		}
		$rootScope.updating = false;
		$rootScope.$apply();
	};
	$window.focus();
	$rootScope.updating = true;
	$rootScope.autoUpdate = function(updater) {
		if ($rootScope.updaterPromise) {
			$interval.cancel($rootScope.updaterPromise);
			$rootScope.updaterPromise = null
		}
		$rootScope.updater = updater;
		if (!$rootScope.updater) return;
		$rootScope.updater();
		$rootScope.updaterPromise = $interval($rootScope.updater, 1000)
	}
}).controller('home', function() {
}).controller('workers', function($rootScope, $scope, $http, $animate) {

	$rootScope.autoUpdate(function() {
		$http.get('/workers').success(function(data) {
			if (!$scope.workers) {
				$scope.workers = data;
				return;
			}
			var newWorkers = data;
			var workerIndex = {}
			for (var i = 0; i < $scope.workers.length; i++) {
				workerIndex[$scope.workers[i].instanceId] = i;
			}
			for (var i = 0; i < data.length; i++) {
				var index = workerIndex[data[i].instanceId]
				if (index) {
//					for (var property in data[i]) {
//						$scope.workers[index][property] = data[i][property];
//					}
					$scope.workers[index].secondsSinceUpdate = data[i].secondsSinceUpdate;
					$scope.workers[index].secondsSinceCreation = data[i].secondsSinceCreation;
					delete workerIndex[data[i].instanceId];
				}
				else {
					$scope.workers.push(data[i]);
				}
			}
			for (var index in workerIndex) {
				for (var i = 0; i < $scope.workers.length; i++) {
					if ($scope.workers[i].instanceId == index) {
						$scope.workers.splice(i, 1);
						break;
					}
				}
			}
		}).error(function() {
			$scope.workers = []
		})
	});
	$scope.canaryClass = function(canary) {
		if (canary.secondsSinceCreation < 2) return "gone";
		if (canary.secondsSinceCreation < 4) return "new";
		if (canary.secondsSinceUpdate > 26) return "gone";
		if (canary.secondsSinceUpdate > 16) return "dead";
		if (canary.secondsSinceUpdate > 6) return "late";
		return "healthy";
	}
});