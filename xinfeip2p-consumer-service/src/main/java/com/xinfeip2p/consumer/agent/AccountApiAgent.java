package com.xinfeip2p.consumer.agent;

import com.xinfeip2p.api.account.model.AccountDTO;
import com.xinfeip2p.api.account.model.AccountRegisterDTO;
import com.xinfeip2p.common.domain.RestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value="account-service")
public interface AccountApiAgent {

    @PostMapping(value = "/account/l/accounts")
    RestResponse<AccountDTO> register(@RequestBody AccountRegisterDTO accountRegisterDTO);
}
