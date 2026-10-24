package com.xfjr.message.channel;

import com.xfjr.message.entity.Notification;
import org.springframework.stereotype.Component;

/**
 * 站内信渠道：内容已落库，直接视为投递成功
 */
@Component("STATION")
public class StationChannel implements MessageChannel {

    @Override
    public boolean send(Notification notification) throws Exception {
        return true;
    }
}
