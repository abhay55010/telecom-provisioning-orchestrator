package com.abhaypanday.provisioning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Component
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final KafkaTemplate<String, Object> kafka;

    public EventPublisher(KafkaTemplate<String, Object> kafka) {
        this.kafka = kafka;
    }

    public void publish(String topic, UUID sagaId, Object event) {
        log.info("Publishing {} to '{}' (sagaId={})", event.getClass().getSimpleName(), topic, sagaId);
        kafka.send(topic, sagaId.toString(), event);
    }
}
