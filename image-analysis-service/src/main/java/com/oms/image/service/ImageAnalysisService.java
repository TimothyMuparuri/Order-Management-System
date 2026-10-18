package com.oms.image.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oms.eventing.Topics;
import com.oms.eventing.events.Events;
import com.oms.eventing.idempotency.IdempotentConsumer;
import com.oms.eventing.publish.DomainEventPublisher;
import com.oms.image.vision.VisionProvider;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ImageAnalysisService {

    private final VisionProvider visionProvider;
    private final DomainEventPublisher events;
    private final IdempotentConsumer idempotentConsumer;
    private final ObjectMapper objectMapper;

    public ImageAnalysisService(
            VisionProvider visionProvider,
            DomainEventPublisher events,
            IdempotentConsumer idempotentConsumer,
            ObjectMapper objectMapper
    ) {
        this.visionProvider = visionProvider;
        this.events = events;
        this.idempotentConsumer = idempotentConsumer;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = Topics.IMAGES_UPLOADED, groupId = "image-analysis-service")
    @Transactional
    public void onImageUploaded(String payload) throws Exception {
        Events.ImageUploaded event = objectMapper.readValue(payload, Events.ImageUploaded.class);
        String key = Topics.IMAGES_UPLOADED + ":" + event.imageId();
        idempotentConsumer.processOnce(key, () -> {
            List<VisionProvider.ItemCandidate> matches = visionProvider.matchProduct(
                    new VisionProvider.ImageRef(event.imageId(), event.objectKey(), event.contentType()));
            List<Events.Candidate> candidates = matches.stream()
                    .map(m -> new Events.Candidate(m.itemRef(), m.confidence()))
                    .toList();
            events.publish(event.imageId(), Topics.IMAGES_MATCH_SUGGESTED,
                    new Events.ImageMatchSuggested(event.imageId(), candidates));
        });
    }
}
