package com.pinyougou.seckill.task;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class SeckillTask {

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 秒杀前将通过审核的秒杀商品数据导入redis中
     */
    @Scheduled(cron = "0/10 * * * * ?")
    public void synchronizeSeckillGoodsToRedis() {
        /**
         * 满足条件：
         * 1.审核通过
         * 2.有库存
         * 3.当前时间大于秒杀开始时间，小于秒杀结束时间
         */
        TbSeckillGoodsExample example = new TbSeckillGoodsExample();
        TbSeckillGoodsExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1").andStockCountGreaterThan(0).andStartTimeLessThanOrEqualTo(new Date()).andEndTimeGreaterThanOrEqualTo(new Date());
        List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);

        //存入redis中,以商品的id 依次保存到数据库
        for (TbSeckillGoods tbSeckillGoods : seckillGoodsList) {
            redisTemplate.boundHashOps("SeckillGoods").put(tbSeckillGoods.getId(), tbSeckillGoods);
            //记录该商品的的库存 [1,1,1,1,1]
            for (Integer i = 0; i < tbSeckillGoods.getStockCount(); i++) {
                redisTemplate.boundListOps("seckill_goods_queue_"+tbSeckillGoods.getId()).leftPush(tbSeckillGoods.getId());
            }
        }
        System.out.println("finished...");
    }
}
