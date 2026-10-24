package com.xfjr.risk.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xfjr.api.risk.RiskDecisionDTO;
import com.xfjr.api.risk.RiskDecisionRequest;
import com.xfjr.common.domain.BusinessException;
import com.xfjr.risk.common.RiskErrorCode;
import com.xfjr.risk.entity.RiskDecision;
import com.xfjr.risk.entity.RiskList;
import com.xfjr.risk.entity.RiskRule;
import com.xfjr.risk.mapper.RiskDecisionMapper;
import com.xfjr.risk.mapper.RiskListMapper;
import com.xfjr.risk.mapper.RiskRuleMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 风控决策引擎（FR-121）。
 *
 * 决策流程（优先级从高到低）：
 * 1) 黑名单命中 → REJECT；
 * 2) 白名单命中 → PASS（可信，跳过规则引擎）；
 * 3) 规则引擎：以基础分为基准，命中规则按 risk_weight 扣分，
 *    依据阈值判定 PASS / REJECT / REVIEW。
 *
 * 规则与名单每次决策均从数据库实时加载，配置变更对后续请求即时生效，无需重启（AC2）。
 * 决策结果按 bizNo 落库留痕，可追溯完整决策链路（AC3），并对同一 bizNo 幂等。
 */
@Service
public class RiskDecisionService {

    private static final Logger log = LoggerFactory.getLogger(RiskDecisionService.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final RiskRuleMapper ruleMapper;
    private final RiskListMapper listMapper;
    private final RiskDecisionMapper decisionMapper;

    @Value("${risk.base-score:100}")
    private int baseScore;
    @Value("${risk.pass-threshold:80}")
    private int passThreshold;
    @Value("${risk.reject-threshold:40}")
    private int rejectThreshold;

    public RiskDecisionService(RiskRuleMapper ruleMapper,
                               RiskListMapper listMapper,
                               RiskDecisionMapper decisionMapper) {
        this.ruleMapper = ruleMapper;
        this.listMapper = listMapper;
        this.decisionMapper = decisionMapper;
    }

    public RiskDecisionDTO decide(RiskDecisionRequest req) {
        if (req == null || isBlank(req.getBizNo())) {
            throw new BusinessException(RiskErrorCode.E_180201, "业务流水号(bizNo)不能为空");
        }
        RiskDecision existing = findByBizNo(req.getBizNo());
        if (existing != null) {
            return toDto(existing);
        }

        // 1) 黑名单命中 → 直接拒绝
        List<RiskList> lists = listMapper.selectList(new LambdaQueryWrapper<RiskList>().eq(RiskList::getStatus, 1));
        for (RiskList l : lists) {
            if ("BLACK".equals(l.getListType()) && dimensionMatch(l, req)) {
                List<String> hits = new ArrayList<>();
                hits.add("BLACKLIST:" + l.getDimension() + ":" + l.getValue());
                return toDto(save(req, "REJECT", hits, 0, buildDetail(req, new ArrayList<>()), true));
            }
        }

        // 2) 白名单命中 → 直接放行
        for (RiskList l : lists) {
            if ("WHITE".equals(l.getListType()) && dimensionMatch(l, req)) {
                List<String> hits = new ArrayList<>();
                hits.add("WHITELIST:" + l.getDimension() + ":" + l.getValue());
                return toDto(save(req, "PASS", hits, baseScore, buildDetail(req, new ArrayList<>()), false));
            }
        }

        // 3) 规则引擎：按权重扣分，分数决定结论
        int score = baseScore;
        List<String> hits = new ArrayList<>();
        List<String> adjustments = new ArrayList<>();
        List<RiskRule> rules = ruleMapper.selectList(new LambdaQueryWrapper<RiskRule>().eq(RiskRule::getStatus, 1));
        for (RiskRule r : rules) {
            if (ruleTriggered(r, req)) {
                int weight = r.getRiskWeight() == null ? 0 : r.getRiskWeight();
                score = Math.max(0, score - weight);
                hits.add(r.getRuleCode());
                adjustments.add(r.getRuleCode() + ":-" + weight);
            }
        }
        String decision = score >= passThreshold ? "PASS"
                : (score < rejectThreshold ? "REJECT" : "REVIEW");
        return toDto(save(req, decision, hits, score, buildDetail(req, adjustments), false));
    }

    public RiskDecisionDTO getByBizNo(String bizNo) {
        RiskDecision d = findByBizNo(bizNo);
        if (d == null) {
            throw new BusinessException(RiskErrorCode.E_180202, "风控决策记录不存在: " + bizNo);
        }
        return toDto(d);
    }

    private RiskDecision findByBizNo(String bizNo) {
        List<RiskDecision> list = decisionMapper.selectList(
                new LambdaQueryWrapper<RiskDecision>().eq(RiskDecision::getBizNo, bizNo)
                        .orderByDesc(RiskDecision::getId));
        return list.isEmpty() ? null : list.get(0);
    }

    private RiskDecision save(RiskDecisionRequest req, String decision, List<String> hits,
                              int score, String detail, boolean blacklist) {
        RiskDecision d = new RiskDecision();
        d.setBizNo(req.getBizNo());
        d.setBizType(req.getBizType());
        d.setConsumerNo(req.getConsumerNo());
        d.setRuleHits(blacklist && hits.isEmpty() ? null : String.join(",", hits));
        d.setDecision(decision);
        d.setScore(score);
        d.setDetail(detail);
        d.setCreateDate(LocalDateTime.now());
        decisionMapper.insert(d);
        return d;
    }

    private boolean dimensionMatch(RiskList l, RiskDecisionRequest req) {
        String v = l.getValue() == null ? "" : l.getValue().trim();
        if (v.isEmpty() || l.getDimension() == null) {
            return false;
        }
        switch (l.getDimension()) {
            case "MOBILE": return v.equals(trimToEmpty(req.getMobile()));
            case "ID_CARD": return v.equals(trimToEmpty(req.getIdCard()));
            case "DEVICE": return v.equals(trimToEmpty(req.getDevice()));
            case "IP": return v.equals(trimToEmpty(req.getIp()));
            default: return false;
        }
    }

    /**
     * 规则触发判定。expression 为 JSON，形如 {"field":"applyAmount","op":">","value":50000}；
     * 对请求字段做数值比较。解析失败或不支持的字段视为不触发。
     */
    private boolean ruleTriggered(RiskRule r, RiskDecisionRequest req) {
        String expression = r.getExpression();
        if (isBlank(expression)) {
            return false;
        }
        try {
            JsonNode node = objectMapper.readTree(expression);
            String field = node.path("field").asText(null);
            String op = node.path("op").asText(">");
            double threshold = node.path("value").asDouble(Double.NaN);
            Double actual = resolveField(req, field);
            if (actual == null || Double.isNaN(threshold)) {
                return false;
            }
            return compare(actual, op, threshold);
        } catch (Exception e) {
            log.warn("风控规则表达式解析失败 ruleCode={}", r.getRuleCode(), e);
            return false;
        }
    }

    private Double resolveField(RiskDecisionRequest req, String field) {
        if (field == null) {
            return null;
        }
        switch (field) {
            case "applyAmount":
                return req.getApplyAmount() == null ? null : req.getApplyAmount().doubleValue();
            default:
                log.debug("风控决策暂不支持的字段: {}", field);
                return null;
        }
    }

    private boolean compare(double actual, String op, double value) {
        if (op == null) {
            return false;
        }
        switch (op) {
            case ">": return actual > value;
            case ">=": return actual >= value;
            case "<": return actual < value;
            case "<=": return actual <= value;
            case "==": return Double.compare(actual, value) == 0;
            case "!=": return Double.compare(actual, value) != 0;
            default: return false;
        }
    }

    private String buildDetail(RiskDecisionRequest req, List<String> adjustments) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("bizType", req.getBizType());
        m.put("consumerNo", req.getConsumerNo());
        m.put("adjustments", adjustments);
        try {
            return objectMapper.writeValueAsString(m);
        } catch (Exception e) {
            return "{}";
        }
    }

    private RiskDecisionDTO toDto(RiskDecision d) {
        RiskDecisionDTO dto = new RiskDecisionDTO();
        dto.setBizNo(d.getBizNo());
        dto.setBizType(d.getBizType());
        dto.setConsumerNo(d.getConsumerNo());
        dto.setDecision(d.getDecision());
        dto.setRuleHits(d.getRuleHits());
        dto.setScore(d.getScore());
        dto.setDetail(d.getDetail());
        dto.setCreateDate(d.getCreateDate());
        return dto;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String trimToEmpty(String s) {
        return s == null ? "" : s.trim();
    }
}
