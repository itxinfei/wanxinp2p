package com.xfjr.api.message;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class NotificationTemplateDTO {
    private String templateCode;
    private String channel;
    private String title;
    private String contentTemplate;
    private String status;
    private LocalDateTime createDate;
}
