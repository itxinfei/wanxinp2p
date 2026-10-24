package com.xfjr.search.service;

import com.xfjr.api.search.model.ProjectQueryParamsDTO;
import com.xfjr.api.transaction.model.ProjectDTO;
import com.xfjr.common.domain.BusinessException;
import com.xfjr.common.domain.PageVO;
import com.xfjr.search.common.constant.ContentSearchErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 标的全文检索（FR-126）：多维筛选 + 排序 + 分页 + 索引同步（近实时）。
 * 查询异常向上抛出（不再吞成 null），保证与数据库查询一致（AC1）。
 */
@Slf4j
@Service
public class ProjectIndexServiceImpl implements ProjectIndexService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Value("${xfjr.es.index}")
    private String projectIndex;

    @Override
    public PageVO<ProjectDTO> queryProjectIndex(ProjectQueryParamsDTO projectQueryParamsDTO, Integer pageNo, Integer pageSize, String sortBy, String order) {
        //1.创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest(projectIndex);

        //2.搜索条件（多维筛选）
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        //2.1 全文关键词：名称/描述分词匹配（OR）
        if (StringUtils.isNotBlank(projectQueryParamsDTO.getKeyword())) {
            queryBuilder.must(QueryBuilders.boolQuery()
                    .should(QueryBuilders.matchQuery("name", projectQueryParamsDTO.getKeyword()))
                    .should(QueryBuilders.matchQuery("description", projectQueryParamsDTO.getKeyword()))
                    .minimumShouldMatch(1));
        }
        //2.2 名称分词
        if (StringUtils.isNotBlank(projectQueryParamsDTO.getName())) {
            queryBuilder.must(QueryBuilders.matchQuery("name", projectQueryParamsDTO.getName()));
        }
        //2.3 描述分词
        if (StringUtils.isNotBlank(projectQueryParamsDTO.getDescription())) {
            queryBuilder.must(QueryBuilders.matchQuery("description", projectQueryParamsDTO.getDescription()));
        }
        //2.4 金额区间
        if (projectQueryParamsDTO.getAmountMin() != null) {
            queryBuilder.must(QueryBuilders.rangeQuery("amount").gte(projectQueryParamsDTO.getAmountMin()));
        }
        if (projectQueryParamsDTO.getAmountMax() != null) {
            queryBuilder.must(QueryBuilders.rangeQuery("amount").lte(projectQueryParamsDTO.getAmountMax()));
        }
        //2.5 期限区间
        if (projectQueryParamsDTO.getStartPeriod() != null) {
            queryBuilder.must(QueryBuilders.rangeQuery("period").gte(projectQueryParamsDTO.getStartPeriod()));
        }
        if (projectQueryParamsDTO.getEndPeriod() != null) {
            queryBuilder.must(QueryBuilders.rangeQuery("period").lte(projectQueryParamsDTO.getEndPeriod()));
        }
        //2.6 年化利率区间
        if (projectQueryParamsDTO.getStartAnnualRate() != null) {
            queryBuilder.must(QueryBuilders.rangeQuery("annualrate").gte(projectQueryParamsDTO.getStartAnnualRate()));
        }
        if (projectQueryParamsDTO.getEndAnnualRate() != null) {
            queryBuilder.must(QueryBuilders.rangeQuery("annualrate").lte(projectQueryParamsDTO.getEndAnnualRate()));
        }
        //2.7 标的状态
        if (StringUtils.isNotBlank(projectQueryParamsDTO.getProjectStatus())) {
            queryBuilder.must(QueryBuilders.termQuery("projectstatus", projectQueryParamsDTO.getProjectStatus()));
        }
        //2.8 可用状态
        if (projectQueryParamsDTO.getStatus() != null) {
            queryBuilder.must(QueryBuilders.termQuery("status", projectQueryParamsDTO.getStatus()));
        }
        //2.9 是否债权出让
        if (projectQueryParamsDTO.getIsAssignment() != null) {
            queryBuilder.must(QueryBuilders.termQuery("isassignment", projectQueryParamsDTO.getIsAssignment()));
        }
        //2.10 还款方式
        if (StringUtils.isNotBlank(projectQueryParamsDTO.getRepaymentWay())) {
            queryBuilder.must(QueryBuilders.termQuery("repaymentway", projectQueryParamsDTO.getRepaymentWay()));
        }

        //3.创建SearchSourceBuilder对象---总的封装对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        //3.1 设置排序参数
        if (StringUtils.isNotBlank(sortBy) && StringUtils.isNotBlank(order)) {
            if (order.equalsIgnoreCase("asc")) {
                searchSourceBuilder.sort(sortBy, SortOrder.ASC);
            } else if (order.equalsIgnoreCase("desc")) {
                searchSourceBuilder.sort(sortBy, SortOrder.DESC);
            }
        } else {
            searchSourceBuilder.sort("createdate", SortOrder.DESC);
        }
        //3.2 设置分页参数
        searchSourceBuilder.from((pageNo - 1) * pageSize);
        searchSourceBuilder.size(pageSize);
        searchRequest.source(searchSourceBuilder);

        //4.执行搜索
        List<ProjectDTO> list = new ArrayList<>();
        PageVO<ProjectDTO> pageVO = new PageVO<>();
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits hits = searchResponse.getHits();
            long totalHits = hits.getTotalHits().value;
            pageVO.setTotal(totalHits);
            for (SearchHit hit : hits.getHits()) {
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                ProjectDTO projectDTO = new ProjectDTO();
                Object amount = sourceAsMap.get("amount");
                Object annualRate = sourceAsMap.get("annualrate");
                projectDTO.setAmount(amount == null ? null : new BigDecimal(amount.toString()));
                projectDTO.setProjectStatus((String) sourceAsMap.get("projectstatus"));
                Object period = sourceAsMap.get("period");
                projectDTO.setPeriod(period == null ? null : Integer.parseInt(period.toString()));
                projectDTO.setName((String) sourceAsMap.get("name"));
                projectDTO.setDescription((String) sourceAsMap.get("description"));
                projectDTO.setAnnualRate(annualRate == null ? null : new BigDecimal(annualRate.toString()));
                Object id = sourceAsMap.get("id");
                projectDTO.setId(id == null ? null : Long.parseLong(id.toString()));
                list.add(projectDTO);
            }
        } catch (Exception e) {
            log.error("标的检索异常", e);
            throw new BusinessException(ContentSearchErrorCode.E_190101, "标的检索失败: " + e.getMessage());
        }
        //5.封装为PageVO对象并返回
        pageVO.setContent(list);
        pageVO.setPageSize(pageSize);
        pageVO.setPageNo(pageNo);
        return pageVO;
    }

    @Override
    public void indexProject(ProjectDTO project) {
        if (project == null || project.getId() == null) {
            throw new BusinessException(ContentSearchErrorCode.E_190201, "标的 id 不能为空");
        }
        try {
            IndexRequest indexRequest = new IndexRequest(projectIndex)
                    .id(String.valueOf(project.getId()))
                    .source(toEsDoc(project), XContentType.JSON);
            IndexResponse resp = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
            if (resp.status() != RestStatus.CREATED && resp.status() != RestStatus.OK) {
                throw new BusinessException(ContentSearchErrorCode.E_190201, "索引写入失败: " + resp.status());
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("标的索引写入异常 id={}", project.getId(), e);
            throw new BusinessException(ContentSearchErrorCode.E_190201, "索引写入失败: " + e.getMessage());
        }
    }

    @Override
    public void removeProject(Long id) {
        if (id == null) {
            throw new BusinessException(ContentSearchErrorCode.E_190202, "标的 id 不能为空");
        }
        try {
            DeleteRequest deleteRequest = new DeleteRequest(projectIndex, String.valueOf(id));
            DeleteResponse resp = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
            if (resp.status() != RestStatus.OK) {
                throw new BusinessException(ContentSearchErrorCode.E_190202, "索引删除失败: " + resp.status());
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("标的索引删除异常 id={}", id, e);
            throw new BusinessException(ContentSearchErrorCode.E_190202, "索引删除失败: " + e.getMessage());
        }
    }

    /**
     * ProjectDTO → ES 文档字段（与检索使用的字段名保持一致：小写、无下划线）。
     */
    private Map<String, Object> toEsDoc(ProjectDTO p) {
        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("id", p.getId());
        doc.put("consumerid", p.getConsumerId());
        doc.put("userno", p.getUserNo());
        doc.put("projectno", p.getProjectNo());
        doc.put("name", p.getName());
        doc.put("description", p.getDescription());
        doc.put("type", p.getType());
        doc.put("period", p.getPeriod());
        doc.put("annualrate", p.getAnnualRate());
        doc.put("borrowerannualrate", p.getBorrowerAnnualRate());
        doc.put("commissionannualrate", p.getCommissionAnnualRate());
        doc.put("repaymentway", p.getRepaymentWay());
        doc.put("amount", p.getAmount());
        doc.put("remainingamount", p.getRemainingAmount());
        doc.put("projectstatus", p.getProjectStatus());
        doc.put("status", p.getStatus());
        doc.put("isassignment", p.getIsAssignment());
        doc.put("risk", p.getRisk());
        doc.put("tendercount", p.getTenderCount());
        doc.put("createdate", p.getCreateDate() == null ? null : p.getCreateDate().toString());
        return doc;
    }
}
