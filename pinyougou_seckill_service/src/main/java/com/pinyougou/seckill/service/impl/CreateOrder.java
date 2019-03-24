package com.pinyougou.seckill.service.impl;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.utils.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * 基于用户id和商品id 保存订单
 */
@Component
public class CreateOrder implements Runnable {
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TbSeckillOrderMapper seckillOrderMapper;
    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    Object lock = new Object();

    @Override
    public void run() {
        //3.1从队列中获取任务
        Map<String, Object> params = (Map<String, Object>) redisTemplate.boundListOps("seckill_order_queue").rightPop();
        String userId = (String) params.get("userId");
        Long seckillId = (Long) params.get("seckillId");

        TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("SeckillGoods").get(seckillId);
        //4.可以秒杀,创建订单
        TbSeckillOrder seckillOrder = new TbSeckillOrder();
        //`id` bigint(20) NOT NULL COMMENT '主键',
        long id = idWorker.nextId();
        seckillOrder.setId(id);
        //`seckill_id` bigint(20) DEFAULT NULL COMMENT '秒杀商品ID',
        seckillOrder.setSeckillId(seckillGoods.getId());
        //`money` decimal(10,2) DEFAULT NULL COMMENT '支付金额',
        seckillOrder.setMoney(seckillGoods.getCostPrice());
        //`user_id` varchar(50) DEFAULT NULL COMMENT '用户',
        seckillOrder.setUserId(userId);
        //`seller_id` varchar(50) DEFAULT NULL COMMENT '商家',
        seckillOrder.setSellerId(seckillGoods.getSellerId());
        //`create_time` datetime DEFAULT NULL COMMENT '创建时间',
        seckillOrder.setCreateTime(new Date());
        //`status` varchar(1) DEFAULT NULL COMMENT '状态', ----未付款
        seckillOrder.setStatus("1");
        seckillOrderMapper.insert(seckillOrder);

        //4.2 将订单存入redis中
        redisTemplate.boundHashOps("SeckillOrders").put(userId, seckillOrder);

        //4.3 下单成功后记录用户所购买的商品{id,[aaa,bbb,ccc,ddd]}
        redisTemplate.boundSetOps("seckill_user_goods_" + seckillId).add(userId);

        //秒杀下单成功后,排队人数-1
        redisTemplate.boundValueOps("seckill_user_queue_" + seckillId).increment(-1);


        //5.判断redis中的商品库存数是否为0,写入数据库
        synchronized (lock) {
            seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
        }

        if (seckillGoods.getStockCount() <= 0) {
            //从redis 中删除
            redisTemplate.boundHashOps("SeckillGoods").delete(seckillId);
            //商品卖完，同步到数据库
            seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
        } else {
            //redis中秒杀商品的库存-1
            redisTemplate.boundHashOps("SeckillGoods").put(seckillGoods.getId(), seckillGoods);
        }
    }
}
