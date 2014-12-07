'use strict';

describe('Change password controller', function () {
    var scope;
    var controller;
    var $httpBackend;

    beforeEach(module('todo.controllers'));

    beforeEach(inject(function (_$httpBackend_, $controller, $rootScope) {
        $httpBackend = _$httpBackend_;

        scope = $rootScope.$new();
        controller = $controller('ChangePasswordController', {
            $scope: scope
        });
    }));
});
