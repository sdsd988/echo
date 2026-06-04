pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "echo"

// seed 4개 서브프로젝트만 — 나머지는 챕터별 라이브 시연으로 탄생
include("common")
include("modules:user")
include("modules:post")
include("modules:timeline")
include("apps:customer-api")
