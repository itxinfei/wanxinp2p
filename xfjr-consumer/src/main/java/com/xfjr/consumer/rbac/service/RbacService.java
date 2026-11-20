package com.xfjr.consumer.rbac.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xfjr.common.domain.BusinessException;
import com.xfjr.consumer.common.ConsumerErrorCode;
import com.xfjr.consumer.mapper.AccountRoleMapper;
import com.xfjr.consumer.mapper.MenuMapper;
import com.xfjr.consumer.mapper.PrivilegeMapper;
import com.xfjr.consumer.mapper.RoleMapper;
import com.xfjr.consumer.mapper.RolePrivilegeMapper;
import com.xfjr.consumer.rbac.entity.AccountRole;
import com.xfjr.consumer.rbac.entity.Menu;
import com.xfjr.consumer.rbac.entity.Privilege;
import com.xfjr.consumer.rbac.entity.Role;
import com.xfjr.consumer.rbac.entity.RolePrivilege;
import com.xfjr.consumer.rbac.model.AccountDTO;
import com.xfjr.consumer.rbac.model.RoleDTO;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 管理后台 RBAC（FR-131）：启用六表 role/menu/privilege/privilege_group/role_privilege/account_role。
 * 提供按账号查授权菜单（AC1）、角色/账号管理（AC4 权限变更）、权限校验（AC2，等价 @PreAuthorize 的授权判定）。
 */
@Service
public class RbacService {

    private final RoleMapper roleMapper;
    private final MenuMapper menuMapper;
    private final PrivilegeMapper privilegeMapper;
    private final RolePrivilegeMapper rolePrivilegeMapper;
    private final AccountRoleMapper accountRoleMapper;

    public RbacService(RoleMapper roleMapper, MenuMapper menuMapper, PrivilegeMapper privilegeMapper,
                       RolePrivilegeMapper rolePrivilegeMapper, AccountRoleMapper accountRoleMapper) {
        this.roleMapper = roleMapper;
        this.menuMapper = menuMapper;
        this.privilegeMapper = privilegeMapper;
        this.rolePrivilegeMapper = rolePrivilegeMapper;
        this.accountRoleMapper = accountRoleMapper;
    }

    /** 按账号查授权菜单（AC1：仅返回授权菜单；未绑定权限的菜单视为公共菜单）。 */
    public List<Menu> listMenusByAccount(Long accountId) {
        List<Long> roleIds = roleIdsOfAccount(accountId);
        List<String> privCodes = privilegeCodesOfRoles(roleIds);
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<Menu>()
                .eq(Menu::getStatus, 1)
                .and(w -> w.isNull(Menu::getPrivilegeCode)
                        .or().in(!privCodes.isEmpty(), Menu::getPrivilegeCode, privCodes))
                .orderByAsc(Menu::getSort);
        return menuMapper.selectList(wrapper);
    }

    /** 角色列表（含各角色关联的权限 id）。 */
    public List<RoleDTO> listRoles() {
        List<Role> roles = roleMapper.selectList(null);
        if (roles.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> roleIds = roles.stream().map(Role::getId).collect(Collectors.toList());
        List<RolePrivilege> allRps = rolePrivilegeMapper.selectList(
                new LambdaQueryWrapper<RolePrivilege>().in(RolePrivilege::getRoleId, roleIds));
        Map<Long, List<Long>> rolePrivMap = allRps.stream()
                .collect(Collectors.groupingBy(RolePrivilege::getRoleId,
                        Collectors.mapping(RolePrivilege::getPrivilegeId, Collectors.toList())));
        return roles.stream().map(r -> {
            RoleDTO dto = new RoleDTO();
            dto.setId(r.getId());
            dto.setName(r.getName());
            dto.setCode(r.getCode());
            dto.setPrivilegeIds(rolePrivMap.getOrDefault(r.getId(), Collections.emptyList()));
            return dto;
        }).collect(Collectors.toList());
    }

    /** 创建角色（含权限关联）。 */
    @Transactional(rollbackFor = Exception.class)
    public RoleDTO createRole(RoleDTO req) {
        if (req == null || isBlank(req.getCode())) {
            throw new BusinessException(ConsumerErrorCode.E_140502, "角色编码不能为空");
        }
        Role role = new Role();
        role.setName(req.getName());
        role.setCode(req.getCode());
        roleMapper.insert(role);
        linkRolePrivileges(role.getId(), req.getPrivilegeIds());
        req.setId(role.getId());
        return req;
    }

    /** 更新角色（重建权限关联，AC4 权限变更）。 */
    @Transactional(rollbackFor = Exception.class)
    public RoleDTO updateRole(Long roleId, RoleDTO req) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ConsumerErrorCode.E_140502, "角色不存在: " + roleId);
        }
        role.setName(req.getName());
        role.setCode(req.getCode());
        roleMapper.updateById(role);
        rolePrivilegeMapper.delete(new LambdaQueryWrapper<RolePrivilege>().eq(RolePrivilege::getRoleId, roleId));
        linkRolePrivileges(roleId, req.getPrivilegeIds());
        RoleDTO dto = new RoleDTO();
        dto.setId(roleId);
        dto.setName(role.getName());
        dto.setCode(role.getCode());
        dto.setPrivilegeIds(req.getPrivilegeIds());
        return dto;
    }

    /** 账号-角色列表。 */
    public List<AccountDTO> listAccounts() {
        List<AccountRole> all = accountRoleMapper.selectList(null);
        Map<Long, List<Long>> accRoleMap = all.stream()
                .collect(Collectors.groupingBy(AccountRole::getAccountId,
                        Collectors.mapping(AccountRole::getRoleId, Collectors.toList())));
        return accRoleMap.entrySet().stream().map(e -> {
            AccountDTO dto = new AccountDTO();
            dto.setAccountId(e.getKey());
            dto.setRoleIds(e.getValue());
            return dto;
        }).collect(Collectors.toList());
    }

    /** 绑定账号-角色（重建，AC4）。 */
    @Transactional(rollbackFor = Exception.class)
    public AccountDTO createAccount(AccountDTO req) {
        if (req == null || req.getAccountId() == null) {
            throw new BusinessException(ConsumerErrorCode.E_140502, "账号不能为空");
        }
        accountRoleMapper.delete(new LambdaQueryWrapper<AccountRole>().eq(AccountRole::getAccountId, req.getAccountId()));
        if (req.getRoleIds() != null) {
            for (Long roleId : req.getRoleIds()) {
                AccountRole ar = new AccountRole();
                ar.setAccountId(req.getAccountId());
                ar.setRoleId(roleId);
                accountRoleMapper.insert(ar);
            }
        }
        return req;
    }

    /** 权限校验（AC2）：账号是否拥有指定权限编码。等价于 @PreAuthorize 的授权判定。 */
    public boolean hasAuthority(Long accountId, String code) {
        if (accountId == null || isBlank(code)) {
            return false;
        }
        List<Long> roleIds = roleIdsOfAccount(accountId);
        if (roleIds.isEmpty()) {
            return false;
        }
        List<Long> privIds = privilegeIdsOfRoles(roleIds);
        if (privIds.isEmpty()) {
            return false;
        }
        Long count = privilegeMapper.selectCount(
                new LambdaQueryWrapper<Privilege>().in(Privilege::getId, privIds).eq(Privilege::getCode, code));
        return count != null && count > 0;
    }

    private List<Long> roleIdsOfAccount(Long accountId) {
        if (accountId == null) {
            return Collections.emptyList();
        }
        List<AccountRole> accountRoles = accountRoleMapper.selectList(
                new LambdaQueryWrapper<AccountRole>().eq(AccountRole::getAccountId, accountId));
        return accountRoles.stream().map(AccountRole::getRoleId).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<Long> privilegeIdsOfRoles(List<Long> roleIds) {
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<RolePrivilege> rps = rolePrivilegeMapper.selectList(
                new LambdaQueryWrapper<RolePrivilege>().in(RolePrivilege::getRoleId, roleIds));
        return rps.stream().map(RolePrivilege::getPrivilegeId).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<String> privilegeCodesOfRoles(List<Long> roleIds) {
        List<Long> privIds = privilegeIdsOfRoles(roleIds);
        if (privIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Privilege> privileges = privilegeMapper.selectList(
                new LambdaQueryWrapper<Privilege>().in(Privilege::getId, privIds));
        return privileges.stream().map(Privilege::getCode).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private void linkRolePrivileges(Long roleId, List<Long> privilegeIds) {
        if (privilegeIds == null) {
            return;
        }
        for (Long privilegeId : privilegeIds) {
            RolePrivilege rp = new RolePrivilege();
            rp.setRoleId(roleId);
            rp.setPrivilegeId(privilegeId);
            rolePrivilegeMapper.insert(rp);
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
