package com.xfjr.depository.service;

import com.xfjr.depository.entity.Tender;
import com.xfjr.depository.model.ConfirmLoanResponse;
import com.xfjr.depository.model.UserAutoPreTransactionRequest;
import com.xfjr.depository.model.UserAutoPreTransactionResponse;
import com.baomidou.mybatisplus.spring.service.IService;

/**
 * <p>
 * 投标信息表 服务类
 * </p>
 *
 * @author zhupeiyuan
 * @since 2019-05-13
 */
public interface TenderService extends IService<Tender> {

	/**
	 * 投标预授权处理
	 * @param preTransactionRequest
	 * @return
	 */
	UserAutoPreTransactionResponse autoPreTransactionForTender(UserAutoPreTransactionRequest preTransactionRequest);

	/**
	 * 放款确认
	 * @param reqData
	 * @return
	 */
	ConfirmLoanResponse confirmLoan(String reqData);

}
