package com.xfjr.uaa.agent;

import com.xfjr.api.account.model.AccountDTO;
import com.xfjr.api.account.model.AccountLoginDTO;
import com.xfjr.common.domain.RestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "account-service")
public interface AccountApiAgent {

    @PostMapping(value = "/account/l/accounts/session")
    RestResponse<AccountDTO> login(@RequestBody AccountLoginDTO accountLoginDTO);
}
