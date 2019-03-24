package com.pinyougou.seckill.service;

import com.pinyougou.pojo.TbSeckillGoods;

import java.util.List;

public interface SeckillGoodsService {

    List<TbSeckillGoods> findSeckillGoodsList();

    TbSeckillGoods findOneFromRedis(Long seckillId);

}
