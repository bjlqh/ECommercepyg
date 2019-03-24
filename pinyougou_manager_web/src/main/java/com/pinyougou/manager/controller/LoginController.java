package com.pinyougou.manager.controller;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {

    @RequestMapping("/user")
    public Map userLogin() {
        Map<String,String> map = new HashMap<>();
        //基于安全框架获取登录人信息
        SecurityContext context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        map.put("username",name);
        return map;
    }
}
