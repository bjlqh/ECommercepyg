app.controller("payController", function ($scope, $controller,$location,payService) {
    $controller("baseController", {$scope: $scope});

    //生成二维码
    $scope.createNative = function () {
        payService.createNative().success(function (response) {
            //订单号
            $scope.out_trade_no = response.out_trade_no;
            //金额
            $scope.totalFee = (response.total_fee / 100).toFixed(2);
            //二维码
            new QRious({
                element: document.getElementById("qrious"),
                size: 300,
                level: "H",
                value: response.code_url
            });

            $scope.queryPayStatus(response.out_trade_no);
        })
    };

    //查询支付状态
    $scope.queryPayStatus = function (out_trade_no) {
        payService.queryPayStatus(out_trade_no).success(function (response) {
            if (response.success) {
                location.href = "paysuccess.html#?money=" + $scope.totalFee;
            } else {
                if (response.message === "timeout") {
                    $scope.createNative();
                } else {
                    location.href = "payfail.html";
                }
            }
        })
    };

    //传递支付金额
    $scope.getMoney=function () {
        return $location.search()["money"];
    };
});