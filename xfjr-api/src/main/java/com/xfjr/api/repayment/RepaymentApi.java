package com.xfjr.api.repayment;

import com.xfjr.api.repayment.model.ProjectWithTendersDTO;
import com.xfjr.common.domain.RestResponse;

public interface RepaymentApi {

    /**
     * 启动还款
     * @param projectWithTendersDTO
     * @return
     */
    public RestResponse<String> startRepayment(ProjectWithTendersDTO
                                                       projectWithTendersDTO);
}
