package com.xfjr.risk.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xfjr.api.risk.RiskListDTO;
import com.xfjr.api.risk.RiskListRequest;
import com.xfjr.common.domain.BusinessException;
import com.xfjr.risk.entity.RiskList;
import com.xfjr.risk.mapper.RiskListMapper;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("名单与额度管理（FR-122）")
class RiskListServiceTest {

    @Mock
    private RiskListMapper listMapper;
    @InjectMocks
    private RiskListService service;

    private RiskListRequest req(String type, String dim, String value) {
        RiskListRequest r = new RiskListRequest();
        r.setListType(type);
        r.setDimension(dim);
        r.setValue(value);
        r.setReason("测试原因");
        r.setOperator("alice");
        return r;
    }

    @Test
    @DisplayName("新增黑名单 → 生效、记录操作人并落库")
    void add_should_persist_blacklist_with_operator() {
        when(listMapper.insert(any(RiskList.class))).thenReturn(1);

        RiskListDTO dto = service.add(req("BLACK", "MOBILE", "13800000000"));

        assertEquals("BLACK", dto.getListType());
        assertEquals("MOBILE", dto.getDimension());
        assertEquals(1, dto.getStatus().intValue());
        assertEquals("alice", dto.getOperator());

        ArgumentCaptor<RiskList> cap = ArgumentCaptor.forClass(RiskList.class);
        verify(listMapper).insert(cap.capture());
        assertEquals("13800000000", cap.getValue().getValue());
        assertEquals(1, cap.getValue().getStatus().intValue());
    }

    @Test
    @DisplayName("名单类型非法 → 抛业务异常")
    void add_should_throw_when_type_invalid() {
        assertThrows(BusinessException.class, () -> service.add(req("GREEN", "MOBILE", "138")));
    }

    @Test
    @DisplayName("名单维度非法 → 抛业务异常")
    void add_should_throw_when_dimension_invalid() {
        assertThrows(BusinessException.class, () -> service.add(req("BLACK", "EMAIL", "a@b.c")));
    }

    @Test
    @DisplayName("名单值为空 → 抛业务异常")
    void add_should_throw_when_value_blank() {
        assertThrows(BusinessException.class, () -> service.add(req("BLACK", "MOBILE", "   ")));
    }

    @Test
    @DisplayName("删除存在的名单 → 删除")
    void delete_should_remove_existing() {
        RiskList e = new RiskList();
        e.setId(5L);
        when(listMapper.selectById(5L)).thenReturn(e);

        service.delete(5L);

        verify(listMapper).deleteById(5L);
    }

    @Test
    @DisplayName("删除不存在的名单 → 抛业务异常")
    void delete_should_throw_when_not_found() {
        when(listMapper.selectById(99L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> service.delete(99L));
    }

    @Test
    @DisplayName("按类型查询名单（大小写不敏感）")
    void list_should_filter_by_type() {
        when(listMapper.selectList(any())).thenReturn(List.of(new RiskList()));

        List<RiskListDTO> dtos = service.list("black");

        assertEquals(1, dtos.size());
    }

    @Test
    @DisplayName("查询不指定类型 → 返回全部")
    void list_should_return_all_when_type_absent() {
        when(listMapper.selectList(any())).thenReturn(Collections.emptyList());

        List<RiskListDTO> dtos = service.list(null);

        assertEquals(0, dtos.size());
    }

    @Test
    @DisplayName("查询非法类型 → 抛业务异常")
    void list_should_throw_when_type_invalid() {
        assertThrows(BusinessException.class, () -> service.list("PURPLE"));
    }
}
