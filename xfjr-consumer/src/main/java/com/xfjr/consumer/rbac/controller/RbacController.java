package com.xfjr.consumer.rbac.controller;

import com.xfjr.common.domain.BusinessException;
import com.xfjr.common.domain.RestResponse;
import com.xfjr.consumer.common.ConsumerErrorCode;
import com.xfjr.consumer.rbac.entity.Menu;
import com.xfjr.consumer.rbac.model.AccountDTO;
import com.xfjr.consumer.rbac.model.PermCheckDTO;
import com.xfjr.consumer.rbac.model.RoleDTO;
import com.xfjr.consumer.rbac.service.RbacService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理后台 RBAC（FR-131）。/api/v1 管理接口。
 * 鉴权采用服务级权限校验（hasAuthority），等价于 @PreAuthorize 的授权判定；
 * 当服务启用 OAuth2 资源服务器后可无缝替换为 Spring Security 方法级注解。
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Rbac", description = "管理后台与RBAC API")
public class RbacController {

    @Autowired
    private RbacService rbacService;

    @Operation(summary = "当前用户菜单")
    @GetMapping("/menus")
    public RestResponse<List<Menu>> menus(@RequestParam("accountId") Long accountId) {
        return RestResponse.success(rbacService.listMenusByAccount(accountId));
    }

    @Operation(summary = "角色列表")
    @GetMapping("/roles")
    public RestResponse<List<RoleDTO>> roles() {
        return RestResponse.success(rbacService.listRoles());
    }

    @Operation(summary = "创建角色")
    @PostMapping("/roles")
    public RestResponse<RoleDTO> createRole(@RequestBody RoleDTO req) {
        return RestResponse.success(rbacService.createRole(req));
    }

    @Operation(summary = "更新角色")
    @PutMapping("/roles/{id}")
    public RestResponse<RoleDTO> updateRole(@PathVariable("id") Long id, @RequestBody RoleDTO req) {
        return RestResponse.success(rbacService.updateRole(id, req));
    }

    @Operation(summary = "账号-角色列表")
    @GetMapping("/accounts")
    public RestResponse<List<AccountDTO>> accounts() {
        return RestResponse.success(rbacService.listAccounts());
    }

    @Operation(summary = "绑定账号-角色")
    @PostMapping("/accounts")
    public RestResponse<AccountDTO> createAccount(@RequestBody AccountDTO req) {
        return RestResponse.success(rbacService.createAccount(req));
    }

    @Operation(summary = "权限校验（无权限返回403，AC2）")
    @PostMapping("/perm/check")
    public RestResponse<Boolean> check(@RequestBody PermCheckDTO req) {
        if (!rbacService.hasAuthority(req.getAccountId(), req.getCode())) {
            throw new BusinessException(ConsumerErrorCode.E_140501, "无权限访问: " + req.getCode());
        }
        return RestResponse.success(true);
    }
}
