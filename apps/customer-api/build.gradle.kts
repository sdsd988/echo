// customer-api — 일반 사용자 HTTP API 진입점
plugins {
    id("java")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":modules:user"))
    implementation(project(":modules:post"))
    implementation(project(":modules:timeline"))
    implementation(project(":modules:follow"))
    implementation(project(":modules:engagement"))
    implementation(project(":modules:moderation"))

    // Spring Boot Web + JPA + Validation + Actuator
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // PostgreSQL 드라이버 + Flyway 마이그레이션 (7장~)
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    // Swagger UI
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")

    // 테스트 — 통합 테스트는 Testcontainers로 운영과 동일한 PostgreSQL을 띄운다
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
}
