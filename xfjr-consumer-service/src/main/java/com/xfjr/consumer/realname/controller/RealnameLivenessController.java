package com.xfjr.consumer.realname.controller;

import com.xfjr.common.domain.RestResponse;
import com.xfjr.consumer.realname.model.LivenessStatusDTO;
import com.xfjr.consumer.realname.model.LivenessSubmitRequest;
import com.xfjr.consumer.realname.service.RealnameLivenessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 实名活体/人脸核验（FR-135）。
 */
@RestController
@Slf4j
@RequestMapping("/realname")
@Tag(name = "RealnameLiveness", description = "实名活体/人脸核验API")
public class RealnameLivenessController {

    @Autowired
    private RealnameLivenessService realnameLivenessService;

    @Operation(summary = "提交活体+人脸核验")
    @PostMapping("/l/realnames/liveness")
    public RestResponse<LivenessStatusDTO> submit(@RequestBody LivenessSubmitRequest req) {
        return RestResponse.success(realnameLivenessService.submit(req));
    }

    @Operation(summary = "查询当前用户核验状态")
    @GetMapping("/my/realname/liveness/status")
    public RestResponse<LivenessStatusDTO> status() {
        return RestResponse.success(realnameLivenessService.statusOfCurrentUser());
    }
}
