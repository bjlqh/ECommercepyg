package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.PayService;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrdersService;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/SeckillPay")
public class SeckillPayController {
    @Reference
    private PayService payService;
    @Reference
    private SeckillOrdersService seckillOrdersService;

    @RequestMapping("/createNative")
    public Map<String, Object> createNative() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        //读取订单里的订单号和金额
        TbSeckillOrder seckillOrder = seckillOrdersService.querySeckillOrderFromRedis(userId);

        try {
            Map<String, Object> map = payService.createNative(seckillOrder.getId() + "", seckillOrder.getMoney().longValue() * 100 + "");
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(Long id) {
        int count = 1;
        try {
            //持续性查询用户支付状态
            while (true) {
                //每隔3秒查询一次
                Thread.sleep(3000);

                //如果用户5分钟没有支付，支付超时，跳转循环
                count++;
                System.out.println(count);
                if (count >= 100) {
                    return new Result(false, "timeout");
                }
                Map<String, String> resultMap = payService.queryPayStatus(id + "");
                //获取支付状态
                if ("SUCCESS".equals(resultMap.get("trade_state"))) {
                    //支付成功后，获取微信返回的交易流水号
                    String transaction_id = resultMap.get("transaction_id");
                    //修改订单状态
                    seckillOrdersService.updateSeckillOrderStatus(id, transaction_id);
                    return new Result(true, "支付成功");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "支付失败");
        }
    }
}
