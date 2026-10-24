package com.xfjr.api.message;

import java.util.Map;
import lombok.Data;

/**
 * 发送消息请求（跨服务契约 DTO）
 */
@Data
public class SendNotificationRequest {
    private String channel;      // SMS / STATION / APP
    private String receiverId;
    private String templateCode; // 可选：填了则按模板渲染
    private String title;        // 未用模板时直接指定
    private String content;      // 未用模板时直接指定
    private Map<String, String> params;
}
