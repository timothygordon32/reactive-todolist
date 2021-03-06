'use strict';

angular.module('todo.controllers', ['ngResource']);

angular.module('todo.controllers').controller('TasksController', function ($scope, $resource, $http, $rootScope, $location) {

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

    $scope.changePassword = function () {
        $location.path("/password");
    };
});

angular.module('todo.controllers').controller('LoginController', function ($rootScope, $scope, $http, $location, form) {

    $scope.formData = {};

    $scope.setUsername = function () {
        form.submission($scope, $http.post('/users/authenticate/userpass', $scope.formData))
            .success(function (login) {
                $rootScope.login = login;
                $location.path("/tasks");
            })
            .error(function (errors) {
                $scope.formData.errors = errors;
            });
    }
});

angular.module('todo.controllers').service('form', function() {
    return {
        submission: function (scope, method) {
            var self = this;

            var successAndError = {
                apply: function () {
                    if (successAndError.success && successAndError.error) {

                        scope.submitting = true;

                        method
                            .success(function () {
                                successAndError.success.apply(self, arguments);
                                scope.submitting = false;
                            })
                            .error(function () {
                                successAndError.error.apply(self, arguments);
                                scope.submitting = false;
                            });
                    }
                }
            };

            var result = {
                success: function (success) {
                    successAndError.success = success;
                    successAndError.apply();
                    return result;
                },
                error: function (error) {
                    successAndError.error = error;
                    successAndError.apply();
                    return result;
                }
            };

            return result;
        }
    };
});

angular.module('todo.controllers').controller('SignupController', function ($scope, $http, form) {

    $scope.formData = {};

    $scope.emailSent = false;

    $scope.sendEmail = function () {
        form.submission($scope, $http.post('/users/signup', $scope.formData))
            .success(function () {
                $scope.emailSent = true;
            }).error(function (errors) {
                $scope.formData.errors = errors;
            });
    };
});

angular.module('todo.controllers').controller('SignupVerifiedController', function ($scope, $http, $location, $routeParams, form) {

    $scope.formData = {};

    $scope.signUp = function () {
        form.submission($scope, $http.post('/users/signup/' + $routeParams.token, $scope.formData))
            .success(function () {
                $location.path("/login");
            })
            .error(function (response) {
                $scope.formData.errors = response;

                if (response["password"]) {
                    $scope.formData.errors["password.password2"] = response["password"];
                }
            });
    }
});

angular.module('todo.controllers').controller('ResetController', function ($scope, $http, form) {

    $scope.formData = {};

    $scope.emailSent = false;

    $scope.sendEmail = function () {
        form.submission($scope, $http.post('/users/reset', $scope.formData))
            .success(function () {
                $scope.emailSent = true;
            })
            .error(function (errors) {
                $scope.formData.errors = errors;
            });
    };
});

angular.module('todo.controllers').controller('ResetVerifiedController', function ($scope, $http, $location, $routeParams, form) {

    $scope.formData = {};

    $scope.reset = function () {
        form.submission($scope, $http.post('/users/reset/' + $routeParams.token, $scope.formData))
            .success(function () {
                $location.path('/login');
            })
            .error(function (errors) {
                $scope.formData.errors = errors;
            });
    };
});

angular.module('todo.controllers').controller('PasswordController', function ($scope, $http, $location, form) {

    $scope.formData = {};

    $scope.change = function () {
        form.submission($scope, $http.post('/users/password', $scope.formData))
            .success(function () {
                $location.path('/tasks');
            })
            .error(function (errors) {
                $scope.formData.errors = errors;
                if (errors["newPassword.password1"]) {
                    $scope.formData.errors["newPassword.password2"] = errors["newPassword.password1"];
                }
            });
    };
});
