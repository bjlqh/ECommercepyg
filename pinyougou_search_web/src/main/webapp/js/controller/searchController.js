//控制层
app.controller('searchController', function ($scope, $controller, searchService) {

    $controller('baseController', {$scope: $scope});//继承

    $scope.searchMap = {
        keywords: "",
        category: "",
        brand: "",
        price: "",
        spec: {},
        sort: "ASC",
        sortField: "",
        pageNo: 1,
        pageSize: 60
    };
    $scope.resultMap = {};
    //搜索
    $scope.search = function () {
        searchService.search($scope.searchMap).success(function (response) {
            $scope.resultMap = response;

            buildPageLabel();
        })
    };

    //添加条件查询
    $scope.addFilterCondition = function (key, value) {

        if ("brand" === key || "category" === key || "price" === key) {
            $scope.searchMap[key] = value;
        } else {
            //分装规格数据
            $scope.searchMap.spec[key] = value;
        }
        $scope.search();
    };

    //移除查询条件
    $scope.removeSearchItem = function (key) {

        if ("brand" === key || "category" === key || "price" === key) {
            $scope.searchMap[key] = "";
        } else {
            //分装规格数据
            delete $scope.searchMap.spec[key];
        }
        $scope.search();
    };

    //页面排序
    $scope.sortSearch = function (sortField, sort) {

        //价格排序
        if (sortField === "price") {
            $scope.searchMap.sortField = sortField;
            $scope.searchMap.sort = sort;
        }
        //新品排序
        if (sortField === "updatetime") {
            $scope.searchMap.sortField = sortField;
            $scope.searchMap.sort = sort;
        }

        $scope.search();
    };

    //构建分页工具条代码
    buildPageLabel = function () {
        //1. 新增分页栏属性
        $scope.pageLabel = [];
        //2. 得到最后的页码
        var maxPageNo = $scope.resultMap.totalPages;
        //3.定义省略号属性
        $scope.firstDot = true;
        $scope.lastDot = true;

        var firstPage = 1;
        var lastPage = maxPageNo;

        //4.根据总页数和当前页判断页码显示数
        if (maxPageNo > 5) {
            //4.1总数大于5页
            if ($scope.searchMap.pageNo <= 3) {
                //4.2当前页小于等于3，没有前...
                $scope.firstDot = false;
                lastPage = 5;
            } else if ($scope.searchMap.pageNo + 2 >= maxPageNo) {
                //4.2当前页+2大于等于总页数，没有后...
                $scope.lastDot = false;
                firstPage = maxPageNo - 4;
            } else {
                //4.2 都有...
                $scope.firstDot = true;
                $scope.lastDot = true;

                firstPage = $scope.searchMap.pageNo - 2;
                lastPage = $scope.searchMap.pageNo + 2;
            }
        } else {
            //4.1总数小于等于5页
            $scope.firstDot = false;
            $scope.lastDot = false;
        }
        //5.循环产生页码标签
        for (var i = firstPage; i <= lastPage; i++) {
            $scope.pageLabel.push(i)
        }

    };

    //分页查询
    $scope.queryForPage = function (pageNo) {

        $scope.searchMap.pageNo = parseInt(pageNo);
        $scope.search();
    }
    //分页页码展示逻辑
    //1.如果分页不足5页,全部展示
    //2.分页超过5页。省略号的展示位置有三种

    //判断是是第一页,禁用《上一页》
    $scope.isTopPage = function () {
        if ($scope.searchMap.pageNo === 1) {
            return true;
        } else {
            return false;
        }
    };
    //是最后一页,禁用《下一页》
    $scope.isLastPage = function () {
        if ($scope.searchMap.pageNo === $scope.resultMap.totalPages) {
            return true;
        } else {
            return false;
        }
    }

});	
