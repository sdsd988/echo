// timeline 모듈 — 피드 조립 (POJO, Spring Web/JPA 의존 없음)
// 글 데이터는 앱 계층(customer-api)이 PostService에서 받아 넘긴다 — post 모듈에 직접 의존하지 않는다.
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

    // @Service 빈 등록만 위해 spring-context (Web/JPA 없음)
    implementation("org.springframework:spring-context")
}
