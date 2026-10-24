package com.xfjr.consumer.member.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xfjr.common.domain.BusinessException;
import com.xfjr.consumer.common.ConsumerErrorCode;
import com.xfjr.consumer.mapper.MemberInfoMapper;
import com.xfjr.consumer.mapper.MemberLevelHistoryMapper;
import com.xfjr.consumer.mapper.MemberLevelMapper;
import com.xfjr.consumer.member.entity.MemberInfo;
import com.xfjr.consumer.member.entity.MemberLevel;
import com.xfjr.consumer.member.entity.MemberLevelHistory;
import com.xfjr.consumer.member.model.MemberInfoDTO;
import com.xfjr.consumer.member.model.MemberLevelDTO;
import com.xfjr.consumer.member.model.MemberLevelHistoryDTO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 会员等级（FR-127）：等级配置、用户档案、出借累计 + 自动升级 + 变更历史。
 */
@Slf4j
@Service
public class MemberLevelService {

    private final MemberInfoMapper memberInfoMapper;
    private final MemberLevelMapper memberLevelMapper;
    private final MemberLevelHistoryMapper historyMapper;

    public MemberLevelService(MemberInfoMapper memberInfoMapper,
                              MemberLevelMapper memberLevelMapper,
                              MemberLevelHistoryMapper historyMapper) {
        this.memberInfoMapper = memberInfoMapper;
        this.memberLevelMapper = memberLevelMapper;
        this.historyMapper = historyMapper;
    }

    /** 等级配置列表（启用）。 */
    public List<MemberLevelDTO> listLevels() {
        List<MemberLevel> levels = memberLevelMapper.selectList(
                new LambdaQueryWrapper<MemberLevel>().eq(MemberLevel::getStatus, 1).orderByAsc(MemberLevel::getLevelNo));
        return levels.stream().map(this::toLevelDto).collect(Collectors.toList());
    }

    /** 用户会员档案（不存在则返回空档案）。 */
    public MemberInfoDTO getMemberInfo(String consumerNo) {
        MemberInfo info = findInfo(consumerNo);
        return toInfoDto(info == null ? newMemberInfo(consumerNo) : info);
    }

    /** 等级变更历史（AC1）。 */
    public List<MemberLevelHistoryDTO> getLevelHistory(String consumerNo) {
        List<MemberLevelHistory> list = historyMapper.selectList(
                new LambdaQueryWrapper<MemberLevelHistory>()
                        .eq(MemberLevelHistory::getConsumerNo, consumerNo)
                        .orderByDesc(MemberLevelHistory::getId));
        return list.stream().map(this::toHistoryDto).collect(Collectors.toList());
    }

    /**
     * 记录出借并自动升级（AC1）。累计出借金额增加；若达到更高等级门槛则升级并记录变更历史。
     */
    @Transactional(rollbackFor = Exception.class)
    public MemberInfoDTO recordLend(String consumerNo, BigDecimal lendAmount) {
        if (consumerNo == null || consumerNo.trim().isEmpty()) {
            throw new BusinessException(ConsumerErrorCode.E_140205, "用户编码不能为空");
        }
        if (lendAmount == null || lendAmount.signum() <= 0) {
            throw new BusinessException(ConsumerErrorCode.E_140205, "出借金额必须大于 0");
        }

        MemberInfo info = findInfo(consumerNo);
        if (info == null) {
            info = newMemberInfo(consumerNo);
        }
        BigDecimal before = info.getCumulativeLendAmount() == null ? BigDecimal.ZERO : info.getCumulativeLendAmount();
        BigDecimal after = before.add(lendAmount);
        info.setCumulativeLendAmount(after);
        info.setUpdateDate(LocalDateTime.now());

        List<MemberLevel> levels = memberLevelMapper.selectList(
                new LambdaQueryWrapper<MemberLevel>().eq(MemberLevel::getStatus, 1).orderByDesc(MemberLevel::getLevelNo));
        int currentNo = info.getCurrentLevel() == null ? 0 : levelNoOf(levels, info.getCurrentLevel());
        String upgradedTo = null;
        int bestNo = currentNo;
        for (MemberLevel lv : levels) {
            BigDecimal threshold = lv.getMinLendAmount() == null ? BigDecimal.ZERO : lv.getMinLendAmount();
            if (after.compareTo(threshold) >= 0 && lv.getLevelNo() > bestNo) {
                upgradedTo = lv.getLevelCode();
                bestNo = lv.getLevelNo();
            }
        }
        if (upgradedTo != null && !upgradedTo.equals(info.getCurrentLevel())) {
            String from = info.getCurrentLevel() == null ? "-" : info.getCurrentLevel();
            info.setCurrentLevel(upgradedTo);
            MemberLevelHistory h = new MemberLevelHistory();
            h.setConsumerNo(consumerNo);
            h.setFromLevel(from);
            h.setToLevel(upgradedTo);
            h.setChangeReason("累计出借达到等级门槛自动升级");
            h.setCreateDate(LocalDateTime.now());
            historyMapper.insert(h);
            log.info("用户 {} 会员等级升级 {} -> {}", consumerNo, from, upgradedTo);
        }

        if (info.getId() == null) {
            info.setCreateDate(LocalDateTime.now());
            memberInfoMapper.insert(info);
        } else {
            memberInfoMapper.updateById(info);
        }
        return toInfoDto(info);
    }

    private MemberInfo findInfo(String consumerNo) {
        return memberInfoMapper.selectOne(
                new LambdaQueryWrapper<MemberInfo>().eq(MemberInfo::getConsumerNo, consumerNo));
    }

    private MemberInfo newMemberInfo(String consumerNo) {
        MemberInfo info = new MemberInfo();
        info.setConsumerNo(consumerNo);
        info.setCurrentLevel(null);
        info.setCumulativeLendAmount(BigDecimal.ZERO);
        info.setActiveScore(0);
        return info;
    }

    private int levelNoOf(List<MemberLevel> levels, String levelCode) {
        return levels.stream().filter(l -> levelCode.equals(l.getLevelCode()))
                .mapToInt(MemberLevel::getLevelNo).min().orElse(0);
    }

    private MemberLevelDTO toLevelDto(MemberLevel lv) {
        MemberLevelDTO dto = new MemberLevelDTO();
        dto.setLevelCode(lv.getLevelCode());
        dto.setLevelName(lv.getLevelName());
        dto.setLevelNo(lv.getLevelNo());
        dto.setMinLendAmount(lv.getMinLendAmount());
        dto.setPrivilege(lv.getPrivilege());
        return dto;
    }

    private MemberInfoDTO toInfoDto(MemberInfo info) {
        MemberInfoDTO dto = new MemberInfoDTO();
        dto.setConsumerNo(info.getConsumerNo());
        dto.setCurrentLevel(info.getCurrentLevel());
        dto.setCumulativeLendAmount(info.getCumulativeLendAmount());
        dto.setActiveScore(info.getActiveScore());
        return dto;
    }

    private MemberLevelHistoryDTO toHistoryDto(MemberLevelHistory h) {
        MemberLevelHistoryDTO dto = new MemberLevelHistoryDTO();
        dto.setConsumerNo(h.getConsumerNo());
        dto.setFromLevel(h.getFromLevel());
        dto.setToLevel(h.getToLevel());
        dto.setChangeReason(h.getChangeReason());
        dto.setCreateDate(h.getCreateDate());
        return dto;
    }
}
