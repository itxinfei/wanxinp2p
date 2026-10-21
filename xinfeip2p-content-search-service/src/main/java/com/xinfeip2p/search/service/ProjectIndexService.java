package com.xinfeip2p.search.service;

import com.xinfeip2p.api.search.model.ProjectQueryParamsDTO;
import com.xinfeip2p.api.transaction.model.ProjectDTO;
import com.xinfeip2p.common.domain.PageVO;

/**
 * 标的检索业务层接口
 */
public interface ProjectIndexService {
    PageVO<ProjectDTO> queryProjectIndex(ProjectQueryParamsDTO
                                                 projectQueryParamsDTO,
                                         Integer pageNo, Integer pageSize,
                                         String sortBy, String order);
}
