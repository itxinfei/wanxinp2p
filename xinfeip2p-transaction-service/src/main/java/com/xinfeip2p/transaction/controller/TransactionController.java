package com.xinfeip2p.transaction.controller;

import com.xinfeip2p.api.transaction.TransactionApi;
import com.xinfeip2p.api.transaction.model.*;
import com.xinfeip2p.common.domain.PageVO;
import com.xinfeip2p.common.domain.RestResponse;
import com.xinfeip2p.transaction.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "transaction")
@RestController
public class TransactionController implements TransactionApi {

    @Autowired
    private ProjectService projectService;

    @Override
    @Operation(summary = "借款人发标")
    @PostMapping("/my/projects")
    public RestResponse<ProjectDTO> createProject(@RequestBody ProjectDTO
                                                          projectDTO) {
        ProjectDTO dto = projectService.createProject(projectDTO);
        return RestResponse.success(dto);
    }

    @Override
    @Operation(summary = "检索标的信息")
    @Parameters({
            @Parameter(name = "order", description = "顺序", in = ParameterIn.QUERY),
            @Parameter(name = "pageNo", description = "页码", required = true, in = ParameterIn.QUERY),
            @Parameter(name = "pageSize", description = "每页记录数", required = true, in = ParameterIn.QUERY),
            @Parameter(name = "sortBy", description = "排序字段", required = true, in = ParameterIn.QUERY)})
    @PostMapping("/projects/q")
    public RestResponse<PageVO<ProjectDTO>> queryProjects(@RequestBody ProjectQueryDTO projectQueryDTO, String order,
                                                                  Integer pageNo, Integer pageSize, String sortBy) {
        PageVO<ProjectDTO> projects =
                projectService.queryProjectsByQueryDTO(projectQueryDTO, order, pageNo, pageSize,sortBy);
        return RestResponse.success(projects);
     }

    @Override
    @Operation(summary = "管理员审核标的信息")
    @Parameters({
            @Parameter(name = "id", description = "标的id", required = true, in = ParameterIn.PATH),
            @Parameter(name = "approveStatus", description = "审批状态", required = true, in = ParameterIn.PATH)})
    @PutMapping("/m/projects/{id}/projectStatus/{approveStatus}")
    public RestResponse<String> projectsApprovalStatus(
            @PathVariable("id") Long id,
            @PathVariable("approveStatus") String approveStatus){
        String result = projectService.projectsApprovalStatus(id,approveStatus);
        return RestResponse.success(result);
    }

    @Override
    @Operation(summary = "从ES检索标的信息")
    @Parameters({
            @Parameter(name = "order", description = "顺序", in = ParameterIn.QUERY),
            @Parameter(name = "pageNo", description = "页码", required = true, in = ParameterIn.QUERY),
            @Parameter(name = "pageSize", description = "每页记录数", required = true, in = ParameterIn.QUERY),
            @Parameter(name = "sortBy", description = "排序字段", in = ParameterIn.QUERY)})
    @PostMapping("/projects/indexes/q")
    public RestResponse<PageVO<ProjectDTO>> queryProjects(@RequestBody ProjectQueryDTO projectQueryDTO, Integer pageNo, Integer pageSize, String sortBy,String order){
        PageVO<ProjectDTO> projects = projectService.queryProjects(projectQueryDTO,
                order, pageNo, pageSize, sortBy);
        return RestResponse.success(projects);
    }

    @Override
    @Operation(summary = "通过ids获取多个标的")
    @GetMapping("/projects/{ids}")
    public RestResponse<List<ProjectDTO>> queryProjectsIds(@PathVariable String ids) {
        List<ProjectDTO> projectDTOS=projectService.queryProjectsIds(ids);
        return RestResponse.success(projectDTOS);
    }

    @Override
    @Operation(summary = "根据标的id查询投标记录")
    @GetMapping("/tenders/projects/{id}")
    public RestResponse<List<TenderOverviewDTO>> queryTendersByProjectId(@PathVariable Long id) {
       return  RestResponse.success(projectService.queryTendersByProjectId(id));
    }

    @Override
    @Operation(summary = "用户投标")
    @PostMapping("/my/tenders")
    public RestResponse<TenderDTO> createTender(@RequestBody ProjectInvestDTO projectInvestDTO) {
        TenderDTO tenderDTO=projectService.createTender(projectInvestDTO);
        return RestResponse.success(tenderDTO);
    }

    @Override
    @Operation(summary = "审核标的满标放款")
    @Parameters({
            @Parameter(name = "id", description = "标的id", required = true, in = ParameterIn.PATH),
            @Parameter(name = "approveStatus", description = "标的状态", required = true, in = ParameterIn.PATH),
            @Parameter(name = "commission", description = "平台佣金", required = true, in = ParameterIn.QUERY)})
    @PutMapping("/m/loans/{id}/projectStatus/{approveStatus}")
    public RestResponse<String> loansApprovalStatus(@PathVariable("id") Long id, @PathVariable("approveStatus") String approveStatus, String commission) {
        String result=projectService.loansApprovalStatus(id,approveStatus,commission);
        return RestResponse.success(result);
    }
}