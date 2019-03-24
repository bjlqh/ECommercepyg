app.service("cartService", function ($http) {
    //购物车列表
    this.findCartList = function () {
        return $http.get("/cart/findCartList.do")
    };
    //添加商品数量
    this.addGoodsToCart = function (itemId, num) {
        return $http.get('/cart/addItemToCartList.do?itemId=' + itemId + "&num=" + num)
    }

});