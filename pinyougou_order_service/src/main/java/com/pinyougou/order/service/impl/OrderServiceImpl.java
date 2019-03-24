package com.pinyougou.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.pojo.*;
import com.pinyougou.pojo.TbOrderExample.Criteria;
import com.pinyougou.service.OrderService;
import com.pinyougou.utils.IdWorker;
import entity.PageResult;
import entitygroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private TbOrderMapper orderMapper;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TbOrderItemMapper orderItemMapper;
    @Autowired
    private TbPayLogMapper payLogMapper;
    @Autowired
    private TbItemMapper itemMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbOrder> findAll() {
        return orderMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbOrder> page = (Page<TbOrder>) orderMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbOrder order) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundValueOps(order.getUserId()).get();
        //添加订单列表
        List<String> ids = new ArrayList<>();
        double totalPayment = 0.00;
        for (Cart cart : cartList) {
            List<TbOrderItem> orderItemList = cart.getOrderItemList();
            //组装订单:
            TbOrder tbOrder = new TbOrder();
            //order_id` bigint(20) NOT NULL COMMENT '订单id',  //注意：订单id没有设置主键自增策略，基于idworker工具类生成
            long orderId = idWorker.nextId();
            tbOrder.setOrderId(orderId);
            //`status` varchar(1) COLLATE utf8_bin DEFAULT NULL COMMENT '状态：1、未付款，2、已付款，3、未发货，4、已发货，5、交易成功，6、交易关闭,7、待评价',
            tbOrder.setStatus("1");
            //`create_time` datetime DEFAULT NULL COMMENT '订单创建时间',
            tbOrder.setCreateTime(new Date());
            //`update_time` datetime DEFAULT NULL COMMENT '订单更新时间',
            tbOrder.setUpdateTime(new Date());
            //`user_id` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT '用户id',
            tbOrder.setUserId(order.getUserId());
            //`source_type` varchar(1) COLLATE utf8_bin DEFAULT NULL COMMENT '订单来源：1:app端，2：pc端，3：M端，4：微信端，5：手机qq端',
            tbOrder.setSourceType("2");
            //`seller_id` varchar(100) COLLATE utf8_bin DEFAULT NULL COMMENT '商家ID',  //商家id从购物车中
            tbOrder.setSellerId(cart.getSellerId());

            //页面传递数据
            //eceiver_area_name`varchar(100) COLLATE utf8_bin DEFAULT NULL COMMENT '收货人地区名称(省，市，县)街道',
            tbOrder.setReceiverAreaName(order.getReceiverAreaName());
            //`receiver_mobile`varchar(12) COLLATE utf8_bin DEFAULT NULL COMMENT '收货人手机',
            tbOrder.setReceiverMobile(order.getReceiverMobile());
            //`receiver`varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT '收货人',
            tbOrder.setReceiver(order.getReceiver());
            //payment_type`varchar(1) COLLATE utf8_bin DEFAULT NULL COMMENT '支付类型，1、在线支付，2、货到付款',
            tbOrder.setPaymentType(order.getPaymentType());
            //`payment` decimal(20,2) DEFAULT NULL COMMENT '实付金额。精确到2位小数;单位:元。如:200.07，表示:200元7分',
            double payment = 0.00;

            // tb_order_item 订单项表（订单明细表）
            for (TbOrderItem orderItem : orderItemList) {
                /**
                 * SKU的价格根据itemId,从数据库里查询，
                 */
                TbItem item = itemMapper.selectByPrimaryKey(orderItem.getItemId());
                if (item.getNum() > 0) {
                    payment += item.getPrice().longValue();
                    //`id`bigint(20) NOT NULL,   //注意：订单id没有设置主键自增策略，基于idworker工具类生成
                    orderItem.setId(idWorker.nextId());
                    //`order_id`bigint(20) NOT NULL COMMENT '订单id',
                    orderItem.setOrderId(tbOrder.getOrderId());
                    orderItemMapper.insert(orderItem);
                }

                /*payment += orderItem.getTotalFee().longValue();*/
            }
            tbOrder.setPayment(new BigDecimal(payment));
            orderMapper.insert(tbOrder);
            //添加到订单列表中
            ids.add(orderId + "");
            //累计总金额
            totalPayment += payment;
        }
        //如果是微信支付,记录
        if ("1".equals(order.getPaymentType())) {
            TbPayLog payLog = new TbPayLog();
            //`out_trade_no` varchar(30) NOT NULL COMMENT '支付订单号',   //分布式存储 idWorker
            long out_trade_no = idWorker.nextId();
            payLog.setOutTradeNo(out_trade_no + "");
            //`create_time` datetime DEFAULT NULL COMMENT '创建日期',
            payLog.setCreateTime(new Date());
            //`pay_time` datetime DEFAULT NULL COMMENT '支付完成时间',
            //`total_fee` bigint(20) DEFAULT NULL COMMENT '支付金额（分）',
            payLog.setTotalFee((long) totalPayment * 100);
            //`user_id` varchar(50) DEFAULT NULL COMMENT '用户ID',
            payLog.setUserId(order.getUserId());
            //`transaction_id` varchar(30) DEFAULT NULL COMMENT '交易号码',  //微信返回，支付成功后需要更新的数据
            //`trade_state` varchar(1) DEFAULT NULL COMMENT '交易状态',
            payLog.setTradeState("0");
            //`order_list` varchar(200) DEFAULT NULL COMMENT '订单编号列表',  //一笔支付可能对应多笔订单  1,2,3
            String orderList = ids.toString().replace("[", "").replace("]", "").replace(" ", "");
            payLog.setOrderList(orderList);
            //`pay_type` varchar(1) DEFAULT NULL COMMENT '支付类型',  //微信支付
            payLog.setPayType("1");

            payLogMapper.insert(payLog);
            redisTemplate.boundHashOps("payLog").put(order.getUserId(), payLog);
        }
        //清除redis中的购物车数据
        redisTemplate.delete(order.getUserId());

    }


    /**
     * 修改
     */
    @Override
    public void update(TbOrder order) {
        orderMapper.updateByPrimaryKey(order);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbOrder findOne(Long id) {
        return orderMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            orderMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbOrderExample example = new TbOrderExample();
        Criteria criteria = example.createCriteria();

        if (order != null) {
            if (order.getPaymentType() != null && order.getPaymentType().length() > 0) {
                criteria.andPaymentTypeLike("%" + order.getPaymentType() + "%");
            }
            if (order.getPostFee() != null && order.getPostFee().length() > 0) {
                criteria.andPostFeeLike("%" + order.getPostFee() + "%");
            }
            if (order.getStatus() != null && order.getStatus().length() > 0) {
                criteria.andStatusLike("%" + order.getStatus() + "%");
            }
            if (order.getShippingName() != null && order.getShippingName().length() > 0) {
                criteria.andShippingNameLike("%" + order.getShippingName() + "%");
            }
            if (order.getShippingCode() != null && order.getShippingCode().length() > 0) {
                criteria.andShippingCodeLike("%" + order.getShippingCode() + "%");
            }
            if (order.getUserId() != null && order.getUserId().length() > 0) {
                criteria.andUserIdLike("%" + order.getUserId() + "%");
            }
            if (order.getBuyerMessage() != null && order.getBuyerMessage().length() > 0) {
                criteria.andBuyerMessageLike("%" + order.getBuyerMessage() + "%");
            }
            if (order.getBuyerNick() != null && order.getBuyerNick().length() > 0) {
                criteria.andBuyerNickLike("%" + order.getBuyerNick() + "%");
            }
            if (order.getBuyerRate() != null && order.getBuyerRate().length() > 0) {
                criteria.andBuyerRateLike("%" + order.getBuyerRate() + "%");
            }
            if (order.getReceiverAreaName() != null && order.getReceiverAreaName().length() > 0) {
                criteria.andReceiverAreaNameLike("%" + order.getReceiverAreaName() + "%");
            }
            if (order.getReceiverMobile() != null && order.getReceiverMobile().length() > 0) {
                criteria.andReceiverMobileLike("%" + order.getReceiverMobile() + "%");
            }
            if (order.getReceiverZipCode() != null && order.getReceiverZipCode().length() > 0) {
                criteria.andReceiverZipCodeLike("%" + order.getReceiverZipCode() + "%");
            }
            if (order.getReceiver() != null && order.getReceiver().length() > 0) {
                criteria.andReceiverLike("%" + order.getReceiver() + "%");
            }
            if (order.getInvoiceType() != null && order.getInvoiceType().length() > 0) {
                criteria.andInvoiceTypeLike("%" + order.getInvoiceType() + "%");
            }
            if (order.getSourceType() != null && order.getSourceType().length() > 0) {
                criteria.andSourceTypeLike("%" + order.getSourceType() + "%");
            }
            if (order.getSellerId() != null && order.getSellerId().length() > 0) {
                criteria.andSellerIdLike("%" + order.getSellerId() + "%");
            }

        }

        Page<TbOrder> page = (Page<TbOrder>) orderMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public TbPayLog queryPayLogFromRedis(String userId) {
        return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
    }

    @Override
    public void updateOrderStatus(String out_trade_no, String transaction_id) {
        //1.修改支付日志的状态
        TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
        //`pay_time` datetime DEFAULT NULL COMMENT '支付完成时间',
        payLog.setPayTime(new Date());
        //`transaction_id` varchar(30) DEFAULT NULL COMMENT '交易号码',  //微信返回，支付成功后需要更新的数据
        payLog.setTransactionId(transaction_id);
        payLogMapper.updateByPrimaryKey(payLog);
        //2.修改订单的状态Status和库存
        String[] ids = payLog.getOrderList().split(",");
        for (String orderId : ids) {
            TbOrder tbOrder = orderMapper.selectByPrimaryKey(Long.parseLong(orderId));
            if (tbOrder != null) {
                tbOrder.setStatus("2");
                orderMapper.updateByPrimaryKey(tbOrder);
            }
        }
        //3.清除redis里的日志缓存
        redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
    }

}
