package com.xfjr.depository.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xfjr.api.depository.model.DepositoryCommandRequest;
import com.xfjr.api.depository.model.DepositoryCommandStatusDTO;
import com.xfjr.common.domain.BusinessException;
import com.xfjr.depository.common.constant.DepositoryErrorCode;
import com.xfjr.depository.entity.DepositoryRecord;
import com.xfjr.depository.mapper.DepositoryRecordMapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("存管指令下发（FR-134）：重试/熔断/补偿")
class DepositoryCommandServiceTest {

    /** 纯 Mockito 测试无 MyBatis-Plus 上下文，需手动初始化实体 TableInfo 以支持 Lambda 列解析。 */
    @BeforeAll
    static void initTableInfo() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), DepositoryRecord.class);
    }

    @Mock
    private DepositoryRecordMapper recordMapper;
    @Mock
    private OkHttpService okHttpService;
    @Mock
    private ConfigService configService;
    @InjectMocks
    private DepositoryCommandService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "maxFailures", 5);
        ReflectionTestUtils.setField(service, "recoveryMillis", 30000L);
        ReflectionTestUtils.setField(service, "maxRetry", 3);
        ReflectionTestUtils.setField(service, "baseBackoffMillis", 1L); // 缩短退避，加速测试
    }

    private DepositoryCommandRequest req() {
        DepositoryCommandRequest r = new DepositoryCommandRequest();
        r.setServiceName("TRANSFER_OUT");
        r.setRequestNo("CMD-001");
        r.setPayload("{\"amt\":1}");
        return r;
    }

    private void stubConfig() {
        when(configService.getDepositoryUrl()).thenReturn("http://dep");
        when(configService.getP2pCode()).thenReturn("P2P");
    }

    @Test
    @DisplayName("AC1 下发成功 → 落库 SUCCESS 并返回 DTO")
    void send_success() {
        when(recordMapper.selectOne(any())).thenReturn(null);
        stubConfig();
        when(okHttpService.doSyncGet(any())).thenReturn("ok");

        DepositoryCommandStatusDTO dto = service.sendCommand(req());

        assertEquals("SUCCESS", dto.getStatus());
        assertNotNull(dto.getRequestNo());
        verify(recordMapper).insert(any(DepositoryRecord.class));
    }

    @Test
    @DisplayName("幂等：已成功指令直接返回，不再调用存管")
    void send_idempotent_success() {
        DepositoryRecord existing = new DepositoryRecord();
        existing.setId(99L);
        existing.setRequestNo("CMD-001");
        existing.setStatus("SUCCESS");
        when(recordMapper.selectOne(any())).thenReturn(existing);

        DepositoryCommandStatusDTO dto = service.sendCommand(req());

        assertEquals("SUCCESS", dto.getStatus());
        verify(okHttpService, never()).doSyncGet(any());
        verify(recordMapper, never()).insert(any(DepositoryRecord.class));
    }

    @Test
    @DisplayName("备选流② 连续失败重试 maxRetry 次后失败（未触发熔断）")
    void send_fail_after_retries() {
        when(recordMapper.selectOne(any())).thenReturn(null);
        stubConfig();
        when(okHttpService.doSyncGet(any())).thenThrow(new RuntimeException("timeout"));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.sendCommand(req()));

        assertEquals(DepositoryErrorCode.E_160105, ex.getErrorCode());
        verify(okHttpService, times(3)).doSyncGet(any());
    }

    @Test
    @DisplayName("AC5 熔断触发后，后续调用直接拒绝（不再调存管）")
    void send_rejects_after_breaker_open() {
        ReflectionTestUtils.setField(service, "maxFailures", 1); // 单次失败即熔断
        when(recordMapper.selectOne(any())).thenReturn(null);
        stubConfig();
        when(okHttpService.doSyncGet(any())).thenThrow(new RuntimeException("timeout"));

        BusinessException first = assertThrows(BusinessException.class, () -> service.sendCommand(req()));
        assertThrows(BusinessException.class, () -> service.sendCommand(req()));

        assertEquals(DepositoryErrorCode.E_160104, first.getErrorCode());
        verify(okHttpService, times(1)).doSyncGet(any()); // 仅第 1 次真正调用了存管
    }

    @Test
    @DisplayName("AC4 补偿查询成功 → 更新为 SUCCESS")
    void compensate_success() {
        DepositoryRecord record = new DepositoryRecord();
        record.setId(5L);
        record.setRequestNo("CMD-001");
        record.setStatus("PROCESSING");
        when(recordMapper.selectOne(any())).thenReturn(record);
        stubConfig();
        when(okHttpService.doSyncGet(any())).thenReturn("ok");

        DepositoryCommandStatusDTO dto = service.compensate("CMD-001");

        assertEquals("SUCCESS", dto.getStatus());
        verify(recordMapper).updateById(any(DepositoryRecord.class));
    }

    @Test
    @DisplayName("AC4 补偿查询失败 → 抛 E_160105 并落 FAIL")
    void compensate_fail_throws() {
        DepositoryRecord record = new DepositoryRecord();
        record.setId(5L);
        record.setRequestNo("CMD-001");
        when(recordMapper.selectOne(any())).thenReturn(record);
        stubConfig();
        when(okHttpService.doSyncGet(any())).thenThrow(new RuntimeException("boom"));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.compensate("CMD-001"));

        assertEquals(DepositoryErrorCode.E_160105, ex.getErrorCode());
        verify(recordMapper).updateById(any(DepositoryRecord.class));
    }

    @Test
    @DisplayName("指令不存在 → 抛 E_160105")
    void commandStatus_not_found_throws() {
        when(recordMapper.selectOne(any())).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.commandStatus("NOPE"));

        assertEquals(DepositoryErrorCode.E_160105, ex.getErrorCode());
    }
}
