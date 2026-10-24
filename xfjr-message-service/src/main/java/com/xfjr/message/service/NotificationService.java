package com.xfjr.message.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xfjr.api.message.NotificationDTO;
import com.xfjr.api.message.NotificationTemplateDTO;
import com.xfjr.api.message.SendNotificationRequest;
import com.xfjr.common.domain.BusinessException;
import com.xfjr.message.channel.MessageChannel;
import com.xfjr.message.common.MessageErrorCode;
import com.xfjr.message.entity.Notification;
import com.xfjr.message.entity.NotificationTemplate;
import com.xfjr.message.mapper.NotificationMapper;
import com.xfjr.message.mapper.NotificationTemplateMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 统一消息中心业务实现：
 * 1. 渠道可插拔（SMS / STATION / APP），通过 Map&lt;beanName, MessageChannel&gt; 注入；
 * 2. 频率限制：同一用户同一渠道每小时上限可配置；
 * 3. 模板渲染 + 直填内容；
 * 4. 投递失败重试（最多 max-retry 次），状态落库（PENDING/SENT/FAILED）。
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final int DEFAULT_MAX_RETRY = 3;
    private static final int DEFAULT_MAX_PER_HOUR = 5;

    private final NotificationMapper notificationMapper;
    private final NotificationTemplateMapper templateMapper;
    private final Map<String, MessageChannel> channelMap;

    @Value("${message.max-per-hour:5}")
    private int maxPerHour;
    @Value("${message.max-retry:3}")
    private int maxRetry;

    @Autowired
    public NotificationService(NotificationMapper notificationMapper,
                               NotificationTemplateMapper templateMapper,
                               Map<String, MessageChannel> channelMap) {
        this.notificationMapper = notificationMapper;
        this.templateMapper = templateMapper;
        this.channelMap = channelMap;
    }

    /**
     * 发送消息：校验渠道与频率 -> 模板渲染/直填 -> 落库 -> 渠道投递（含重试）
     */
    public NotificationDTO send(SendNotificationRequest req) {
        String channel = req.getChannel();
        if (channel == null || !channelMap.containsKey(channel)) {
            throw new BusinessException(MessageErrorCode.E_MESSAGE_120201, "不支持的渠道: " + channel);
        }

        int limit = maxPerHour > 0 ? maxPerHour : DEFAULT_MAX_PER_HOUR;
        long recentCount = notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getReceiverId, req.getReceiverId())
                .eq(Notification::getChannel, channel)
                .ge(Notification::getCreateDate, LocalDateTime.now().minusHours(1)));
        if (recentCount >= limit) {
            throw new BusinessException(MessageErrorCode.E_MESSAGE_120202, "消息发送过于频繁，请稍后再试");
        }

        Notification entity = new Notification();
        entity.setNotificationId(UUID.randomUUID().toString().replace("-", ""));
        entity.setChannel(channel);
        entity.setReceiverId(req.getReceiverId());
        entity.setRetryCount(0);
        entity.setStatus("PENDING");
        entity.setCreateDate(LocalDateTime.now());
        entity.setUpdateDate(LocalDateTime.now());

        if (req.getTemplateCode() != null && !req.getTemplateCode().isEmpty()) {
            NotificationTemplate tpl = templateMapper.selectOne(new LambdaQueryWrapper<NotificationTemplate>()
                    .eq(NotificationTemplate::getTemplateCode, req.getTemplateCode()));
            if (tpl == null) {
                throw new BusinessException(MessageErrorCode.E_MESSAGE_120203, "模板不存在: " + req.getTemplateCode());
            }
            entity.setTemplateCode(tpl.getTemplateCode());
            entity.setTitle(tpl.getTitle());
            entity.setContent(render(tpl.getContentTemplate(), req.getParams()));
        } else {
            if (isEmpty(req.getTitle()) && isEmpty(req.getContent())) {
                throw new BusinessException(MessageErrorCode.E_MESSAGE_120204, "消息标题或内容不能为空");
            }
            entity.setTitle(req.getTitle());
            entity.setContent(req.getContent());
        }

        notificationMapper.insert(entity);
        dispatch(entity);
        return toDTO(entity);
    }

    /**
     * 渠道投递：失败按 max-retry 重试，最终更新状态 SENT / FAILED
     */
    private void dispatch(Notification entity) {
        MessageChannel ch = channelMap.get(entity.getChannel());
        int retry = maxRetry > 0 ? maxRetry : DEFAULT_MAX_RETRY;
        boolean ok = false;
        while (entity.getRetryCount() < retry) {
            try {
                ok = ch.send(entity);
                if (ok) {
                    break;
                }
            } catch (Exception e) {
                log.warn("渠道投递异常 channel={} id={}", entity.getChannel(), entity.getNotificationId(), e);
            }
            entity.setRetryCount(entity.getRetryCount() + 1);
        }
        if (ok) {
            entity.setStatus("SENT");
            entity.setSendTime(LocalDateTime.now());
        } else {
            entity.setStatus("FAILED");
        }
        entity.setUpdateDate(LocalDateTime.now());
        notificationMapper.updateById(entity);
    }

    /**
     * 查询模板列表
     */
    public List<NotificationTemplateDTO> templates() {
        List<NotificationTemplate> list = templateMapper.selectList(null);
        return list.stream().map(this::toTemplateDTO).collect(Collectors.toList());
    }

    /**
     * 查询指定接收人的消息列表（按创建时间倒序）
     */
    public List<NotificationDTO> myMessages(String receiverId) {
        List<Notification> list = notificationMapper.selectList(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getReceiverId, receiverId)
                .orderByDesc(Notification::getCreateDate));
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    private String render(String template, Map<String, String> params) {
        if (template == null) {
            return null;
        }
        if (params == null || params.isEmpty()) {
            return template;
        }
        String result = template;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            result = result.replace("${" + entry.getKey() + "}", entry.getValue() == null ? "" : entry.getValue());
        }
        return result;
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private NotificationDTO toDTO(Notification entity) {
        NotificationDTO dto = new NotificationDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private NotificationTemplateDTO toTemplateDTO(NotificationTemplate tpl) {
        NotificationTemplateDTO dto = new NotificationTemplateDTO();
        BeanUtils.copyProperties(tpl, dto);
        return dto;
    }
}
