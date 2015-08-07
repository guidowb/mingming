angular.module('mingming', [ 'ngRoute', 'ngResource', 'angular.filter' ]).config(
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
}).controller('workers', function($rootScope, $scope, $http) {

	$rootScope.autoUpdate(function() {
		$http.get('/workers').success(function(data) {
			$scope.workers = data;
		}).error(function() {
			$scope.workers = []
		})
	});

});