package com.xfjr.consumer.realname.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xfjr.common.domain.BusinessException;
import com.xfjr.consumer.common.ConsumerErrorCode;
import com.xfjr.consumer.common.SecurityUtil;
import com.xfjr.consumer.entity.Consumer;
import com.xfjr.consumer.mapper.ConsumerMapper;
import com.xfjr.consumer.mapper.ConsumerRealnameMapper;
import com.xfjr.consumer.mapper.RealnameLivenessRecordMapper;
import com.xfjr.consumer.realname.entity.ConsumerRealname;
import com.xfjr.consumer.realname.entity.RealnameLivenessRecord;
import com.xfjr.consumer.realname.model.LivenessStatusDTO;
import com.xfjr.consumer.realname.model.LivenessSubmitRequest;
import com.xfjr.consumer.realname.verify.LivenessVerifier;
import com.xfjr.consumer.realname.verify.LivenessVerifyRequest;
import com.xfjr.consumer.realname.verify.LivenessVerifyResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 实名活体/人脸核验（FR-135）：调用算法服务 + 决策状态机。
 * 活体不通过→拒绝/达重试上限转人工(AC2)；比对分≥阈值→通过(AC1)；低于阈值→人工复核(AC3)。
 * 仅落库分数与结论，不存原始人脸图像(AC4)。
 */
@Slf4j
@Service
public class RealnameLivenessService {

    private final LivenessVerifier livenessVerifier;
    private final RealnameLivenessRecordMapper recordMapper;
    private final ConsumerRealnameMapper realnameMapper;
    private final ConsumerMapper consumerMapper;

    /** 人脸比对通过阈值 */
    @Value("${realname.face-match-threshold:0.80}")
    private BigDecimal faceMatchThreshold;
    /** 活体检测通过阈值 */
    @Value("${realname.liveness-threshold:0.60}")
    private BigDecimal livenessThreshold;
    /** 活体不通过的最大重试次数，超过转人工复核 */
    @Value("${realname.max-retry:3}")
    private int maxRetry;

    public RealnameLivenessService(LivenessVerifier livenessVerifier,
                                   RealnameLivenessRecordMapper recordMapper,
                                   ConsumerRealnameMapper realnameMapper,
                                   ConsumerMapper consumerMapper) {
        this.livenessVerifier = livenessVerifier;
        this.recordMapper = recordMapper;
        this.realnameMapper = realnameMapper;
        this.consumerMapper = consumerMapper;
    }

    /** 提交活体+人脸核验（FR-135 主流程）。 */
    @Transactional(rollbackFor = Exception.class)
    public LivenessStatusDTO submit(LivenessSubmitRequest req) {
        if (req == null || req.getConsumerNo() == null || req.getConsumerNo().trim().isEmpty()) {
            throw new BusinessException(ConsumerErrorCode.E_140405, "用户标识不能为空");
        }
        LivenessVerifyRequest vr = new LivenessVerifyRequest();
        vr.setConsumerNo(req.getConsumerNo());
        vr.setRealName(req.getRealName());
        vr.setIdCard(req.getIdCard());
        vr.setLivenessImageRef(req.getLivenessImageRef());
        vr.setFaceImageRef(req.getFaceImageRef());
        vr.setChallengeType(req.getChallengeType());

        LivenessVerifyResult result;
        try {
            result = livenessVerifier.verify(vr);
        } catch (Exception e) {
            // 异常流①：算法服务超时/失败
            throw new BusinessException(ConsumerErrorCode.E_140401, "活体核验失败: " + e.getMessage());
        }

        RealnameLivenessRecord latest = findLatest(req.getConsumerNo());
        int retryCount = (latest == null || latest.getRetryCount() == null ? 0 : latest.getRetryCount()) + 1;

        RealnameLivenessRecord record = new RealnameLivenessRecord();
        record.setConsumerNo(req.getConsumerNo());
        record.setLivenessScore(result.getLivenessScore());
        record.setFaceMatchScore(result.getFaceMatchScore());
        record.setLivenessPassed(result.isLivenessPassed() ? 1 : 0);
        record.setAttackType(result.getAttackType());
        record.setRetryCount(retryCount);
        record.setMessage(result.getMessage());
        record.setStatus(decide(result, retryCount));

        LocalDateTime now = LocalDateTime.now();
        record.setCreateDate(now);
        record.setUpdateDate(now);
        recordMapper.insert(record);

        // 同步实名档案（最新分数/结果 + 风险标记）
        ConsumerRealname rn = findRealname(req.getConsumerNo());
        boolean risk = result.isAttackDetected() || !result.isLivenessPassed();
        if (rn == null) {
            rn = new ConsumerRealname();
            rn.setConsumerNo(req.getConsumerNo());
            rn.setCreateDate(now);
        }
        rn.setRealName(req.getRealName());
        rn.setIdCard(req.getIdCard());
        rn.setLivenessScore(result.getLivenessScore());
        rn.setFaceMatchScore(result.getFaceMatchScore());
        rn.setLivenessPassed(result.isLivenessPassed() ? 1 : 0);
        rn.setRiskFlag(risk ? 1 : 0);
        rn.setStatus(toRealnameStatus(record.getStatus()));
        rn.setUpdateDate(now);
        if (rn.getId() == null) {
            realnameMapper.insert(rn);
        } else {
            realnameMapper.updateById(rn);
        }
        return toStatusDto(record, rn);
    }

    /** 查询当前登录用户最新核验状态（/my/realname/liveness/status）。 */
    public LivenessStatusDTO statusOfCurrentUser() {
        String mobile = SecurityUtil.getUser().getMobile();
        if (mobile == null || mobile.trim().isEmpty()) {
            return emptyStatus(null);
        }
        Consumer consumer = consumerMapper.selectOne(
                new LambdaQueryWrapper<Consumer>().eq(Consumer::getMobile, mobile).last("limit 1"));
        if (consumer == null || consumer.getUserNo() == null) {
            return emptyStatus(null);
        }
        return status(consumer.getUserNo());
    }

    /** 查询指定用户最新核验状态。 */
    public LivenessStatusDTO status(String consumerNo) {
        RealnameLivenessRecord latest = findLatest(consumerNo);
        ConsumerRealname rn = findRealname(consumerNo);
        if (latest == null) {
            return emptyStatus(consumerNo);
        }
        return toStatusDto(latest, rn);
    }

    /** 决策状态机。 */
    private String decide(LivenessVerifyResult r, int retryCount) {
        boolean livenessOk = r.isLivenessPassed() && r.getLivenessScore().compareTo(livenessThreshold) >= 0;
        if (r.isAttackDetected() || !livenessOk) {
            // AC2：翻拍/照片攻击 → 拒绝并标记风险；达重试上限转人工复核（备选流①）
            return retryCount >= maxRetry ? "REVIEW" : "REJECTED";
        }
        // AC1/AC3：活体通过，按人脸比对分数判定
        return r.getFaceMatchScore().compareTo(faceMatchThreshold) >= 0 ? "PASSED" : "REVIEW";
    }

    private String toRealnameStatus(String livenessStatus) {
        switch (livenessStatus) {
            case "PASSED": return "VERIFIED";
            case "REVIEW": return "REVIEW";
            case "REJECTED": return "REJECTED";
            default: return "UNVERIFIED";
        }
    }

    private RealnameLivenessRecord findLatest(String consumerNo) {
        return recordMapper.selectOne(new LambdaQueryWrapper<RealnameLivenessRecord>()
                .eq(RealnameLivenessRecord::getConsumerNo, consumerNo)
                .orderByDesc(RealnameLivenessRecord::getId).last("limit 1"));
    }

    private ConsumerRealname findRealname(String consumerNo) {
        return realnameMapper.selectOne(new LambdaQueryWrapper<ConsumerRealname>()
                .eq(ConsumerRealname::getConsumerNo, consumerNo).last("limit 1"));
    }

    private LivenessStatusDTO toStatusDto(RealnameLivenessRecord record, ConsumerRealname rn) {
        LivenessStatusDTO dto = new LivenessStatusDTO();
        dto.setConsumerNo(record.getConsumerNo());
        dto.setStatus(record.getStatus());
        dto.setLivenessScore(record.getLivenessScore());
        dto.setFaceMatchScore(record.getFaceMatchScore());
        dto.setLivenessPassed(record.getLivenessPassed());
        dto.setAttackType(record.getAttackType());
        dto.setRetryCount(record.getRetryCount());
        dto.setMessage(record.getMessage());
        dto.setCreateDate(record.getCreateDate());
        dto.setRiskFlag(rn == null || rn.getRiskFlag() == null ? 0 : rn.getRiskFlag());
        return dto;
    }

    private LivenessStatusDTO emptyStatus(String consumerNo) {
        LivenessStatusDTO dto = new LivenessStatusDTO();
        dto.setConsumerNo(consumerNo);
        dto.setStatus("UNVERIFIED");
        dto.setRiskFlag(0);
        return dto;
    }
}
