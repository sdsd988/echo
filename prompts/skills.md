# echo 프롬프트 가이드 — 실습-스킬 (Skills)

> **진입:** `git checkout skills-base` (실습-스킬 시작점 — 이 프롬프트만, 결과 코드 없음)
> 아래 프롬프트를 **S0 → S2 순서대로** Claude Code에 붙여넣으세요. S0~S2는 한 세션에서 이어서 진행합니다.
> Claude 응답은 매번 조금씩 다릅니다 — 결과가 안내와 다르면 다시 시키거나 직접 다듬으면 됩니다.
> 막히거나 완성본을 보려면 `git checkout skills-done`과 비교하세요.

---

## 이번 편 요구사항 — engagement 모듈 (좋아요)

echo에 사용자 참여(engagement)의 첫 기능인 **좋아요**를 새 모듈로 추가합니다.

- **기능:** 좋아요 / 좋아요 취소 / 좋아요 수 조회.
- **엔드포인트:** `POST /posts/{postId}/likes` · `DELETE /posts/{postId}/likes` · `GET /posts/{postId}/likes/count`
- **예외 응답:** 중복 좋아요 → 409, 없는 게시물 → 404
- **사용자 식별:** `X-User-Id` 헤더 (인증은 이번 범위 외)
- **관습(기존 echo 구조를 따른다):** 도메인 모듈 Spring Web 금지(POJO+JPA) · Controller·DTO는 `apps/customer-api`에 · 모듈 직접 의존 금지(공유 타입은 `common` 경유) · 응답은 `ApiResponse.ok(data)` · 테스트 JUnit 5 + H2 · `settings.gradle.kts`에 모듈 등록.

> 핵심은 이 관습을 **매번 손으로 옮기지 않는 것**입니다. 이번 편에서 그 절차를 `/scaffold-module` 스킬로 굳혀, engagement 신설에 한 줄로 적용합니다.

---

## S0 — 반복 절차 들여다보기

engagement를 바로 만들기 전에, 방금 follow에서 거친 **모듈 신설 절차**를 Claude에게 정리시킵니다. 이게 스킬의 재료입니다.

```
방금 follow 모듈 만들 때 거친 "새 모듈 신설 절차"를 정리해줘.
- 어떤 디렉터리·파일을 어떤 순서로 만드는지 (헥사고날 domain/application/infrastructure)
- 매번 지켜야 하는 규칙 (도메인 모듈 Spring Web 금지, 모듈 직접 의존 금지·common 경유,
  Controller는 customer-api, ApiResponse.ok, 짝 테스트, settings.gradle.kts 등록)
이걸 모듈 이름만 바꾸면 그대로 재사용할 수 있는 체크리스트 형태로 만들어줘.
```

### 끝난 뒤 확인

- Claude가 follow 신설 때 한 절차를 단계 목록으로 정리해 보여줍니다(파일 생성 X).

> **⚠ 짚어둘 것:** 이 절차는 follow에서 했고, engagement·moderation·notification에서도 **똑같이** 반복됩니다. 반복되는 절차는 자동화 대상입니다 — S1에서 이걸 스킬로 굳힙니다.

---

## S1 — `/scaffold-module` 스킬 제작

방금 정리한 절차를 재사용 가능한 **스킬**로 만듭니다.

```
방금 정리한 모듈 신설 절차를 /scaffold-module 스킬로 만들어줘.
- 모듈 이름만 주면 헥사고날 패키지 골격(domain/application/infrastructure)과
  짝 테스트 자리까지 잡아주게.
- echo 관습(모듈 직접 의존 금지·common 경유, Controller는 customer-api,
  ApiResponse.ok, settings.gradle.kts 등록)을 스킬 안에 담아줘.
- 긴 파일 템플릿은 SKILL.md 본문에 다 넣지 말고 참조 파일로 분리해줘.
```

### 끝난 뒤 확인

- `.claude/skills/scaffold-module/SKILL.md` 생성. frontmatter에 `name` · `description` · `when_to_use`(또는 동등 필드) · `allowed-tools`가 있는지 확인.
- 긴 템플릿이 `templates/`(또는 참조 파일)로 분리됐는지 — SKILL.md 본문은 짧게.
- 패키지를 찍어내는 `scripts/` 쉘 스크립트가 동봉됐는지.

```
ls -R .claude/skills/scaffold-module/
```

> **짚기:** `description`은 단순 설명이 아니라 **자동발동 트리거**입니다. "echo에 새 모듈을 추가할 때…"처럼 *언제 쓰는 스킬인지*를 적어두면, 다음 단계에서 우리가 `/scaffold-module`을 직접 치지 않아도 Claude가 알아서 발동합니다.

---

## S2 — description 자동발동으로 engagement 신설

이제 좋아요 요구사항을 **짧게** 던집니다. 관습은 스킬에 박혀 있으니 다시 적지 않습니다.

```
echo에 engagement 모듈을 추가해줘 — 좋아요 기능이야.
- 좋아요 / 좋아요 취소 / 좋아요 수 조회
- POST /posts/{postId}/likes, DELETE /posts/{postId}/likes, GET /posts/{postId}/likes/count
- 같은 사용자가 같은 게시물에 중복 좋아요는 409, 없는 게시물은 404
- 사용자 식별은 X-User-Id 헤더
```

### 끝난 뒤 확인

- Claude가 `/scaffold-module`을 **자동 발동**했는지 — 우리가 스킬 이름을 치지 않았는데 스킬을 골라 쓴 흔적.
- `engagement/`(domain/application/infrastructure) 생성, `apps/customer-api` 아래 Like Controller·DTO 존재, `settings.gradle.kts`에 `include("modules:engagement")` 추가.
- 실습-훅의 PostToolUse 훅이 발동해 engagement 테스트를 챙겼는지(짝 테스트 누락 경고 → 보완).
- 테스트 통과:

```
./gradlew :modules:engagement:test :apps:customer-api:test
```

> **짚기:** follow 때는 관습을 프롬프트에 길게 적었습니다. 이번엔 한 줄도 안 적었는데 같은 관습이 그대로 적용됐죠 — **관습이 스킬에 담겨서**입니다. 스킬(절차 재사용)과 훅(품질 강제)이 함께 일합니다: 스킬이 골격을 깔고, 훅이 테스트를 강제합니다.

> **참고 — 두 가지 호출 방식:** 방금은 우리가 스킬 이름을 안 쳤는데 `description`으로 **자동발동**됐습니다. 원하면 `/scaffold-module`로 **직접 호출**할 수도 있습니다 — 자동(평소) + 수동(확실히 부르고 싶을 때) 둘 다 됩니다.

---

## 화면 확인 (선택) — 좋아요 버튼 붙이기

백엔드만이 아니라 화면으로도 확인하고 싶으면, echo의 `index.html`에 좋아요 버튼을 붙입니다.

```
resources/static/index.html에 좋아요 버튼과 좋아요 수 표시를 추가해줘.
방금 만든 POST/DELETE /posts/{postId}/likes와 GET .../likes/count를 호출하게. 바닐라 JS fetch로.
```

### 끝난 뒤 확인

- `index.html`에 좋아요 버튼과 좋아요 수가 붙습니다.
- 빌드 없이 **브라우저 새로고침**만으로 버튼을 눌러보면 좋아요 수가 바뀝니다.

---

> SKILL.md·`description` 자동발동·`scripts/`·Progressive Disclosure가 모두 자리 잡고 engagement 모듈이 완성되면 실습-스킬 완료입니다. 막히거나 완성본을 보려면 `git checkout skills-done`과 비교하세요.
