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

    // Spring Boot Web + JPA + Validation + Actuator
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // H2 인메모리 DB (4~6장)
    runtimeOnly("com.h2database:h2")

    // Swagger UI
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")

    // 테스트
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
