package com.xfjr.api.message;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class NotificationDTO {
    private String notificationId;
    private String templateCode;
    private String channel;
    private String receiverId;
    private String title;
    private String content;
    private String status;
    private LocalDateTime sendTime;
    private LocalDateTime createDate;
}
