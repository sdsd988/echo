# echo 프롬프트 가이드 — 실습-훅 (Hooks)

> **진입:** `git checkout hooks-base` (실습-훅 시작점 — 이 프롬프트만, 코드 없음)
> 아래 프롬프트를 **S0 → S3 순서대로** Claude Code에 붙여넣으세요. S0~S3은 한 세션에서 이어서 진행합니다.
> Claude 응답은 매번 조금씩 다릅니다 — 결과가 안내와 다르면 다시 시키거나 직접 다듬으면 됩니다.
> 막히거나 완성본을 보려면 `git checkout hooks-done`과 비교하세요.

---

## 이번 편 요구사항 — follow 모듈 (팔로우·언팔로우)

echo에 소셜 그래프의 토대가 되는 **follow 모듈**을 새로 만듭니다.

- **기능:** 팔로우 / 언팔로우.
- **엔드포인트:** `POST /follows/{targetUserId}` · `DELETE /follows/{targetUserId}`
- **예외 응답:** 없는 유저 → 404, 이미 팔로우 중 → 409
- **사용자 식별:** `X-User-Id` 헤더 (인증은 이번 범위 외)
- **관습(기존 echo 구조를 따른다):** 도메인 모듈 Spring Web 금지(POJO+JPA) · Controller·DTO는 `apps/customer-api`에 · 모듈 직접 의존 금지(공유 타입은 `common` 경유) · 응답은 `ApiResponse.ok(data)` · 테스트 JUnit 5 + H2 · `settings.gradle.kts`에 모듈 등록.

---

## S0 — follow 모듈 구현 위임

위 요구사항을 그대로 Claude에 위임합니다.

```
아래 요구사항대로 echo에 follow 모듈을 구현해줘.

[기능]
- 팔로우 / 언팔로우.
- 엔드포인트: POST /follows/{targetUserId}, DELETE /follows/{targetUserId}
- 예외: 없는 유저 404, 이미 팔로우 중 409
- 사용자 식별은 X-User-Id 헤더 (인증은 범위 외)

[관습 — 기존 echo 구조를 따를 것]
- modules/follow에 hexagonal 구조 (domain/application/infrastructure)
- 도메인 모듈에 Spring Web 의존 금지 (POJO + JPA만)
- Controller·DTO는 apps/customer-api/.../follow/에
- 모듈 간 직접 의존 금지, 공유 타입은 common 경유
- 응답은 ApiResponse.ok(data)로
- settings.gradle.kts에 modules:follow include
- 테스트: FollowService 단위 테스트 (JUnit 5, H2)
```

### 끝난 뒤 확인

- `modules/follow/`(domain/application/infrastructure) 생성, `apps/customer-api` 아래 follow Controller·DTO 존재
- `settings.gradle.kts`에 `include("modules:follow")` 추가됨
- 테스트 통과:

```
./gradlew :modules:follow:test :apps:customer-api:test
```

> **⚠ 짚어둘 것:** 기존 PostToolUse 훅(`run-module-tests.sh`)이 follow 파일을 써도 경고를 안 냅니다 — 훅의 case문에 follow 매핑이 없어서입니다. **S2에서** 이 훅을 확장해 해결합니다.

---

## S1 — PreToolUse 훅: 모듈 경계 차단

문서에 적힌 "모듈 직접 의존 금지"는 *부탁*일 뿐입니다. Claude가 깜빡하면 침범이 일어나고, 빌드가 깨진 뒤에야 발견됩니다. 이걸 **코드가 써지기 전에** 막는 강제 장치(PreToolUse 훅)로 바꿉니다.

```
follow 모듈에서 user나 post 패키지를 직접 import하는 걸 막고 싶어.
파일이 써지기 전에 잡아야 하니까 PreToolUse 훅으로.
막을 때 그냥 거부만 하지 말고, common 모듈을 경유하라고 안내까지 해줘.
```

### 동작 확인 — 경계 위반이 막히는지

```
follow 모듈의 FollowService에서 user 모듈의 UserRepository를 직접 import해봐.
```

### 끝난 뒤 확인

- `.claude/hooks/check-module-boundary.sh` 존재
- `.claude/settings.json`에 `PreToolUse` 훅 블록(matcher: `Edit|Write|MultiEdit`) — `PostToolUse`(기존)와 공존
- 위반 시도 시 stderr에 경계 위반 메시지가 뜨고 파일이 바뀌지 않음

---

## S2 — PostToolUse 훅 확장: follow 매핑 + 테스트 누락 경고

S0에서 발견한 문제를 해결합니다. 기존 `run-module-tests.sh`가 follow를 모르고, 테스트 파일을 안 만들어도 경고가 없습니다.

```
기존 run-module-tests.sh 훅이 follow 모듈은 모르네 — follow 파일을 써도 테스트가 안 돌아.
follow도 테스트가 돌게 해주고, 겸사겸사 Java 파일을 만들 때 짝 테스트가 없으면 경고하게 해줘.
성공은 조용히, 문제 있을 때만 시끄럽게.
```

### 동작 확인 — 테스트 누락 경고

```
follow 모듈에 FollowQueryService.java를 추가해줘 (팔로우 목록 조회). 테스트는 아직 만들지 마.
```

훅이 `FollowQueryServiceTest.java`가 없다는 경고를 냅니다. Claude가 경고를 읽고 테스트도 함께 만듭니다.

### 끝난 뒤 확인

- `run-module-tests.sh` case문에 `*/modules/follow/*` 블록 추가됨, 테스트 파일 존재 검사 추가됨
- `./gradlew :modules:follow:test`로 follow 테스트가 자동 실행됨

---

## S3 — Stop 훅: 세션 종료 시 작업 요약 남기기

마지막 훅입니다. 세션이 끝나기 직전, 이번에 바뀐 파일 목록·요약을 **파일로 남겨** 다음 세션이 이어받게 합니다. (세션은 막지 않습니다 — exit 0.)

```
세션이 끝날 때, 이번 세션에서 바뀐 파일 목록이랑 간단한 작업 요약을
.claude/session-summary.md 파일에 남기는 Stop 훅을 만들어줘.
세션은 막지 말고(요약만 파일로 남기고 그냥 통과), 다음 세션이 그 파일을 보고 이어받게.
```

> **왜 파일인가:** Stop 훅 출력에는 `additionalContext`가 없습니다(PreToolUse·SessionStart 등에만 있음). 컨텍스트를 직접 주입하려면 `decision: block`으로 세션을 되돌려야 하는데, 그건 무한루프 위험이 있어 `stop_hook_active` 가드가 필요해집니다. 막지 않는 설계라면 그 복잡함을 피하고 **파일로 남기는 게** 정석입니다. 남긴 파일은 다음 세션이 읽으면 됩니다(SessionStart 훅으로 자동 주입까지 확장 가능).

### 끝난 뒤 확인

- `.claude/hooks/session-summary.sh` 존재 (요약을 `.claude/session-summary.md`에 기록, 비차단 exit 0)
- 세션 종료 후 `.claude/session-summary.md`에 변경 파일 요약이 남는지 확인
- `.claude/settings.json`에 `Stop` 훅 블록 — 총 3개 훅(PreToolUse·PostToolUse·Stop) 공존

```
ls .claude/hooks/
# check-module-boundary.sh  run-module-tests.sh  session-summary.sh
```

---

> 훅 3종(PreToolUse·PostToolUse·Stop)이 모두 자리 잡으면 실습-훅 완료입니다. 막히거나 완성본을 보려면 `git checkout hooks-done`과 비교하세요.
