app.service("uploadService", function ($http) {
    this.uploadPic = function () {
        var formData = new FormData();
        //参数一:提交值 提交到后端 ; 参数二:标签id值
        formData.append("file", file.files[0]);
        //angularJs需要结合html5完成 , FormData作为文件提交参数
        return $http({
            method: "post",
            url: "../upload/pic.do",
            data: formData,
            headers: {'content-Type': undefined}, //将会指定为formdata
            transformRequest: angular.identity    //对整个表单进行序列化
        })
    }
});