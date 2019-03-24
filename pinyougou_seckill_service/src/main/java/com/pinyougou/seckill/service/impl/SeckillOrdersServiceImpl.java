package com.pinyougou.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class SeckillOrdersServiceImpl implements SeckillOrdersService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ThreadPoolTaskExecutor executor;
    @Autowired
    private CreateOrder createOrder;
    @Autowired
    private TbSeckillOrderMapper seckillOrderMapper;

    @Override
    public void saveSeckillOrder(Long seckillId, String userId) {

        //进入该商品秒杀，下单的排队人数+1
        redisTemplate.boundValueOps("seckill_user_queue_" + seckillId).increment(1);

        //优化: 用户先从队列中获取商品的id，能获取到才能下单
        Long goodsId = (Long) redisTemplate.boundListOps("seckill_goods_queue_" + seckillId).rightPop();
        if (goodsId == null) {
            throw new RuntimeException("商品已售罄");
        }

        //解决用户重复购买问题 {id,[aaa,bbb,ccc,ddd]}
        Boolean isExist = redisTemplate.boundSetOps("seckill_user_goods_" + seckillId).isMember(userId);
        if (isExist) {
            throw new RuntimeException("您已经抢购过该商品");
        }

        //1.从redis中获取对应商品
        TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("SeckillGoods").get(seckillId);
        //2.判断
        if (seckillGoods == null && seckillGoods.getStockCount() <= 0) {
            //不能秒杀
            throw new RuntimeException("商品已售罄");
        }

        //秒杀排队人数大于库存数,提醒用户
        Long size = redisTemplate.boundValueOps("seckill_user_queue_" + seckillId).size();
        if (size > seckillGoods.getStockCount() + 50) {
            throw new RuntimeException("排队人数过多，请稍后再试！");
        }

        //3.将秒杀的任务保存到缓存中，根据userId和seckillId
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("seckillId", seckillId);
        redisTemplate.boundListOps("seckill_order_queue").leftPush(params);

        //基于多线程调用，保存订单任务
        executor.execute(createOrder);
    }

    @Override
    public TbSeckillOrder querySeckillOrderFromRedis(String userId) {
        return (TbSeckillOrder) redisTemplate.boundHashOps("SeckillOrders").get(userId);
    }

    @Override
    public void updateSeckillOrderStatus(Long id, String transaction_id) {
        //1.修改支付的状态
        TbSeckillOrder seckillOrder = seckillOrderMapper.selectByPrimaryKey(id);
        //pay_timedatetime NULL支付时间
        seckillOrder.setPayTime(new Date());
        //statusvarchar(1) NULL状态
        seckillOrder.setStatus("2");
        //transaction_idvarchar(30) NULL交易流水
        seckillOrder.setTransactionId(transaction_id);
        seckillOrderMapper.insertSelective(seckillOrder);

        //2.1清除redis里的订单用户信息
        redisTemplate.boundHashOps("SeckillOrders").delete(seckillOrder.getUserId());
        //2.2清除用户商品信息
        redisTemplate.boundSetOps("seckill_user_goods_" + id).remove(seckillOrder.getUserId());
    }

}
