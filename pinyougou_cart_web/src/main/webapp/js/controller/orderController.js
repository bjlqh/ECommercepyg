app.controller("orderController", function ($scope, $controller, addressService, cartService, orderService) {
    $controller("baseController", {$scope: $scope});

    $scope.findCartList = function () {
        cartService.findCartList().success(function (response) {
            $scope.cartList = response;
            $scope.totalValue = {totalNum: 0, totalPrice: 0.00};
            sum();
        })
    };
    //合计实体
    $scope.totalValue = {totalNum: 0, totalPrice: 0.00};
    sum = function () {
        var cartList = $scope.cartList;
        for (var i = 0; i < cartList.length; i++) {
            var cart = cartList[i];   //购物车对象
            var orderItemList = cart.orderItemList;
            for (var j = 0; j < orderItemList.length; j++) {
                var orderItem = orderItemList[j]; //商品明细对象
                $scope.totalValue.totalNum += orderItem.num;
                $scope.totalValue.totalPrice += orderItem.totalFee;
            }
        }
    };

    $scope.findAddressByUserId = function () {
        addressService.findAddressByUserId().success(function (response) {
            $scope.addrList = response;

            for (var i = 0; i < response.length; i++) {
                if (response[i].isDefault === "1") {
                    $scope.address = response[i];
                    break;
                }
                //如果没有设置默认地址，给第一个收件人
                $scope.address = response[0];
            }

        })
    };
    //定义默认勾选地址
    $scope.address = null;

    //默认勾选效果
    $scope.isSelection = function (addr) {
        if ($scope.address === addr) {
            return true;
        } else {
            return false;
        }
    };
    //改变勾选效果
    $scope.selectAddress = function (addr) {
        //把当前对象作为默认勾选对象
        $scope.address = addr;
    };

    //定义订单对象
    $scope.entity = {paymentType: '1'};
    //支付方式
    $scope.selectedPaymentType = function (type) {
        $scope.entity.paymentType = type;
    };


    $scope.submitOrder = function () {
        //绑定数据
        //`receiver_area_name` varchar(100) COLLATE utf8_bin DEFAULT NULL COMMENT '收货人地区名称(省，市，县)街道',
        $scope.entity.receiverAreaName = $scope.address.address;
        //`receiver_mobile` varchar(12) COLLATE utf8_bin DEFAULT NULL COMMENT '收货人手机',
        $scope.entity.receiverMobile = $scope.entity.mobile;
        //`receiver` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT '收货人',
        $scope.entity.receiver = $scope.entity.contact;
        //`payment_type` varchar(1) COLLATE utf8_bin DEFAULT NULL COMMENT '支付类型，1、在线支付，2、货到付款',
        //提交订单
        orderService.add($scope.entity).success(function (response) {
            if (response.success) {
                location.href = "pay.html";
            } else {
                alert(response.message)
            }
        });
    }

});