package com.pinyougou.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.seckill.service.SeckillGoodsService;
import com.pinyougou.utils.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SeckillGoodsServiceImpl implements SeckillGoodsService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IdWorker idWorker;

    @Override
    public List<TbSeckillGoods> findSeckillGoodsList() {
        return redisTemplate.boundHashOps("SeckillGoods").values();
    }

    @Override
    public TbSeckillGoods findOneFromRedis(Long seckillId) {
        return (TbSeckillGoods) redisTemplate.boundHashOps("SeckillGoods").get(seckillId);
    }
}
