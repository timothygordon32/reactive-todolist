'use strict';

angular.module('todo', ['todo.controllers']);

angular.module('todo')
    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider
            .when("/tasks", {
                templateUrl: "assets/partials/tasks.html",
                controller: "TodoCtrl"
            }).otherwise({
                redirectTo: "/tasks"
            });
    }]);