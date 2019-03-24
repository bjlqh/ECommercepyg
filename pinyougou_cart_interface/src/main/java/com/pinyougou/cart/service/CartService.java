package com.pinyougou.cart.service;

import entitygroup.Cart;

import java.util.List;

public interface CartService {
    /**
     * 根据商品信息把商品存入购物车列表中
     */
    List<Cart> addItemToCartList(Long itemId, Integer num, List<Cart> cartList);

    /**
     * 根据登录用户名从redis查询购物车集合
     *
     * @param sessionId
     * @return
     */
    List<Cart> findCartListFromRedis(String sessionId);

    /**
     * 根据登录的用户名将购物车添加到redis中
     *
     * @param key
     * @param cartList
     */
    void addCartListToRedis(String key, List<Cart> cartList);

    /**
     * 合并购物车
     * @param cartList_username
     * @param cartList_sessionId
     * @return
     */
    List<Cart> mergeCartList(List<Cart> cartList_username, List<Cart> cartList_sessionId);

    /**
     * 删除redis 中的购物车数据
     * @param sessionId
     */
    void deleCartListFromRedis(String sessionId);
}
