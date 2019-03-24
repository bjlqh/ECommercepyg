package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.seckill.service.SeckillGoodsService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/seckillGoods")
public class SeckillGoodsController {

    @Reference
    private SeckillGoodsService seckillGoodsService;

    @RequestMapping("/findSeckillGoodsList")
    public List<TbSeckillGoods> findSeckillGoodsList() {
        return seckillGoodsService.findSeckillGoodsList();
    }

    @RequestMapping("/findSeckillGoods")
    public TbSeckillGoods findOneFromRedis(Long seckillGoodsId) {
        return seckillGoodsService.findOneFromRedis(seckillGoodsId);
    }
}
