//服务层
app.service('registerService', function ($http) {
    //新增
    this.add = function (smsCode, entity) {
        return $http.post("/user/add.do?smsCode=" + smsCode, entity)
    }

    //给手机发送短信验证
    this.sendCode = function (phone) {
        return $http.get('/user/sendCode.do?phone=' + phone)
    }
});
