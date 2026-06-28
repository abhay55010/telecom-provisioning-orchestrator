package com.abhaypanday.provisioning;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;


@Component
public class SagaListeners {

    private static final Logger log = LoggerFactory.getLogger(SagaListeners.class);

    private final ProvisioningService service;
    private final IdempotencyService idempotency;
    private final EventPublisher publisher;
    private final HssActivationClient hss;
    private final MeterRegistry metrics;

    public SagaListeners(ProvisioningService service,
                         IdempotencyService idempotency,
                         EventPublisher publisher,
                         HssActivationClient hss,
                         MeterRegistry metrics) {
        this.service = service;
        this.idempotency = idempotency;
        this.publisher = publisher;
        this.hss = hss;
        this.metrics = metrics;
    }

    /** Step 1: reserve a subscriber number (MSISDN). */
    @KafkaListener(topics = KafkaConfig.REQUESTED, groupId = "${app.kafka.group-id}")
    public void reserveNumber(Events.ProvisioningRequested event) {
        if (!idempotency.firstTime("reserve", event.eventId())) {
            return;
        }
        String msisdn = "+9198" + String.format("%08d", ThreadLocalRandom.current().nextInt(100_000_000));
        service.markNumberReserved(event.sagaId(), msisdn);
        metrics.counter("provisioning.numbers.reserved").increment();
        log.info("Reserved msisdn={} for sagaId={}", msisdn, event.sagaId());

        publisher.publish(KafkaConfig.NUMBER_RESERVED, event.sagaId(),
                Events.NumberReserved.of(event.sagaId(), msisdn, event.simulateActivationFailure()));
    }


    @KafkaListener(topics = KafkaConfig.NUMBER_RESERVED, groupId = "${app.kafka.group-id}")
    public void activate(Events.NumberReserved event) {
        if (!idempotency.firstTime("activate", event.eventId())) {
            return;
        }
        try {
            hss.activate(event.sagaId(), event.msisdn(), event.simulateActivationFailure());
            service.markCompleted(event.sagaId());
            metrics.counter("provisioning.activation.succeeded").increment();
        } catch (RuntimeException ex) {
            log.warn("Activation failed for sagaId={}; compensating. Reason: {}", event.sagaId(), ex.getMessage());
            metrics.counter("provisioning.activation.failed").increment();
            service.markFailed(event.sagaId(), ex.getMessage());
            publisher.publish(KafkaConfig.RELEASE_NUMBER, event.sagaId(),
                    Events.ReleaseNumber.of(event.sagaId(), event.msisdn(), ex.getMessage()));
        }
    }


    @KafkaListener(topics = KafkaConfig.RELEASE_NUMBER, groupId = "${app.kafka.group-id}")
    public void releaseNumber(Events.ReleaseNumber command) {
        if (!idempotency.firstTime("release", command.eventId())) {
            return;
        }
        service.markCompensated(command.sagaId(), command.reason());
        metrics.counter("provisioning.numbers.released").increment();
        log.info("Released msisdn={} for sagaId={} (compensation)", command.msisdn(), command.sagaId());
    }
}
