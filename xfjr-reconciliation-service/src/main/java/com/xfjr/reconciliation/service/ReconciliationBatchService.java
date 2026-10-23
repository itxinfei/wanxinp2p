package com.xfjr.reconciliation.service;

import org.springframework.stereotype.Service;

@Service
public class ReconciliationBatchService {
    public String ping() {
        return "Reconciliation service alive";
    }
}
