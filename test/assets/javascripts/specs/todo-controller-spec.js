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
            {id: 1, text: 'task1', done: false},
            {id: 2, text: 'task2', done: false}
        ]);

        scope = $rootScope.$new();
        todoCtrl = $controller('TodoCtrl', {
            $scope: scope
        });

        expect(scope.todos).toEqual([]); //notice that default state is not undefined, but empty!
        $httpBackend.flush();
        expect(scope.todos.length).toBe(2);
    }));

    it('should create a task', inject(function ($controller, $rootScope) {
        $httpBackend.expectGET("/username").respond({username: 'testuser'});
        $httpBackend.expectGET("/tasks").respond([]);
        scope = $rootScope.$new();
        todoCtrl = $controller('TodoCtrl', {
            $scope: scope
        });
        $httpBackend.flush();
        expect(scope.todos).toEqual([]);

        $httpBackend.expectPOST("/tasks", {text: 'text', done: false}).respond({id: 1, text: 'text', done: false});

        scope.formTodoText = "text";
        scope.addTodo();
        $httpBackend.flush();
        // Todo would prefer to use Jasmine 2
//        expect(scope.todos[0]).toEqual(jasmine.objectContaining({id:1, text:'text', done: false}));
        expect(scope.todos[0].id).toEqual(1);
        expect(scope.todos[0].text).toEqual('text');
        expect(scope.todos[0].done).toEqual(false);
    }));

    it('should update a task', inject(function($controller, $rootScope) {
        $httpBackend.expectGET("/username").respond({username: 'testuser'});
        $httpBackend.expectGET("/tasks").respond([
            {id: 1, text: 'task1', done: false}
        ]);
        scope = $rootScope.$new();
        todoCtrl = $controller('TodoCtrl', {
            $scope: scope
        });
        $httpBackend.flush();
        expect(scope.todos.length).toEqual(1);

        $httpBackend.expectPUT("/tasks/1", {id: 1, text: 'task1', done: true}).respond(204);
        scope.update({id: 1, text: 'task1', done: true});
        $httpBackend.flush();
    }));
});
