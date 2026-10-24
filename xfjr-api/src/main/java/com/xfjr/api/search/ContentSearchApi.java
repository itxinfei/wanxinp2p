package com.xfjr.api.search;

import com.xfjr.api.search.model.ProjectQueryParamsDTO;
import com.xfjr.api.transaction.model.ProjectDTO;
import com.xfjr.common.domain.PageVO;
import com.xfjr.common.domain.RestResponse;

public interface ContentSearchApi {
    /**
     * 检索标的
     * @param projectQueryParamsDTO
     * @return
     */
    RestResponse<PageVO<ProjectDTO>> queryProjectIndex(
            ProjectQueryParamsDTO projectQueryParamsDTO,
            Integer pageNo,Integer pageSize,String sortBy,String order);

    /**
     * 索引同步（内部调用）：标的创建/状态变更时 upsert
     */
    RestResponse<Void> indexProject(ProjectDTO project);

    /**
     * 从索引移除标的（内部调用）
     */
    RestResponse<Void> removeProject(Long id);
}
