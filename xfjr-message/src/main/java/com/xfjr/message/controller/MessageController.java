package com.xfjr.message.controller;

import com.xfjr.api.message.MessageAPI;
import com.xfjr.api.message.NotificationDTO;
import com.xfjr.api.message.NotificationTemplateDTO;
import com.xfjr.api.message.SendNotificationRequest;
import com.xfjr.common.domain.RestResponse;
import com.xfjr.message.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/l/notifications")
@Tag(name = "Message服务API")
public class MessageController implements MessageAPI {

    @Autowired
    private NotificationService notificationService;

    @Operation(summary = "服务健康检查")
    @GetMapping("/health")
    public RestResponse<String> health() {
        return RestResponse.success("Message service alive");
    }

    @Operation(summary = "发送消息")
    @PostMapping
    public RestResponse<NotificationDTO> send(@RequestBody SendNotificationRequest req) {
        return RestResponse.success(notificationService.send(req));
    }

    @Operation(summary = "模板管理")
    @GetMapping("/templates")
    public RestResponse<List<NotificationTemplateDTO>> templates() {
        return RestResponse.success(notificationService.templates());
    }

    @Operation(summary = "我的消息")
    @GetMapping("/my")
    public RestResponse<List<NotificationDTO>> myMessages(@RequestParam("receiverId") String receiverId) {
        return RestResponse.success(notificationService.myMessages(receiverId));
    }
}
