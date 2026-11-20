package com.xfjr.message.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("notification")
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private String notificationId;
    private String templateCode;
    private String channel;
    private String receiverId;
    private String title;
    private String content;
    private String status;
    private Integer retryCount;
    private LocalDateTime nextRetryTime;
    private LocalDateTime sendTime;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
}
