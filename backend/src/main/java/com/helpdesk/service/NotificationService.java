package com.helpdesk.service;

import com.helpdesk.config.KafkaTopicConfig;
import com.helpdesk.dto.TicketEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final KafkaTemplate<String, TicketEvent> kafkaTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private static final String NOTIF_KEY_PREFIX = "user:notifications:";

    public void publishTicketEvent(TicketEvent event) {
        if (event.getTimestamp() == null) {
            event.setTimestamp(LocalDateTime.now());
        }
        log.info("🚀 [KAFKA PRODUCER] Publishing event for ticket #{}: {}", event.getTicketCode(), event.getMessage());
        kafkaTemplate.send(KafkaTopicConfig.TICKET_EVENTS_TOPIC, event.getTicketCode(), event);
    }

    @KafkaListener(topics = KafkaTopicConfig.TICKET_EVENTS_TOPIC, groupId = "helpdesk-notification-service")
    public void consumeTicketEvent(TicketEvent event) {
        log.info("📥 [KAFKA CONSUMER] Consumed TicketEvent: recipientId={} ticketCode={} message={}",
                event.getRecipientUserId(), event.getTicketCode(), event.getMessage());

        if (event.getRecipientUserId() != null) {
            try {
                String key = NOTIF_KEY_PREFIX + event.getRecipientUserId();
                redisTemplate.opsForList().leftPush(key, event);
                redisTemplate.opsForList().trim(key, 0, 19); // keep last 20 messages
                redisTemplate.expire(key, Duration.ofDays(7));
                log.info("Stored Kafka notification in Redis for recipientId={}", event.getRecipientUserId());
            } catch (Exception e) {
                log.error("Failed to persist notification in Redis: {}", e.getMessage());
            }
        }
    }

    public List<TicketEvent> getNotificationsForUser(Long userId) {
        String key = NOTIF_KEY_PREFIX + userId;
        try {
            List<Object> rawList = redisTemplate.opsForList().range(key, 0, -1);
            if (rawList == null || rawList.isEmpty()) {
                return List.of();
            }

            List<TicketEvent> result = new ArrayList<>();
            for (Object obj : rawList) {
                if (obj instanceof TicketEvent te) {
                    result.add(te);
                } else {
                    result.add(objectMapper.convertValue(obj, TicketEvent.class));
                }
            }
            return result;
        } catch (Exception e) {
            log.error("Failed to retrieve notifications from Redis for userId={}: {}", userId, e.getMessage());
            return List.of();
        }
    }

    public void dismissNotification(Long userId, String eventId) {
        String key = NOTIF_KEY_PREFIX + userId;
        try {
            List<TicketEvent> existing = getNotificationsForUser(userId);
            List<TicketEvent> updated = existing.stream()
                    .filter(e -> e.getEventId() == null || !e.getEventId().equals(eventId))
                    .toList();

            redisTemplate.delete(key);
            for (int i = updated.size() - 1; i >= 0; i--) {
                redisTemplate.opsForList().leftPush(key, updated.get(i));
            }
            redisTemplate.expire(key, Duration.ofDays(7));
        } catch (Exception e) {
            log.error("Failed to dismiss notification: {}", e.getMessage());
        }
    }

    public void clearAllNotifications(Long userId) {
        redisTemplate.delete(NOTIF_KEY_PREFIX + userId);
    }
}
