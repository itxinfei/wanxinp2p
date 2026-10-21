package com.xfjr.transaction.agent;

import com.xfjr.api.consumer.model.BalanceDetailsDTO;
import com.xfjr.api.consumer.model.ConsumerDTO;
import com.xfjr.common.domain.RestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
        C端服务代理
        */
@FeignClient(value = "consumer-service")
public interface ConsumerApiAgent {
    @GetMapping("/consumer/l/currConsumer/{mobile}")
    RestResponse<ConsumerDTO> getCurrConsumer(@PathVariable("mobile") String mobile);

    @GetMapping("/consumer/l/balances/{userNo}")
    public RestResponse<BalanceDetailsDTO> getBalance(@PathVariable("userNo")
                                                              String userNo);
}
