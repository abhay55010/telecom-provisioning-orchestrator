package com.abhaypanday.provisioning;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "provisioning_request", indexes = @Index(name = "idx_status", columnList = "status"))
public class ProvisioningRequest {

    @Id
    private UUID id;

    private String subscriberName;
    private String serviceType;
    private String msisdn;

    @Enumerated(EnumType.STRING)
    private ProvisioningStatus status;

    private String failureReason;

    /** Demo hook: when true, activation deterministically fails so we can show compensation. */
    private boolean simulateActivationFailure;

    private Instant createdAt;
    private Instant updatedAt;

    protected ProvisioningRequest() {
        // for JPA
    }

    public static ProvisioningRequest start(String subscriberName, String serviceType, boolean simulateFailure) {
        ProvisioningRequest r = new ProvisioningRequest();
        r.id = UUID.randomUUID();
        r.subscriberName = subscriberName;
        r.serviceType = serviceType;
        r.simulateActivationFailure = simulateFailure;
        r.status = ProvisioningStatus.PENDING;
        r.createdAt = Instant.now();
        r.updatedAt = r.createdAt;
        return r;
    }

    public void setStatus(ProvisioningStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getSubscriberName() {
        return subscriberName;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public ProvisioningStatus getStatus() {
        return status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public boolean isSimulateActivationFailure() {
        return simulateActivationFailure;
    }
}
