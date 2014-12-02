'use strict';

angular.module('todo.controllers', ['ngResource']);

angular.module('todo.controllers').controller('TodoCtrl', function ($scope, $resource, $http, $rootScope, $location) {

    $scope.loaded = false;

    $scope.todos = [];

    var Task = $resource("/tasks/:taskId", {taskId: '@id'}, {'update': {method: 'PUT'}});

    $scope.todos = Task.query(function () {
        $scope.loaded = true;
    });

    $scope.getTodosCount = function () {
        return $scope.todos.length;
    };

    $scope.addTodo = function () {
        var task = new Task({text: $scope.formTodoText, done: false});
        task.$save(function (saved) {
            $scope.todos.push(saved);
            $scope.formTodoText = "";
        });
    };

    $scope.getRemainingCount = function () {
        return $scope.getRemaining().length;
    };

    $scope.getRemaining = function () {
        return _.filter($scope.todos, function (todo) {
            return !todo.done;
        });
    };

    $scope.clearCompleted = function () {
        $http({
            method: 'DELETE',
            url: '/tasks/done'
        }).success(function (tasks) {
            $scope.todos = _.map(tasks, function (task) {
                return new Task(task);
            });
        });
    };

    $scope.update = function (task) {
        task.$update();
    };

    $scope.toggle = function (task) {
        task.done = !task.done;
        task.$update();
    };

    $scope.mute = function ($event) {
        $event.stopPropagation();
    };

    $scope.logoff = function () {
        $http({
            method: 'DELETE',
            url: '/login'
        }).success(function () {
            $rootScope.login = {};
            $location.path("/login");
        });
    };
});

angular.module('todo.controllers').controller('LoginController', function ($rootScope, $scope, $http, $location) {

    $scope.formData = {};

    $scope.setUsername = function () {
        $http.post('/users/authenticate/userpass', $scope.formData)
            .success(function (login) {
                $rootScope.login = login;
                $location.path("/tasks");
            })
            .error(function (errors) {
                $scope.formData.errors = errors;
            });
    }
});

angular.module('todo.controllers').controller('SignupController', function ($scope, $http, $location) {

    $scope.formData = {};

    $scope.setEmail = function () {
        $http.post('/users/signup', $scope.formData).success(function () {
            $location.path("/login");
        });
    }
});

angular.module('todo.controllers').controller('SignupVerifiedController', function ($scope, $http, $location, $routeParams) {

    $scope.formData = {};

    $scope.signUp = function () {
        $http.post('/users/signup/' + $routeParams.token, $scope.formData)
            .success(function () {
                $location.path("/login");
            }).error(function (response) {
                $scope.formData.errors = response;

                if (response["password"]) {
                    $scope.formData.errors["password.password2"] = response["password"];
                }
            });
    }
});

angular.module('todo.controllers').controller('ResetController', function ($scope, $http, $location, $routeParams) {

    $scope.formData = {};
});
