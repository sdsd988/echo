#!/usr/bin/env bash
# PreToolUse 훅 — modules/follow 안의 파일이 user/post 도메인 패키지를 직접
# import하려 하면, 파일이 써지기 전에 차단한다.
# 모듈 간 직접 의존 금지(아키텍처 규칙) 위반을 사전에 막고, common 경유를 안내한다.
# PreToolUse 훅은 JSON으로 permissionDecision=deny 를 내보내 도구 실행을 거부한다.
set -uo pipefail

# 1) 훅 입력(JSON, stdin) 수신
input="$(cat)"
file_path="$(printf '%s' "$input" | jq -r '.tool_input.file_path // empty')"

# 경로가 없으면 통과
[ -z "$file_path" ] && exit 0

# 2) modules/follow 안의 .java 파일만 대상 (apps 계층은 모듈 의존 허용)
case "$file_path" in
  */modules/follow/*.java) ;;
  *) exit 0 ;;
esac

# 3) 쓰여질 내용 추출 — 도구별로 위치가 다르다.
#    Write → content, Edit → new_string, MultiEdit → edits[].new_string
content="$(printf '%s' "$input" | jq -r '
  .tool_input.content
  // .tool_input.new_string
  // ([.tool_input.edits[]?.new_string] | join("\n"))
  // empty
')"

[ -z "$content" ] && exit 0

# 4) com.echo.user / com.echo.post 직접 import 탐지
violation="$(printf '%s' "$content" \
  | grep -oE '^[[:space:]]*import[[:space:]]+(static[[:space:]]+)?com\.echo\.(user|post)\.[^;]*;' \
  | head -3)"

[ -z "$violation" ] && exit 0

# 5) 차단 + 안내 메시지
reason="$(cat <<EOF
🚫 modules/follow 에서 user/post 도메인 패키지를 직접 import할 수 없습니다.

감지된 import:
$violation

[아키텍처 규칙] 모듈 간 직접 의존 금지 — modules/* 는 common 만 의존할 수 있습니다.
follow 가 user/post 의 정보를 필요로 한다면 common 모듈을 경유하세요:
  • 공유가 필요한 식별자/DTO 는 common 의 record 로 정의하고 그쪽을 import 합니다.
  • 도메인 동작이 필요하면 apps/customer-api 계층에서 조합(orchestration)으로 풀어냅니다.
follow 도메인 모듈 안에서 user/post 패키지를 import 하도록 고치지 마세요.
EOF
)"

jq -n --arg reason "$reason" '{
  hookSpecificOutput: {
    hookEventName: "PreToolUse",
    permissionDecision: "deny",
    permissionDecisionReason: $reason
  }
}'
exit 0
