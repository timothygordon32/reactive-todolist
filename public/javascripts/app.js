'use strict';

angular.module('todo', ['ngRoute', 'todo.controllers']);

angular.module('todo')
    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider
            .when("/landing", {
                templateUrl: "assets/partials/landing.html",
                controller: "LandingCtrl"})
            .when("/signup", {
                templateUrl: "assets/partials/signup.html",
                controller: "SignupController"})
            .when("/signup/:token", {
                templateUrl: "assets/partials/signup-verified.html",
                controller: "SignupVerifiedController"})
            .when("/tasks", {
                templateUrl: "assets/partials/tasks.html",
                controller: "TodoCtrl"})
            .otherwise({
                redirectTo: "/tasks"
            });
    }]).run(function($location, $http, $rootScope) {
        if (!$location.path().match(/signup\//)) {
            $http.get("/login").success(function(login) {
                $rootScope.login = login;
                $location.path("/tasks");
            }).error(function(data, status) {
                if (status == 401) {
                    $location.path("/landing");
                }
            });

        }
    });

angular.module('todo')
    .directive('formGroup', function () {
        return {
            restrict: 'E',
            transclude: true,
            scope: {
                'errors': '='
            },
            templateUrl: 'assets/partials/form-group.html'
        };
    });