app.controller("userController", function ($scope, $controller, userService) {
    $controller("baseController", {$scope: $scope});

    //登录人用户展示
    $scope.login = function () {
        userService.login().success(function (response) {
            $scope.username = response.username
        })
    }
});