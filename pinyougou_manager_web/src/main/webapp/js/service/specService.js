app.service("specService", function ($http) {
    //分页查询
    this.search = function (pageNum, pageSize,searchEntity) {
        return $http.post("../spec/search.do?pageNum=" + pageNum + "&pageSize=" + pageSize,searchEntity);
    };
    //添加
    this.add = function (specification) {
        return $http.post("../spec/addSpec.do",specification)
    };
    //修改
    this.update = function (specification) {
        return $http.post("../spec/updateSpec.do",specification)
    };
    //id查询
    this.findOne = function (id) {
        return $http.get("../spec/findOne.do?id=" + id)
    };
    //删除
    this.dele = function (ids) {
        return $http.get("../spec/delete.do?ids=" + ids)
    }
    //查询规格列表
    this.selectSpecList=function () {
        return $http.get("../spec/specList.do");
    }
});