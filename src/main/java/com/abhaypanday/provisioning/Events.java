package com.abhaypanday.provisioning;

import java.util.UUID;



public final class Events {

    private Events() {
    }


    public record ProvisioningRequested(
            UUID eventId, UUID sagaId, boolean simulateActivationFailure) {

        public static ProvisioningRequested of(UUID sagaId, boolean simulateFailure) {
            return new ProvisioningRequested(UUID.randomUUID(), sagaId, simulateFailure);
        }
    }


    public record NumberReserved(
            UUID eventId, UUID sagaId, String msisdn, boolean simulateActivationFailure) {

        public static NumberReserved of(UUID sagaId, String msisdn, boolean simulateFailure) {
            return new NumberReserved(UUID.randomUUID(), sagaId, msisdn, simulateFailure);
        }
    }


    public record ReleaseNumber(
            UUID eventId, UUID sagaId, String msisdn, String reason) {

        public static ReleaseNumber of(UUID sagaId, String msisdn, String reason) {
            return new ReleaseNumber(UUID.randomUUID(), sagaId, msisdn, reason);
        }
    }
}
