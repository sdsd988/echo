#!/usr/bin/env bash
# PostToolUse 훅 — Java 파일이 Edit/Write/MultiEdit로 수정되면,
#   (1) 새로 만든(Write) main 소스에 짝 테스트가 없으면 경고하고,
#   (2) 그 파일이 속한 모듈의 테스트만 실행한다.
# 문제가 있으면 stderr로 내보내고 exit 2 로 신호한다.
# (PostToolUse 훅이 exit 2 면 stderr 내용이 Claude에게 피드백되어 자동 수정을 유도한다.)
# 성공/해당 없음은 조용히 통과(exit 0). — 시끄러운 건 문제가 있을 때만.
set -uo pipefail

# 1) 훅 입력(JSON, stdin)에서 도구 이름과 수정된 파일 경로 추출
input="$(cat)"
tool_name="$(printf '%s' "$input" | jq -r '.tool_name // empty')"
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
  */modules/follow/*)    task=":modules:follow:test" ;;
  */modules/post/*)      task=":modules:post:test" ;;
  */modules/timeline/*)  task=":modules:timeline:test" ;;
  */modules/user/*)      task=":modules:user:test" ;;
  */apps/customer-api/*) task=":apps:customer-api:test" ;;
  *) exit 0 ;;  # 매핑되는 모듈 없음 → 통과
esac

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$PROJECT_ROOT" || exit 0

# 4) 짝 테스트 점검 — main 소스를 새로 만들(Write) 때만.
#    main/.../Foo.java → test/.../FooTest.java 가 있거나,
#    같은 모듈 test 트리에서 Foo 가 참조되면 "테스트 있음"으로 본다.
warn=""
if [ "$tool_name" = "Write" ] && [[ "$file_path" == */src/main/java/* ]]; then
  class_name="$(basename "$file_path" .java)"
  module_root="${file_path%/src/main/java/*}"          # 모듈 루트
  rel_path="${file_path#*/src/main/java/}"             # 패키지 경로 + 파일명
  expected_test="${module_root}/src/test/java/${rel_path%.java}Test.java"
  test_root="${module_root}/src/test"
  if [ ! -f "$expected_test" ] \
     && ! grep -rqw "$class_name" "$test_root" 2>/dev/null; then
    warn=$(printf '⚠️  %s — 짝 테스트가 없습니다. %s 를 추가하는 게 좋겠어.' \
      "$class_name.java" "$(basename "$expected_test")")
  fi
fi

# 5) 해당 모듈 테스트만 실행
output="$(./gradlew "$task" 2>&1)"
status=$?

if [ "$status" -ne 0 ]; then
  {
    printf '%s\n\n' "$output"
    printf '❌ %s 실패 — 위 테스트 오류를 확인하고 코드를 고쳐줘.\n' "$task"
    [ -n "$warn" ] && printf '%s\n' "$warn"
  } >&2
  exit 2
fi

# 테스트는 통과했지만 짝 테스트가 없으면 경고만 시끄럽게
if [ -n "$warn" ]; then
  {
    printf '✅ %s 통과\n' "$task"
    printf '%s\n' "$warn"
  } >&2
  exit 2
fi

# 전부 정상 → 조용히 통과
printf '✅ %s 통과\n' "$task"
exit 0
