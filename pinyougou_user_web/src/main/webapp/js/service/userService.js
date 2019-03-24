app.service("userService",function ($http) {
    this.login=function () {
        return $http.get("../user/username.do")
    }
});