package com.xfjr.depository.service;

import com.xfjr.depository.entity.RechargeDetails;
import com.xfjr.depository.model.RechargeRequest;
import com.xfjr.depository.model.RechargeResponse;
import com.baomidou.mybatisplus.spring.service.IService;

/**
 * <p>
 * 充值记录表 服务类
 * </p>
 *
 * @author zhupeiyuan
 * @since 2019-05-13
 */
public interface RechargeDetailsService extends IService<RechargeDetails> {

	/**
	 * 用户充值
	 * @param rechargeRequest
	 * @return
	 */
	RechargeResponse recharge(RechargeRequest rechargeRequest);

}
