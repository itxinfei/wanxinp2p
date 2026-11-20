package com.xfjr.depository.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xfjr.api.depository.model.DepositoryCommandRequest;
import com.xfjr.api.depository.model.DepositoryCommandStatusDTO;
import com.xfjr.common.domain.BusinessException;
import com.xfjr.depository.common.constant.DepositoryErrorCode;
import com.xfjr.depository.entity.DepositoryRecord;
import com.xfjr.depository.mapper.DepositoryRecordMapper;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 存管指令下发（FR-134）：参数组装+签名下发+落库可追溯（AC1）；
 * 指数退避重试（备选流②）、超时补偿查询（AC4）、熔断降级（AC5）。
 * 签名由 {@link OkHttpService} 的 SignatureInterceptor 完成；验签/幂等沿用既有拦截器与 handleIdempotent。
 */
@Slf4j
@Service
public class DepositoryCommandService {

    private final DepositoryRecordMapper recordMapper;
    private final OkHttpService okHttpService;
    private final ConfigService configService;

    /** 熔断：连续失败阈值 */
    @Value("${depository.circuit.max-failures:5}")
    private int maxFailures;
    /** 熔断恢复窗口(毫秒) */
    @Value("${depository.circuit.recovery-millis:30000}")
    private long recoveryMillis;
    /** 最大重试次数 */
    @Value("${depository.command.max-retry:3}")
    private int maxRetry;
    /** 退避基础(毫秒)，按 2^n 递增并封顶 */
    @Value("${depository.command.base-backoff-millis:500}")
    private long baseBackoffMillis;

    private final AtomicLong consecutiveFailures = new AtomicLong(0);
    private volatile long breakerOpenedAt = 0L;

    public DepositoryCommandService(DepositoryRecordMapper recordMapper,
                                    OkHttpService okHttpService,
                                    ConfigService configService) {
        this.recordMapper = recordMapper;
        this.okHttpService = okHttpService;
        this.configService = configService;
    }

    /** 下发存管指令。 */
    public DepositoryCommandStatusDTO sendCommand(DepositoryCommandRequest req) {
        if (req == null || isBlank(req.getServiceName())) {
            throw new BusinessException(DepositoryErrorCode.E_160106, "存管接口名不能为空");
        }
        checkBreaker();
        String requestNo = isBlank(req.getRequestNo()) ? genRequestNo() : req.getRequestNo();

        DepositoryRecord existing = findByRequestNo(requestNo);
        if (existing != null && "SUCCESS".equals(existing.getStatus())) {
            return toDto(existing); // 幂等：已成功指令直接返回
        }

        String url = buildCommandUrl(req, requestNo);
        DepositoryRecord record = existing != null ? existing : newRecord(req, requestNo);
        record.setRequestPayload(req.getPayload());
        record.setStatus("PROCESSING");
        record.setErrorMessage(null);
        record.setRetryCount(0);
        record.setUpdateDate(LocalDateTime.now());
        if (record.getId() == null) {
            record.setCreateDate(LocalDateTime.now());
        }

        int attempt = 0;
        String lastError = null;
        while (attempt < maxRetry) {
            attempt++;
            record.setRetryCount(attempt);
            try {
                String resp = okHttpService.doSyncGet(url);
                record.setResponseData(resp);
                record.setStatus("SUCCESS");
                record.setConfirmDate(LocalDateTime.now());
                record.setUpdateDate(LocalDateTime.now());
                onSuccess();
                persist(record);
                return toDto(record);
            } catch (Exception e) {
                lastError = e.getMessage();
                record.setErrorMessage(lastError);
                record.setUpdateDate(LocalDateTime.now());
                onFailure();
                log.warn("存管指令 {} 第 {} 次下发失败: {}", requestNo, attempt, lastError);
                if (breakerOpen() || attempt >= maxRetry) {
                    break;
                }
                sleep(backoff(attempt));
            }
        }

        record.setStatus("FAIL");
        record.setUpdateDate(LocalDateTime.now());
        persist(record);
        if (breakerOpen()) {
            throw new BusinessException(DepositoryErrorCode.E_160104, "存管服务不可用（熔断中）: " + lastError);
        }
        throw new BusinessException(DepositoryErrorCode.E_160105, "存管指令下发失败(已重试" + attempt + "次): " + lastError);
    }

    /** 指令状态查询。 */
    public DepositoryCommandStatusDTO commandStatus(String requestNo) {
        DepositoryRecord record = findByRequestNo(requestNo);
        if (record == null) {
            throw new BusinessException(DepositoryErrorCode.E_160105, "指令不存在: " + requestNo);
        }
        return toDto(record);
    }

    /** 补偿查询（AC4）：指令超时未响应时主动向存管查询并更新状态，最终一致。 */
    public DepositoryCommandStatusDTO compensate(String requestNo) {
        DepositoryRecord record = findByRequestNo(requestNo);
        if (record == null) {
            throw new BusinessException(DepositoryErrorCode.E_160105, "指令不存在: " + requestNo);
        }
        checkBreaker();
        String queryUrl = buildQueryUrl(record);
        try {
            String resp = okHttpService.doSyncGet(queryUrl);
            record.setResponseData(resp);
            record.setStatus("SUCCESS");
            record.setErrorMessage(null);
            record.setConfirmDate(LocalDateTime.now());
            record.setUpdateDate(LocalDateTime.now());
            onSuccess();
            persist(record);
            return toDto(record);
        } catch (Exception e) {
            record.setStatus("FAIL");
            record.setErrorMessage(e.getMessage());
            record.setUpdateDate(LocalDateTime.now());
            onFailure();
            persist(record);
            throw new BusinessException(DepositoryErrorCode.E_160105, "补偿查询失败: " + e.getMessage());
        }
    }

    private String buildCommandUrl(DepositoryCommandRequest req, String requestNo) {
        String base = configService.getDepositoryUrl();
        if (isBlank(base)) {
            throw new BusinessException(DepositoryErrorCode.E_160104, "存管服务地址未配置(depository.url)");
        }
        StringBuilder sb = new StringBuilder(base);
        sb.append(base.contains("?") ? "&" : "?");
        sb.append("serviceName=").append(enc(req.getServiceName()));
        sb.append("&platformNo=").append(enc(configService.getP2pCode()));
        sb.append("&requestNo=").append(enc(requestNo));
        if (!isBlank(req.getPayload())) {
            sb.append("&reqData=").append(enc(req.getPayload()));
        }
        return sb.toString();
    }

    private String buildQueryUrl(DepositoryRecord record) {
        String base = configService.getDepositoryUrl();
        if (isBlank(base)) {
            throw new BusinessException(DepositoryErrorCode.E_160104, "存管服务地址未配置(depository.url)");
        }
        StringBuilder sb = new StringBuilder(base);
        sb.append(base.contains("?") ? "&" : "?");
        sb.append("serviceName=").append(enc("QUERY_STATUS"));
        sb.append("&platformNo=").append(enc(configService.getP2pCode()));
        sb.append("&requestNo=").append(enc(record.getRequestNo()));
        return sb.toString();
    }

    private void checkBreaker() {
        if (breakerOpen()) {
            throw new BusinessException(DepositoryErrorCode.E_160104, "存管服务不可用（熔断中）");
        }
    }

    private boolean breakerOpen() {
        if (consecutiveFailures.get() < maxFailures) {
            return false;
        }
        return System.currentTimeMillis() - breakerOpenedAt < recoveryMillis;
    }

    private void onSuccess() {
        consecutiveFailures.set(0);
    }

    private void onFailure() {
        if (consecutiveFailures.incrementAndGet() >= maxFailures) {
            breakerOpenedAt = System.currentTimeMillis();
        }
    }

    private long backoff(int attempt) {
        long shift = Math.min(attempt - 1, 4);
        return Math.min(baseBackoffMillis * (1L << shift), 8000L);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private DepositoryRecord newRecord(DepositoryCommandRequest req, String requestNo) {
        DepositoryRecord r = new DepositoryRecord();
        r.setRequestNo(requestNo);
        r.setServiceName(req.getServiceName());
        r.setServiceCode(req.getServiceCode());
        r.setDirection(req.getDirection());
        r.setAmount(req.getAmount());
        return r;
    }

    private DepositoryRecord findByRequestNo(String requestNo) {
        return recordMapper.selectOne(new LambdaQueryWrapper<DepositoryRecord>()
                .eq(DepositoryRecord::getRequestNo, requestNo).last("limit 1"));
    }

    private void persist(DepositoryRecord r) {
        if (r.getId() == null) {
            recordMapper.insert(r);
        } else {
            recordMapper.updateById(r);
        }
    }

    private String enc(String s) {
        return s == null ? "" : URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String genRequestNo() {
        return "CMD" + System.currentTimeMillis() + (int) (Math.random() * 9000 + 1000);
    }

    private DepositoryCommandStatusDTO toDto(DepositoryRecord r) {
        DepositoryCommandStatusDTO dto = new DepositoryCommandStatusDTO();
        dto.setRequestNo(r.getRequestNo());
        dto.setServiceName(r.getServiceName());
        dto.setServiceCode(r.getServiceCode());
        dto.setDirection(r.getDirection());
        dto.setAmount(r.getAmount());
        dto.setStatus(r.getStatus());
        dto.setRequestPayload(r.getRequestPayload());
        dto.setResponsePayload(r.getResponseData());
        dto.setRetryCount(r.getRetryCount());
        dto.setErrorMessage(r.getErrorMessage());
        dto.setCreateDate(r.getCreateDate());
        dto.setUpdateDate(r.getUpdateDate());
        return dto;
    }
}
