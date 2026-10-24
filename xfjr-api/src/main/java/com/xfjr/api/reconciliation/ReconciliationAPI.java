package com.xfjr.api.reconciliation;

import com.xfjr.common.domain.RestResponse;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/**
 * 对账服务契约（供网关/其他服务通过 Feign 调用）
 */
public interface ReconciliationAPI {

    RestResponse<String> health();

    RestResponse<ReconciliationBatchDTO> createBatch(String bizDate, MultipartFile bankStatement, MultipartFile platformStatement);

    RestResponse<List<ReconciliationBatchDTO>> listBatches();

    RestResponse<List<ReconciliationDiffDTO>> getDiffs(String batchNo);

    RestResponse<Void> handleDiff(Long id, DiffHandleRequest req);

    RestResponse<String> export(String batchNo);
}
