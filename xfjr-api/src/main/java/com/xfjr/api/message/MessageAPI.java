package com.xfjr.api.message;

import com.xfjr.common.domain.RestResponse;
import java.util.List;

/**
 * 消息服务契约（供网关/其他服务通过 Feign 调用）
 */
public interface MessageAPI {

    RestResponse<String> health();

    RestResponse<NotificationDTO> send(SendNotificationRequest req);

    RestResponse<List<NotificationTemplateDTO>> templates();

    RestResponse<List<NotificationDTO>> myMessages(String receiverId);
}
