//控制层
app.controller('registerController', function ($scope, $controller, registerService) {

    $controller('baseController', {$scope: $scope});//继承

    //新增
    $scope.reg = function () {
        if ($scope.entity.password === $scope.repassword) {
            registerService.add($scope.smsCode, $scope.entity).success(function (response) {
                if (response.success) {
                    location.href = "login.html"
                } else {
                    alert(response.message)
                }
            })
        }else {
            alert("密码不一致,请重新输入");
        }
    };

    //发送短信验证码
    $scope.sendCode=function () {
        registerService.sendCode($scope.entity.phone).success(function (response) {
            alert(response.message)
        })
    }

});	
