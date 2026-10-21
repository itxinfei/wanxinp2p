package cn.itcast.xinfeidepository.service;

import cn.itcast.xinfeidepository.entity.Tender;
import cn.itcast.xinfeidepository.model.ConfirmLoanResponse;
import cn.itcast.xinfeidepository.model.UserAutoPreTransactionRequest;
import cn.itcast.xinfeidepository.model.UserAutoPreTransactionResponse;
import com.baomidou.mybatisplus.extension.service.IService;

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
