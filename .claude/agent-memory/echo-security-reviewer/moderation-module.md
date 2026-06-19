---
name: moderation-module
description: moderation 모듈(금칙어 필터 + 신고)의 보안 약점 위치와 검증된 안전 패턴
metadata:
  type: project
---

modules/moderation — 콘텐츠 안전. 2026-06-19 첫 보안 검토.

## 금칙어 필터 (BannedWordService.findViolation)
- 위치: modules/moderation/.../application/BannedWordService.java
- 매칭은 `toLowerCase(Locale.ROOT)` 후 `contains`만. 대소문자만 정규화.
- 우회 가능: 유니코드 NFKC 미정규화, 제로폭 문자(U+200B 등) 삽입, 공백/특수문자 삽입(b a d w o r d), 동형문자(homoglyph), 리트(l33t). → Medium.
- 전수 `findAll()` 후 스트림 contains → 금칙어 늘면 O(N) 메모리/성능. 기능 우회는 아님.

## 신고 엔드포인트 (ReportController)
- 위치: apps/customer-api/.../moderation/ReportController.java
- PATCH /reports/{id}/status: 인가 검사 전무. 인증 미도입 상태라 누구나 신고 상태 전이 가능(관리자 전용이어야 함). → 구조적 High/Critical (인증 도입 시 해결 예정).
- GET /reports/{id}: 소유권/권한 검사 없음. 타인의 신고 내용(reporterId, reason 등) 열람 가능 → IDOR 정보노출.
- POST /reports: reporterId를 X-User-Id 헤더에서 받고 defaultValue=1 → 신고자 위조 가능. 대상(targetId) 존재 검증 없음(주석상 의도된 설계).

## 안전 검증된 항목 (거짓양성 방지)
- 모든 리포지토리는 Spring Data 파생 쿼리만, native/@Query 문자열 연결 없음 → SQL 인젝션 없음.
- V2__moderation.sql: 시크릿 없음. INSERT는 VALUES 리터럴 시드만.
- application.yml: DB 비번 `${POSTGRES_PASSWORD}` 환경변수 주입, 평문 없음.
- 입력 검증: CreateReportRequest에 @NotNull/@NotBlank/@Size, 컨트롤러 @Valid 적용. Report/BannedWord 도메인 생성자에도 방어적 검증 이중화.
- 모듈 경계: moderation은 common만 의존, Spring Web 없음(build.gradle.kts). 조립은 customer-api 컨트롤러에서. 헥사고날 준수.
