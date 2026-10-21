package com.xinfeip2p.depository.message;

import com.xinfeip2p.api.depository.model.DepositoryConsumerResponse;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Component
public class GatewayMessageProducer {

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    public void personalRegister(DepositoryConsumerResponse response) {
        rocketMQTemplate.convertAndSend("TP_GATEWAY_NOTIFY_AGENT:PERSONAL_REGISTER",
                response);
    }
}
