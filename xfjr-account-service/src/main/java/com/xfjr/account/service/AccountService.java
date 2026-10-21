package com.xfjr.account.service;

import com.xfjr.account.entity.Account;
import com.xfjr.api.account.model.AccountDTO;
import com.xfjr.api.account.model.AccountLoginDTO;
import com.xfjr.api.account.model.AccountRegisterDTO;
import com.xfjr.common.domain.RestResponse;
import com.baomidou.mybatisplus.spring.service.IService;


public interface AccountService extends IService<Account> {

    RestResponse getSMSCode(String mobile);

    Integer checkMobile(String mobile, String key, String code);

    AccountDTO register(AccountRegisterDTO accountRegisterDTO);

    AccountDTO login(AccountLoginDTO accountLoginDTO);
}
