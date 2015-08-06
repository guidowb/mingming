angular.module('mingming', [ 'ngRoute', 'ngResource' ]).config(
		function($routeProvider) {

			$routeProvider.otherwise('/');
			$routeProvider.when('/', {
				templateUrl : 'views/home.html',
				controller : 'home'
			}).when('/projects', {
				templateUrl : 'views/workers.html',
				controller : 'workers'
			});

		}).controller('navigation', function($scope, $http, $window, $route) {
	$scope.tab = function(route) {
		return $route.current && route === $route.current.controller;
	};
	if (!$scope.user) {
		$http.get('/api/user').success(function(data) {
			$scope.user = data;
			$scope.authenticated = true;
		}).error(function() {
			$scope.authenticated = false;
		});
	}
	$scope.logout = function() {
		$http.post('/api/logout', {}).success(function() {
			delete $scope.user;
			$scope.authenticated = false;
			// Force reload of home page to reset all state after logout
			$window.location.hash = '';
		});
	};
}).controller('home', function() {
}).controller('workers', function($scope, $http) {

	$http.get('/workers').success(function(data) {
		$scope.workers = data;
	}).error(function() {
		$scope.workers = []
	});

});