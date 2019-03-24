//控制层
app.controller('typeTemplateController', function ($scope, $controller, typeTemplateService, brandService, specService) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中
    $scope.findAll = function () {
        typeTemplateService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    };

    //分页条件查询
    $scope.findPage = function (page, rows) {
        typeTemplateService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    $scope.findOne = function (id) {
        typeTemplateService.findOne(id).success(
            function (response) {
                alert(response.brandIds);
                $scope.entity = response;
                $scope.entity.brandIds = JSON.parse(response.brandIds);
                $scope.entity.specIds = JSON.parse(response.specIds);
                $scope.entity.customAttributeItems = JSON.parse(response.customAttributeItems);
            }
        );
    };

    //保存
    $scope.save = function () {
        var serviceObject;//服务层对象
        if ($scope.entity.id != null) {//如果有ID
            serviceObject = typeTemplateService.update($scope.entity); //修改
        } else {
            serviceObject = typeTemplateService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    //重新查询
                    $scope.reloadList();//重新加载
                } else {
                    alert(response.message);
                }
            }
        );
    };


    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        if (confirm("确定要删除?")) {
            typeTemplateService.dele($scope.ids).success(
                function (response) {
                    if (response.success) {
                        $scope.reloadList();//刷新列表
                    }
                }
            );
        }
    };

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        typeTemplateService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    };

    $scope.brandList = {data: []};
    //$scope.brandList.data = [];
    //从品牌列表中获取品牌数据
    $scope.selectBrandList = function () {
        brandService.selectBrandList().success(function (response) {
            //$scope.brandList.data = response
            $scope.brandList = {data: response};
        })
    };

    $scope.specList = {data: []};
    $scope.selectSpecList = function () {
        specService.selectSpecList().success(function (response) {
            $scope.specList.data = response
        })
    };


    //增加行
    $scope.addTableRow = function () {
        $scope.entity.customAttributeItems.push({});
    };
    //删除行
    $scope.deleTableRow = function (index) {
        $scope.entity.customAttributeItems.splice(index, 1);
    };
});
