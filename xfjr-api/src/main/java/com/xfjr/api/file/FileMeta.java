package com.xfjr.api.file;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 文件元数据（跨服务契约 DTO，置于 xfjr-api 避免 api 反向依赖业务模块）
 */
@Data
public class FileMeta {
    private String fileId;
    private String originalName;
    private String contentType;
    private Long fileSize;
    private String bizType;
    private String status;
    private LocalDateTime createDate;
}
