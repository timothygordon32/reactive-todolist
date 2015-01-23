'use strict';

describe('Reset controller', function () {
    var scope;
    var loginCtrl;
    var $httpBackend;

    beforeEach(module('todo.controllers'));

    beforeEach(inject(function (_$httpBackend_, $controller, $rootScope, $location) {
        $httpBackend = _$httpBackend_;

        scope = $rootScope.$new();
        loginCtrl = $controller('ResetController', {
            $scope: scope
        });
    }));

    it('should submit the reset request', inject(function ($location) {
        scope.formData = {email: "user@nomail.com"};
        $httpBackend.expectPOST('/users/reset', scope.formData)
            .respond(204);

        scope.sendEmail();
        $httpBackend.flush();

        expect(scope.emailSent).toBe(true);
    }));

    it('should return any errors to the page', inject(function ($location) {
        var errors = {errors: {email: ['Email is garbled']}};
        scope.formData = {email: "garbled"};
        $httpBackend.expectPOST('/users/reset', scope.formData)
            .respond(400, errors);

        scope.sendEmail();
        $httpBackend.flush();

        expect(scope.formData.errors).toEqual(errors);
        expect(scope.emailSent).toBe(false);
    }));
});
