# echo 프롬프트 가이드 — 실습-MCP (MCP)

> **진입:** `git checkout mcp-base` (실습-MCP 시작점 — 이 프롬프트만, 결과 코드 없음)
> 아래를 **준비물 → S0 → S2 순서대로** 진행하세요. S0~S2는 한 세션에서 이어서 합니다.
> Claude 응답은 매번 조금씩 다릅니다 — 결과가 안내와 다르면 다시 시키거나 직접 다듬으면 됩니다.
> 막히거나 완성본을 보려면 `git checkout mcp-done`과 비교하세요.

---

## 준비물 — Docker 설치

이번 편은 Postgres를 **Docker 컨테이너**로 띄웁니다. Docker가 없으면 먼저 설치하세요.

- **macOS / Windows:** [docker.com](https://www.docker.com/products/docker-desktop/)에서 **Docker Desktop** 다운로드·설치 후 실행. (Mac은 `brew install --cask docker-desktop`도 가능)
- **Linux:** `curl -fsSL https://get.docker.com | sh`

설치 확인 (Docker Desktop이 **실행 중**이어야 합니다 — 메뉴바 고래 아이콘):

```
docker --version
docker compose version
```

---

## 이번 편 요구사항 — H2 → Postgres 전환 + MCP 연결

지금까지 echo는 인메모리 **H2**로 돌았습니다. 운영을 향해 **Postgres(Docker)**로 옮기고, Claude가 **실제 DB를 직접 들여다보며** 검증하도록 PostgreSQL MCP를 연결합니다.

- **인프라:** 인메모리 H2 → Docker Postgres. **이후 모든 실습은 Postgres 위**에서 돕니다.
- **DB 접속(로컬 개발용 고정):** db `echo` · user `echo` · password `echo` · port `5432`.
- **스키마:** Flyway 마이그레이션 스크립트로 관리(멱등). 기존 user·post·timeline·follow·engagement 테이블 이관.
- **테스트:** 기존 H2 통합 테스트 → **Testcontainers** 기반 Postgres로 전환, 전원 통과.
- **MCP:** Claude가 실제 DB 스키마·데이터를 직접 조회하도록 **PostgreSQL MCP** 연결.
- **관습:** 기존 echo 구조 유지. **DB 비밀번호를 코드/`.mcp.json`에 평문으로 박지 않기**(로컬 데모 비번은 예외).

---

## S0 — H2 → Postgres/Docker 전환

요구사항대로 인프라 전환을 위임합니다. 우리는 컨테이너도 마이그레이션 스크립트도 직접 쓰지 않습니다.

```
echo를 인메모리 H2에서 Docker로 띄우는 Postgres로 전환해줘.

- docker-compose.yml로 Postgres 16 컨테이너 정의 (db echo / user echo / password echo / 5432)
- 앱 datasource를 H2 → Postgres로 바꾸고 H2 의존성 정리
- 기존 스키마(user·post·timeline·follow·engagement)를 Flyway 마이그레이션 스크립트로 이관 (멱등하게)
- 비밀번호는 docker-compose 환경변수로, git에 평문 커밋되지 않게
```

### 끝난 뒤 확인

```
docker compose up -d
./gradlew bootRun
```

- 컨테이너가 뜨고(`docker ps`에 postgres), 앱이 **H2 없이** Postgres에 붙어 기동됩니다.
- `db/migration/`에 Flyway 스크립트(`V1__...sql` 등)가 생겼는지 확인.

> **⚠ 씨앗:** 마이그레이션이 *정말* 잘 들어갔는지, 지금은 Claude가 **코드만 보고 추측**합니다 — 실제 DB를 못 보니까요. "테이블이 진짜 만들어졌나, row가 들어갔나"를 Claude가 직접 확인하게 하려면? → S1에서 **MCP**로 DB를 Claude 손에 쥐어줍니다.

---

## S1 — PostgreSQL MCP 연결

Claude에게 **실제 DB에 접근하는 도구**를 붙입니다. 아래 명령을 **터미널에서 직접** 실행하세요(위임이 아니라 우리가 칩니다).

```
claude mcp add --scope project --transport stdio echo-db \
  -- npx -y @modelcontextprotocol/server-postgres \
  postgresql://echo:echo@localhost:5432/echo
```

세션 안에서 연결 상태를 확인합니다:

```
/mcp
```

`echo-db`가 **✓ Connected**로 뜨면 됩니다. (project 범위라 첫 로드 시 승인을 한 번 물어볼 수 있습니다 — 승인하세요.)

이제 Claude가 DB를 직접 보게 시킵니다:

```
방금 연결한 DB에 직접 접속해서, follow랑 engagement 테이블의 스키마와 현재 row 수를 보여줘.
S0 마이그레이션이 의도대로 들어갔는지 실제 테이블 기준으로 검증해줘.
```

### 끝난 뒤 확인

- `/mcp`에서 `echo-db`가 **Connected**.
- 저장소 루트에 **`.mcp.json`**이 생성됨(이게 팀 공유 범위 — git에 커밋하면 팀원 모두 같은 MCP를 씁니다).
- Claude가 코드가 아니라 **실제 테이블**을 조회해 스키마·row 수를 답함.

> **짚기 세 가지.**
> ① **범위(scope).** `--scope project`라서 `.mcp.json`에 적히고 git으로 팀 공유됩니다(기본값 `local`은 `~/.claude.json`, 나만). MCP 등록을 *팀의 자산*으로 만드는 자리입니다.
> ② **비밀번호.** `.mcp.json`은 git에 올라가니 운영 비번을 평문으로 박으면 안 됩니다. 로컬 데모(`echo:echo@localhost`)라 그냥 뒀지만, 실제로는 `postgresql://echo:${POSTGRES_PASSWORD}@...`처럼 **환경변수 확장**을 쓰세요(`.mcp.json`은 `${VAR}`를 지원).
> ③ **보안.** 이 `@modelcontextprotocol/server-postgres`는 archived된 레퍼런스 서버(트랜잭션 레벨 읽기전용)라 로컬 데모엔 충분하지만, DB MCP는 구조상 조심해야 합니다 — Claude가 자연어를 SQL로 바꿔 실행하니 *프롬프트 인젝션*으로 권한·RLS가 우회될 수 있어요(2025년 Supabase MCP 사건). 실무 원칙: **읽기전용 전용 유저**로 연결(어드민 금지) · **운영 DB 금지**(개발·스테이징만) · 벤더 MCP(Supabase·Prisma 등) 우선.

---

## S2 — Testcontainers로 테스트 전환

마지막으로, 테스트도 H2가 아니라 **진짜 Postgres** 위에서 돌게 바꿉니다.

```
기존 H2 기반 통합 테스트를 Testcontainers의 Postgres로 전환해줘.
- testcontainers-postgresql 의존성 추가
- 공통 베이스 테스트 클래스에서 PostgreSQLContainer를 띄우고 datasource를 거기에 연결
- 기존 통합 테스트가 그 베이스를 상속해 실제 Postgres 위에서 돌게
```

### 끝난 뒤 확인

```
./gradlew test
```

- Testcontainers가 테스트용 Postgres 컨테이너를 자동으로 띄우고, 통합 테스트가 전원 통과합니다.
- 단위 테스트는 그대로, 통합 테스트만 실제 DB 방언 위에서 검증됩니다.

> **짚기:** 운영은 Postgres인데 테스트만 H2면, *방언 차이*(NULL 처리·JSON·인덱스)로 "테스트는 통과했는데 운영에서 터지는" 일이 생깁니다. Testcontainers는 테스트를 운영과 같은 DB 위로 올려 그 틈을 없앱니다.

---

> Docker Postgres 전환 · PostgreSQL MCP 연결 · Testcontainers 테스트까지 자리 잡으면 실습-MCP 완료입니다. 이후 모든 실습은 이 Postgres 위에서 돕니다. 막히거나 완성본을 보려면 `git checkout mcp-done`과 비교하세요.
