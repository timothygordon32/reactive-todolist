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

    it('should balk that the current password is incorrect', inject(function () {
        $httpBackend.expectPOST('/users/password',
            {currentPassword: 'incorrect', newPassword: {password1: 'secret', password2: 'secret'}})
            .respond(400, {currentPassword: "Incorrect password"});
        scope.formData.currentPassword = 'incorrect';
        scope.formData.newPassword = {
            password1: 'secret',
            password2: 'secret'
        };

        scope.change();
        $httpBackend.flush();

        expect(scope.formData.errors['currentPassword']).toBe("Incorrect password");
    }));


    it('should display the same error message for both new password fields', inject(function () {
        $httpBackend.expectPOST('/users/password',
            {currentPassword: 'correct', newPassword: {password1: 'secret', password2: 'doesnotmatch'}})
            .respond(400, {"newPassword.password1": "Does not match"});
        scope.formData.currentPassword = 'correct';
        scope.formData.newPassword = {
            password1: 'secret',
            password2: 'doesnotmatch'
        };

        scope.change();
        $httpBackend.flush();

        expect(scope.formData.errors['newPassword.password2']).toBe("Does not match");
    }));
});
