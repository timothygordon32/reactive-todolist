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
        $httpBackend.expectGET("/username").respond({username: 'testuser'});
        $httpBackend.expectGET("/tasks").respond([
            {id: 1, label: 'task1'},
            {id: 2, label: 'task2'}
        ]);

        scope = $rootScope.$new();
        todoCtrl = $controller('TodoCtrl', {
            $scope: scope
        });

        expect(scope.todos).toEqual([]); //notice that default state is not undefined, but empty!
        $httpBackend.flush();
        expect(scope.todos.length).toBe(2);
    }));

    it('should post a task', inject(function ($controller, $rootScope) {
        $httpBackend.expectGET("/username").respond({username: 'testuser'});
        $httpBackend.expectGET("/tasks").respond([]);
        scope = $rootScope.$new();
        todoCtrl = $controller('TodoCtrl', {
            $scope: scope
        });
        $httpBackend.flush();
        expect(scope.todos).toEqual([]);

        $httpBackend.expectPOST("/tasks", {label: 'label'}).respond({id: 1, label: 'label'});

        scope.formTodoText = "label";
        scope.addTodo();
        $httpBackend.flush();
        expect(scope.todos).toEqual([{text: 'label', done: false}]);
    }));
});
