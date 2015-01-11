'use strict';

angular.module('todo', ['ngRoute', 'todo.controllers']);

angular.module('todo')
    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider
            .when("/login", {
                templateUrl: "assets/partials/login.html",
                controller: "LoginController"})
            .when("/signup", {
                templateUrl: "assets/partials/signup.html",
                controller: "SignupController"})
            .when("/signup/:token", {
                templateUrl: "assets/partials/signup-verified.html",
                controller: "SignupVerifiedController"})
            .when("/reset", {
                templateUrl: "assets/partials/reset.html",
                controller: "ResetController"})
            .when("/reset/:token", {
                templateUrl: "assets/partials/reset-verified.html",
                controller: "ResetVerifiedController"})
            .when("/password", {
                templateUrl: "assets/partials/change-password.html",
                controller: "PasswordController"})
            .when("/tasks", {
                templateUrl: "assets/partials/tasks.html",
                controller: "TodoCtrl"})
            .otherwise({
                redirectTo: "/tasks"
            });
    }]).run(function ($location, $http, $rootScope) {
        var whiteListed = function (path) {
            return _.some([/signup\//, /reset/, /reset\//], function (pattern) {
                return !!path.match(pattern);
            });
        };

        if (!whiteListed($location.path())) {
            $http.get("/login")
                .success(function (login) {
                    $rootScope.login = login;
                    $location.path("/tasks");
                })
                .error(function (data, status) {
                    if (status == 401) {
                        $location.path("/login");
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