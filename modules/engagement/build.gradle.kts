// engagement 모듈 — Like 도메인 (POJO + JPA, Spring Web 의존 없음)
plugins {
    id("java")
    id("io.spring.dependency-management")
}

apply(plugin = "io.spring.dependency-management")

the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.0")
    }
}

dependencies {
    implementation(project(":common"))

    // JPA (Spring Web 없음)
    implementation("jakarta.persistence:jakarta.persistence-api")
    implementation("jakarta.validation:jakarta.validation-api")
    implementation("org.springframework.data:spring-data-jpa")

    // 서비스 단위 테스트용 — 리포지토리 mock (버전은 spring-boot BOM이 관리)
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.mockito:mockito-junit-jupiter")

    // ⚠️ 다른 modules:* 에 의존하지 말 것 — common 경유 또는 customer-api 조립으로 해결한다.
    // ⚠️ spring-boot-starter-web 추가 금지 — 도메인 모듈은 Spring Web에 의존하지 않는다.
}
