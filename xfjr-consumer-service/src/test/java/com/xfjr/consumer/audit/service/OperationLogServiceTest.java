package com.xfjr.consumer.audit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;

import com.xfjr.consumer.audit.entity.OperationLog;
import com.xfjr.consumer.mapper.OperationLogMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("操作日志与审计（FR-132）：哈希链 + 追加写 + 检索")
class OperationLogServiceTest {

    @Mock
    private OperationLogMapper mapper;
    @InjectMocks
    private OperationLogService service;

    /** 内存存储：模拟追加写的只读回放（selectOne/selectList 依赖已插入记录） */
    private final List<OperationLog> store = new ArrayList<>();
    private final AtomicLong idSeq = new AtomicLong(0);

    @BeforeEach
    void setUp() {
        store.clear();
        idSeq.set(0);
        lenient().doAnswer(inv -> {
            OperationLog l = inv.getArgument(0);
            l.setId(idSeq.incrementAndGet());
            store.add(l);
            return 1;
        }).when(mapper).insert(any(OperationLog.class));
        // append 取最新一条作为 prev
        lenient().when(mapper.selectOne(any()))
                .thenAnswer(inv -> store.isEmpty() ? null : store.get(store.size() - 1));
        // verifyChain / query 取全部
        lenient().when(mapper.selectList(any())).thenAnswer(inv -> new ArrayList<>(store));
    }

    @Test
    @DisplayName("首条日志 prevHash = GENESIS，curHash 为 64 位 SHA-256")
    void append_first_uses_genesis_prev_hash() {
        OperationLog log = service.append("alice", "CREATE", "proj:1", "OK", "1.2.3.4");

        assertEquals(OperationLogService.GENESIS_HASH, log.getPrevHash());
        assertNotNull(log.getCurHash());
        assertEquals(64, log.getCurHash().length());
    }

    @Test
    @DisplayName("追加日志链式链接前一条 curHash（AC2 链式）")
    void append_links_to_previous_hash() {
        OperationLog first = service.append("alice", "CREATE", "p1", "OK", "1.1.1.1");
        OperationLog second = service.append("bob", "DELETE", "p2", "OK", "2.2.2.2");

        assertEquals(first.getCurHash(), second.getPrevHash());
    }

    @Test
    @DisplayName("完整链校验通过 → true")
    void verify_chain_valid() {
        service.append("alice", "CREATE", "p1", "OK", "1.1.1.1");
        service.append("bob", "UPDATE", "p2", "OK", "2.2.2.2");
        service.append("carol", "DELETE", "p3", "OK", "3.3.3.3");

        assertTrue(service.verifyChain());
    }

    @Test
    @DisplayName("篡改中间记录 → 校验失败（可检测，AC2）")
    void verify_chain_detects_tampering() {
        service.append("alice", "CREATE", "p1", "OK", "1.1.1.1");
        service.append("bob", "UPDATE", "p2", "OK", "2.2.2.2");
        service.append("carol", "DELETE", "p3", "OK", "3.3.3.3");

        store.get(1).setOperator("EVIL"); // 篡改中间记录的操作人

        assertFalse(service.verifyChain());
    }

    @Test
    @DisplayName("按操作人/动作检索 → 委托 Mapper（AC3）")
    void query_delegates_to_mapper() {
        service.append("alice", "CREATE", "p1", "OK", "1.1.1.1");
        service.append("bob", "UPDATE", "p2", "OK", "2.2.2.2");

        List<OperationLog> result = service.query("alice", "CREATE", null, null);

        assertNotNull(result);
        assertEquals(2, result.size());
    }
}
