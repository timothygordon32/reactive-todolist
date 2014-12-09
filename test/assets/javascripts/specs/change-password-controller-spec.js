'use strict';

describe('Change password controller', function () {
    var scope;
    var controller;
    var $httpBackend;

    beforeEach(module('todo.controllers'));

    beforeEach(inject(function (_$httpBackend_, $controller, $rootScope) {
        $httpBackend = _$httpBackend_;

        scope = $rootScope.$new();
        controller = $controller('PasswordController', {
            $scope: scope
        });
    }));

    it('should change the user password', inject(function ($location) {
        $httpBackend.expectPOST('/users/password',
            {currentPassword: 'current', newPassword: {password1: 'secret', password2: 'secret'}})
            .respond(200);
        scope.formData.currentPassword = 'current';
        scope.formData.newPassword = {
            password1: 'secret',
            password2: 'secret'
        };

        scope.change();
        $httpBackend.flush();

        expect($location.url()).toBe('/tasks');
    }));

});
