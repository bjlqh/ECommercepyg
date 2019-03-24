package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import entitygroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public List<Cart> addItemToCartList(Long itemId, Integer num, List<Cart> cartList) {
        //1.获取商家的id
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item == null) {
            throw new RuntimeException("商品不存在");
        }
        String sellerId = item.getSellerId();
        //2.判断商家是否在购物车列表中(抽取一个方法)
        Cart cart = judgeCartBySellerId(sellerId, cartList);
        //3.判断该商家购物车对象是否存在
        if (cart == null) {
            //商家购物车对象不存在，创建购物车
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());
            //创建购物车明细表
            List<TbOrderItem> orderItemList = new ArrayList<>();
            //创建购物车明细对象
            TbOrderItem orderItem = createOrderItem(item, num);
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            //将购物车对象添加到购物车列表中
            cartList.add(cart);
        } else {
            //商家购物车对象存在,判断该商品是否存在明细列表中
            List<TbOrderItem> orderItemList = cart.getOrderItemList();
            TbOrderItem tbOrderItem = judgeOrderItemByItemId(orderItemList, itemId);
            if (tbOrderItem == null) {
                // 不存在该商品,创建商品明细对象，将其添加到购物车明细列表
                tbOrderItem = createOrderItem(item, num);
                orderItemList.add(tbOrderItem);
            } else {
                //商品存在于明细列表中，修改数量和商品总金额
                tbOrderItem.setNum(tbOrderItem.getNum() + num);
                tbOrderItem.setTotalFee(new BigDecimal(tbOrderItem.getPrice().doubleValue() * tbOrderItem.getNum()));
                //如果商品数量减到0,则删除对应的明细列表
                if (tbOrderItem.getNum() <= 0) {
                    orderItemList.remove(tbOrderItem);
                }
                //如果商家的商品数量为0,则从购物车列表中删除商家
                if (orderItemList.size() <= 0) {
                    cartList.remove(cart);
                }
            }
        }
        return cartList;
    }

    private TbOrderItem judgeOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem tbOrderItem : orderItemList) {
            if (itemId.longValue() == tbOrderItem.getItemId().longValue()) {
                //商品已存在返回商品明细对象
                return tbOrderItem;
            }
        }
        //商品不存在
        return null;
    }

    private Cart judgeCartBySellerId(String sellerId, List<Cart> cartList) {
        for (Cart cart : cartList) {
            if (sellerId.equals(cart.getSellerId())) {
                //商家已存在返回购物车对象
                return cart;
            }
        }
        //商家不存在
        return null;
    }

    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        if (num < 1) {
            throw new RuntimeException("添加商品数量不能小于1");
        }
        TbOrderItem tbOrderItem = new TbOrderItem();
        //`item_id` bigint(20) NOT NULL COMMENT '商品id',
        tbOrderItem.setItemId(item.getId());
        //`goods_id` bigint(20) DEFAULT NULL COMMENT 'SPU_ID',
        tbOrderItem.setGoodsId(item.getGoodsId());
        //`title` varchar(200) COLLATE utf8_bin DEFAULT NULL COMMENT '商品标题',
        tbOrderItem.setTitle(item.getTitle());
        //`price` decimal(20,2) DEFAULT NULL COMMENT '商品单价',
        tbOrderItem.setPrice(item.getPrice());
        //`num` int(10) DEFAULT NULL COMMENT '商品购买数量',
        tbOrderItem.setNum(num);
        //`total_fee` decimal(20,2) DEFAULT NULL COMMENT '商品总金额',
        tbOrderItem.setTotalFee(new BigDecimal(tbOrderItem.getPrice().doubleValue() * tbOrderItem.getNum()));
        //`pic_path` varchar(200) COLLATE utf8_bin DEFAULT NULL COMMENT '商品图片地址',
        String image = item.getImage();
        List<Map> imageList = JSON.parseArray(image, Map.class);
        for (Map map : imageList) {
            String url = (String) map.get("url");
            tbOrderItem.setPicPath(url);
        }
        //`seller_id` varchar(100) COLLATE utf8_bin DEFAULT NULL,
        tbOrderItem.setSellerId(item.getSellerId());
        return tbOrderItem;
    }

    @Override
    public List<Cart> findCartListFromRedis(String sessionId) {
        System.out.println("according sessionId from redis...");
        List<Cart> cartList = (List<Cart>) redisTemplate.boundValueOps(sessionId).get();
        if (cartList == null) {
            //如果redis中无购物车数据则创建一个购物车列表[],null不能被json转换
            cartList = new ArrayList<>();
        }
        return cartList;
    }

    @Override
    public void addCartListToRedis(String key, List<Cart> cartList) {
        System.out.println("according " + key + " add redis...");
        redisTemplate.boundValueOps(key).set(cartList);
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cartList_username, List<Cart> cartList_sessionId) {
        for (Cart cart : cartList_sessionId) {
            List<TbOrderItem> orderItemList = cart.getOrderItemList();
            for (TbOrderItem tbOrderItem : orderItemList) {
                //合并购物车
                cartList_username = addItemToCartList(tbOrderItem.getItemId(), tbOrderItem.getNum(), cartList_username);
            }
        }
        return cartList_username;
    }

    @Override
    public void deleCartListFromRedis(String sessionId) {
        redisTemplate.delete(sessionId);
    }
}
