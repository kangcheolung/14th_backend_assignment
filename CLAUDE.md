# CLAUDE.md — backend14th

코테이토 14기 백엔드 과제 프로젝트. 일하는 방식과 컨벤션은 개인 하네스(`~/.claude/CLAUDE.md`, `~/.claude/rules/`, `~/.claude/skills/`)를 따르고, 이 파일에는 이 프로젝트만의 내용을 적는다.
`backend/`(Spring Boot)와 `frontend/`(React)를 한 레포에서 관리하는 모노레포다. (이슈 #9)

## 프로젝트 개요
- Spring Boot 3.5.16, Java 17, Gradle
- Spring Web, Spring Data JPA, Spring Security, Validation, springdoc(Swagger) 2.8.5, Lombok, jjwt 0.12.6(JWT)
- DB: MySQL (`com.mysql:mysql-connector-j`), `ddl-auto: update` (마이그레이션 도구 없음). Repository 테스트(`@DataJpaTest`)는 테스트 전용 H2 를 쓴다.
- 프론트: React 19, TypeScript, Vite, Tailwind CSS 4, React Router, axios, TanStack Query. E2E 는 Playwright. (Next.js 는 쓰지 않는다)
- 베이스 패키지: `com.cotato.backend14th`
- 원격: `kangcheolung/14th_backend_assignment`, 기본 브랜치 `develop`

## 구조
```
.claude/, CLAUDE.md, .github/            # 하네스와 이슈·PR 템플릿 (루트)
docs/{api-spec.md, features/}            # API 명세, 기능 문서 (features/{이슈번호}-{기능명}.md)
backend/                                 # Spring Boot (Gradle)
├── build.gradle, gradlew, .env.example
└── src/main/java/com/cotato/backend14th/
    ├── Backend14thApplication.java
    ├── HelloController.java
    ├── domain/
    │   ├── book/{controller, dto, entity, repository}     # 학습용 초기 코드. service 계층 없음
    │   ├── user/{controller, dto/request, dto/response, entity, repository, service}
    │   └── studypost/{controller, dto/request, dto/response, entity, repository, service}   # 모집글 + 댓글
    └── global/
        ├── common/entity/BaseEntity.java                  # createdAt, updatedAt
        ├── common/response/{ApiResponse, ErrorResponse, PageResponse, ResponseUtils}
        ├── config/{CorsConfig, SecurityConfig, SwaggerConfig}
        ├── exception/{Backend14thException, ErrorCode, GlobalExceptionHandler}
        └── security/{JwtProvider, JwtAuthenticationFilter, AuthEntryPoint, RevokedToken, RevokedTokenRepository, TokenPayload}
    (resources: application.yml, application-local.yml / test: 서비스·Repository·보안 테스트와 fixture/)
frontend/                                # React + TypeScript + Vite + Tailwind (개발 서버 3000번)
├── package.json, vite.config.ts, playwright.config.ts, .env.example
├── e2e/                                 # Playwright 시나리오 (auth, studies, comments)
└── src/{api, auth, components, pages, utils}
```

## 이 프로젝트의 약속
- 성공 응답은 `ResponseUtils.ok/created` 로 `ApiResponse` 에 감싼다. 본문 없는 204 는 감싸지 않고 `ResponseEntity.noContent().build()` 를 쓴다. (204 용 헬퍼는 없다)
- 에러는 `Backend14thException(ErrorCode)` 을 던지고 `GlobalExceptionHandler` 가 `ErrorResponse` 로 바꾼다.
  - `@Valid` 검증 실패, 읽을 수 없는 요청 본문, 파라미터 타입 오류는 핸들러가 `400 INVALID_INPUT` 으로 바꾼다. 검증 실패는 첫 필드의 안내 문구를 `message` 로 내려준다.
  - 시큐리티 필터 단계의 인증 실패는 `AuthEntryPoint` 가 같은 `ErrorResponse` 형식(401)으로 내려준다.
- 에러 코드는 `ErrorCode` enum 하나에 모은다. 응답의 `code` 는 `도메인-일련번호` (예: `STUDY-001`)이고, enum 상수 이름은 과제 명세 이름을 쓴다 (예: `STUDY_POST_NOT_FOUND`). 프론트와 E2E 는 `code` 문자열이 아니라 HTTP 상태와 `message` 를 기준으로 쓴다.
- 인증은 JWT(`Authorization: Bearer`)다. 로그아웃은 토큰의 `jti` 를 `revoked_tokens` 에 저장해 무효 처리한다. `SecurityConfig` 의 공개 경로 외에는 로그인이 필요하고, 새 API 를 추가하면 이 경로 설정을 같이 확인한다. 작성자 본인 확인은 Service 에서 한다.
- 공개 API 에 잘못된 토큰이 오면 익명으로 처리한다. 컨트롤러는 `@AuthenticationPrincipal Long userId` 로 회원 id 를 받고, 익명이면 `null` 이다.
- 모집글과 댓글은 같은 `domain/studypost` 에 둔다. 다른 도메인의 Repository 를 직접 쓰지 않고 그 도메인의 Service(`UserService.getUser`)를 호출한다.
- `CorsConfig` 는 `http://localhost:3000`, `http://localhost:8080` 만 허용한다.
- 프론트: HTTP 호출은 `src/api/client.ts` 의 axios 인스턴스 하나만 쓰고, 서버 데이터는 TanStack Query 로 가져온다. 토큰은 `auth/tokenStorage.ts` 에서만 읽고 쓴다.

## 환경 설정
- `application.yml` 이 작업 폴더의 `.env` 를 읽는다. (`optional:file:.env[.properties]`) 서버를 `backend/` 에서 실행하므로 이 파일은 `backend/` 안에 둔다. 필요한 변수는 `backend/.env.example` 에 있다. (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`)
  - `JWT_SECRET` 은 32바이트 이상이어야 한다. 짧으면 서버가 시작할 때 거부한다.
- 프론트는 `frontend/` 의 환경 파일로 백엔드 주소(`VITE_API_URL`)를 받는다. 이름은 `frontend/.env.example` 에 있다.
- 기본 프로파일은 `local` (`SPRING_PROFILES_ACTIVE` 로 바꾼다)
- `.env` 는 Git 에 올리지 않고 읽거나 고치지 않는다. 값이 필요하면 사용자에게 묻는다.

## 주요 명령어
```bash
# 백엔드 (backend/ 에서)
./gradlew build            # 빌드 + 테스트
./gradlew test             # 테스트
./gradlew bootRun          # 실행 (MySQL 과 backend/ 의 환경 파일 필요, 8080)

# 프론트엔드 (frontend/ 에서)
npm run dev                # 개발 서버 (3000, API 를 쓰려면 백엔드가 먼저 떠 있어야 함)
npm run build              # 타입 검사 + 빌드
npm run lint               # 린트
npx playwright test        # E2E (아래 참고)
npx playwright show-report # E2E 리포트
```

**E2E 실행**: 백엔드(8080)와 MySQL 을 먼저 띄운 뒤 `frontend/` 에서 `npx playwright test` 를 실행한다. 프론트 개발 서버는 Playwright 가 직접 띄운다. 처음 한 번 `npx playwright install chromium` 이 필요하다. 테스트는 개발 DB 에 `e2e-` 로 시작하는 이메일의 계정과 글을 남긴다.
Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## 읽을 곳
- 작업 규칙(브랜치·커밋·이슈·PR): `~/.claude/rules/git-conventions.md`, `pr-conventions.md`
- 백엔드 코드 규칙: `~/.claude/rules/` 의 `java-style`, `api-design`, `exception-handling`, `testing-backend` 등
- 과제 진행 방식(브랜치 `이름/basic` → `이름/harness`, PR 대상)은 과제 안내(Notion)를 따른다.

## 영구 금지
- `git add -A` / `git add .`, `git push --force`, `--no-verify`
- `main`, `develop` 직접 push (PR 로만)
- 시크릿을 `application*.yml` 이나 코드에 하드코딩
- Entity 를 Controller 에서 직접 반환
