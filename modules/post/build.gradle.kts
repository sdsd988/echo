// post 모듈 — Post 도메인 (POJO + JPA, Spring Web 의존 없음)
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
}
