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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @Operation(summary = "索引同步（内部调用：创建/状态变更 upsert）")
    @PostMapping(value = "/l/projects/indexes/sync")
    public RestResponse<Void> indexProject(@RequestBody ProjectDTO project) {
        projectIndexService.indexProject(project);
        return RestResponse.success();
    }

    @Operation(summary = "从索引移除标的（内部调用：流标/删除）")
    @DeleteMapping(value = "/l/projects/indexes/{id}")
    public RestResponse<Void> removeProject(@PathVariable("id") Long id) {
        projectIndexService.removeProject(id);
        return RestResponse.success();
    }
}