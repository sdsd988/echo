# echo 프롬프트 가이드 — 실습-서브에이전트 (Subagents)

> **진입:** `git checkout subagent-base` (실습-서브에이전트 시작점 — 이 프롬프트만, 결과 코드 없음)
> 아래 프롬프트를 **S0 → S2 순서대로** Claude Code에 붙여넣으세요. S0~S2는 한 세션에서 이어서 진행합니다.
> Claude 응답은 매번 조금씩 다릅니다 — 결과가 안내와 다르면 다시 시키거나 직접 다듬으면 됩니다.
> 막히거나 완성본을 보려면 `git checkout subagent-done`과 비교하세요.

---

## 이번 편 요구사항 — moderation 모듈 (금칙어 필터·신고)

echo에 콘텐츠 안전 관리의 토대가 되는 **moderation 모듈**을 새로 만듭니다.

- **기능:** 금칙어 필터 · 콘텐츠 신고 · 신고 상태 관리.
- **엔드포인트:** `POST /reports`(신고 접수) · `GET /reports/{reportId}`(조회) · `PATCH /reports/{reportId}/status`(처리)
- **규칙:** 게시물 등록 시 금칙어가 포함되면 **400**으로 막고 저장하지 않음 · 금칙어 목록은 **DB 관리**(하드코딩 금지) · 신고는 `PENDING → REVIEWED → RESOLVED` 상태로 전환.
- **사용자 식별:** `X-User-Id` 헤더.
- **관습(기존 echo 구조):** 헥사고날(domain/application/infrastructure) · 도메인 모듈 Spring Web 금지 · Controller·DTO는 `apps/customer-api` · 모듈 직접 의존 금지(`common` 경유) · 응답 `ApiResponse.ok(data)` · Postgres + Testcontainers · 테스트 동반.

> 이번 편의 주인공은 모듈이 아니라, **앞으로 모든 코드를 보안 관점으로 검토하는 상비 검수자**(`security-reviewer` 서브에이전트)입니다. moderation은 그 검수자의 *첫 대상*입니다.

---

## S0 — moderation 모듈 신설

요구사항을 그대로 위임합니다. 관습은 `/scaffold-module` 스킬에 담겨 있으니 짧게 던집니다.

```
echo에 moderation 모듈을 추가해줘 — 콘텐츠 안전 관리야.
- 금칙어 필터: 게시물 등록 시 금칙어가 들어 있으면 400으로 막기 (금칙어 목록은 DB 관리, 하드코딩 금지)
- 신고 접수: POST /reports (접수 시 PENDING)
- 신고 처리: PATCH /reports/{reportId}/status 로 PENDING→REVIEWED→RESOLVED 전환
- 신고 조회: GET /reports/{reportId}
- 사용자 식별은 X-User-Id 헤더
```

### 끝난 뒤 확인

- `modules/moderation/`(domain/application/infrastructure) 생성, `apps/customer-api` 아래 Moderation Controller·DTO, `settings.gradle.kts`에 `include("modules:moderation")`.
- 테스트 통과:

```
./gradlew :modules:moderation:test :apps:customer-api:test
```

> **⚠ 씨앗:** echo에 *보안·안전이 걸린 코드*가 점점 쌓입니다 — MCP 비밀번호(`.mcp.json`), 신고 처리(인가), 금칙어 필터(우회)…. 이걸 매번 사람이 보안 관점으로 훑긴 벅찹니다. 그래서 **보안 전담 검수자를 상비로** 세웁니다 — 특정 모듈이 아니라 *앞으로 모든 코드*에 발동하는. 그게 S1입니다.

---

## S1 — security-reviewer 서브에이전트 신규 (상비)

보안 관점으로 코드를 검토하는, **읽기 전용 상비** 서브에이전트를 만듭니다. **두 가지 방법** 중 하나로 — 결과는 같은 `.claude/agents/security-reviewer.md`입니다.

**방법 A — `/agents` (공식 권장).** 가이드 UI로 만듭니다.

```
/agents
```

→ **Library** 탭 → **Create new agent** → 범위 **Project**(`.claude/agents/`) → **"Generate with Claude"** 선택 → 아래를 설명으로 입력:

```
보안 관점으로 echo의 모든 새 코드·변경을 검토하는 상비 검수자.
점검: SQL/입력 인젝션·입력 검증 누락, 인가 검사 누락(신고 처리·상태 변경 등), 시크릿 평문 노출(.mcp.json·설정), 금칙어/필터 우회, 모듈 경계 위반. 발견을 심각도와 함께 보고하고, 코드는 절대 수정하지 말 것.
```

→ Claude가 name·description·시스템 프롬프트 초안을 만들면, **tools는 읽기 전용만(Read·Grep·Glob)** 체크 · **model은 `opus`**(보안 검수는 정확도 우선) 선택 → 저장. 즉시 발동 가능.

**방법 B — 정의 파일 위임 (복붙·재현용).** 복붙으로 똑같이 만들 수 있습니다.

```
보안 관점으로 코드를 검토하는 상비 서브에이전트를 만들어줘.
- 정의 파일: .claude/agents/security-reviewer.md
- 특정 모듈 전용이 아니라, 앞으로 echo의 모든 새 코드·변경에 발동하는 보안 검수자
- 코드를 고치면 안 되고 읽고 검토만 한다 → tools는 읽기 도구만 (Read, Grep, Glob). Write·Edit·Bash 금지
- model은 opus (보안 검수는 미묘한 취약점을 놓치면 안 되니 강한 모델로)
- 시스템 프롬프트로 점검할 것:
  · SQL/입력 인젝션, 입력 검증 누락
  · 인가·권한 검사 누락 (예: 신고 처리·상태 변경)
  · 비밀번호·토큰 등 시크릿 평문 노출 (.mcp.json·설정 포함)
  · 금칙어/필터 우회 가능성
  · 모듈 경계 위반 (직접 의존)
  발견을 심각도와 함께 보고하고, 코드는 절대 수정하지 말 것.
```

### 끝난 뒤 확인

- `.claude/agents/security-reviewer.md` 생성. frontmatter에 `name` · `description` · `tools`(읽기 도구만) · `model: opus`.

```
cat .claude/agents/security-reviewer.md   # frontmatter 확인
```

> **짚기.**
> - **상비 — 특정 모듈에 안 묶임.** `description`이 "echo의 모든 코드를 보안 관점으로 검토"라서, 앞으로 새 코드가 생길 때마다 메인이 *알아서* 이 검수자를 부릅니다(자동위임). 한 번 만들어 **계속 재사용**하는 게 서브에이전트의 핵심 가치예요.
> - **`tools: Read, Grep, Glob` = 최소 권한.** Write·Edit가 *아예 없으니* 구조적으로 코드를 못 고칩니다. 권한 제한은 이 화이트리스트로 합니다. (도구를 안 적으면 메인 도구를 *전부 상속*하니, 좁히려면 *반드시* 명시.)
> - **`permissionMode`는 별개 축** — `permissionMode`(`default·acceptEdits·dontAsk…`)는 "*물어볼지/자동승인할지*"를 정하는 다른 축입니다. 읽기 전용은 `tools`로 이미 잠겨서 안 건드려도 됩니다. (`strict` 같은 값은 없습니다 — *무엇을 줄지*는 `tools`, *어떻게 물을지*는 `permissionMode`.)
> - **`model: opus` — 역할에 맞춰.** 스캐폴딩 같은 *기계적인 일*은 싼 모델(haiku)로 충분하지만, **보안 검수는 미묘한 취약점을 하나라도 놓치면 사고**입니다. 그래서 *강한 추론 모델(opus)*을 씁니다. 역할별 모델 라우팅은 무조건 싸게가 아니라 **난이도·리스크에 맞춰** 고르는 것. (비용이 부담되면 sonnet이 중간.)
> - **서브에이전트 vs 스킬.** 에이전트는 **독립 컨텍스트 + 자기 시스템 프롬프트**(맨바닥 시작, CLAUDE.md·규칙만 가져감). 스킬은 *현재 대화*에 지시를 얹는 것. 그리고 기존 `code-reviewer`(일반 품질)와 **역할이 갈립니다** — 이쪽은 *보안 전담*.
> - **만드는 법 두 가지.** 공식은 **`/agents`(가이드 UI + "Generate with Claude" 초안)를 권장**합니다 — 도구·모델을 클릭으로 안전하게 고름. 방법 B(파일 직접/위임)는 복붙으로 재현·버전관리에 좋아 이 가이드는 B를 함께 실었습니다. 둘 다 같은 정의 파일을 만듭니다.

---

## S2 — 오케스트레이터-워커: 첫 임무는 moderation 검수

메인 에이전트(오케스트레이터)가 방금 만든 상비 검수자에게 첫 일을 맡깁니다 — 가장 위험한 모듈, moderation.

```
방금 만든 moderation 모듈을 security-reviewer에게 보안 검토시켜줘.
금칙어 필터를 우회할 구멍은 없는지, 신고 처리·상태 변경에 인가 검사가 빠지진 않았는지,
시크릿 노출이나 입력 검증 누락이 없는지 봐줘.
```

### 끝난 뒤 확인

- 메인 대화에 **보안 검토 요약**이 돌아옵니다(심각도별 지적·확인).
- security-reviewer는 파일을 **고치지 않습니다** — 지적만 합니다. 고칠지는 메인(과 사람)이 판단.

> **짚기.**
> - **오케스트레이터-워커.** 메인이 일을 받아 검수를 워커에게 *동적으로* 위임합니다(미리 쪼개 두지 않음).
> - **독립 컨텍스트 + 요약 반환.** 워커는 맨바닥에서 코드만 읽고, 장황한 검토 과정은 메인을 채우지 않고 **요약만** 돌아옵니다 — 컨텍스트 분리의 이점.
> - **권한을 끊어 둔 게 작동.** 워커가 문제를 찾아도 *직접 못 고칩니다*(Write 없음). 검수와 수정의 분리예요.
> - **재사용이 핵심.** 이 `security-reviewer`는 moderation으로 끝이 아니라, **다음 편 notification, 그 뒤 모든 모듈**에 그대로 발동합니다. 한 번 만들어 계속 쓰는 — 그래서 서브에이전트에 투자하는 겁니다.
> - (한 걸음 더: "security-reviewer로 보안 보고, code-reviewer로 일반 품질도 봐줘"처럼 오케스트레이터가 **두 전문 워커를 조율**할 수도 있습니다.)

---

## 화면 확인 (선택) — 신고 버튼 붙이기

```
resources/static/index.html에 각 게시물에 "신고" 버튼을 추가해줘.
POST /reports를 호출하고, 신고되면 간단한 확인 메시지를 보여주게. 바닐라 JS fetch로.
```

### 끝난 뒤 확인

- 게시물에 신고 버튼이 붙고, 브라우저 새로고침으로 눌러보면 신고가 접수됩니다.

---

> moderation 모듈 · 상비 `security-reviewer` 에이전트(읽기전용·최소권한) · 오케스트레이터-워커 검수까지 자리 잡으면 실습-서브에이전트 완료입니다. 이 검수자는 이후 모든 편에서 재사용됩니다. 막히거나 완성본을 보려면 `git checkout subagent-done`과 비교하세요.
