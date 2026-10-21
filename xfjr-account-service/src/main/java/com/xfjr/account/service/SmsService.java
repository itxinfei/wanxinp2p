package com.xfjr.account.service;


import com.xfjr.account.common.AccountErrorCode;
import com.xfjr.common.domain.BusinessException;
import com.xfjr.common.domain.CommonErrorCode;
import com.xfjr.common.domain.RestResponse;
import com.xfjr.common.util.OkHttpUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    @Value("${sms.url}")
    private String smsURL;

    @Value("${sms.enable}")
    private Boolean smsEnable;


    public RestResponse getSMSCode(String mobile) {
        if (smsEnable) {
            return OkHttpUtil.post(smsURL + "/generate?effectiveTime=300&name=sms", "{\"mobile\":" + mobile + "}");

        }
        return RestResponse.success();
    }

    public void verifySmsCode(String key,String code){
        if (smsEnable) {
            StringBuilder params = new StringBuilder("/verify?name=sms");
            params.append("&verificationKey=").append(key).append("&verificationCode=").append(code);
            RestResponse smsResponse = OkHttpUtil.post(smsURL + params, "");
            if (smsResponse.getCode() != CommonErrorCode.SUCCESS.getCode() || smsResponse.getResult().toString().equalsIgnoreCase("false")) {
                throw new BusinessException(AccountErrorCode.E_140152);
            }
        }
    }
}
