package cn.itcast.xinfeidepository.service;

import cn.itcast.xinfeidepository.entity.WithdrawDetails;
import cn.itcast.xinfeidepository.model.RechargeRequest;
import cn.itcast.xinfeidepository.model.RechargeResponse;
import cn.itcast.xinfeidepository.model.WithdrawRequest;
import cn.itcast.xinfeidepository.model.WithdrawResponse;
import com.baomidou.mybatisplus.extension.service.IService;

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
