package com.xfjr.consumer.audit.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xfjr.consumer.audit.entity.OperationLog;
import com.xfjr.consumer.mapper.OperationLogMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 操作日志与审计（FR-132）：追加写 + 哈希链（不可篡改，AC2）+ 按操作人/时间/动作检索（AC3）。
 * 哈希链使任意历史记录的修改/删除都会导致后续 curHash 校验失败，从而可被检测。
 */
@Service
public class OperationLogService {

    public static final String GENESIS_HASH = "GENESIS";

    private final OperationLogMapper operationLogMapper;

    public OperationLogService(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    /** 追加一条操作日志（AC1）。仅插入，不更新/删除。 */
    public OperationLog append(String operator, String action, String target, String result, String ip) {
        OperationLog prev = operationLogMapper.selectOne(
                new LambdaQueryWrapper<OperationLog>().orderByDesc(OperationLog::getId).last("limit 1"));
        String prevHash = prev == null ? GENESIS_HASH : prev.getCurHash();

        OperationLog log = new OperationLog();
        log.setOperator(operator);
        log.setAction(action);
        log.setTarget(target);
        log.setResult(result);
        log.setIp(ip);
        log.setPrevHash(prevHash);
        log.setCreateDate(LocalDateTime.now());
        log.setCurHash(sha256(prevHash + "|" + nullToEmpty(operator) + "|" + nullToEmpty(action)
                + "|" + nullToEmpty(target) + "|" + nullToEmpty(result) + "|" + nullToEmpty(ip)
                + "|" + log.getCreateDate()));
        operationLogMapper.insert(log);
        return log;
    }

    /** 按操作人/时间范围/动作类型检索（AC3）。参数均可为空表示不过滤。 */
    public List<OperationLog> query(String operator, String action, LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<OperationLog>()
                .eq(operator != null && !operator.trim().isEmpty(), OperationLog::getOperator, operator)
                .eq(action != null && !action.trim().isEmpty(), OperationLog::getAction, action)
                .ge(start != null, OperationLog::getCreateDate, start)
                .le(end != null, OperationLog::getCreateDate, end)
                .orderByDesc(OperationLog::getId);
        return operationLogMapper.selectList(wrapper);
    }

    /** 校验哈希链完整性（AC2 可检测篡改）。返回 false 表示存在断裂/篡改。 */
    public boolean verifyChain() {
        List<OperationLog> all = operationLogMapper.selectList(
                new LambdaQueryWrapper<OperationLog>().orderByAsc(OperationLog::getId));
        String expectedPrev = GENESIS_HASH;
        for (OperationLog log : all) {
            if (!expectedPrev.equals(log.getPrevHash())) {
                return false;
            }
            String recomputed = sha256(log.getPrevHash() + "|" + nullToEmpty(log.getOperator()) + "|"
                    + nullToEmpty(log.getAction()) + "|" + nullToEmpty(log.getTarget()) + "|"
                    + nullToEmpty(log.getResult()) + "|" + nullToEmpty(log.getIp()) + "|" + log.getCreateDate());
            if (!recomputed.equals(log.getCurHash())) {
                return false;
            }
            expectedPrev = log.getCurHash();
        }
        return true;
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private String sha256(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
