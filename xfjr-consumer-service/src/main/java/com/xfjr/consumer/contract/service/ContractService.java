package com.xfjr.consumer.contract.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xfjr.api.contract.ContractDTO;
import com.xfjr.api.contract.ContractEvidenceDTO;
import com.xfjr.api.contract.CreateContractRequest;
import com.xfjr.api.contract.SignRequest;
import com.xfjr.common.domain.BusinessException;
import com.xfjr.consumer.common.ConsumerErrorCode;
import com.xfjr.consumer.common.SecurityUtil;
import com.xfjr.consumer.contract.entity.Contract;
import com.xfjr.consumer.contract.entity.ContractEvidence;
import com.xfjr.consumer.contract.entity.ContractSign;
import com.xfjr.consumer.contract.entity.ContractTemplate;
import com.xfjr.consumer.entity.Consumer;
import com.xfjr.consumer.mapper.ConsumerMapper;
import com.xfjr.consumer.mapper.ContractEvidenceMapper;
import com.xfjr.consumer.mapper.ContractMapper;
import com.xfjr.consumer.mapper.ContractSignMapper;
import com.xfjr.consumer.mapper.ContractTemplateMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 电子合同与存证（FR-133）。
 * 生成→（推送签署）→签署(证据链)→存证；内容 SHA-256 哈希用于防篡改（AC3）。
 */
@Slf4j
@Service
public class ContractService {

    private static final String DEFAULT_LOAN_CONTENT =
            "借款合同\n项目：${projectId}\n出借人：${lenderId}\n借款人：${borrowerId}\n签署日期：${signDate}\n" +
            "（模板内容占位，生产环境接入合同模板引擎渲染为 PDF）";
    private static final String DEFAULT_BROKERAGE_CONTENT =
            "居间服务协议\n项目：${projectId}\n借款人：${borrowerId}\n签署日期：${signDate}\n（模板内容占位）";

    private final ContractMapper contractMapper;
    private final ContractSignMapper signMapper;
    private final ContractEvidenceMapper evidenceMapper;
    private final ContractTemplateMapper templateMapper;
    private final ConsumerMapper consumerMapper;

    public ContractService(ContractMapper contractMapper,
                           ContractSignMapper signMapper,
                           ContractEvidenceMapper evidenceMapper,
                           ContractTemplateMapper templateMapper,
                           ConsumerMapper consumerMapper) {
        this.contractMapper = contractMapper;
        this.signMapper = signMapper;
        this.evidenceMapper = evidenceMapper;
        this.templateMapper = templateMapper;
        this.consumerMapper = consumerMapper;
    }

    /** 放款成功触发生成合同（AC1）：每个出借人一份借款合同 + 一份居间服务协议。 */
    @Transactional(rollbackFor = Exception.class)
    public List<ContractDTO> create(CreateContractRequest req) {
        if (req == null || req.getProjectId() == null) {
            throw new BusinessException(ConsumerErrorCode.E_140305, "项目标识不能为空");
        }
        if (req.getBorrowerId() == null) {
            throw new BusinessException(ConsumerErrorCode.E_140305, "借款人不能为空");
        }
        String type = (req.getContractType() == null || req.getContractType().trim().isEmpty())
                ? "LOAN" : req.getContractType().trim().toUpperCase();
        List<Long> lenders = (req.getLenderIds() == null || req.getLenderIds().isEmpty())
                ? Collections.singletonList(req.getBorrowerId()) : req.getLenderIds();
        LocalDateTime now = LocalDateTime.now();

        List<ContractDTO> result = new ArrayList<>();
        for (Long lenderId : lenders) {
            result.add(toDto(genContract(req.getProjectId(), lenderId, req.getBorrowerId(), type, now)));
        }
        if (!"BROKERAGE".equals(type)) {
            result.add(toDto(genContract(req.getProjectId(), null, req.getBorrowerId(), "BROKERAGE", now)));
        }
        // 推送签署（FR-133：经 message-service 推送）。此处记录日志；生产环境经 Feign 调用 message-service。
        for (ContractDTO c : result) {
            log.info("合同 {} 已生成，推送各方签署", c.getContractNo());
        }
        return result;
    }

    /** 我的合同列表（当前登录用户作为出借人或借款人）。 */
    public List<ContractDTO> myContracts() {
        String mobile = SecurityUtil.getUser().getMobile();
        if (mobile == null || mobile.trim().isEmpty()) {
            return Collections.emptyList();
        }
        Consumer consumer = consumerMapper.selectOne(
                new LambdaQueryWrapper<Consumer>().eq(Consumer::getMobile, mobile).last("limit 1"));
        if (consumer == null || consumer.getId() == null) {
            return Collections.emptyList();
        }
        Long cid = consumer.getId();
        List<Contract> list = contractMapper.selectList(new LambdaQueryWrapper<Contract>()
                .and(w -> w.eq(Contract::getLenderId, cid).or().eq(Contract::getBorrowerId, cid))
                .orderByDesc(Contract::getCreateDate));
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    /** 合同详情/下载（含文件哈希，供防篡改校验 AC3）。 */
    public ContractDTO detail(String contractNo) {
        Contract c = findByContractNo(contractNo);
        if (c == null) {
            throw new BusinessException(ConsumerErrorCode.E_140301, "合同不存在: " + contractNo);
        }
        return toDto(c);
    }

    /** 签署合同（AC2）：记录签署人/证书序列号/签署时间，签署完成即存证。 */
    @Transactional(rollbackFor = Exception.class)
    public ContractDTO sign(String contractNo, SignRequest req) {
        Contract c = findByContractNo(contractNo);
        if (c == null) {
            throw new BusinessException(ConsumerErrorCode.E_140301, "合同不存在: " + contractNo);
        }
        if (!"GENERATED".equals(c.getStatus())) {
            throw new BusinessException(ConsumerErrorCode.E_140302, "合同当前状态不可签署: " + c.getStatus());
        }
        LocalDateTime now = LocalDateTime.now();
        ContractSign s = new ContractSign();
        s.setContractNo(contractNo);
        s.setSigner(req.getSigner());
        s.setSignType(req.getSignType());
        s.setCertSerial(req.getCertSerial());
        s.setSignTime(now);
        s.setCreateDate(now);
        signMapper.insert(s);
        createEvidence(c, now);
        c.setSignTime(now);
        c.setStatus("EVIDENCED");
        c.setUpdateDate(now);
        contractMapper.updateById(c);
        return toDto(c);
    }

    /** 存证报告（合同全部存证记录）。 */
    public List<ContractEvidenceDTO> evidence(String contractNo) {
        List<ContractEvidence> list = evidenceMapper.selectList(
                new LambdaQueryWrapper<ContractEvidence>()
                        .eq(ContractEvidence::getContractNo, contractNo)
                        .orderByAsc(ContractEvidence::getId));
        return list.stream().map(this::toEvidenceDto).collect(Collectors.toList());
    }

    private Contract genContract(Long projectId, Long lenderId, Long borrowerId, String type, LocalDateTime now) {
        ContractTemplate tpl = findTemplate(type);
        Map<String, String> ctx = new HashMap<>();
        ctx.put("projectId", String.valueOf(projectId));
        ctx.put("lenderId", lenderId == null ? "-" : String.valueOf(lenderId));
        ctx.put("borrowerId", String.valueOf(borrowerId));
        ctx.put("signDate", now.toLocalDate().toString());
        String template = tpl != null ? tpl.getTemplateContent()
                : ("BROKERAGE".equals(type) ? DEFAULT_BROKERAGE_CONTENT : DEFAULT_LOAN_CONTENT);
        String content = render(template, ctx);

        Contract c = new Contract();
        c.setContractNo(genContractNo(type));
        c.setType(type);
        c.setProjectId(projectId);
        c.setLenderId(lenderId);
        c.setBorrowerId(borrowerId);
        c.setTemplateId(tpl == null ? null : tpl.getId());
        // 生产环境：渲染为 PDF 后上传 file-service 并回填真实 fileId
        c.setFileId(UUID.randomUUID().toString().replace("-", ""));
        c.setFileHash(sha256(content));
        c.setContent(content);
        c.setStatus("GENERATED");
        c.setCreateDate(now);
        c.setUpdateDate(now);
        contractMapper.insert(c);
        return c;
    }

    private void createEvidence(Contract c, LocalDateTime now) {
        ContractEvidence e = new ContractEvidence();
        e.setContractNo(c.getContractNo());
        e.setEvidenceType("THIRD_PARTY");
        // 基于内容哈希生成存证编号，防篡改且可追溯
        e.setEvidenceNo("EV" + c.getFileHash().substring(0, 16).toUpperCase());
        e.setStorageUrl("evidence://" + c.getContractNo());
        e.setCreateDate(now);
        evidenceMapper.insert(e);
    }

    private ContractTemplate findTemplate(String type) {
        return templateMapper.selectOne(new LambdaQueryWrapper<ContractTemplate>()
                .eq(ContractTemplate::getContractType, type)
                .eq(ContractTemplate::getStatus, 1)
                .last("limit 1"));
    }

    private Contract findByContractNo(String contractNo) {
        return contractMapper.selectOne(
                new LambdaQueryWrapper<Contract>().eq(Contract::getContractNo, contractNo).last("limit 1"));
    }

    private String render(String template, Map<String, String> ctx) {
        if (template == null) {
            return "";
        }
        String result = template;
        for (Map.Entry<String, String> e : ctx.entrySet()) {
            result = result.replace("${" + e.getKey() + "}", e.getValue() == null ? "" : e.getValue());
        }
        return result;
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

    private String genContractNo(String type) {
        String prefix = "LOAN".equals(type) ? "CTL" : "CTB";
        return prefix + System.currentTimeMillis() + (int) (Math.random() * 9000 + 1000);
    }

    private ContractDTO toDto(Contract c) {
        ContractDTO dto = new ContractDTO();
        BeanUtils.copyProperties(c, dto);
        return dto;
    }

    private ContractEvidenceDTO toEvidenceDto(ContractEvidence e) {
        ContractEvidenceDTO dto = new ContractEvidenceDTO();
        BeanUtils.copyProperties(e, dto);
        return dto;
    }
}
