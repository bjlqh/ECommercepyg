package com.pinyougou.seckill.service;

import com.pinyougou.pojo.TbSeckillOrder;

public interface SeckillOrdersService {
    /**
     * 保存订单
     * @param seckillId
     * @param userId
     */
    void saveSeckillOrder(Long seckillId, String userId);

    /**
     * 根据用户名从redis中查询订单
     * @param userId
     * @return
     */
    TbSeckillOrder querySeckillOrderFromRedis(String userId);

    /**
     * 根据订单号和支付返回的流水号更新支付状态
     * @param id
     * @param transaction_id
     */
    void updateSeckillOrderStatus(Long id,String transaction_id);
}
