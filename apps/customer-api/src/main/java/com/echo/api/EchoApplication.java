package com.echo.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Echo 서비스 진입점.
 * 멀티모듈 구조이므로 각 모듈의 패키지를 명시적으로 스캔한다.
 */
@SpringBootApplication(scanBasePackages = {
    "com.echo.api",
    "com.echo.post.application",
    "com.echo.timeline.application",
    "com.echo.user"
})
@EntityScan(basePackages = {
    "com.echo.user.domain",
    "com.echo.post.domain"
})
@EnableJpaRepositories(basePackages = {
    "com.echo.post.infrastructure"
})
public class EchoApplication {

    public static void main(String[] args) {
        SpringApplication.run(EchoApplication.class, args);
    }
}
