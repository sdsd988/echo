plugins {
    id("java")
    id("org.springframework.boot") version "3.5.0" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

// 모든 서브프로젝트 공통 설정
subprojects {
    apply(plugin = "java")

    // JDK 25 고정 (foojay-resolver가 없으면 자동 다운로드)
    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        // 테스트는 모든 서브프로젝트에서 JUnit 5 사용
        testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
