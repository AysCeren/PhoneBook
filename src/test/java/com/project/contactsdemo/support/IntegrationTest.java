package com.project.contactsdemo.support;

import com.hazelcast.core.HazelcastInstance;
import com.project.contactsdemo.core.cache.CacheNames;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;

/**
 * Base class for tests that run the whole application over HTTP against real PostgreSQL and Hazelcast
 * containers (Docker required).
 * <p>
 * The containers are started once per test run and shared by all subclasses ("singleton containers"),
 * because starting them per class would dominate the run time. Each test starts from an empty database
 * and empty caches.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate // Spring Boot 4 no longer adds a TestRestTemplate automatically
@ActiveProfiles("test")
public abstract class IntegrationTest {

    // Same major version as the local PostgreSQL.
    @ServiceConnection // Spring Boot points the DataSource (and Liquibase) at this container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:14-alpine");

    // Must match the Hazelcast client version managed by Spring Boot.
    static final GenericContainer<?> HAZELCAST = new GenericContainer<>("hazelcast/hazelcast:5.5.0")
            .withExposedPorts(5701)
            .waitingFor(Wait.forLogMessage(".*is STARTED.*", 1));

    static {
        Startables.deepStart(POSTGRES, HAZELCAST).join(); //start both in parallel
    }

    // Our Hazelcast client is configured through app.hazelcast.*, so the container address is passed in here.
    @DynamicPropertySource
    static void hazelcastProperties(DynamicPropertyRegistry registry) {
        registry.add("app.hazelcast.address", () -> HAZELCAST.getHost() + ":" + HAZELCAST.getMappedPort(5701));
        registry.add("app.hazelcast.cluster-name", () -> "dev");
    }

    @Autowired
    protected TestRestTemplate restTemplate;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private HazelcastInstance hazelcastInstance;

    @BeforeEach
    void startFromEmptyState() {
        jdbcTemplate.update("delete from contact");
        jdbcTemplate.update("delete from person");
        List.of(CacheNames.PERSON_RESPONSE_ALL, CacheNames.CONTACT_RESPONSE_ALL, CacheNames.PERSON_WITH_CONTACTS)
                .forEach(map -> hazelcastInstance.getMap(map).clear());
    }

    protected ResponseEntity<String> get(String path) {
        return exchange(HttpMethod.GET, path, null);
    }

    protected ResponseEntity<String> post(String path, Object body) {
        return exchange(HttpMethod.POST, path, body);
    }

    protected ResponseEntity<String> put(String path, Object body) {
        return exchange(HttpMethod.PUT, path, body);
    }

    private ResponseEntity<String> exchange(HttpMethod method, String path, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON)); //XML is also available (via JasperReports' Jackson XML)
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.exchange(path, method, new HttpEntity<>(body, headers), String.class);
    }
}
