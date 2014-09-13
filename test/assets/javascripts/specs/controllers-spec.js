'use strict';

describe('controllers', function () {
    var scope;
    var todoCtrl;
    var $httpBackend;

    beforeEach(module('todo.controllers'));

    beforeEach(inject(function(_$httpBackend_, $controller, $rootScope) {
        $httpBackend = _$httpBackend_;
        $httpBackend.expectGET("/json/tasks").
            respond([{id: 1, label: 'task1'}, {id: 2, label: 'task2'}]);


        scope = $rootScope.$new();
        todoCtrl = $controller('TodoCtrl', {
            $scope: scope
        });
    }));

    it('should be empty by default', function () {
        expect(scope.todos).toEqual([]); //notice that default state is not undefined, but empty!
        $httpBackend.flush();
        expect(scope.todos.length).toBe(2);
    });
});
