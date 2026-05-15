package com.casbytes.core;

import static org.assertj.core.api.Assertions.assertThat;

import com.casbytes.core.infrastructure.casbin.CasbinAuthorizationService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles({"integration"})
@Tag("integration")
class CasbytesContainersIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("casbytes_core_db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired(required = false)
    private CasbinAuthorizationService casbinAuthorizationService;

    @Test
    void flywayAppliesAndCasbinJdbcLoadsPolicies() {
        assertThat(POSTGRES.isRunning()).isTrue();
        assertThat(casbinAuthorizationService).isNotNull();
        assertThat(casbinAuthorizationService.enforce("alice", "/api/v1/reference/items", "GET"))
                .isTrue();
    }
}
