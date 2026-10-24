package com.xfjr.message.channel;

import com.xfjr.message.entity.Notification;

/**
 * 消息投递渠道抽象，支持 SMS / STATION / APP 可插拔实现
 */
public interface MessageChannel {

    boolean send(Notification notification) throws Exception;
}
