# 새 모듈 신설 체크리스트

`follow` 모듈을 만들 때 거친 절차를 일반화한 것. **`{module}`(소문자 모듈명, 예: `follow`)** 과
**`{Domain}`(엔티티명, 예: `Follow`)** 만 바꾸면 그대로 재사용한다.

> 전제: 새 모듈은 **챕터에서 명시할 때만** 만든다. 임의 신설 금지.

---

## 0. 변수 정하기

| 자리표시자 | 예시 | 설명 |
|---|---|---|
| `{module}` | `follow` | 소문자 모듈명 = 디렉터리명 = 패키지 끝마디 |
| `{Domain}` | `Follow` | 엔티티/대표 클래스명 |
| 패키지 루트 | `com.echo.follow` | `com.echo.{module}` 고정 |

---

## 1. 디렉터리·파일 생성 순서 (헥사고날)

루트는 `modules/{module}/`. 아래 순서대로 만든다 — **안쪽(domain) → 바깥(application) → infra → 조립(api)**.

```
modules/{module}/
├── build.gradle.kts                                  # ② 모듈 빌드 설정
└── src/
    ├── main/java/com/echo/{module}/
    │   ├── domain/                                    # ③ 가장 먼저: 순수 도메인
    │   │   ├── {Domain}.java                          #    @Entity (POJO + JPA)
    │   │   └── {Domain}Exception.java                 #    도메인 예외(필요 시)
    │   ├── application/                               # ④ 유스케이스
    │   │   ├── {Domain}Service.java                   #    쓰기(@Transactional)
    │   │   └── {Domain}QueryService.java              #    읽기(@Transactional(readOnly=true))
    │   └── infrastructure/                            # ⑤ 어댑터
    │       └── {Domain}Repository.java                #    JpaRepository
    └── test/java/com/echo/{module}/                   # ⑥ 짝 테스트(소스와 1:1)
        ├── domain/{Domain}Test.java
        └── application/{Domain}ServiceTest.java
                  /{Domain}QueryServiceTest.java
```

Controller·응답 DTO는 **모듈이 아니라 `apps/customer-api`** 에 만든다 (⑦ 참고).

---

## 2. 단계별 체크리스트

### ① `settings.gradle.kts`에 모듈 등록
- [ ] `include("modules:{module}")` 한 줄 추가 (apps 줄보다 위, 다른 modules 옆).

### ② `modules/{module}/build.gradle.kts`
- [ ] `java` + `io.spring.dependency-management` 플러그인만 (Spring Boot 플러그인 X — 실행 단위 아님).
- [ ] spring-boot-dependencies BOM import.
- [ ] 의존성은 **`project(":common")` + JPA/Validation API + spring-data-jpa 만**.
- [ ] **`spring-boot-starter-web` 절대 추가 금지** (도메인 모듈 = Spring Web 의존 없음).
- [ ] **다른 `modules:*`에 직접 의존 금지** (모듈 간 연결은 common 경유 or 컨트롤러 조립).
- [ ] 테스트용 `mockito-core`, `mockito-junit-jupiter` (서비스 단위 테스트용).

### ③ `domain/{Domain}.java`
- [ ] `package com.echo.{module}.domain;`
- [ ] `@Entity` + `@Table` (필요 시 `@UniqueConstraint` 등 제약).
- [ ] **POJO + JPA만** — `org.springframework.web.*` import 0건.
- [ ] 불변식은 **생성자에서 강제** (null/자기참조 등 → `IllegalArgumentException`).
- [ ] JPA용 `protected` 기본 생성자 + 도메인용 public 생성자.
- [ ] getter만 노출 (setter 지양).
- [ ] 도메인 예외는 `domain/{Domain}Exception.java`로 — **HTTP 상태 매핑은 여기서 하지 않는다**(컨트롤러 몫).

### ④ `application/{Domain}Service.java` / `{Domain}QueryService.java`
- [ ] `package com.echo.{module}.application;`
- [ ] `@Service`, 생성자 주입(필드 주입 X).
- [ ] **쓰기/읽기 분리**: `Service`는 `@Transactional`, `QueryService`는 `@Transactional(readOnly = true)`.
- [ ] 다른 모듈이 필요한 검증(예: 대상 유저 존재 404)은 **여기서 하지 않고** 컨트롤러로 위임 — 주석으로 이유 명시.

### ⑤ `infrastructure/{Domain}Repository.java`
- [ ] `package com.echo.{module}.infrastructure;`
- [ ] `extends JpaRepository<{Domain}, Long>`.
- [ ] 파생 쿼리 메서드에 한 줄 주석으로 의미 명시.

### ⑥ 짝 테스트 (`src/test`, 소스와 1:1)
- [ ] `{Domain}Test` — 도메인 불변식(생성자 검증) 테스트.
- [ ] `{Domain}ServiceTest` / `{Domain}QueryServiceTest` — 리포지토리 **mock**으로 유스케이스 검증.
- [ ] 새 public 클래스마다 짝 테스트가 있는지 확인.

### ⑦ `apps/customer-api` — 조립 지점(composition root)
- [ ] `apps/customer-api/build.gradle.kts`에 `implementation(project(":modules:{module}"))` 추가.
- [ ] **`EchoApplication.java`의 스캔 3곳에 모듈 패키지 등록** (⚠️ 빠뜨리면 컨텍스트 로드 실패 → 통합 테스트 전멸):
  - `@SpringBootApplication(scanBasePackages = ... "com.echo.{module}.application")`
  - `@EntityScan(basePackages = ... "com.echo.{module}.domain")`
  - `@EnableJpaRepositories(basePackages = ... "com.echo.{module}.infrastructure")`
- [ ] `com.echo.api.{module}` 패키지에 `{Domain}Controller` 작성 (`@RestController`).
- [ ] 요청/응답 **DTO는 record**, 같은 패키지에 (`{Domain}Response` 등). `from`/`of` 정적 팩터리.
- [ ] **모든 응답을 `common`의 `ApiResponse`로 감싼다** (`ApiResponse.ok(data)` / `ok(data, message)`).
- [ ] 도메인 예외 → HTTP 상태 변환은 **컨트롤러에서** (`ResponseStatusException`): 예) `AlreadyFollowingException`→409, `IllegalArgumentException`→400, 미존재→404.
- [ ] 여러 모듈을 잇는 검증(예: user 존재 확인)은 컨트롤러에서 각 모듈 서비스를 호출해 처리 — 모듈끼리 직접 의존시키지 않는다.

### ⑧ 빌드·테스트 검증
- [ ] `./gradlew :modules:{module}:test` 통과.
- [ ] `./gradlew build` 전체 통과.

---

## 3. 매번 지켜야 하는 불변 규칙 (요약)

- **의존성 방향**: `apps/*` → `modules/*`, `common` / `modules/*` → `common`.
- **모듈 간 직접 의존 금지** — 연결은 `common` 경유 또는 `customer-api` 컨트롤러에서 조립.
- **도메인 모듈에 Spring Web 의존 금지** — POJO + JPA만.
- **Controller는 `apps/customer-api`에만** — 모듈 안에 두지 않는다.
- **응답은 `ApiResponse`로 감싼다.**
- **DTO는 record.**
- **패키지는 `com.echo.{module}.{layer}`.**
- **public 클래스엔 짝 테스트.**
- **새 모듈은 챕터 명시 시에만** + `settings.gradle.kts` 등록 필수.
