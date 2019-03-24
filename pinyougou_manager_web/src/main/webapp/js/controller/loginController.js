app.controller("loginController", function ($scope, $controller, loginService) {
    $controller("baseController", {$scope: $scope});

    //登录人用户展示
    $scope.login = function () {
        loginService.login().success(function (response) {
            $scope.username = response.username
        })
    }
});