#!/usr/bin/env bash
# Stop 훅 — 세션이 멈출 때(턴 종료 시점) 이번 세션에서 바뀐 파일 목록과
# 작업 요약(사용자 요청 모음)을 .claude/session-summary.md 에 남긴다.
#
# 세션을 막지 않는다 — 파일만 덮어쓰고 그냥 통과(exit 0).
# 트랜스크립트 전체를 매번 다시 읽어 쓰므로 파일은 항상 "현재 세션의 최신 상태"를 담는다.
# 다음 세션은 시작할 때 이 파일을 읽고 이어받으면 된다.
#
# 주의: Claude Code에는 "세션 종료" 단일 이벤트가 없어 Stop 훅은 매 턴 끝에 실행된다.
#       덕분에 요약은 턴마다 최신으로 갱신된다(부담은 jq 파싱 한 번 수준).
set -uo pipefail

# 1) 훅 입력(JSON, stdin)에서 트랜스크립트 경로와 세션 id 추출
input="$(cat)"
transcript="$(printf '%s' "$input" | jq -r '.transcript_path // empty')"
session_id="$(printf '%s' "$input" | jq -r '.session_id // empty')"

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
out="$PROJECT_ROOT/.claude/session-summary.md"

# 트랜스크립트가 없거나 읽을 수 없으면 조용히 통과(세션은 막지 않는다)
if [ -z "$transcript" ] || [ ! -f "$transcript" ]; then
  exit 0
fi

# 2) 이번 세션 사용자 요청 — last-prompt 엔트리에서 추출.
#    여러 줄 프롬프트는 한 줄로 펴고(gsub), 순서를 유지하며 중복 제거(awk).
requests="$(jq -r 'select(.type=="last-prompt") | .lastPrompt | gsub("[\r\n]+"; " ")' \
  "$transcript" 2>/dev/null | awk 'NF && !seen[$0]++')"

# 3) 이번 세션에서 Write/Edit/MultiEdit로 바뀐 파일 — 순서 유지 중복 제거.
files="$(jq -r 'select(.type=="assistant") | .message.content[]?
        | select(.type=="tool_use" and (.name=="Edit" or .name=="Write" or .name=="MultiEdit"))
        | .input.file_path // empty' "$transcript" 2>/dev/null | awk 'NF && !seen[$0]++')"

# 4) 요약 파일 작성(덮어쓰기 — 항상 현재 세션 최신 상태)
{
  printf '# 세션 요약\n\n'
  printf '_마지막 갱신: %s · 세션: %s_\n\n' "$(date '+%Y-%m-%d %H:%M:%S')" "${session_id:-unknown}"

  printf '## 이번 세션 요청\n\n'
  if [ -n "$requests" ]; then
    printf '%s\n' "$requests" | while IFS= read -r line; do
      printf -- '- %s\n' "$line"
    done
  else
    printf '_기록된 요청 없음_\n'
  fi
  printf '\n'

  printf '## 바뀐 파일\n\n'
  if [ -n "$files" ]; then
    printf '%s\n' "$files" | while IFS= read -r f; do
      printf -- '- `%s`\n' "${f#"$PROJECT_ROOT"/}"
    done
  else
    printf '_바뀐 파일 없음_\n'
  fi
  printf '\n'

  printf '> 다음 세션은 이 파일을 읽고 이어서 작업한다.\n'
} > "$out"

exit 0
