'use strict';

angular.module('todo.controllers', ['ngResource']);

angular.module('todo.controllers').controller('TodoCtrl', function ($scope, $resource, $q) {

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

    var deferred1;

    $scope.makePromise1 = function() {
        deferred1 = $q.defer();
        $scope.fulfilledPromise = $q.all([deferred1.promise]);
    };

    var deferred2;

    $scope.makePromise2 = function() {
        deferred2 = $q.defer();
        $scope.fulfilledPromise = $q.all([deferred1.promise, deferred2.promise]);
    };

    $scope.fulfillPromise1 = function() {
        deferred1.resolve($scope.promisedData1);
    };

    $scope.fulfillPromise2 = function() {
        deferred2.resolve($scope.promisedData2);
    };
});
