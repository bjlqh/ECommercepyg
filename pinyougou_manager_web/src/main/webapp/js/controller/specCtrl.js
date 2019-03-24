//定义控制器 参数一：控制器名称  参数二：控制器要做的事情
//$controller 实现angluarjs继承机制
app.controller("specCtrl", function ($scope, $controller, specService) {

    //语法参数:  参数一:继承的父控制器的名称  参数二:共享$scope配置
    $controller("baseController", {$scope: $scope});

    //条件分页查询
    $scope.searchEntity = {};
    $scope.search = function (pageNum, pageSize) {
        specService.search(pageNum, pageSize, $scope.searchEntity).success(function (pageResult) {
            $scope.paginationConf.totalItems = pageResult.total;
            $scope.list = pageResult.rows;
        })
    };


    //修改与新增的保存用的是同一个保存,判断是否有id如果有，就是修改；反之，就是新增。
    $scope.save = function () {
        var method = null;
        if ($scope.entity.tbSpecification.id == null) {
            //执行新增保存
            method = specService.add($scope.entity)
        } else {
            //执行修改保存
            method = specService.update($scope.entity)
        }
        method.success(function (result) {
            if (result.success) {
                $scope.reloadList();
            } else {
                alert(result.message)
            }
        })
    };

    //初始化规格和规格选项组合实体对象 {}
    $scope.entity = {options: [], tbSpecification: {}};
    //新增规格选项
    $scope.addTableRow = function () {
        $scope.entity.options.push({});
    };

    //删除规格选项
    $scope.deleTableRow = function (index) {
        $scope.entity.options.splice(index, 1);
    };

    //id查询品牌
    $scope.findOne = function (id) {
        specService.findOne(id).success(function (response) {
            $scope.entity = response;
        })
    };

    //批量删除
    $scope.dele = function () {
        if (confirm("确定要删除?")) {
            specService.dele($scope.ids).success(function (result) {
                if (result.success) {
                    $scope.reloadList();
                } else {
                    alert(result.message)
                }
            })
        }
    }
});