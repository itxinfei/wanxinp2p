package com.xfjr.repayment.controller;

import com.xfjr.api.repayment.RepaymentApi;
import com.xfjr.api.repayment.model.ProjectWithTendersDTO;
import com.xfjr.common.domain.RestResponse;
import com.xfjr.repayment.service.RepaymentService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 还款微服务的Controller
 */
@RestController
public class RepaymentController implements RepaymentApi {

    @Autowired
    private RepaymentService repaymentService;


    @Override
    @Operation(summary = "启动还款")
    @PostMapping("/l/start-repayment")
    public RestResponse<String> startRepayment(@RequestBody ProjectWithTendersDTO projectWithTendersDTO) {
        String result=repaymentService.startRepayment(projectWithTendersDTO);
        return RestResponse.success(result);
    }

    /*@Operation(summary = "测试用户还款")
    @GetMapping("/execute-repayment/{date}")
    public void testExecuteRepayment(@PathVariable String date) {
        repaymentService.executeRepayment(date);
    }*/

    @Operation(summary = "测试还款短信提醒")
    @GetMapping("/repayment-notify/{date}")
    public void testRepaymentNotify(@PathVariable String date) {
        repaymentService.sendRepaymentNotify(date);
    }

}