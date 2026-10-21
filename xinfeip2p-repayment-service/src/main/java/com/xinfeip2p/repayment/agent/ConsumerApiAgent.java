package com.xinfeip2p.repayment.agent;

import com.xinfeip2p.api.consumer.model.BorrowerDTO;
import com.xinfeip2p.common.domain.RestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "consumer-service")
public interface ConsumerApiAgent {
    @GetMapping(value = "/consumer/l/borrowers/{id}")
    RestResponse<BorrowerDTO> getBorrowerMobile(@PathVariable("id") Long id);
}
