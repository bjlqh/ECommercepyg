package com.pinyougou.sms.controller;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.pinyougou.sms.util.SmsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 品优购短信平台，接收发送短信的请求
 */
@RestController
@RequestMapping("/sms")
public class SMSController {
    //调用阿里发送短信的工具类

    @Autowired
    private SmsUtil smsUtil;

    @RequestMapping(value = "/sendSMS", method = RequestMethod.POST)
    public Map<String, String> sendSMS(String phoneNumbers, String signName, String templateCode, String param) {
        try {
            SendSmsResponse response = smsUtil.sendSms(phoneNumbers, signName, templateCode, param);
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("Code", response.getCode());
            resultMap.put("BizId", response.getBizId());
            resultMap.put("Message", response.getMessage());
            resultMap.put("RequestId", response.getRequestId());

            System.out.println("Code:" + response.getCode());
            System.out.println("BizId:" + response.getBizId());
            System.out.println("Message:" + response.getMessage());
            System.out.println("RequestId:" + response.getRequestId());
            return resultMap;
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return null;
    }

}
