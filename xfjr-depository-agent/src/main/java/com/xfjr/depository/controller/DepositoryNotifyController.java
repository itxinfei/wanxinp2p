package com.xfjr.depository.controller;

import com.xfjr.api.depository.model.DepositoryConsumerResponse;
import com.xfjr.common.util.EncryptUtil;
import com.xfjr.depository.message.GatewayMessageProducer;
import com.xfjr.depository.service.DepositoryRecordService;
import com.alibaba.fastjson2.JSON;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "depository-agent", description = "银行存管系统通知服务")
@RestController
public class DepositoryNotifyController {

    @Autowired
    private DepositoryRecordService depositoryRecordService;

    @Autowired
    private GatewayMessageProducer gatewayMessageProducer;

    @Operation(summary = "接受银行存管系统开户回调结果")
    @Parameters({
            @Parameter(name = "serviceName", description = "请求的存管接口名", required = true, in = ParameterIn.QUERY),
            @Parameter(name = "platformNo", description = "平台编号，平台与存管系统签约时获取", required = true, in = ParameterIn.QUERY),
            @Parameter(name = "signature", description = "对reqData参数的签名", required = true, in = ParameterIn.QUERY),
            @Parameter(name = "reqData", description = "业务数据报文，json格式", required = true, in = ParameterIn.QUERY)})
    @RequestMapping(value = "/gateway", method = RequestMethod.GET,params = "serviceName=PERSONAL_REGISTER")
    public String receiveDepositoryCreateConsumerResult(@RequestParam("serviceName") String serviceName,
                                                        @RequestParam("platformNo") String platformNo,
                                                        @RequestParam("signature") String signature,
                                                        @RequestParam("reqData") String reqData){
        //1.更新数据
        DepositoryConsumerResponse depositoryConsumerResponse=JSON.parseObject(EncryptUtil.decodeUTF8StringBase64(reqData),DepositoryConsumerResponse.class);
        depositoryRecordService.modifyRequestStatus(depositoryConsumerResponse.getRequestNo(),depositoryConsumerResponse.getStatus());

        //2.发送消息给用户中心
        gatewayMessageProducer.personalRegister(depositoryConsumerResponse);

        //3.返回结果
        return "OK";
    }

}