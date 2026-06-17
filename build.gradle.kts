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

        // Testcontainers가 Docker Desktop의 비표준 소켓을 찾도록 보정.
        // DOCKER_HOST가 이미 있으면 그대로 두고, 없으면 홈의 Docker Desktop 소켓을 주입한다.
        // (Linux CI의 표준 /var/run/docker.sock 환경에서는 소켓이 없으므로 건너뛴다.)
        if (System.getenv("DOCKER_HOST") == null) {
            val desktopSocket = file("${System.getProperty("user.home")}/.docker/run/docker.sock")
            if (desktopSocket.exists()) {
                environment("DOCKER_HOST", "unix://${desktopSocket.absolutePath}")
            }
        }
    }
}
