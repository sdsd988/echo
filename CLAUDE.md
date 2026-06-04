# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

응답은 한국어로 작성한다.

## 프로젝트

Echo — Threads류 텍스트 SNS 백엔드.

## 기술 스택

Java 25 · Spring Boot 3.5.x · Gradle 멀티프로젝트 · H2(인메모리) · JUnit 5

## 멀티모듈 구조

- `apps/customer-api` — HTTP 진입점(유일한 Spring Boot 실행 단위)
- `modules/post` — Post 도메인
- `modules/user` — User 도메인
- `common` — 표준 응답/오류 record

## 핵심 규칙

- **의존성 방향**: `apps` → `modules`, `common` / `modules` → `common`. **모듈 간 직접 의존 금지.**
- **각 모듈은 hexagonal**: `domain` / `application` / `infrastructure` 계층.
- **도메인 모듈은 Spring Web 의존 없음** — POJO + JPA만.
- **DTO는 record**.
- **패키지 규칙**: `com.echo.{module}.{layer}`.

## 빌드 / 실행 / 테스트

```bash
./gradlew build                       # 전체 빌드
./gradlew :apps:customer-api:bootRun  # 앱 실행 (http://localhost:8080)
./gradlew test                        # 전체 테스트
./gradlew :apps:customer-api:test --tests "com.echo.api.PostIntegrationTest"  # 단일 테스트
```
