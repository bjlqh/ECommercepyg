app.service("payService", function ($http) {

    //获取二维码请求地址
    this.createNative = function () {
        return $http.get("/SeckillPay/createNative.do");
    };

    //查询支付状态
    this.queryPayStatus = function (id) {
        return $http.get("/SeckillPay/queryPayStatus.do?id=" + id);
    };
});