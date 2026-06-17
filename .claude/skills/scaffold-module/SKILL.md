---
name: scaffold-module
description: Echo 프로젝트에 새 헥사고날 모듈 골격을 생성한다. 모듈 이름만 주면 modules/{module}의 domain/application/infrastructure 계층과 짝 테스트, customer-api 컨트롤러·DTO 자리까지 잡고 settings.gradle.kts·customer-api 빌드에 등록한다. 새 모듈을 신설할 때 사용.
---

# scaffold-module — 새 모듈 골격 생성

Echo의 모듈 신설 절차(헥사고날 + echo 관습)를 자동화한다. 자세한 배경은
`docs/new-module-checklist.md` 참고. 긴 파일 템플릿은 이 스킬의 `templates/`에 있다.

## 0. 입력 받기

사용자가 모듈 이름을 줬으면 그대로 쓰고, 안 줬으면 묻는다.

- **`{{module}}`** — 소문자 모듈명(예: `like`, `comment`). 디렉터리명·패키지 끝마디·테이블 접두사.
- **`{{Domain}}`** — 엔티티명(PascalCase, 예: `Like`, `Comment`). 보통 `{{module}}`의 첫 글자 대문자.
  사용자가 따로 말 안 하면 `{{module}}`을 PascalCase로 변환해 쓰고, 한 줄로 알린다.

> ⚠️ 새 모듈은 **챕터에서 명시할 때만** 만든다. 사용자가 모듈 신설을 요청한 맥락인지 확인한다.

## 1. 사전 점검

- `settings.gradle.kts`에 `include("modules:{{module}}")`가 **이미 있으면 중단**하고 사용자에게 알린다(중복 생성 방지).
- `modules/{{module}}/` 디렉터리가 이미 있으면 마찬가지로 중단하고 확인을 받는다.

## 2. 템플릿 → 대상 파일 매핑

이 스킬 디렉터리(`.claude/skills/scaffold-module/templates/`)의 각 `*.tmpl`을 **Read**해서
토큰을 치환한 뒤 아래 경로에 **Write**한다.

치환 규칙: 파일 내용의 `{{module}}` → 모듈명, `{{Domain}}` → 엔티티명 (전부 치환).

| 템플릿 | 대상 경로 |
|---|---|
| `build.gradle.kts.tmpl` | `modules/{{module}}/build.gradle.kts` |
| `Domain.java.tmpl` | `modules/{{module}}/src/main/java/com/echo/{{module}}/domain/{{Domain}}.java` |
| `DomainException.java.tmpl` | `modules/{{module}}/src/main/java/com/echo/{{module}}/domain/{{Domain}}Exception.java` |
| `DomainRepository.java.tmpl` | `modules/{{module}}/src/main/java/com/echo/{{module}}/infrastructure/{{Domain}}Repository.java` |
| `DomainService.java.tmpl` | `modules/{{module}}/src/main/java/com/echo/{{module}}/application/{{Domain}}Service.java` |
| `DomainQueryService.java.tmpl` | `modules/{{module}}/src/main/java/com/echo/{{module}}/application/{{Domain}}QueryService.java` |
| `DomainTest.java.tmpl` | `modules/{{module}}/src/test/java/com/echo/{{module}}/domain/{{Domain}}Test.java` |
| `DomainServiceTest.java.tmpl` | `modules/{{module}}/src/test/java/com/echo/{{module}}/application/{{Domain}}ServiceTest.java` |
| `DomainController.java.tmpl` | `apps/customer-api/src/main/java/com/echo/api/{{module}}/{{Domain}}Controller.java` |
| `DomainResponse.java.tmpl` | `apps/customer-api/src/main/java/com/echo/api/{{module}}/{{Domain}}Response.java` |

> Write가 부모 디렉터리를 만들어주므로 mkdir은 필요 없다.

## 3. 빌드 등록 (수기 편집)

### 3-1. `settings.gradle.kts`
다른 `include("modules:...")` 줄들 옆, `apps` 줄보다 위에 한 줄 추가:

```kotlin
include("modules:{{module}}")
```

### 3-2. `apps/customer-api/build.gradle.kts`
다른 `project(":modules:...")` 의존 옆에 추가:

```kotlin
implementation(project(":modules:{{module}}"))
```

### 3-3. `apps/customer-api/.../EchoApplication.java` ⚠️ 빠뜨리기 쉬움
멀티모듈이라 메인 앱이 **각 모듈 패키지를 세 곳에 명시적으로 스캔**한다. 세 곳 모두 추가하지 않으면
빈/엔티티/리포지토리를 못 찾아 **Spring 컨텍스트가 통째로 깨지고 통합 테스트가 전부 실패**한다.

```java
@SpringBootApplication(scanBasePackages = { ..., "com.echo.{{module}}.application" })
@EntityScan(basePackages              = { ..., "com.echo.{{module}}.domain" })
@EnableJpaRepositories(basePackages   = { ..., "com.echo.{{module}}.infrastructure" })
```

## 4. echo 관습 — 생성하며 반드시 지킬 것

템플릿이 이미 지키고 있지만, 사용자가 내용을 채우도록 안내할 때도 강조한다.

- **모듈 간 직접 의존 금지** — `modules/{{module}}`는 `common`만 의존한다. 다른 모듈 정보가 필요하면
  common 경유 또는 `apps/customer-api` 컨트롤러에서 조립. (PreToolUse 훅이 일부 직접 import를 차단할 수 있음.)
- **도메인 모듈에 Spring Web 의존 금지** — POJO + JPA만. `spring-boot-starter-web` 추가 금지.
- **Controller는 `apps/customer-api`에만** — 모듈 안에 두지 않는다.
- **응답은 `common`의 `ApiResponse`로 감싼다** — `ApiResponse.ok(data)` / `ApiResponse.ok(data, message)`.
- **DTO는 record.** 패키지는 `com.echo.{{module}}.{layer}` (컨트롤러·DTO는 `com.echo.api.{{module}}`).
- **도메인 예외 → HTTP 상태 변환은 컨트롤러에서** (`ResponseStatusException`). domain은 상태 코드를 모른다.
- **읽기/쓰기 서비스 분리** — 쓰기 `{{Domain}}Service`(`@Transactional`), 읽기 `{{Domain}}QueryService`(`readOnly=true`).
- **public 클래스엔 짝 테스트.**

## 5. 검증

```bash
./gradlew :modules:{{module}}:test
./gradlew build
```

골격은 컴파일·통과되도록 만들어져 있다(예시 `value` 필드 기반). 통과를 확인한 뒤,
어떤 파일에 어떤 TODO가 남았는지(도메인 필드·유스케이스·실제 요청 DTO 등) 사용자에게 요약한다.

## 6. 마무리 안내

생성된 골격은 **예시 필드(`value`)와 예시 유스케이스(`create`/`count`)** 를 담고 있다.
실제 도메인에 맞게 교체해야 함을 알리고, `docs/new-module-checklist.md`의 단계별 체크리스트를 참조하도록 안내한다.
