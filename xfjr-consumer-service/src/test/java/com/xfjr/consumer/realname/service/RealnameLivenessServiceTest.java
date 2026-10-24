package com.xfjr.consumer.realname.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xfjr.common.domain.BusinessException;
import com.xfjr.consumer.mapper.ConsumerMapper;
import com.xfjr.consumer.mapper.ConsumerRealnameMapper;
import com.xfjr.consumer.mapper.RealnameLivenessRecordMapper;
import com.xfjr.consumer.realname.entity.RealnameLivenessRecord;
import com.xfjr.consumer.realname.model.LivenessStatusDTO;
import com.xfjr.consumer.realname.model.LivenessSubmitRequest;
import com.xfjr.consumer.realname.verify.LivenessVerifier;
import com.xfjr.consumer.realname.verify.LivenessVerifyResult;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("实名活体/人脸核验状态机（FR-135）")
class RealnameLivenessServiceTest {

    @Mock
    private LivenessVerifier verifier;
    @Mock
    private RealnameLivenessRecordMapper recordMapper;
    @Mock
    private ConsumerRealnameMapper realnameMapper;
    @Mock
    private ConsumerMapper consumerMapper;
    @InjectMocks
    private RealnameLivenessService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "faceMatchThreshold", new BigDecimal("0.80"));
        ReflectionTestUtils.setField(service, "livenessThreshold", new BigDecimal("0.60"));
        ReflectionTestUtils.setField(service, "maxRetry", 3);
    }

    private LivenessVerifyResult result(boolean attack, boolean passed, String liveness, String face) {
        LivenessVerifyResult r = new LivenessVerifyResult();
        r.setAttackDetected(attack);
        r.setLivenessPassed(passed);
        r.setLivenessScore(new BigDecimal(liveness));
        r.setFaceMatchScore(new BigDecimal(face));
        r.setAttackType(attack ? "PHOTO" : null);
        return r;
    }

    private LivenessSubmitRequest req() {
        LivenessSubmitRequest r = new LivenessSubmitRequest();
        r.setConsumerNo("U1");
        r.setRealName("张三");
        r.setIdCard("110101199001011234");
        r.setChallengeType("ACTION");
        return r;
    }

    @Test
    @DisplayName("AC1 活体通过且人脸分达标 → PASSED，分数落库")
    void submit_pass() {
        when(verifier.verify(any())).thenReturn(result(false, true, "0.90", "0.90"));
        when(recordMapper.selectOne(any())).thenReturn(null);
        when(realnameMapper.selectOne(any())).thenReturn(null);

        LivenessStatusDTO dto = service.submit(req());

        assertEquals("PASSED", dto.getStatus());
        ArgumentCaptor<RealnameLivenessRecord> cap = ArgumentCaptor.forClass(RealnameLivenessRecord.class);
        verify(recordMapper).insert(cap.capture());
        assertEquals(0, new BigDecimal("0.90").compareTo(cap.getValue().getLivenessScore()));
        assertEquals(0, new BigDecimal("0.90").compareTo(cap.getValue().getFaceMatchScore()));
    }

    @Test
    @DisplayName("AC2 攻击/活体不通过 → REJECTED 且标记风险")
    void submit_reject_attack() {
        when(verifier.verify(any())).thenReturn(result(true, false, "0.20", "0.10"));
        when(recordMapper.selectOne(any())).thenReturn(null);
        when(realnameMapper.selectOne(any())).thenReturn(null);

        LivenessStatusDTO dto = service.submit(req());

        assertEquals("REJECTED", dto.getStatus());
        assertEquals(1, dto.getRiskFlag().intValue());
    }

    @Test
    @DisplayName("AC3 活体通过但人脸分低于阈值 → REVIEW")
    void submit_review_low_face() {
        when(verifier.verify(any())).thenReturn(result(false, true, "0.90", "0.50"));
        when(recordMapper.selectOne(any())).thenReturn(null);
        when(realnameMapper.selectOne(any())).thenReturn(null);

        LivenessStatusDTO dto = service.submit(req());

        assertEquals("REVIEW", dto.getStatus());
    }

    @Test
    @DisplayName("备选流① 活体不通过且重试达上限 → REVIEW")
    void submit_review_retry_exhausted() {
        RealnameLivenessRecord prev = new RealnameLivenessRecord();
        prev.setRetryCount(3);
        when(verifier.verify(any())).thenReturn(result(true, false, "0.20", "0.10"));
        when(recordMapper.selectOne(any())).thenReturn(prev);
        when(realnameMapper.selectOne(any())).thenReturn(null);

        LivenessStatusDTO dto = service.submit(req());

        assertEquals("REVIEW", dto.getStatus());
        assertEquals(4, dto.getRetryCount().intValue());
    }

    @Test
    @DisplayName("异常流① 算法服务失败 → 抛业务异常")
    void submit_throw_when_verifier_fails() {
        when(verifier.verify(any())).thenThrow(new RuntimeException("timeout"));
        assertThrows(BusinessException.class, () -> service.submit(req()));
    }
}
