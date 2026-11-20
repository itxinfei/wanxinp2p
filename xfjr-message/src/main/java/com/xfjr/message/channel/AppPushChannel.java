package com.xfjr.message.channel;

import com.xfjr.message.entity.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * App 推送渠道（占位实现）。TODO 集成厂商推送网关。
 */
@Component("APP")
public class AppPushChannel implements MessageChannel {

    private static final Logger log = LoggerFactory.getLogger(AppPushChannel.class);

    @Override
    public boolean send(Notification notification) throws Exception {
        log.info("[APP] to={} content={}", notification.getReceiverId(), notification.getContent());
        return true;
    }
}
