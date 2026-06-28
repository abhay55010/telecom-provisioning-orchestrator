package com.abhaypanday.provisioning;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class HssActivationClient {

    private static final Logger log = LoggerFactory.getLogger(HssActivationClient.class);

    @Retry(name = "hssActivation")
    @CircuitBreaker(name = "hssActivation")
    public void activate(UUID sagaId, String msisdn, boolean simulateFailure) {
        log.info("Calling HSS to activate msisdn={} (sagaId={})", msisdn, sagaId);
        if (simulateFailure) {
            throw new IllegalStateException("HSS activation rejected for msisdn=" + msisdn);
        }
        log.info("HSS activation succeeded for msisdn={} (sagaId={})", msisdn, sagaId);
    }
}
