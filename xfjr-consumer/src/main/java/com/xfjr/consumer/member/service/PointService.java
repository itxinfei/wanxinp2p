package com.xfjr.consumer.member.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xfjr.common.domain.BusinessException;
import com.xfjr.consumer.common.ConsumerErrorCode;
import com.xfjr.consumer.mapper.MemberPointRecordMapper;
import com.xfjr.consumer.member.entity.MemberPointRecord;
import com.xfjr.consumer.member.model.PointRecordDTO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 积分（FR-127）：获取（出借/还款/邀请）、消耗（抵扣/兑换）、流水可查、余额。
 * 余额 = 获取合计 - 消耗合计；同一 bizNo 幂等。
 */
@Slf4j
@Service
public class PointService {

    private final MemberPointRecordMapper pointRecordMapper;

    /** 每出借 loanPer 元发放 1 积分 */
    @Value("${member.point.loan-per:100}")
    private long loanPer;

    public PointService(MemberPointRecordMapper pointRecordMapper) {
        this.pointRecordMapper = pointRecordMapper;
    }

    /** 当前积分余额 = 获取合计 - 消耗合计。 */
    public long currentBalance(String consumerNo) {
        List<MemberPointRecord> list = pointRecordMapper.selectList(
                new LambdaQueryWrapper<MemberPointRecord>().eq(MemberPointRecord::getConsumerNo, consumerNo));
        long earn = list.stream().filter(r -> "EARN".equals(r.getPointType())).mapToLong(MemberPointRecord::getAmount).sum();
        long consume = list.stream().filter(r -> "CONSUME".equals(r.getPointType())).mapToLong(MemberPointRecord::getAmount).sum();
        return earn - consume;
    }

    /** 积分流水可查（AC3）。 */
    public List<PointRecordDTO> listRecords(String consumerNo) {
        List<MemberPointRecord> list = pointRecordMapper.selectList(
                new LambdaQueryWrapper<MemberPointRecord>()
                        .eq(MemberPointRecord::getConsumerNo, consumerNo)
                        .orderByDesc(MemberPointRecord::getId));
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    /** 发放积分（AC2）：同一 bizNo 幂等。 */
    @Transactional(rollbackFor = Exception.class)
    public PointRecordDTO earn(String consumerNo, String bizType, long amount, String bizNo, String reason) {
        requireNonBlank(consumerNo);
        if (amount <= 0) {
            throw new BusinessException(ConsumerErrorCode.E_140205, "积分变动量必须大于 0");
        }
        MemberPointRecord existing = findByBizNo(consumerNo, bizNo);
        if (existing != null) {
            return toDto(existing);
        }
        long balance = currentBalance(consumerNo);
        MemberPointRecord r = newRecord(consumerNo, "EARN", bizType, amount, balance + amount, bizNo, reason);
        pointRecordMapper.insert(r);
        return toDto(r);
    }

    /** 消耗积分（抵扣/兑换，AC3）：校验余额充足；同一 bizNo 幂等。 */
    @Transactional(rollbackFor = Exception.class)
    public PointRecordDTO consume(String consumerNo, String bizType, long amount, String bizNo, String reason) {
        requireNonBlank(consumerNo);
        if (amount <= 0) {
            throw new BusinessException(ConsumerErrorCode.E_140205, "积分变动量必须大于 0");
        }
        MemberPointRecord existing = findByBizNo(consumerNo, bizNo);
        if (existing != null) {
            return toDto(existing);
        }
        long balance = currentBalance(consumerNo);
        if (balance < amount) {
            throw new BusinessException(ConsumerErrorCode.E_140203, "积分余额不足，当前余额: " + balance);
        }
        MemberPointRecord r = newRecord(consumerNo, "CONSUME", bizType, amount, balance - amount, bizNo, reason);
        pointRecordMapper.insert(r);
        return toDto(r);
    }

    /** 按出借金额规则发放积分（AC2）：每 loanPer 元 1 积分，不足 1 不发放。 */
    @Transactional(rollbackFor = Exception.class)
    public PointRecordDTO earnByLend(String consumerNo, BigDecimal lendAmount, String bizNo) {
        if (lendAmount == null || lendAmount.signum() <= 0) {
            return null;
        }
        long points = lendAmount.divide(BigDecimal.valueOf(loanPer), 0, RoundingMode.DOWN).longValue();
        if (points <= 0) {
            return null;
        }
        return earn(consumerNo, "LOAN", points, bizNo, "出借奖励");
    }

    private MemberPointRecord findByBizNo(String consumerNo, String bizNo) {
        if (bizNo == null || bizNo.trim().isEmpty()) {
            return null;
        }
        return pointRecordMapper.selectOne(
                new LambdaQueryWrapper<MemberPointRecord>()
                        .eq(MemberPointRecord::getConsumerNo, consumerNo)
                        .eq(MemberPointRecord::getBizNo, bizNo));
    }

    private MemberPointRecord newRecord(String consumerNo, String pointType, String bizType,
                                        long amount, long balanceAfter, String bizNo, String reason) {
        MemberPointRecord r = new MemberPointRecord();
        r.setConsumerNo(consumerNo);
        r.setPointType(pointType);
        r.setBizType(bizType);
        r.setAmount(amount);
        r.setBalanceAfter(balanceAfter);
        r.setBizNo(bizNo);
        r.setReason(reason);
        r.setCreateDate(LocalDateTime.now());
        return r;
    }

    private void requireNonBlank(String s) {
        if (s == null || s.trim().isEmpty()) {
            throw new BusinessException(ConsumerErrorCode.E_140205, "用户编码不能为空");
        }
    }

    private PointRecordDTO toDto(MemberPointRecord r) {
        PointRecordDTO dto = new PointRecordDTO();
        dto.setId(r.getId());
        dto.setConsumerNo(r.getConsumerNo());
        dto.setPointType(r.getPointType());
        dto.setBizType(r.getBizType());
        dto.setAmount(r.getAmount());
        dto.setBalanceAfter(r.getBalanceAfter());
        dto.setBizNo(r.getBizNo());
        dto.setReason(r.getReason());
        dto.setCreateDate(r.getCreateDate());
        return dto;
    }
}
