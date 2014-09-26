'use strict';

angular.module('todo.controllers', ['ngResource']);

angular.module('todo.controllers').controller('TodoCtrl', function ($scope, $resource) {

    $scope.identity = $resource("/username").get();

    $scope.loaded = false;

    $scope.todos = [];

    var Task = $resource("/tasks/:taskId", {taskId: '@id'}, {'update': {method: 'PUT'}});

    Task.query(function(data) {
        $scope.todos = _.map(data, function(task) {
            return {id:task.id, text: task.label, done: task.done };
        });
        $scope.loaded = true;
    });

    $scope.getTodosCount = function () {
        return $scope.todos.length;
    };

    $scope.addTodo = function() {
        var task = new Task({label: $scope.formTodoText, done: false});
        task.$save(function(saved) {
            $scope.todos.push({id:saved.id, text: saved.label, done: saved.done });
            $scope.formTodoText = "";
        });
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

    $scope.update= function(todo) {
        Task.update({id: todo.id, label: todo.text, done: todo.done});
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
