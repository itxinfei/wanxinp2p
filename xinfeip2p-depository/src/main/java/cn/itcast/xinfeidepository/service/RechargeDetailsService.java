package cn.itcast.xinfeidepository.service;

import cn.itcast.xinfeidepository.entity.RechargeDetails;
import cn.itcast.xinfeidepository.model.RechargeRequest;
import cn.itcast.xinfeidepository.model.RechargeResponse;
import com.baomidou.mybatisplus.extension.service.IService;

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
