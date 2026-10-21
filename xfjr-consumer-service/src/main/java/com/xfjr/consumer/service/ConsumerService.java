package com.xfjr.consumer.service;

import com.xfjr.api.consumer.model.BorrowerDTO;
import com.xfjr.api.consumer.model.ConsumerDTO;
import com.xfjr.api.consumer.model.ConsumerRegisterDTO;
import com.xfjr.api.consumer.model.ConsumerRequest;
import com.xfjr.api.depository.GatewayRequest;
import com.xfjr.api.depository.model.DepositoryConsumerResponse;
import com.xfjr.common.domain.RestResponse;
import com.xfjr.consumer.entity.Consumer;
import com.baomidou.mybatisplus.spring.service.IService;

public interface ConsumerService extends IService<Consumer> {
    /**
     * 检测用户是否存在
     * @param mobile
     * @return
     */
    Integer checkMobile(String mobile);
    /**
     * 用户注册
     * @param consumerRegisterDTO
     * @return
     */
    void register(ConsumerRegisterDTO consumerRegisterDTO);

    /**
    生成开户数据
    @param consumerRequest
    @return
    */
    RestResponse<GatewayRequest> createConsumer(ConsumerRequest consumerRequest);

    /**
     * 更新开户结果
     * @param response
     * @return
     */
    Boolean modifyResult(DepositoryConsumerResponse response);

    /**
     * 通过手机号获取当前用户信息
     * @param mobile
     * @return
     */
    ConsumerDTO getByMobile(String mobile);

    /**
        * 获取借款人基本信息
        * @param id
        * @return
     */
    BorrowerDTO getBorrower(Long id);
}