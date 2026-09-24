package com.helpdesk.service;

import com.helpdesk.config.KafkaTopicConfig;
import com.helpdesk.dto.TicketEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Publishes ticket lifecycle events to Kafka, and (in the same service, for
 * simplicity in this scaffold) consumes them to simulate sending an
 * email/push/SMS notification. In a real deployment the consumer would live
 * in a separate notification microservice.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final KafkaTemplate<String, TicketEvent> kafkaTemplate;

    public void publishTicketEvent(TicketEvent event) {
        event.setTimestamp(LocalDateTime.now());
        kafkaTemplate.send(KafkaTopicConfig.TICKET_EVENTS_TOPIC, event.getTicketCode(), event);
    }

    @KafkaListener(topics = KafkaTopicConfig.TICKET_EVENTS_TOPIC, groupId = "helpdesk-notification-service")
    public void consumeTicketEvent(TicketEvent event) {
        // Scaffold: log only. Wire this up to spring-boot-starter-mail / an SMS
        // gateway / a push-notification provider to actually notify the user.
        log.info("Notification -> userId={} ticket={} status={} message={}",
                event.getRecipientUserId(), event.getTicketCode(), event.getStatus(), event.getMessage());
    }
}
