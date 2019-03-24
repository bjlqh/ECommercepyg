app.controller("cartController", function ($scope, $controller, cartService) {
    $controller("baseController", {$scope: $scope});

    $scope.addGoodsToCart = function (itemId, num) {
        cartService.addGoodsToCart(itemId, num).success(function (response) {
            if (response.success) {
                $scope.findCartList();
            } else {
                alert(response.message)
            }
        })
    };
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
    }

});