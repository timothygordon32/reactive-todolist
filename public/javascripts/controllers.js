'use strict';

angular.module('todo.controllers', ['ngResource']);

angular.module('todo.controllers').controller('TodoCtrl', function ($scope, $resource) {

    $scope.identity = $resource("/username").get();

    $scope.loaded = false;

    $scope.todos = [];

    var Task = $resource("/tasks/:taskId", {taskId: '@id'}, {'update': {method: 'PUT'}});

    $scope.todos = Task.query(function() {
        $scope.loaded = true;
    });

    $scope.getTodosCount = function () {
        return $scope.todos.length;
    };

    $scope.addTodo = function() {
        var task = new Task({text: $scope.formTodoText, done: false});
        task.$save(function(saved) {
            $scope.todos.push(saved);
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

    var getCompleted = function() {
        return _.filter($scope.todos, function (todo) {
            return todo.done;
        });
    };

    $scope.clearCompleted = function () {
        _.each(getCompleted(), function(completed) {
            completed.$delete(function() {
                $scope.todos = _.filter($scope.todos, function(task) {
                    return task.id != completed.id;
                });
            });
        });
    };

    $scope.update= function(task) {
        Task.update({id: task.id, text: task.text, done: task.done});
    };
});

angular.module('todo.controllers').controller('LandingCtrl', function ($rootScope, $scope, $http, $location) {

    $scope.formData = {};

    $scope.setUsername = function() {
        $http({
            method: 'POST',
            url: '/login',
            data: $scope.formData
        }).success(function () {
            $location.path("/tasks");
        });
    }
});
