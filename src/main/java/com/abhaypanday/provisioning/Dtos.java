package com.abhaypanday.provisioning;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;


public final class Dtos {

    private Dtos() {
    }


    public record CreateRequest(
            @NotBlank(message = "subscriberName is required") String subscriberName,
            @NotBlank(message = "serviceType is required") String serviceType,
            boolean simulateActivationFailure) {
    }


    public record Response(
            UUID sagaId,
            String subscriberName,
            String serviceType,
            String msisdn,
            ProvisioningStatus status,
            String failureReason) {

        public static Response from(ProvisioningRequest r) {
            return new Response(r.getId(), r.getSubscriberName(), r.getServiceType(),
                    r.getMsisdn(), r.getStatus(), r.getFailureReason());
        }
    }
}
