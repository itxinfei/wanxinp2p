package com.xfjr.search.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.xfjr.api.search.model.ProjectQueryParamsDTO;
import com.xfjr.api.transaction.model.ProjectDTO;
import com.xfjr.common.domain.BusinessException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("标的全文检索（FR-126）")
class ProjectIndexServiceTest {

    @Mock
    private RestHighLevelClient client;
    @InjectMocks
    private ProjectIndexServiceImpl service;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "projectIndex", "p2p_project");
    }

    @Test
    @DisplayName("AC1 ES 查询异常 → 抛出 BusinessException（不再吞成 null）")
    void query_should_throw_when_es_fails() throws Exception {
        when(client.search(any(SearchRequest.class), any(RequestOptions.class)))
                .thenThrow(new RuntimeException("connect timeout"));
        ProjectQueryParamsDTO dto = new ProjectQueryParamsDTO();
        dto.setKeyword("测试");

        assertThrows(BusinessException.class, () -> service.queryProjectIndex(dto, 1, 10, "amount", "desc"));
    }

    @Test
    @DisplayName("AC 索引写入 id 为空 → 抛 BusinessException")
    void index_should_throw_when_id_null() {
        ProjectDTO p = new ProjectDTO();
        assertThrows(BusinessException.class, () -> service.indexProject(p));
    }

    @Test
    @DisplayName("AC 删除 id 为空 → 抛 BusinessException")
    void remove_should_throw_when_id_null() {
        assertThrows(BusinessException.class, () -> service.removeProject(null));
    }
}
