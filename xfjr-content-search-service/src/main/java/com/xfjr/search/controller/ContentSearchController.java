package com.xfjr.search.controller;

import com.xfjr.api.search.model.ProjectQueryParamsDTO;
import com.xfjr.api.transaction.model.ProjectDTO;
import com.xfjr.common.domain.PageVO;
import com.xfjr.common.domain.RestResponse;
import com.xfjr.search.service.ProjectIndexService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Tag(name = "ContentSearch", description = "检索服务API")
public class ContentSearchController {

    @Autowired
    private ProjectIndexService projectIndexService;


    @Operation(summary = "检索标的")
    @Parameters({
            @Parameter(name = "pageNo", description = "页码", required = true, in = ParameterIn.QUERY),
            @Parameter(name = "pageSize", description = "每页记录数", required = true, in = ParameterIn.QUERY),
            @Parameter(name = "sortBy", description = "排序字段", in = ParameterIn.QUERY),
            @Parameter(name = "order", description = "顺序", in = ParameterIn.QUERY)})
    @PostMapping(value = "/l/projects/indexes/q")
    public RestResponse<PageVO<ProjectDTO>> queryProjectIndex(
            @RequestBody ProjectQueryParamsDTO projectQueryParamsDTO,
            @RequestParam Integer pageNo,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String order){
        PageVO<ProjectDTO> pageVO=projectIndexService.queryProjectIndex(projectQueryParamsDTO,pageNo,pageSize,sortBy,order);
        return RestResponse.success(pageVO);
    }
}