package com.xfjr.consumer.rbac.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.xfjr.common.domain.BusinessException;
import com.xfjr.consumer.common.ConsumerErrorCode;
import com.xfjr.consumer.mapper.AccountRoleMapper;
import com.xfjr.consumer.mapper.MenuMapper;
import com.xfjr.consumer.mapper.PrivilegeMapper;
import com.xfjr.consumer.mapper.RoleMapper;
import com.xfjr.consumer.mapper.RolePrivilegeMapper;
import com.xfjr.consumer.rbac.entity.AccountRole;
import com.xfjr.consumer.rbac.entity.Privilege;
import com.xfjr.consumer.rbac.entity.RolePrivilege;
import com.xfjr.consumer.rbac.model.RoleDTO;
import java.util.Collections;
import java.util.List;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("管理后台 RBAC（FR-131）：权限校验 + 角色管理")
class RbacServiceTest {

    /** 纯 Mockito 测试无 MyBatis-Plus 上下文，需手动初始化实体 TableInfo 以支持 Lambda 列解析。 */
    @BeforeAll
    static void initTableInfo() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), AccountRole.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), RolePrivilege.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), Privilege.class);
    }

    @Mock
    private RoleMapper roleMapper;
    @Mock
    private MenuMapper menuMapper;
    @Mock
    private PrivilegeMapper privilegeMapper;
    @Mock
    private RolePrivilegeMapper rolePrivilegeMapper;
    @Mock
    private AccountRoleMapper accountRoleMapper;
    @InjectMocks
    private RbacService service;

    @Test
    @DisplayName("AC2 账号经角色持有匹配权限 → true")
    void hasAuthority_true() {
        AccountRole ar = new AccountRole();
        ar.setRoleId(1L);
        when(accountRoleMapper.selectList(any())).thenReturn(List.of(ar));
        RolePrivilege rp = new RolePrivilege();
        rp.setPrivilegeId(10L);
        when(rolePrivilegeMapper.selectList(any())).thenReturn(List.of(rp));
        when(privilegeMapper.selectCount(any())).thenReturn(1L);

        assertTrue(service.hasAuthority(100L, "project:approve"));
    }

    @Test
    @DisplayName("AC2 角色不持有该权限 → false")
    void hasAuthority_false_when_code_absent() {
        AccountRole ar = new AccountRole();
        ar.setRoleId(1L);
        when(accountRoleMapper.selectList(any())).thenReturn(List.of(ar));
        RolePrivilege rp = new RolePrivilege();
        rp.setPrivilegeId(10L);
        when(rolePrivilegeMapper.selectList(any())).thenReturn(List.of(rp));
        when(privilegeMapper.selectCount(any())).thenReturn(0L);

        assertFalse(service.hasAuthority(100L, "project:approve"));
    }

    @Test
    @DisplayName("账号无角色 → false（提前返回）")
    void hasAuthority_false_when_no_roles() {
        when(accountRoleMapper.selectList(any())).thenReturn(Collections.emptyList());

        assertFalse(service.hasAuthority(100L, "project:approve"));
        verify(rolePrivilegeMapper, never()).selectList(any());
    }

    @Test
    @DisplayName("AC4 创建角色 → 落库并建立权限关联")
    void createRole_persists_with_privileges() {
        RoleDTO req = new RoleDTO();
        req.setCode("admin");
        req.setName("运营管理员");
        req.setPrivilegeIds(List.of(10L, 20L));

        RoleDTO dto = service.createRole(req);

        assertEquals("admin", dto.getCode());
        verify(roleMapper).insert(any(com.xfjr.consumer.rbac.entity.Role.class));
        verify(rolePrivilegeMapper, times(2)).insert(any(RolePrivilege.class));
    }

    @Test
    @DisplayName("角色编码为空 → 抛 E_140502")
    void createRole_throws_when_code_blank() {
        RoleDTO req = new RoleDTO();
        req.setName("无编码");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.createRole(req));

        assertEquals(ConsumerErrorCode.E_140502, ex.getErrorCode());
    }

    @Test
    @DisplayName("更新不存在的角色 → 抛 E_140502")
    void updateRole_throws_when_not_found() {
        when(roleMapper.selectById(99L)).thenReturn(null);
        RoleDTO req = new RoleDTO();
        req.setCode("admin");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.updateRole(99L, req));

        assertEquals(ConsumerErrorCode.E_140502, ex.getErrorCode());
    }
}
