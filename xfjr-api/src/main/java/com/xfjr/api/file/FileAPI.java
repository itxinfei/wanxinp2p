package com.xfjr.api.file;

import com.xfjr.common.domain.RestResponse;

/**
 * 文件服务契约（供网关/其他服务通过 Feign 调用）
 */
public interface FileAPI {

    /**
     * 服务健康检查
     */
    RestResponse<String> health();

    /**
     * 文件元数据查询
     */
    RestResponse<FileMeta> metadata(String fileId);

    /**
     * 文件删除（软删除 + 物理清理）
     */
    RestResponse<Void> delete(String fileId);
}
