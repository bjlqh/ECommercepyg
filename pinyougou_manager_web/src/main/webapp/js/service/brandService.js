app.service("brandService", function ($http) {
    //查询所有
    this.findAll=function(){
        return $http.get('../brand/findAll.do');
    }
    //分页查询
    this.search = function (pageNum, pageSize,searchEntity) {
        return $http.post("../brand/findPage.do?pageNum=" + pageNum + "&pageSize=" + pageSize,searchEntity);
    };
    //添加
    this.add = function (entity) {
        return $http.post("../brand/addBrand.do",entity)
    };
    //修改
    this.update = function (entity) {
        return $http.post("../brand/updateBrand.do",entity)
    };
    //id查询
    this.findOne = function (id) {
        return $http.get("../brand/findById.do?id=" + id)
    };
    //删除
    this.delete = function (ids) {
        return $http.get("../brand/delete.do?ids=" + ids)
    }
    //查询品牌列表
    this.selectBrandList=function () {
        return $http.get("../brand/brandList.do");
    }
});