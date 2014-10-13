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
            .when("/tasks", {
                templateUrl: "assets/partials/tasks.html",
                controller: "TodoCtrl"})
            .otherwise({
                redirectTo: "/tasks"
            });
    }]).run(function($location, $http, $rootScope) {
        $http.get("/login").success(function(login) {
            $rootScope.login = login;
            $location.path("/tasks");
        }).error(function(data, status) {
            if (status == 401) {
                $location.path("/landing");
            }
        });
    });