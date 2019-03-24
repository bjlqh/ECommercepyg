app.controller("itemPageController", function ($scope, $controller, $http) {
    $controller("baseController", {$scope: $scope});

    $scope.num = 1;
    //购买数量加减
    $scope.updateNum = function (num) {
        $scope.num = num;
        if (num > 0) {
            $scope.num = num;
        } else {
            $scope.num = 1;
        }
    };

    $scope.isSelected = function (specName, specOption) {
        if (spec[specName] === specOption) {
            return true;
        } else {
            return false;
        }
    };


    //判断某个规格是否被用户选中
    $scope.isSelected = function (specName, specOption) {
        if (spec[specName] === specOption) {
            return true;
        } else {
            return false;
        }
    };


    //用户选择规格,规格列表发生变化
    $scope.selectSpec = function (specName, specOption) {

        //改变spec
        spec[specName] = specOption;

        //遍历specList
        for (var i = 0; i < specList.length; i++) {
            if (matchMap(specList[i].spec, spec)) {
                location.href = specList[i].id + ".html";
            }
        }
    };

    matchMap = function (map1, map2) {

        //遍历map语法
        for (var k in map1) {
            if (map1[k] != map2[k]) {
                return false;
            }
        }
        for (var k in map2) {
            if (map1[k] != map2[k]) {
                return false;
            }
        }
        return true;
    }

    //加入购物车
    $scope.addItemToCartList = function () {
        $http.get('http://localhost:8087/cart/addItemToCartList.do?itemId=' + itemId + "&num=" + $scope.num).success(function () {
            if (response.success) {
                location.href = "http://localhost:8087/cart.html";
            } else {
                alert(response.message);
            }
        })
    }
});