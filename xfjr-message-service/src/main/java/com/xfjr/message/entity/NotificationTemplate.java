package com.xfjr.message.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("notification_template")
public class NotificationTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private String templateCode;
    private String channel;
    private String title;
    private String contentTemplate;
    private String status;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
}
