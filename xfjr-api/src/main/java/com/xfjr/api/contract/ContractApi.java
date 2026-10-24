package com.xfjr.api.contract;

import com.xfjr.common.domain.RestResponse;
import java.util.List;

/**
 * 电子合同与存证服务契约（FR-133，供网关/交易服务通过 Feign 调用）。
 */
public interface ContractApi {

    /** 生成合同（放款成功时触发）。 */
    RestResponse<List<ContractDTO>> create(CreateContractRequest req);

    /** 我的合同列表。 */
    RestResponse<List<ContractDTO>> myContracts();

    /** 合同详情/下载（含文件哈希，供防篡改校验）。 */
    RestResponse<ContractDTO> detail(String contractNo);

    /** 签署合同。 */
    RestResponse<ContractDTO> sign(String contractNo, SignRequest req);

    /** 存证报告。 */
    RestResponse<List<ContractEvidenceDTO>> evidence(String contractNo);
}
