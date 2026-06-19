---
name: auth-context
description: Echo의 인증/인가 현황 — X-User-Id 헤더 기반 신원, Spring Security 미도입 상태
metadata:
  type: project
---

Echo는 현재 Spring Security를 도입하지 않았다. 인증은 "15장에서 도입" 예정으로 컨트롤러 주석에 명시(PostController, ReportController).

현재 신원 식별 방식: `@RequestHeader("X-User-Id") Long userId`, 게다가 `defaultValue = "1"`로 헤더가 없어도 통과한다.

**Why:** 학습용 프로젝트라 인증이 후속 챕터로 미뤄짐. 그래서 클라이언트가 X-User-Id를 자유롭게 위조 가능하고, 어떤 엔드포인트도 역할(role) 검사를 못 한다.

**How to apply:** 인가 검사 누락을 보고할 때, "Spring Security가 아직 없다"는 사실을 전제로 심각도를 매긴다. 신고 상태 변경(PATCH) 같은 관리자 전용 작업이 인증 자체가 없으니 누구나 호출 가능 → 구조적 Critical/High. 단, 인증이 아직 미도입이라는 프로젝트 단계를 함께 언급해 거짓 양성 인상을 줄인다. [[moderation-module]]
