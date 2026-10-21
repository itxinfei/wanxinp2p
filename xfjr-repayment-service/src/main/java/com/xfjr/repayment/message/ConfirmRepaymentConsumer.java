package com.xfjr.repayment.message;



import com.xfjr.api.depository.model.RepaymentRequest;

import com.xfjr.repayment.entity.RepaymentPlan;

import com.xfjr.repayment.service.RepaymentService;

import com.alibaba.fastjson2.JSON;

import com.alibaba.fastjson2.JSONObject;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;

import org.apache.rocketmq.spring.core.RocketMQListener;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;



@Component

@RocketMQMessageListener(topic = "TP_CONFIRM_REPAYMENT", consumerGroup =

        "CID_CONFIRM_REPAYMENT")

public class ConfirmRepaymentConsumer implements RocketMQListener<String> {



    @Autowired

    private RepaymentService repaymentService;



    @Override

    public void onMessage(String message) {

        //1.解析消息

        JSONObject jsonObject=JSON.parseObject(message);

        RepaymentPlan repaymentPlan=JSONObject.parseObject(jsonObject.getString("repaymentPlan"),RepaymentPlan.class);

        RepaymentRequest repaymentRequest=JSONObject.parseObject(jsonObject.getString("repaymentRequest"), RepaymentRequest.class);



        //2.执行本地业务

        repaymentService.invokeConfirmRepayment(repaymentPlan,repaymentRequest);

    }

}

