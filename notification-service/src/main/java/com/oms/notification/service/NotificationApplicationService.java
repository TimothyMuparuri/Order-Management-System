package com.oms.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oms.common.result.Result;
import com.oms.eventing.Topics;
import com.oms.eventing.events.Events;
import com.oms.eventing.idempotency.IdempotentConsumer;
import com.oms.notification.domain.NotificationRecord;
import com.oms.notification.repo.NotificationRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class NotificationApplicationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationApplicationService.class);
    public static final int MAX_ATTEMPTS = 3;
    public static final Duration WINDOW = Duration.ofSeconds(60);

    private final NotificationRecordRepository repository;
    private final NotificationSender sender;
    private final IdempotentConsumer idempotentConsumer;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public NotificationApplicationService(
            NotificationRecordRepository repository,
            NotificationSender sender,
            IdempotentConsumer idempotentConsumer,
            ObjectMapper objectMapper,
            Clock clock
    ) {
        this.repository = repository;
        this.sender = sender;
        this.idempotentConsumer = idempotentConsumer;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @KafkaListener(topics = Topics.NOTIFICATIONS_REQUESTED, groupId = "notification-service")
    @Transactional
    public void onNotificationRequested(String payload) throws Exception {
        Events.NotificationRequested event = objectMapper.readValue(payload, Events.NotificationRequested.class);
        String key = Topics.NOTIFICATIONS_REQUESTED + ":" + event.orderId();
        idempotentConsumer.processOnce(key, () -> notifyDelivery(event.orderId(), event.customerId(),
                Instant.ofEpochMilli(event.deliveredAtEpochMillis())));
    }

    @Transactional
    public Result<Boolean> notifyDelivery(String orderId, String customerId, Instant deliveredAt) {
        NotificationRecord record = repository.findById(orderId).orElseGet(() ->
                repository.save(new NotificationRecord(orderId, customerId, deliveredAt)));

        if (record.getState() == NotificationRecord.NotificationState.SENT) {
            return Result.ok(Boolean.TRUE);
        }

        Instant now = clock.instant();
        if (Duration.between(deliveredAt, now).compareTo(WINDOW) > 0 && record.getAttempts() == 0) {
            log.warn("Notification for {} outside 60s window; still attempting with bounded retry", orderId);
        }

        while (record.getAttempts() < MAX_ATTEMPTS) {
            record.incrementAttempts();
            try {
                sender.send(customerId, orderId);
                record.markSent();
                repository.save(record);
                return Result.ok(Boolean.TRUE);
            } catch (Exception e) {
                log.warn("Notification attempt {} failed for {}: {}", record.getAttempts(), orderId, e.getMessage());
            }
        }
        record.markFailed();
        repository.save(record);
        // retain delivered-to-customer timestamp on the record
        return Result.domain("NOTIFICATION_FAILED", "Notification failed after " + MAX_ATTEMPTS + " attempts");
    }
}
