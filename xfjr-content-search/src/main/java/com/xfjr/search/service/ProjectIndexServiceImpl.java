package com.xfjr.search.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.xfjr.api.search.model.ProjectQueryParamsDTO;
import com.xfjr.api.transaction.model.ProjectDTO;
import com.xfjr.common.domain.BusinessException;
import com.xfjr.common.domain.PageVO;
import com.xfjr.search.common.constant.ContentSearchErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 标的全文检索（FR-126）：多维筛选 + 排序 + 分页 + 索引同步（近实时）。
 *
 * <p>I-06 改造：改用 Elasticsearch 低级 REST 客户端 + JSON 直构 DSL，不再依赖已停止维护的
 * HLRC 7.2 与老 core 包；服务器为 ES 9.5.7，接口层 DSL 兼容。查询异常向上抛出（不吞成 null）。
 */
@Slf4j
@Service
public class ProjectIndexServiceImpl implements ProjectIndexService {

    @Autowired
    private RestClient restClient;

    @Value("${xfjr.es.index}")
    private String projectIndex;

    @Override
    public PageVO<ProjectDTO> queryProjectIndex(ProjectQueryParamsDTO projectQueryParamsDTO, Integer pageNo, Integer pageSize, String sortBy, String order) {
        Map<String, Object> searchBody = new LinkedHashMap<>();
        searchBody.put("query", buildQuery(projectQueryParamsDTO));

        List<Object> sorts = new ArrayList<>();
        if (StringUtils.isNotBlank(sortBy) && StringUtils.isNotBlank(order)
                && ("asc".equalsIgnoreCase(order) || "desc".equalsIgnoreCase(order))) {
            sorts.add(Map.of(sortBy, Map.of("order", order.toLowerCase())));
        } else {
            sorts.add(Map.of("createdate", Map.of("order", "desc")));
        }
        searchBody.put("sort", sorts);
        searchBody.put("from", (pageNo - 1) * pageSize);
        searchBody.put("size", pageSize);

        List<ProjectDTO> list = new ArrayList<>();
        PageVO<ProjectDTO> pageVO = new PageVO<>();
        try {
            Request request = new Request("POST", "/" + projectIndex + "/_search");
            request.setJsonEntity(JSON.toJSONString(searchBody));
            Response response = restClient.performRequest(request);
            String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            JSONObject root = JSON.parseObject(body);
            JSONObject hits = root.getJSONObject("hits");
            if (hits == null) {
                throw new IllegalStateException("ES 响应缺少 hits 节点: " + body);
            }
            pageVO.setTotal(hits.getJSONObject("total").getLongValue("value"));
            JSONArray hitArray = hits.getJSONArray("hits");
            for (int i = 0; i < hitArray.size(); i++) {
                JSONObject source = hitArray.getJSONObject(i).getJSONObject("_source");
                list.add(toProjectDTO(source));
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("标的检索异常", e);
            throw new BusinessException(ContentSearchErrorCode.E_190101, "标的检索失败: " + e.getMessage());
        }
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
            Request request = new Request("PUT", "/" + projectIndex + "/_doc/" + project.getId());
            request.setJsonEntity(JSON.toJSONString(toEsDoc(project)));
            Response response = restClient.performRequest(request);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200 && statusCode != 201) {
                throw new BusinessException(ContentSearchErrorCode.E_190201, "索引写入失败: HTTP " + statusCode);
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
            Request request = new Request("DELETE", "/" + projectIndex + "/_doc/" + id);
            restClient.performRequest(request);
        } catch (ResponseException e) {
            // 文档不存在按幂等成功处理，避免重复清理报错
            if (e.getResponse().getStatusLine().getStatusCode() != 404) {
                log.error("标的索引删除异常 id={}", id, e);
                throw new BusinessException(ContentSearchErrorCode.E_190202, "索引删除失败: " + e.getMessage());
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("标的索引删除异常 id={}", id, e);
            throw new BusinessException(ContentSearchErrorCode.E_190202, "索引删除失败: " + e.getMessage());
        }
    }

    /**
     * 构造 bool 查询 DSL，与原 QueryBuilders 版本语义一致（多维筛选、关键词 OR 匹配）。
     */
    private Map<String, Object> buildQuery(ProjectQueryParamsDTO query) {
        List<Object> must = new ArrayList<>();

        if (StringUtils.isNotBlank(query.getKeyword())) {
            Map<String, Object> keywordBool = new LinkedHashMap<>();
            keywordBool.put("should", List.of(
                    Map.of("match", Map.of("name", query.getKeyword())),
                    Map.of("match", Map.of("description", query.getKeyword()))));
            keywordBool.put("minimum_should_match", 1);
            must.add(Map.of("bool", keywordBool));
        }
        if (StringUtils.isNotBlank(query.getName())) {
            must.add(Map.of("match", Map.of("name", query.getName())));
        }
        if (StringUtils.isNotBlank(query.getDescription())) {
            must.add(Map.of("match", Map.of("description", query.getDescription())));
        }
        addRange(must, "amount", query.getAmountMin(), query.getAmountMax());
        addRange(must, "period", query.getStartPeriod(), query.getEndPeriod());
        addRange(must, "annualrate", query.getStartAnnualRate(), query.getEndAnnualRate());
        if (StringUtils.isNotBlank(query.getProjectStatus())) {
            must.add(Map.of("term", Map.of("projectstatus", query.getProjectStatus())));
        }
        if (query.getStatus() != null) {
            must.add(Map.of("term", Map.of("status", query.getStatus())));
        }
        if (query.getIsAssignment() != null) {
            must.add(Map.of("term", Map.of("isassignment", query.getIsAssignment())));
        }
        if (StringUtils.isNotBlank(query.getRepaymentWay())) {
            must.add(Map.of("term", Map.of("repaymentway", query.getRepaymentWay())));
        }

        Map<String, Object> bool = new LinkedHashMap<>();
        bool.put("must", must);
        return Map.of("bool", bool);
    }

    /**
     * 追加 range 条件（单侧或双侧区间）。
     */
    private void addRange(List<Object> must, String field, Object min, Object max) {
        if (min == null && max == null) {
            return;
        }
        Map<String, Object> rangeValue = new LinkedHashMap<>();
        if (min != null) {
            rangeValue.put("gte", min);
        }
        if (max != null) {
            rangeValue.put("lte", max);
        }
        must.add(Map.of("range", Map.of(field, rangeValue)));
    }

    /**
     * ES 文档字段 → ProjectDTO（字段名与写入时保持一致）。
     */
    private ProjectDTO toProjectDTO(JSONObject source) {
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setId(source.getLong("id"));
        projectDTO.setName(source.getString("name"));
        projectDTO.setDescription(source.getString("description"));
        projectDTO.setProjectStatus(source.getString("projectstatus"));
        projectDTO.setAmount(source.getBigDecimal("amount"));
        projectDTO.setAnnualRate(source.getBigDecimal("annualrate"));
        projectDTO.setPeriod(source.getInteger("period"));
        return projectDTO;
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
