'use strict';

describe('Login controller', function () {
    var scope;
    var loginCtrl;
    var $httpBackend;

    beforeEach(module('todo.controllers'));

    beforeEach(inject(function(_$httpBackend_, $controller, $rootScope) {
        $httpBackend = _$httpBackend_;

        scope = $rootScope.$new();
        loginCtrl = $controller('LandingCtrl', {
            $scope: scope
        });
    }));

    it('should submit the user credentials', inject(function($location) {
        $httpBackend.expectPOST('/login', 'username=testuser&password=secret', function(headers) {
            expect(headers['Content-Type']).toBe('application/x-www-form-urlencoded');
            return true;
        }).respond(200);

        scope.formData.username = 'testuser';
        scope.formData.password = 'secret';
        scope.setUsername();
        $httpBackend.flush();
        expect($location.url()).toBe('/tasks');
    }));
});
