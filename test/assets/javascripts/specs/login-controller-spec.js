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
        expect(scope.submitting).toBe(false);
        scope.formData = {username: 'testuser', password: 'secret'};
        $httpBackend.expectPOST('/users/authenticate/userpass',
            {username: 'testuser', password: 'secret'})
            .respond(200, {firstName: 'Joe'});

        scope.setUsername();

        expect(scope.submitting).toBe(true);

        $httpBackend.flush();

        expect($rootScope.login.firstName).toBe('Joe');
        expect($location.url()).toBe('/tasks');
        expect(scope.submitting).toBe(false);
    }));

    it('should return any errors to the page', function () {
        expect(scope.submitting).toBe(false);
        var errors = {errors: {username: ['Email is garbled']}};
        scope.formData = {username: "garbled"};
        $httpBackend.expectPOST('/users/authenticate/userpass', scope.formData)
            .respond(400, errors);

        scope.setUsername();

        expect(scope.submitting).toBe(true);

        $httpBackend.flush();

        expect(scope.formData.errors).toEqual(errors);
        expect(scope.submitting).toBe(false);
    });

});
