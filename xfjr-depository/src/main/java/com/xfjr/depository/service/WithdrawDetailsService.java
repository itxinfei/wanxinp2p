package com.xfjr.depository.service;

import com.xfjr.depository.entity.WithdrawDetails;
import com.xfjr.depository.model.RechargeRequest;
import com.xfjr.depository.model.RechargeResponse;
import com.xfjr.depository.model.WithdrawRequest;
import com.xfjr.depository.model.WithdrawResponse;
import com.baomidou.mybatisplus.spring.service.IService;

/**
 * <p>
 * 用户余额明细表 服务类
 * </p>
 *
 * @author zhupeiyuan
 * @since 2019-05-13
 */
public interface WithdrawDetailsService extends IService<WithdrawDetails> {

    /**
     * 用户提现
     * @param withdrawRequest
     * @return
     */
    WithdrawResponse withDraw(WithdrawRequest withdrawRequest);

}
