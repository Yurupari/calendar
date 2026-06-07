package com.yurupari.calendar.support;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;

public abstract class PostgreSQLTestcontainerBase {

    @ServiceConnection
    @Container
    static PostgreSQLContainer postgresql = new PostgreSQLContainer("postgres:18-alpine")
            .withDatabaseName("child_weight_monitor")
            .withUsername("root")
            .withPassword("password");
}
