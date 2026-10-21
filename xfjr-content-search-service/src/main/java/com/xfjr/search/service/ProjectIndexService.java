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
}
