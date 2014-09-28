'use strict';

angular.module('todo', ['ngRoute', 'todo.controllers']);

angular.module('todo')
    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider
            .when("/landing", {
                templateUrl: "assets/partials/landing.html",
                controller: "LandingCtrl"})
            .when("/tasks", {
                templateUrl: "assets/partials/tasks.html",
                controller: "TodoCtrl"})
            .otherwise({
                redirectTo: "/landing"
            });
    }]);