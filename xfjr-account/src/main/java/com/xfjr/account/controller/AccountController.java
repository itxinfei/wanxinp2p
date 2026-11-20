package com.xfjr.account.controller;


import com.xfjr.account.service.AccountService;
import com.xfjr.api.AccountAPI;
import com.xfjr.api.account.model.AccountDTO;
import com.xfjr.api.account.model.AccountLoginDTO;
import com.xfjr.api.account.model.AccountRegisterDTO;
import com.xfjr.common.domain.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "统一账户服务的API")
public class AccountController implements AccountAPI {

    @Autowired
    private AccountService accountService;

    @Operation(summary = "测试")
    @GetMapping("/hello")
    public String hello(){
        return "hello";
    }


    @Override
    @Operation(summary = "获取手机验证码")
    @Parameter(name = "mobile", description = "手机号", in = ParameterIn.PATH)
    @GetMapping("/sms/{mobile}")
    public RestResponse getSMSCode(@PathVariable String mobile) {
        return accountService.getSMSCode(mobile);
    }

    @Operation(summary = "校验手机号和验证码")
    @Parameters({
            @Parameter(name = "mobile", description = "手机号", required = true, in = ParameterIn.PATH),
            @Parameter(name = "key", description = "校验标识", required = true, in = ParameterIn.PATH),
            @Parameter(name = "code", description = "验证码", required = true, in = ParameterIn.PATH)})
    @Override
    @GetMapping("/mobiles/{mobile}/key/{key}/code/{code}")
    public RestResponse<Integer> checkMobile(@PathVariable String mobile,
                                             @PathVariable String key,
                                             @PathVariable String code) {
        return RestResponse.success(accountService.checkMobile(mobile,key,code));
    }

    @Override
    @Operation(summary = "用户注册")
    @PostMapping(value = "/l/accounts")
    public RestResponse<AccountDTO> register(@RequestBody AccountRegisterDTO accountRegisterDTO) {
        return  RestResponse.success(accountService.register(accountRegisterDTO));
    }

    @Operation(summary = "用户登录")
    @PostMapping(value = "/l/accounts/session")
    @Override
    public RestResponse<AccountDTO> login(@RequestBody AccountLoginDTO accountLoginDTO) {
        return RestResponse.success(accountService.login(accountLoginDTO));
    }
}