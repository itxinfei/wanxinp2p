package com.xfjr.message.controller;

import com.xfjr.message.service.NotificationService;
import com.xfjr.api.message.MessageAPI;
import com.xfjr.common.domain.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Message服务API")
public class MessageController implements MessageAPI {

    @Autowired
    private NotificationService notificationService;

    @Operation(summary = "服务健康检查")
    @GetMapping("/l/message/health")
    public RestResponse<String> health() {
        return RestResponse.success(notificationService.ping());
    }
}
