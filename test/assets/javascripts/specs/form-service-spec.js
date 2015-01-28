'use strict';

describe('Form service', function () {
    var scope;
    var formService;
    var $httpBackend;

    beforeEach(module('todo.controllers'));

    beforeEach(inject(function (_$httpBackend_, form, $rootScope) {
        $httpBackend = _$httpBackend_;

        scope = $rootScope.$new();
        formService = form;
    }));

    it('should indicate submitting during a successful send of the email', inject(function ($http) {
        expect(scope.emailSent).toBe(undefined);
        expect(scope.submitting).toBe(undefined);
        scope.formData = {email: "user@nomail.com"};
        $httpBackend.expectPOST('/users/signup', scope.formData)
            .respond(204);

        formService.submission(scope, $http.post('/users/signup', scope.formData))
            .success(function () {
                scope.emailSent = true;
            }).error(function (errors) {
                scope.formData.errors = errors;
            });

        expect(scope.submitting).toBe(true);

        $httpBackend.flush();

        expect(scope.submitting).toBe(false);
        expect(scope.emailSent).toBe(true);
    }));

    it('should indicate submitting during a failure to send the email', inject(function ($http) {
        expect(scope.submitting).toBe(undefined);
        var errors = {errors: {email: ['Email is garbled']}};
        scope.formData = {email: "garbled"};
        $httpBackend.expectPOST('/users/signup', scope.formData)
            .respond(400, errors);

        formService.submission(scope, $http.post('/users/signup', scope.formData))
            .success(function () {
                scope.emailSent = true;
            }).error(function (errors) {
                scope.formData.errors = errors;
            });

        expect(scope.submitting).toBe(true);

        $httpBackend.flush();

        expect(scope.submitting).toBe(false);
        expect(scope.formData.errors).toEqual(errors);
    }));
});
