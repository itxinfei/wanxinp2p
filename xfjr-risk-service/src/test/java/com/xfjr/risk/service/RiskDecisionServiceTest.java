package com.xfjr.risk.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xfjr.api.risk.RiskDecisionDTO;
import com.xfjr.api.risk.RiskDecisionRequest;
import com.xfjr.common.domain.BusinessException;
import com.xfjr.risk.entity.RiskDecision;
import com.xfjr.risk.entity.RiskList;
import com.xfjr.risk.entity.RiskRule;
import com.xfjr.risk.mapper.RiskDecisionMapper;
import com.xfjr.risk.mapper.RiskListMapper;
import com.xfjr.risk.mapper.RiskRuleMapper;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("风控决策引擎（FR-121）")
class RiskDecisionServiceTest {

    @Mock
    private RiskRuleMapper ruleMapper;
    @Mock
    private RiskListMapper listMapper;
    @Mock
    private RiskDecisionMapper decisionMapper;
    @InjectMocks
    private RiskDecisionService service;

    @BeforeEach
    void setUp() {
        // 与 application.yml 默认值一致
        ReflectionTestUtils.setField(service, "baseScore", 100);
        ReflectionTestUtils.setField(service, "passThreshold", 80);
        ReflectionTestUtils.setField(service, "rejectThreshold", 40);
    }

    private RiskDecisionRequest req(String bizNo, String mobile, BigDecimal amount) {
        RiskDecisionRequest r = new RiskDecisionRequest();
        r.setBizNo(bizNo);
        r.setBizType("ISSUE_LOAN");
        r.setMobile(mobile);
        r.setApplyAmount(amount);
        return r;
    }

    private RiskList list(String type, String dim, String value) {
        RiskList l = new RiskList();
        l.setListType(type);
        l.setDimension(dim);
        l.setValue(value);
        l.setStatus(1);
        return l;
    }

    private RiskRule rule(String code, int weight, String expression) {
        RiskRule r = new RiskRule();
        r.setRuleCode(code);
        r.setRuleType("QUOTA");
        r.setRiskWeight(weight);
        r.setExpression(expression);
        r.setStatus(1);
        return r;
    }

    @Test
    @DisplayName("命中黑名单 → 拒绝，评分 0，留痕命中项")
    void decide_should_reject_when_blacklist_hit() {
        when(decisionMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(listMapper.selectList(any())).thenReturn(List.of(list("BLACK", "MOBILE", "13800000000")));
        when(decisionMapper.insert(any(RiskDecision.class))).thenReturn(1);

        RiskDecisionDTO dto = service.decide(req("B1", "13800000000", BigDecimal.valueOf(100)));

        assertEquals("REJECT", dto.getDecision());
        assertEquals(0, dto.getScore().intValue());
        assertTrue(dto.getRuleHits().contains("BLACKLIST:MOBILE:13800000000"));
        verify(decisionMapper).insert(any(RiskDecision.class));
    }

    @Test
    @DisplayName("命中白名单 → 放行，评分取基础分")
    void decide_should_pass_when_whitelist_hit() {
        when(decisionMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(listMapper.selectList(any())).thenReturn(List.of(list("WHITE", "MOBILE", "13900000000")));
        when(decisionMapper.insert(any(RiskDecision.class))).thenReturn(1);

        RiskDecisionDTO dto = service.decide(req("B2", "13900000000", BigDecimal.valueOf(999999)));

        assertEquals("PASS", dto.getDecision());
        assertEquals(100, dto.getScore().intValue());
        assertTrue(dto.getRuleHits().contains("WHITELIST:MOBILE:13900000000"));
        // 白名单命中同样落库留痕
        verify(decisionMapper).insert(any(RiskDecision.class));
    }

    @Test
    @DisplayName("无名单命中且无规则命中 → 通过")
    void decide_should_pass_when_no_hits() {
        when(decisionMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(listMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(ruleMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(decisionMapper.insert(any(RiskDecision.class))).thenReturn(1);

        RiskDecisionDTO dto = service.decide(req("B3", "13700000000", BigDecimal.valueOf(500)));

        assertEquals("PASS", dto.getDecision());
        assertEquals(100, dto.getScore().intValue());
    }

    @Test
    @DisplayName("规则命中扣分后落在阈值区间 → 人工审核")
    void decide_should_review_when_score_between_thresholds() {
        when(decisionMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(listMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(ruleMapper.selectList(any()))
                .thenReturn(List.of(rule("R1", 30, "{\"field\":\"applyAmount\",\"op\":\">\",\"value\":50000}")));
        when(decisionMapper.insert(any(RiskDecision.class))).thenReturn(1);

        RiskDecisionDTO dto = service.decide(req("B4", null, BigDecimal.valueOf(100000)));

        assertEquals("REVIEW", dto.getDecision());
        assertEquals(70, dto.getScore().intValue());
        assertTrue(dto.getRuleHits().contains("R1"));
    }

    @Test
    @DisplayName("规则命中扣分过低 → 拒绝")
    void decide_should_reject_when_score_below_reject_threshold() {
        when(decisionMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(listMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(ruleMapper.selectList(any()))
                .thenReturn(List.of(rule("R2", 70, "{\"field\":\"applyAmount\",\"op\":\">\",\"value\":100}")));
        when(decisionMapper.insert(any(RiskDecision.class))).thenReturn(1);

        RiskDecisionDTO dto = service.decide(req("B5", null, BigDecimal.valueOf(1000)));

        assertEquals("REJECT", dto.getDecision());
        assertEquals(30, dto.getScore().intValue());
    }

    @Test
    @DisplayName("bizNo 为空 → 抛业务异常")
    void decide_should_throw_when_bizNo_blank() {
        assertThrows(BusinessException.class, () -> service.decide(req("  ", null, null)));
    }

    @Test
    @DisplayName("同一 bizNo 已有决策 → 幂等返回既有结论，不重复落库")
    void decide_should_return_existing_when_already_decided() {
        RiskDecision existing = new RiskDecision();
        existing.setBizNo("B9");
        existing.setBizType("ISSUE_LOAN");
        existing.setConsumerNo("C1");
        existing.setDecision("PASS");
        existing.setScore(100);
        existing.setRuleHits("R1");
        when(decisionMapper.selectList(any())).thenReturn(List.of(existing));

        RiskDecisionDTO dto = service.decide(req("B9", "13600000000", BigDecimal.valueOf(100)));

        assertEquals("PASS", dto.getDecision());
        assertEquals(100, dto.getScore().intValue());
        verify(decisionMapper, never()).insert(any(RiskDecision.class));
    }

    @Test
    @DisplayName("按 bizNo 查询不存在的决策 → 抛业务异常")
    void getByBizNo_should_throw_when_not_found() {
        when(decisionMapper.selectList(any())).thenReturn(Collections.emptyList());
        assertThrows(BusinessException.class, () -> service.getByBizNo("NOPE"));
    }

    @Test
    @DisplayName("按 bizNo 查询存在的决策 → 返回记录")
    void getByBizNo_should_return_record() {
        RiskDecision existing = new RiskDecision();
        existing.setBizNo("B10");
        existing.setDecision("REVIEW");
        existing.setScore(60);
        when(decisionMapper.selectList(any())).thenReturn(List.of(existing));

        RiskDecisionDTO dto = service.getByBizNo("B10");

        assertEquals("REVIEW", dto.getDecision());
        assertEquals(60, dto.getScore().intValue());
    }
}
