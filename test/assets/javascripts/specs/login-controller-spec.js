'use strict';

describe('Login controller', function () {
    var scope;
    var loginCtrl;
    var $httpBackend;

    beforeEach(module('todo.controllers'));

    beforeEach(inject(function (_$httpBackend_, $controller, $rootScope) {
        $httpBackend = _$httpBackend_;

        scope = $rootScope.$new();
        loginCtrl = $controller('LoginController', {
            $scope: scope
        });
    }));

    it('should submit the user credentials', inject(function ($location, $rootScope) {
        $httpBackend.expectPOST('/users/authenticate/userpass',
            {username: 'testuser', password: 'secret'})
            .respond(200, {firstName: 'Joe'});

        scope.formData.username = 'testuser';
        scope.formData.password = 'secret';
        scope.setUsername();
        $httpBackend.flush();
        expect($rootScope.login.firstName).toBe('Joe');
        expect($location.url()).toBe('/tasks');
    }));
});
