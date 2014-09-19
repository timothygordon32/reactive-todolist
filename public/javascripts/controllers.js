'use strict';

angular.module('todo.controllers', ['ngResource']);

angular.module('todo.controllers').controller('TodoCtrl', function ($scope, $resource) {

    $scope.identity = $resource("/username").get();

    $scope.loaded = false;

    $scope.todos = [];

    $resource("/json/tasks").query(function(data) {
        $scope.todos = _.map(data, function(task) {
            return {text: task.label, done: false };
        });
        $scope.loaded = true;
    });

    $scope.getTodosCount = function () {
        return $scope.todos.length;
    };

    $scope.addTodo = function() {
        $scope.todos.push({text: $scope.formTodoText, done: false });
        $scope.formTodoText = "";
    };

    $scope.getRemainingCount = function() {
        return $scope.getRemaining().length;
    };

    $scope.getRemaining = function () {
        return _.filter($scope.todos, function (todo) {
            return !todo.done;
        });
    };

    $scope.clearCompleted = function () {
        $scope.todos = $scope.getRemaining();
    };
});

angular.module('todo.controllers').controller('LandingCtrl', function ($rootScope, $scope, $http, $location) {

    $scope.formData = {};

    $scope.setUsername = function() {
        $http({
            method  : 'POST',
            url     : '/login',
            data    : $scope.formData,
            headers : { 'Content-Type': 'application/x-www-form-urlencoded' },
            transformRequest: function(obj) {
                var str = [];
                angular.forEach(Object.keys(obj), function(p) {
                    str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
                });
                return str.join("&");
            }
        }).success(function () {
            $location.path("/tasks");
        });
    }
});
