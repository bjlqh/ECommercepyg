package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.seckill.service.SeckillOrdersService;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seckillOrder")
public class SeckillOrderController {
    @Reference
    private SeckillOrdersService seckillOrdersService;

    @RequestMapping("/saveSeckillOrder")
    public Result saveSeckillOrder(Long seckillGoodsId) {
        try {
            String userId = SecurityContextHolder.getContext().getAuthentication().getName();
            if (userId.equals("anonymousUser")) {
                return new Result(false, "请先登录，再抢购");
            }
            seckillOrdersService.saveSeckillOrder(seckillGoodsId, userId);
            return new Result(true, "抢购成功，请在1分钟之内支付");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return new Result(false, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "抢购失败");
        }
    }
}
