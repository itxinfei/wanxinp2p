package com.xfjr.reconciliation.parser;

import com.xfjr.reconciliation.domain.FlowRecord;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 银行/平台对账单 CSV 解析（首行为表头：bizNo,amount），可扩展 XML/Excel。
 */
public class CsvStatementParser {

    public static List<FlowRecord> parse(InputStream is) throws Exception {
        List<FlowRecord> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            boolean header = true;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                if (header) {
                    header = false;
                    continue;
                }
                String[] cols = line.split(",");
                if (cols.length < 2) {
                    continue;
                }
                FlowRecord r = new FlowRecord();
                r.setBizNo(cols[0].trim());
                r.setAmount(new BigDecimal(cols[1].trim()));
                list.add(r);
            }
        }
        return list;
    }
}
