package com.pinyougou.user.controller;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class LoginController {

    @RequestMapping("/username")
    public Map userLogin() {
        Map<String,String> map = new HashMap<>();
        SecurityContext context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        map.put("username",name);
        return map;
    }
}
