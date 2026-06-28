package com.abhaypanday.provisioning;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * End-to-end saga test on real Kafka + PostgreSQL + Redis (Testcontainers).
 *
 * <p>Covers the happy path (-> COMPLETED) and the compensation path (-> COMPENSATED).
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProvisioningSagaIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);

    @Container
    static final KafkaContainer KAFKA =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
    }

    @Autowired
    private TestRestTemplate http;

    @Test
    void happyPath_reachesCompleted() {
        UUID sagaId = start(new Dtos.CreateRequest("Alice", "DATA_5G", false));

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            Dtos.Response status = status(sagaId);
            assertThat(status.status()).isEqualTo(ProvisioningStatus.COMPLETED);
            assertThat(status.msisdn()).isNotBlank();
        });
    }

    @Test
    void activationFailure_reachesCompensated() {
        UUID sagaId = start(new Dtos.CreateRequest("Bob", "VOLTE", true));

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            Dtos.Response status = status(sagaId);
            assertThat(status.status()).isEqualTo(ProvisioningStatus.COMPENSATED);
            assertThat(status.msisdn()).isNull();
        });
    }

    private UUID start(Dtos.CreateRequest body) {
        Dtos.Response response = http.postForObject("/api/v1/provisioning", body, Dtos.Response.class);
        assertThat(response).isNotNull();
        return response.sagaId();
    }

    private Dtos.Response status(UUID sagaId) {
        return http.getForObject("/api/v1/provisioning/" + sagaId, Dtos.Response.class);
    }
}
