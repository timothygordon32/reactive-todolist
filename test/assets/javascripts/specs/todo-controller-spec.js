'use strict';

describe('Todo controller', function () {
    var todoCtrl;
    var scope;
    var $httpBackend;

    beforeEach(module('todo.controllers'));

    beforeEach(inject(function(_$httpBackend_) {
        $httpBackend = _$httpBackend_;
    }));

    afterEach(function() {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
    });

    it('should be empty by default', inject(function ($controller, $rootScope) {
        $httpBackend.expectGET("/tasks").respond([
            {id: 1, text: 'task1', done: false},
            {id: 2, text: 'task2', done: false}
        ]);

        scope = $rootScope.$new();
        todoCtrl = $controller('TasksController', {
            $scope: scope
        });

        expect(scope.todos).toEqual([]); //notice that default state is not undefined, but empty!
        $httpBackend.flush();
        expect(scope.todos.length).toBe(2);
    }));

    it('should create a task', inject(function ($controller, $rootScope) {
        $httpBackend.expectGET("/tasks").respond([]);
        scope = $rootScope.$new();
        todoCtrl = $controller('TasksController', {
            $scope: scope
        });
        $httpBackend.flush();
        expect(scope.todos).toEqual([]);

        $httpBackend.expectPOST("/tasks", {text: 'text', done: false}).respond({id: 1, text: 'text', done: false});

        scope.formTodoText = "text";
        scope.addTodo();
        $httpBackend.flush();
        expect(scope.todos[0]).toEqual(jasmine.objectContaining({id:1, text:'text', done: false}));
    }));

    it('should update a task', inject(function($controller, $rootScope) {
        $httpBackend.expectGET("/tasks").respond([
            {id: 1, text: 'task1', done: false}
        ]);
        scope = $rootScope.$new();
        todoCtrl = $controller('TasksController', {
            $scope: scope
        });
        $httpBackend.flush();
        expect(scope.todos.length).toEqual(1);

        $httpBackend.expectPUT("/tasks/1", {id: 1, text: 'task1', done: true}).respond(204);
        scope.todos[0].done = true;
        scope.update(scope.todos[0]);
        $httpBackend.flush();
    }));

    it('should toggle the task "done" state', inject(function($controller, $rootScope) {
        $httpBackend.expectGET("/tasks").respond([
            {id: 1, text: 'task1', done: false}
        ]);
        scope = $rootScope.$new();
        todoCtrl = $controller('TasksController', {
            $scope: scope
        });
        $httpBackend.flush();
        expect(scope.todos.length).toEqual(1);

        $httpBackend.expectPUT("/tasks/1", {id: 1, text: 'task1', done: true}).respond(204);
        scope.toggle(scope.todos[0]);
        $httpBackend.flush();
    }));

    it('should delete completed tasks', inject(function($controller, $rootScope) {
        // Given
        $httpBackend.expectGET("/tasks").respond([
            {id: 1, text: 'task1', done: false}
        ]);
        scope = $rootScope.$new();
        todoCtrl = $controller('TasksController', {
            $scope: scope
        });
        $httpBackend.flush();
        // When
        scope.todos[0].done = true;
        // Note: redirect from DELETE /tasks/done to /tasks handled by browser and not visible to client JavaScript
        $httpBackend.expectDELETE("/tasks/done").respond(200, [
            {id: 1, text: 'task1', done: false}
        ]);
        scope.clearCompleted();
        // Then
        $httpBackend.flush();
        scope.$apply();
        expect(scope.todos.length).toBe(1);
        // And (check returned object still bound to correct REST URL)
        $httpBackend.expectPUT("/tasks/1", {id: 1, text: 'task1', done: true}).respond(204);
        scope.toggle(scope.todos[0]);
        $httpBackend.flush();
    }));

    it('should log the user off', inject(function($controller, $rootScope, $location) {
        // Given
        $httpBackend.expectGET("/tasks").respond([]);
        scope = $rootScope.$new();
        todoCtrl = $controller('TasksController', {
            $scope: scope
        });
        $httpBackend.flush();
        // When
        $httpBackend.expectDELETE("/login").respond(204);
        scope.logoff();
        // Then
        $httpBackend.flush();
        expect($rootScope.login).toEqual(jasmine.objectContaining({}));
        expect($location.url()).toBe('/login');
    }));

    it('should navigate to the change password view', inject(function ($controller, $rootScope, $location) {
        // Given
        $httpBackend.expectGET("/tasks").respond([]);
        scope = $rootScope.$new();
        todoCtrl = $controller('TasksController', {
            $scope: scope
        });
        $httpBackend.flush();
        // When
        scope.changePassword();
        scope.$apply();
        // Then
        expect($location.url()).toBe('/password');
    }));
});
