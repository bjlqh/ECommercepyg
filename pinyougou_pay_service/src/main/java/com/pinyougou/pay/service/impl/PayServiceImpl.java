package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.PayService;
import com.pinyougou.utils.HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class PayServiceImpl implements PayService {
    @Value("${appid}")
    private String appid;
    @Value("${mchid}")
    private String mch_id;
    @Value("${partnerkey}")
    private String partnerkey;
    @Value("${notifyurl}")
    private String notify_url;

    @Override
    public Map<String, Object> createNative(String out_trade_no, String total_fee) throws Exception {
        Map<String, String> paramMap = new HashMap<>();
        // 1.封装微信支付所需要的参数
        paramMap.put("appid", appid);
        paramMap.put("mch_id", mch_id);
        paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
        paramMap.put("body", "品优购秒杀");
        paramMap.put("out_trade_no", out_trade_no);
        paramMap.put("total_fee", total_fee);
        paramMap.put("spbill_create_ip", "127.0.0.1");
        paramMap.put("notify_url", notify_url);
        paramMap.put("trade_type", "NATIVE");
        // 1.2 map->xml
        String paramXml = WXPayUtil.generateSignedXml(paramMap, partnerkey);
        System.out.println("paramXml======" + paramXml);
        // 2.httpclient发送请求
        HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
        httpClient.setHttps(true);
        httpClient.setXmlParam(paramXml);
        httpClient.post();
        // 3.获取响应结果
        String resultXml = httpClient.getContent();
        System.out.println("resultXml=====" + resultXml);
        Map<String, String> resultMap = WXPayUtil.xmlToMap(resultXml);
        String code_url = resultMap.get("code_url");
        // 4.返回生成二维码的数据
        Map<String, Object> map = new HashMap<>();
        map.put("code_url", code_url);
        map.put("out_trade_no", out_trade_no);
        map.put("total_fee", total_fee);
        return map;
    }

    @Override
    public Map<String, String> queryPayStatus(String out_trade_no) throws Exception {
        Map<String, String> paramMap = new HashMap<>();
        // 1.组装查询参数
        paramMap.put("appid", appid);
        paramMap.put("mch_id", mch_id);
        paramMap.put("out_trade_no", out_trade_no);
        paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
        // 1.2.map->xml
        String paramXml = WXPayUtil.generateSignedXml(paramMap, partnerkey);
        // 2.httpClient发送请求
        HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
        httpClient.setHttps(true);
        httpClient.setXmlParam(paramXml);
        httpClient.post();
        // 3.获取响应结果
        String resultXml = httpClient.getContent();
        Map<String, String> resultMap = WXPayUtil.xmlToMap(resultXml);
        return resultMap;
    }
}
