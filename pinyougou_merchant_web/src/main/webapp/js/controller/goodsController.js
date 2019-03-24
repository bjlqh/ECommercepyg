//控制层
app.controller('goodsController', function ($scope, $controller, goodsService, itemCatService, typeTemplateService, uploadService) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        goodsService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    };

    //分页
    $scope.findPage = function (page, rows) {
        goodsService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    };

    //查询实体
    $scope.findOne = function (id) {
        goodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;
            }
        );
    };

    //保存
    $scope.save = function () {
        $scope.entity.goodsDesc.introduction = edior.html();
        goodsService.add($scope.entity).success(function (response) {
            if (response.success) {
                $scope.entity = {};//清空对象
                edior.html("");
            } else {
                alert(response.message);
            }
        });
    };


    //批量删除
    $scope.dele = function () {
        if (confirm("确认是否删除")) {
            //获取选中的复选框
            goodsService.dele($scope.ids).success(
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
        goodsService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    };
    $scope.status = ['未审核', '已审核', '审核未通过', '关闭'];

    //商品分类列表
    $scope.itemCatList = [];
    //查询所有商品分类列表
    $scope.findItemCatList = function () {
        itemCatService.findAll().success(function (response) {
            for (var i = 0; i < response.length; i++) {
                //id 对应 name
                $scope.itemCatList[response[i].id] = response[i].name;
            }
        })
    };


    $scope.entity = {goods: {isEnableSpec: "1"}, goodsDesc: {itemImages: [], specificationItems: []}, itemList: []};

    //查询分类列表,三级联动
    $scope.selectItemCatList1 = function () {
        itemCatService.findByParentId(0).success(function (response) {
            $scope.ItemCat1List = response;
        })
    };
    $scope.$watch("entity.goods.category1Id", function (newValue, oldValue) {
        itemCatService.findByParentId(newValue).success(function (response) {
            $scope.ItemCat2List = response;
            $scope.entity.goods.category3Id = {};
            $scope.entity.goods.typeTemplateId = "";
            $scope.brandList = {};
            $scope.entity.goodsDesc.customAttributeItems = "";
        })
    });
    $scope.$watch("entity.goods.category2Id", function (newValue, oldValue) {
        itemCatService.findByParentId(newValue).success(function (response) {
            $scope.ItemCat3List = response;
            $scope.entity.goods.typeTemplateId = "";
            $scope.brandList = {};
            $scope.entity.goodsDesc.customAttributeItems = "";
        })
    });
    //查询模板id(typeId)
    $scope.$watch("entity.goods.category3Id", function (newValue, oldValue) {
        itemCatService.findOne(newValue).success(function (response) {
            $scope.entity.goods.typeTemplateId = response.typeId;
            $scope.brandList = {};
            $scope.entity.goodsDesc.customAttributeItems = "";
        })
    });
    //查询品牌、模板扩展属性、规格及选项
    $scope.$watch("entity.goods.typeTemplateId", function (newValue, oldValue) {
        typeTemplateService.findOne(newValue).success(function (response) {
            var jsonBrand = response.brandIds;
            $scope.brandList = JSON.parse(jsonBrand);
            var jsonCustom = response.customAttributeItems;
            $scope.entity.goodsDesc.customAttributeItems = JSON.parse(jsonCustom);
        });

        if (newValue != null && newValue !== "") {
            typeTemplateService.findSpecList(newValue).success(function (response) {
                $scope.specList = response;
            });
        }
    });


    //上传图片
    $scope.upload = function () {
        alert("图片将上传");
        uploadService.uploadPic().success(function (response) {
            if (response.success) {
                //获取url给图片框展示
                $scope.entity_image.url = response.message;
            } else {
                alert(response.message)
            }
        })
    };


    //1.将商品的组合实体类初始化
    //$scope.entity = {goods: {}, goodsDesc: {itemImages: []}, itemList: []};
    //2.保存图片时将图片添加到图片列表中
    $scope.addImage = function () {
        $scope.entity.goodsDesc.itemImages.push($scope.entity_image);
    };
    //3.删除图片列表中的对象
    $scope.deleteImage = function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index, 1)
    };


    //获取规格选项勾选|取消勾选的结果集
    /*[{"attributeName":"网络","attributeValue":["移动3G","移动4G"]},{"attributeName":"屏幕尺寸","attributeValue":["6寸","5寸"]}]*/
    $scope.updateSpecAttribute = function ($event, specName, specOption) {

        //1.判断能否在数组中找到attributeName

        var object = $scope.getObjectByAttrName($scope.entity.goodsDesc.specificationItems, "attributeName", specName);

        //2.判断集合里面是否有返回的对象
        if (object != null) {
            if ($event.target.checked) {
                //2.1勾选添加到attributeValue数组里面
                object.attributeValue.push(specOption);
            } else {
                //2.2取消勾选则从attributeValue中删除
                object.attributeValue.splice(object.attributeValue.indexOf(specOption), 1);
                //2.2都取消了就删除该对象
                if (object.attributeValue.length === 0) {
                    $scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(object), 1);
                }
            }
        } else {
            //3.集合中没有该对象,就添加一个对象
            $scope.entity.goodsDesc.specificationItems.push({attributeName: specName, attributeValue: [specOption]})
        }
    }

    //点击复选框选择规格选项时,生成SKU列表
    $scope.createItemList = function () {
        //1.初始化列表
        $scope.entity.itemsList = [{spec: {}, price: 0, num: 9999, status: '1', isDefault: '0'}];
        /**[{"attributeName":"网络","attributeValue":["移动3G","移动4G"]},{"attributeName":"机身内存","attributeValue":["16G","32G"]}]
         * entity.itemsList = [{spec: {}, price: 0, num: 9999, status: 0, isDefault: 0}]
         */

            //2.循环选择的规格,根据规格名称和已选择的规格选项对列表进行扩充
        var item = $scope.entity.goodsDesc.specificationItems;
        if (item != null) {
            for (var i = 0; i < item.length; i++) {
                //获取
                $scope.entity.itemsList = addColumn($scope.entity.itemsList, item[i].attributeName, item[i].attributeValue)
            }
        } else {
            $scope.entity.itemsList = [];
        }
    };

    //添加列值
    addColumn = function (list, specName, specOptions) {
        var newList = [];
        //1.遍历集合list，获取spec属性
        for (var i = 0; i < list.length; i++) {
            //1.1 获取list里面的一条数据
            var oldItem = list[i];  //list[i] = {spec: {}, price: 0, num: 9999, status: 0, isDefault: 0};  目的是要往list里面的spec属性里面添加数组
            for (var j = 0; j < specOptions.length; j++) {
                //1.1.1遍历规格选项,为spec赋值 -----> 属性名=属性值
                //item.spec[specName] = specOptions[j]; 浅克隆不能追加
                var newItem = JSON.parse(JSON.stringify(oldItem));
                newItem.spec[specName] = specOptions[j];
                newList.push(newItem);
            }
        }
        return newList;
    }
    $scope.Marketable = ["下架", "上架"];
    $scope.updateIsMarketable = function (isMarketable) {
        goodsService.updateIsMarketable($scope.ids, isMarketable).success(function (response) {
            if (response.success) {
                $scope.reloadList();
            } else {
                alert(response.message)
            }
        })
    }
    //点击设置商品状态
    //item中status=1时，代表 启用， status=0时，代表未启用
    //item中isDefault=1时，代表 是默认商品， isDefault=0时，代表非默认商品
    /*$scope.updateIsDefault = function ($event, index) {
        if ($event.target.checked) {
            $scope.entity.itemsList[index].isDefault = 1;
            alert($scope.entity.itemsList[index].isDefault)
        } else {
            $scope.entity.itemsList[index].isDefault = 0;
        }
    }*/


});
