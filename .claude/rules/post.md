---
paths:
  - "modules/post/**"
  - "apps/customer-api/src/**/post/**"
---

# Post 모듈 규칙

## 도메인 불변식 (content)

- `content`는 280자 초과 금지 — `Post.MAX_CONTENT_LENGTH` 상수 사용.
- `content`는 빈 값/`null` 금지.
- 위 검증은 **도메인 생성자(`Post(...)`)에서** 강제한다.

## createdAt

- `createdAt`은 생성 시점에 고정하며 이후 수정 불가(`updatable = false`, setter 없음).

## 계층별 규칙

- **domain** — JPA 엔티티 + 불변식만. **Spring 의존 없음**(POJO + JPA).
- **application** — `@Service`. **쓰기 작업에만 `@Transactional`** (조회는 `@Transactional(readOnly = true)`).
- **infrastructure** — `JpaRepository` 상속 인터페이스.

## HTTP 관심사

- Controller / 요청·응답 DTO는 **post 모듈이 아니라 `apps/customer-api`**에 둔다.
- 요청/응답 DTO는 **record**로 작성한다.
