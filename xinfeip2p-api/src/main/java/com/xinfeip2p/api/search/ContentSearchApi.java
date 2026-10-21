package com.xinfeip2p.api.search;

import com.xinfeip2p.api.search.model.ProjectQueryParamsDTO;
import com.xinfeip2p.api.transaction.model.ProjectDTO;
import com.xinfeip2p.common.domain.PageVO;
import com.xinfeip2p.common.domain.RestResponse;

public interface ContentSearchApi {
    /**
     * 检索标的
     * @param projectQueryParamsDTO
     * @return
     */
    RestResponse<PageVO<ProjectDTO>> queryProjectIndex(
            ProjectQueryParamsDTO projectQueryParamsDTO,
            Integer pageNo,Integer pageSize,String sortBy,String order);
}
