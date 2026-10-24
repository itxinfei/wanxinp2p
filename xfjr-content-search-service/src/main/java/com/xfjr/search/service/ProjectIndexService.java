package com.xfjr.search.service;

import com.xfjr.api.search.model.ProjectQueryParamsDTO;
import com.xfjr.api.transaction.model.ProjectDTO;
import com.xfjr.common.domain.PageVO;

/**
 * 标的检索业务层接口
 */
public interface ProjectIndexService {
    PageVO<ProjectDTO> queryProjectIndex(ProjectQueryParamsDTO
                                                 projectQueryParamsDTO,
                                         Integer pageNo, Integer pageSize,
                                         String sortBy, String order);

    /**
     * 索引同步：标的创建/状态变更时 upsert 到 ES（AC2，近实时）。
     */
    void indexProject(ProjectDTO project);

    /**
     * 从索引移除标的（流标/删除）。
     */
    void removeProject(Long id);
}
