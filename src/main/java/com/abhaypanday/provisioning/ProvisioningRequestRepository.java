package com.abhaypanday.provisioning;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProvisioningRequestRepository extends JpaRepository<ProvisioningRequest, UUID> {
}
