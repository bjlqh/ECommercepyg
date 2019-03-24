app.controller("baseController", function ($scope) {

    //分页配置对象
    $scope.paginationConf = {
        currentPage: 1,  				//当前页
        totalItems: 10,					//总记录数
        itemsPerPage: 10,				//每页记录数
        perPageOptions: [10, 20, 30, 40, 50], //分页选项，下拉选择一页多少条记录
        onChange: function () {			//页面变更后触发的方法
            $scope.reloadList();		//启动就会调用分页组件
        }
    };
    $scope.reloadList = function () {
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    };

    //批量勾选
    $scope.ids = [];
    $scope.selection = function ($event, id) {
        if ($event.target.checked) {
            //加入数组
            $scope.ids.push(id);
        } else {
            //从数组中删除,首先获取元素的索引位置
            var index = $scope.ids.indexOf(id);
            $scope.ids.splice(index, 1);
        }
    };

    //获取对象的属性值,并拼接成字符串
    $scope.selectValueByKey = function (jsonString, key) {
        //将json字符串转成数组
        var value = "";
        var obList = JSON.parse(jsonString);
        for (var i = 0; i < obList.length; i++) {
            if (i === 0) {
                value += obList[i][key];
            } else {
                value += "," + obList[i][key];
            }
        }
        return value;
    }
});
