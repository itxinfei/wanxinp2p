package com.xfjr.consumer.service;

import com.xfjr.api.consumer.model.BankCardDTO;
import com.xfjr.consumer.entity.BankCard;
import com.baomidou.mybatisplus.spring.service.IService;

/**
 * 用户绑定银行卡信息 服务类
 */
public interface BankCardService extends IService<BankCard> {
    /**
     * 获取银行卡信息
     * @param consumerId 用户id
     * @return
     */
    BankCardDTO getByConsumerId(Long consumerId);
    /**
     * 获取银行卡信息
     * @param cardNumber 卡号
     * @return
     */
    BankCardDTO getByCardNumber(String cardNumber);
}
