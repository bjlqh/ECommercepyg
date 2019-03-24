package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.utils.CookieUtil;
import entity.Result;
import entitygroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    private CartService cartService;
    @Autowired
    private HttpSession session;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    /**
     * 设置sessionID基于cookie保存一周
     */
    private String getSession() {
        //尝试基于cookie名称取sessionID
        String sessionId = CookieUtil.getCookieValue(request, "cartCookie", "utf-8");
        if (sessionId == null) {
            //如果找不到sessionId就创建一个
            sessionId = session.getId();
            //基于cookie保存sessionId一周
            CookieUtil.setCookie(request, response, "cartCookie", sessionId, 86400 * 7, "utf-8");
        }
        return sessionId;
    }

    @RequestMapping("/addItemToCartList")
    @CrossOrigin(origins = "http://item.pinyougou.com", allowCredentials = "true")
    public Result addItemToCartList(Long itemId, Integer num) {
        try {
            List<Cart> cartList = findCartList();
            cartList = cartService.addItemToCartList(itemId, num, cartList);
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            String key = getSession();
            if (!"anonymousUser".equals(username)) {
                //已登录
                key = username;
            }
            cartService.addCartListToRedis(key, cartList);
            return new Result(true, "成功添加到购物车");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return new Result(false, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(true, "添加到购物车失败");
        }
    }

    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {
        //判断是否有用户登录
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String sessionId = getSession();
        List<Cart> cartList_sessionId = cartService.findCartListFromRedis(sessionId);
        if ("anonymousUser".equals(username)) {
            //未登录
            return cartList_sessionId;
        }
        //获取登录者的购物车数据
        List<Cart> cartList = cartService.findCartListFromRedis(username);
        if (cartList_sessionId.size() > 0) {
            //合并购物车
            cartList = cartService.mergeCartList(cartList, cartList_sessionId);
            //清除redis中的数据
            cartService.deleCartListFromRedis(sessionId);
            //将购物车存储redis中
            cartService.addCartListToRedis(username, cartList);
        }
        return cartList;
    }
}
