package com.xfjr.consumer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xfjr.consumer.audit.entity.OperationLog;

/**
 * 操作日志 Mapper：仅暴露插入与查询，不提供 update/delete，保证仅追加（AC2）。
 */
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
