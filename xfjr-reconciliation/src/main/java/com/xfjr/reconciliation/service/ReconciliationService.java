package com.xfjr.reconciliation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xfjr.api.reconciliation.DiffHandleRequest;
import com.xfjr.api.reconciliation.ReconciliationBatchDTO;
import com.xfjr.api.reconciliation.ReconciliationDiffDTO;
import com.xfjr.common.domain.BusinessException;
import com.xfjr.reconciliation.common.ReconciliationErrorCode;
import com.xfjr.reconciliation.domain.FlowRecord;
import com.xfjr.reconciliation.entity.ReconciliationBatch;
import com.xfjr.reconciliation.entity.ReconciliationDetail;
import com.xfjr.reconciliation.entity.ReconciliationDiff;
import com.xfjr.reconciliation.mapper.ReconciliationBatchMapper;
import com.xfjr.reconciliation.mapper.ReconciliationDetailMapper;
import com.xfjr.reconciliation.mapper.ReconciliationDiffMapper;
import com.xfjr.reconciliation.parser.CsvStatementParser;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ReconciliationService {

    private final ReconciliationBatchMapper batchMapper;
    private final ReconciliationDetailMapper detailMapper;
    private final ReconciliationDiffMapper diffMapper;

    public ReconciliationService(ReconciliationBatchMapper batchMapper,
                                ReconciliationDetailMapper detailMapper,
                                ReconciliationDiffMapper diffMapper) {
        this.batchMapper = batchMapper;
        this.detailMapper = detailMapper;
        this.diffMapper = diffMapper;
    }

    public ReconciliationBatchDTO createBatch(String bizDate, MultipartFile bankStatement, MultipartFile platformStatement) {
        String batchNo = "RC" + bizDate;
        ReconciliationBatch existing = batchMapper.selectOne(
                new LambdaQueryWrapper<ReconciliationBatch>().eq(ReconciliationBatch::getBatchNo, batchNo));
        if (existing != null) {
            return toBatchDto(existing);
        }
        if (bankStatement == null || bankStatement.isEmpty()) {
            throw new BusinessException(ReconciliationErrorCode.E_RECON_110102, "银行对账单为空");
        }
        List<FlowRecord> bankFlows = parse(bankStatement);
        List<FlowRecord> platformFlows = (platformStatement != null && !platformStatement.isEmpty())
                ? parse(platformStatement) : new ArrayList<>();

        Map<String, FlowRecord> bankMap = bankFlows.stream()
                .collect(Collectors.toMap(FlowRecord::getBizNo, r -> r, (a, b) -> a, LinkedHashMap::new));
        Map<String, FlowRecord> platformMap = platformFlows.stream()
                .collect(Collectors.toMap(FlowRecord::getBizNo, r -> r, (a, b) -> a, LinkedHashMap::new));

        LocalDateTime now = LocalDateTime.now();
        ReconciliationBatch batch = new ReconciliationBatch();
        batch.setBatchNo(batchNo);
        batch.setBizDate(bizDate);
        batch.setStatus("RUNNING");
        batch.setFileType(bankStatement.getContentType());
        batch.setFileName(bankStatement.getOriginalFilename());
        batch.setCreateDate(now);
        batch.setUpdateDate(now);
        batchMapper.insert(batch);

        int matched = 0;
        int diffCount = 0;
        BigDecimal totalPlatform = BigDecimal.ZERO;
        BigDecimal totalBank = BigDecimal.ZERO;

        for (FlowRecord pf : platformFlows) {
            totalPlatform = totalPlatform.add(pf.getAmount());
            FlowRecord bf = bankMap.get(pf.getBizNo());
            boolean matchedFlag;
            if (bf == null) {
                insertDiff(batchNo, pf.getBizNo(), "SHORT", pf.getAmount(), null, now);
                diffCount++;
                matchedFlag = false;
            } else {
                totalBank = totalBank.add(bf.getAmount());
                if (pf.getAmount().compareTo(bf.getAmount()) == 0) {
                    matched++;
                    matchedFlag = true;
                } else {
                    insertDiff(batchNo, pf.getBizNo(), "AMOUNT", pf.getAmount(), bf.getAmount(), now);
                    diffCount++;
                    matchedFlag = false;
                }
            }
            insertDetail(batchNo, pf.getBizNo(), "PLATFORM", pf.getAmount(), matchedFlag ? "MATCHED" : "DIFF", now);
        }
        for (FlowRecord bf : bankFlows) {
            totalBank = totalBank.add(bf.getAmount());
            if (!platformMap.containsKey(bf.getBizNo())) {
                insertDiff(batchNo, bf.getBizNo(), "LONG", null, bf.getAmount(), now);
                diffCount++;
                insertDetail(batchNo, bf.getBizNo(), "BANK", bf.getAmount(), "DIFF", now);
            } else {
                insertDetail(batchNo, bf.getBizNo(), "BANK", bf.getAmount(), "MATCHED", now);
            }
        }

        batch.setTotalPlatform(totalPlatform);
        batch.setTotalBank(totalBank);
        batch.setMatchedCount(matched);
        batch.setDiffCount(diffCount);
        batch.setStatus(diffCount == 0 ? "SUCCESS" : "PARTIAL");
        batch.setUpdateDate(LocalDateTime.now());
        batchMapper.updateById(batch);
        return toBatchDto(batch);
    }

    private List<FlowRecord> parse(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            return CsvStatementParser.parse(is);
        } catch (Exception e) {
            throw new BusinessException(ReconciliationErrorCode.E_RECON_110101, "账单解析失败: " + e.getMessage());
        }
    }

    private void insertDiff(String batchNo, String bizNo, String diffType,
                           BigDecimal platformAmount, BigDecimal bankAmount, LocalDateTime now) {
        ReconciliationDiff d = new ReconciliationDiff();
        d.setBatchNo(batchNo);
        d.setBizNo(bizNo);
        d.setDiffType(diffType);
        d.setPlatformAmount(platformAmount);
        d.setBankAmount(bankAmount);
        d.setStatus("PENDING");
        d.setCreateDate(now);
        diffMapper.insert(d);
    }

    private void insertDetail(String batchNo, String bizNo, String direction,
                             BigDecimal amount, String status, LocalDateTime now) {
        ReconciliationDetail det = new ReconciliationDetail();
        det.setBatchNo(batchNo);
        det.setBizNo(bizNo);
        det.setDirection(direction);
        det.setAmount(amount);
        det.setStatus(status);
        det.setCreateDate(now);
        detailMapper.insert(det);
    }

    public List<ReconciliationBatchDTO> listBatches() {
        List<ReconciliationBatch> list = batchMapper.selectList(
                new LambdaQueryWrapper<ReconciliationBatch>().orderByDesc(ReconciliationBatch::getCreateDate));
        return list.stream().map(this::toBatchDto).collect(Collectors.toList());
    }

    public List<ReconciliationDiffDTO> getDiffs(String batchNo) {
        List<ReconciliationDiff> list = diffMapper.selectList(
                new LambdaQueryWrapper<ReconciliationDiff>().eq(ReconciliationDiff::getBatchNo, batchNo)
                        .orderByAsc(ReconciliationDiff::getId));
        return list.stream().map(this::toDiffDto).collect(Collectors.toList());
    }

    public void handleDiff(Long id, DiffHandleRequest req) {
        ReconciliationDiff diff = diffMapper.selectById(id);
        if (diff == null) {
            throw new BusinessException(ReconciliationErrorCode.E_RECON_110101, "差异记录不存在: " + id);
        }
        diff.setStatus("HANDLED");
        diff.setHandler(req.getHandler());
        diff.setHandleNote(req.getHandleNote());
        diff.setHandleTime(LocalDateTime.now());
        diffMapper.updateById(diff);
    }

    public String export(String batchNo) {
        List<ReconciliationDiff> list = diffMapper.selectList(
                new LambdaQueryWrapper<ReconciliationDiff>().eq(ReconciliationDiff::getBatchNo, batchNo));
        StringBuilder sb = new StringBuilder("bizNo,diffType,platformAmount,bankAmount,status\n");
        for (ReconciliationDiff d : list) {
            sb.append(d.getBizNo()).append(",").append(d.getDiffType()).append(",")
                    .append(d.getPlatformAmount()).append(",").append(d.getBankAmount()).append(",")
                    .append(d.getStatus()).append("\n");
        }
        return sb.toString();
    }

    private ReconciliationBatchDTO toBatchDto(ReconciliationBatch b) {
        ReconciliationBatchDTO d = new ReconciliationBatchDTO();
        d.setBatchNo(b.getBatchNo());
        d.setBizDate(b.getBizDate());
        d.setStatus(b.getStatus());
        d.setTotalPlatform(b.getTotalPlatform());
        d.setTotalBank(b.getTotalBank());
        d.setMatchedCount(b.getMatchedCount());
        d.setDiffCount(b.getDiffCount());
        d.setCreateDate(b.getCreateDate());
        return d;
    }

    private ReconciliationDiffDTO toDiffDto(ReconciliationDiff d) {
        ReconciliationDiffDTO dto = new ReconciliationDiffDTO();
        dto.setId(d.getId());
        dto.setBatchNo(d.getBatchNo());
        dto.setBizNo(d.getBizNo());
        dto.setDiffType(d.getDiffType());
        dto.setPlatformAmount(d.getPlatformAmount());
        dto.setBankAmount(d.getBankAmount());
        dto.setStatus(d.getStatus());
        dto.setHandler(d.getHandler());
        dto.setHandleTime(d.getHandleTime());
        return dto;
    }
}
