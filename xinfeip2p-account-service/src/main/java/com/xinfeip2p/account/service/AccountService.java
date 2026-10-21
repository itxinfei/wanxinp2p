package com.xinfeip2p.account.service;

import com.xinfeip2p.account.entity.Account;
import com.xinfeip2p.api.account.model.AccountDTO;
import com.xinfeip2p.api.account.model.AccountLoginDTO;
import com.xinfeip2p.api.account.model.AccountRegisterDTO;
import com.xinfeip2p.common.domain.RestResponse;
import com.baomidou.mybatisplus.extension.service.IService;


public interface AccountService extends IService<Account> {

    RestResponse getSMSCode(String mobile);

    Integer checkMobile(String mobile, String key, String code);

    AccountDTO register(AccountRegisterDTO accountRegisterDTO);

    AccountDTO login(AccountLoginDTO accountLoginDTO);
}
