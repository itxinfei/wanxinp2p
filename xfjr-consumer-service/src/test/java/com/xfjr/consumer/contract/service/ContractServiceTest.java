package com.xfjr.consumer.contract.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xfjr.api.contract.ContractDTO;
import com.xfjr.api.contract.CreateContractRequest;
import com.xfjr.api.contract.SignRequest;
import com.xfjr.common.domain.BusinessException;
import com.xfjr.consumer.contract.entity.Contract;
import com.xfjr.consumer.contract.entity.ContractEvidence;
import com.xfjr.consumer.contract.entity.ContractSign;
import com.xfjr.consumer.mapper.ConsumerMapper;
import com.xfjr.consumer.mapper.ContractEvidenceMapper;
import com.xfjr.consumer.mapper.ContractMapper;
import com.xfjr.consumer.mapper.ContractSignMapper;
import com.xfjr.consumer.mapper.ContractTemplateMapper;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("电子合同与存证（FR-133）")
class ContractServiceTest {

    @Mock
    private ContractMapper contractMapper;
    @Mock
    private ContractSignMapper signMapper;
    @Mock
    private ContractEvidenceMapper evidenceMapper;
    @Mock
    private ContractTemplateMapper templateMapper;
    @Mock
    private ConsumerMapper consumerMapper;
    @InjectMocks
    private ContractService service;

    @Test
    @DisplayName("AC1 放款 → 生成出借人合同 + 居间协议，含 SHA-256 哈希")
    void create_should_generate() {
        when(templateMapper.selectOne(any())).thenReturn(null);
        when(contractMapper.insert(any(Contract.class))).thenReturn(1);

        CreateContractRequest req = new CreateContractRequest();
        req.setProjectId(1L);
        req.setBorrowerId(100L);
        req.setLenderIds(Arrays.asList(200L, 300L));
        List<ContractDTO> list = service.create(req);

        assertEquals(3, list.size());
        verify(contractMapper, times(3)).insert(any(Contract.class));
        assertEquals(64, list.get(0).getFileHash().length());
    }

    @Test
    @DisplayName("项目标识为空 → 抛业务异常")
    void create_should_throw_when_project_null() {
        CreateContractRequest req = new CreateContractRequest();
        req.setBorrowerId(100L);
        assertThrows(BusinessException.class, () -> service.create(req));
    }

    @Test
    @DisplayName("AC2 签署 → 记录证据链并自动存证")
    void sign_should_record_and_evidence() {
        Contract c = new Contract();
        c.setId(1L);
        c.setContractNo("CTL1");
        c.setStatus("GENERATED");
        c.setFileHash("abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789");
        when(contractMapper.selectOne(any())).thenReturn(c);

        SignRequest sign = new SignRequest();
        sign.setSigner("U100");
        sign.setSignType("SMS_VERIFY");
        sign.setCertSerial("CS-001");
        ContractDTO dto = service.sign("CTL1", sign);

        assertEquals("EVIDENCED", dto.getStatus());
        verify(signMapper).insert(any(ContractSign.class));
        verify(evidenceMapper).insert(any(ContractEvidence.class));
        verify(contractMapper).updateById(any(Contract.class));
    }

    @Test
    @DisplayName("非 GENERATED 状态签署 → 抛业务异常")
    void sign_should_throw_when_not_generated() {
        Contract c = new Contract();
        c.setContractNo("CTL1");
        c.setStatus("SIGNED");
        when(contractMapper.selectOne(any())).thenReturn(c);
        SignRequest sign = new SignRequest();
        sign.setSigner("U100");
        assertThrows(BusinessException.class, () -> service.sign("CTL1", sign));
    }
}
