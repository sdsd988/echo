# Moderation 모듈 규칙

콘텐츠 안전(금칙어 필터 + 신고) 도메인.

## 금칙어(BannedWord)

- 금칙어 목록은 **DB로 관리**한다 — 애플리케이션 코드에 **하드코딩 금지**. 초기 시드는 Flyway 마이그레이션.
- 금칙어 검사는 **게시물 등록 시점**에 적용하며, 위반 시 **400**으로 막는다.
- post 모듈과 moderation 모듈은 **서로 직접 의존하지 않는다** — 검사는 조립 지점(`PostController`)이 `BannedWordService`로 위임한다.

## 신고(Report)

- 상태 전이는 `PENDING → REVIEWED → RESOLVED` **한 방향·한 단계씩만** 허용한다.
  - 전이 규칙은 **도메인(`Report.changeStatus` / `ReportStatus.canTransitionTo`)에서** 강제한다.
  - 건너뛰기·역방향·종료 상태 재전이는 `IllegalReportStatusTransitionException` → **409**.
- 신고 접수 시 초기 상태는 **PENDING**.
- 없는 신고 처리/조회는 **404**.
- 신고 주체(reporter)는 **X-User-Id 헤더**로 식별한다.

## 계층별 규칙

- **domain** — JPA 엔티티 + 불변식/상태머신만. **Spring 의존 없음**(POJO + JPA).
- **application** — `@Service`. **쓰기 작업에만 `@Transactional`** (조회는 `@Transactional(readOnly = true)`).
- **infrastructure** — `JpaRepository` 상속 인터페이스.
- Controller / 요청·응답 DTO는 **moderation 모듈이 아니라 `apps/customer-api`**에 두고 **record**로 작성한다.
