package com.abhaypanday.provisioning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
public class ProvisioningService {

    private static final Logger log = LoggerFactory.getLogger(ProvisioningService.class);

    private final ProvisioningRequestRepository repository;
    private final EventPublisher publisher;

    public ProvisioningService(ProvisioningRequestRepository repository, EventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    @Transactional
    public ProvisioningRequest start(Dtos.CreateRequest cmd) {
        ProvisioningRequest request = ProvisioningRequest.start(
                cmd.subscriberName(), cmd.serviceType(), cmd.simulateActivationFailure());
        repository.save(request);
        log.info("Started saga {} for '{}'", request.getId(), cmd.subscriberName());

        publisher.publish(KafkaConfig.REQUESTED, request.getId(),
                Events.ProvisioningRequested.of(request.getId(), cmd.simulateActivationFailure()));
        return request;
    }

    @Transactional(readOnly = true)
    public ProvisioningRequest get(UUID sagaId) {
        return find(sagaId);
    }

    @Transactional
    public void markNumberReserved(UUID sagaId, String msisdn) {
        ProvisioningRequest r = find(sagaId);
        r.setMsisdn(msisdn);
        r.setStatus(ProvisioningStatus.NUMBER_RESERVED);
    }

    @Transactional
    public void markCompleted(UUID sagaId) {
        find(sagaId).setStatus(ProvisioningStatus.COMPLETED);
        log.info("Saga {} COMPLETED", sagaId);
    }

    @Transactional
    public void markFailed(UUID sagaId, String reason) {
        ProvisioningRequest r = find(sagaId);
        r.setStatus(ProvisioningStatus.FAILED);
        r.setFailureReason(reason);
        log.warn("Saga {} FAILED: {}", sagaId, reason);
    }

    @Transactional
    public void markCompensated(UUID sagaId, String reason) {
        ProvisioningRequest r = find(sagaId);
        r.setMsisdn(null);
        r.setStatus(ProvisioningStatus.COMPENSATED);
        r.setFailureReason(reason);
        log.info("Saga {} COMPENSATED (number released)", sagaId);
    }

    private ProvisioningRequest find(UUID sagaId) {
        return repository.findById(sagaId)
                .orElseThrow(() -> new IllegalArgumentException("No provisioning request for sagaId=" + sagaId));
    }
}
