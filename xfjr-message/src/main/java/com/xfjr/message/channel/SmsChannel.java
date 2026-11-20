package com.xfjr.message.channel;

import com.xfjr.message.entity.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 短信渠道（占位实现）。
 * TODO 集成真实短信网关，替换遗留 sailing.jar；生产需对接新短信服务并校验回执。
 */
@Component("SMS")
public class SmsChannel implements MessageChannel {

    private static final Logger log = LoggerFactory.getLogger(SmsChannel.class);

    @Override
    public boolean send(Notification notification) throws Exception {
        log.info("[SMS] to={} content={}", notification.getReceiverId(), notification.getContent());
        return true;
    }
}
