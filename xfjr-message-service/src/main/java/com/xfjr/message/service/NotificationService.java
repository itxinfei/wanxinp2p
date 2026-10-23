package com.xfjr.message.service;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    public String ping() {
        return "Message service alive";
    }
}
