package com.abhaypanday.provisioning;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;


@RestController
@RequestMapping("/api/v1/provisioning")
public class ProvisioningController {

    private final ProvisioningService service;

    public ProvisioningController(ProvisioningService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Dtos.Response> create(@Valid @RequestBody Dtos.CreateRequest request) {
        ProvisioningRequest saga = service.start(request);
        return ResponseEntity.accepted().body(Dtos.Response.from(saga));
    }

    @GetMapping("/{id}")
    public Dtos.Response get(@PathVariable("id") UUID id) {
        return Dtos.Response.from(service.get(id));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleNotFound(IllegalArgumentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }
}
