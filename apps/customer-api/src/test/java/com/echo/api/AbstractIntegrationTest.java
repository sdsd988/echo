package com.echo.api;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * 통합 테스트 공통 베이스.
 *
 * <p>운영과 동일한 PostgreSQL 16을 Testcontainers로 띄워 Flyway 마이그레이션까지 그대로 검증한다.
 * 컨테이너는 static 싱글톤으로 모든 테스트가 공유하며(JVM 1회 기동), {@code @ServiceConnection}이
 * datasource 속성을 자동으로 주입한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }
}
