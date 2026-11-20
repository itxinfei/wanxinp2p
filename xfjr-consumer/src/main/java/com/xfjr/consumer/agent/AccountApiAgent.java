package com.xfjr.consumer.agent;

import com.xfjr.api.account.model.AccountDTO;
import com.xfjr.api.account.model.AccountRegisterDTO;
import com.xfjr.common.domain.RestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value="account-service")
public interface AccountApiAgent {

    @PostMapping(value = "/account/l/accounts")
    RestResponse<AccountDTO> register(@RequestBody AccountRegisterDTO accountRegisterDTO);
}
