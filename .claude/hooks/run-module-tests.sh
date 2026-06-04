#!/usr/bin/env bash
# PostToolUse 훅 — Java 파일이 Edit/Write/MultiEdit로 수정되면, 그 파일이 속한
# 모듈의 테스트만 실행한다. 실패하면 결과를 stderr로 내보내고 exit 2 로 신호한다.
# (PostToolUse 훅이 exit 2 면 stderr 내용이 Claude에게 피드백되어 자동 수정을 유도한다.)
set -uo pipefail

# 1) 훅 입력(JSON, stdin)에서 수정된 파일 경로 추출
input="$(cat)"
file_path="$(printf '%s' "$input" | jq -r '.tool_input.file_path // empty')"

# 경로가 없으면(해당 없음) 조용히 통과
[ -z "$file_path" ] && exit 0

# 2) Java 파일이 아니면 통과 (build.gradle.kts, 설정 파일 등은 무시)
case "$file_path" in
  *.java) ;;
  *) exit 0 ;;
esac

# 3) 파일 경로로 모듈(Gradle 테스트 태스크)을 판별
case "$file_path" in
  */modules/post/*)      task=":modules:post:test" ;;
  */modules/timeline/*)  task=":modules:timeline:test" ;;
  */modules/user/*)      task=":modules:user:test" ;;
  */apps/customer-api/*) task=":apps:customer-api:test" ;;
  *) exit 0 ;;  # 매핑되는 모듈 없음 → 통과
esac

# 4) 프로젝트 루트에서 해당 모듈 테스트만 실행
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$PROJECT_ROOT" || exit 0

output="$(./gradlew "$task" 2>&1)"
status=$?

if [ "$status" -ne 0 ]; then
  {
    printf '%s\n\n' "$output"
    printf '❌ %s 실패 — 위 테스트 오류를 확인하고 코드를 고쳐줘.\n' "$task"
  } >&2
  exit 2
fi

printf '✅ %s 통과\n' "$task"
exit 0
