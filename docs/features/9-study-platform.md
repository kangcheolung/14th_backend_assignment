# 코테이토 스터디 매칭 플랫폼 (모노레포, E2E 포함)

이슈 #9. 인증, 스터디 모집글, 댓글(= 스터디 신청)을 백엔드와 프론트엔드로 함께 구현했다. API 의 요청·응답·에러 전체는 [api-spec.md](../api-spec.md) 에 있고, 이 문서는 무엇을 어떻게 만들었는지와 왜 그렇게 정했는지를 적는다.

## 1. 배경

코테이토의 스터디 신청은 네이버 카페에서 이뤄지고 있다. 전용 플랫폼으로 옮기기 위해 가장 핵심적인 기능만 골라 만들었다. 별도의 "가입 신청" 기능은 없고, **모집글에 댓글을 남기는 것이 곧 신청**이다.

- 이메일 로그인과 로그아웃, 회원가입 때 기수·파트 입력, 마이페이지 확인
- 모집글 작성, 조회, 검색, 수정, 삭제
- 댓글 작성, 수정, 삭제

이번에는 한 레포에서 백엔드와 프론트엔드를 같이 관리하는 모노레포 구조로 바꾸고, 주요 사용자 흐름은 Playwright E2E 로 확인했다.

## 2. 구현 내용

### 2-1. 모노레포

```txt
.claude/, CLAUDE.md, .github/, docs/      루트 (하네스, 템플릿, 문서)
backend/                                  Spring Boot (Gradle)
frontend/                                 React + TypeScript + Vite + Tailwind CSS
```

- 기존 `src/`, `build.gradle`, `gradle/`, `gradlew*` 를 `git mv` 로 `backend/` 에 옮겼다. 이력은 이동(rename)으로 남는다.
- 훅과 규칙은 `$CLAUDE_PROJECT_DIR` 기준 경로와 `**/` 글롭을 쓰고 있어 고치지 않았다. 고친 곳은 `CLAUDE.md`(구조, 명령어, 환경 파일 위치)와 `.gitattributes`(`/gradlew` → `**/gradlew`)다.

### 2-2. 백엔드 (`backend/src/main/java/com/cotato/backend14th/`)

| 위치 | 하는 일 |
|---|---|
| `domain/user` | `User`, `Part`(PM/DE/BE/FE), `AuthService`(가입·로그인·로그아웃), `UserService`(내 정보, 다른 도메인이 작성자를 찾는 입구), `AuthController`, `UserController` |
| `domain/studypost` | `StudyPost`, `StudyPostComment`, `StudyStatus`, `StudyPostService`, `StudyPostCommentService`, `StudyPostController`, `CommentController`, 검색 조건 `StudyPostSpecifications` |
| `global/security` | `JwtProvider`(발급·검증), `JwtAuthenticationFilter`(요청마다 토큰 확인), `RevokedToken`(로그아웃한 토큰 기록), `AuthEntryPoint`(401 을 공통 에러 형식으로) |
| `global/config` | `SecurityConfig`(공개/로그인 필요 경로, BCrypt), `SwaggerConfig`(Authorize 버튼) |
| `global/exception`, `global/common/response` | `ErrorCode` 추가, `@Valid`·본문 오류·파라미터 타입 오류를 400 으로 처리, `PageResponse` |

**요청 흐름**

1. `JwtAuthenticationFilter` 가 `Authorization: Bearer` 헤더의 토큰을 검증한다. 서명·만료가 맞고 `revoked_tokens` 에 없으면 회원 id 를 principal 로 올린다. 틀리면 아무것도 올리지 않고 넘긴다.
2. `SecurityConfig` 의 경로 규칙이 공개 경로는 익명으로 통과시키고, 나머지는 인증이 없으면 `AuthEntryPoint` 가 401 을 내려준다.
3. 컨트롤러는 `@AuthenticationPrincipal Long userId` 로 회원 id 를 받아 서비스에 넘긴다. 로그인하지 않은 공개 요청에서는 `null` 이다.
4. 작성자 본인 확인은 서비스에서 한다. 아니면 `NOT_POST_AUTHOR`, `NOT_COMMENT_AUTHOR`(403).

### 2-3. 프론트엔드 (`frontend/src/`)

| 위치 | 하는 일 |
|---|---|
| `api/` | axios 인스턴스 한 개(`client.ts`), 도메인별 API 함수(`auth.ts`, `studyPosts.ts`, `comments.ts`), 타입 |
| `auth/` | 토큰 저장 한 곳(`tokenStorage.ts`), 로그인 상태와 내 정보(`useAuth.ts`), 로그인이 필요한 화면 보호(`ProtectedRoute.tsx`) |
| `pages/` | 회원가입, 로그인, 마이페이지, 스터디 목록, 상세, 작성/수정 |
| `components/` | 헤더, 입력 컴포넌트, 버튼, 페이지 이동, 댓글 영역 등 |

- 요청 인터셉터가 토큰을 붙이고, 응답 인터셉터가 공통 응답(`success`, `data`)을 벗겨 `data` 만 돌려준다. 토큰을 보냈는데 401 이면 토큰을 지우고, 로그인이 필요한 화면은 로그인 화면으로 보낸다.
- 목록의 검색 조건(키워드, 모집 상태, 파트, 페이지)은 주소의 쿼리스트링에 담아서 새로고침, 뒤로가기, 링크 공유가 되게 했다.
- 서버 데이터를 보여주는 모든 화면은 로딩, 에러, 빈 상태, 정상 네 가지를 그린다. 서버가 내려준 에러 메시지를 그대로 보여준다.
- 작성자에게만 수정, 삭제, 모집 마감 버튼이 보이고, 마감된 글에는 댓글 입력창 대신 안내가 나온다.

### 2-4. E2E (`frontend/e2e/`)

`auth.spec.ts`(7), `studies.spec.ts`(6), `comments.spec.ts`(6) — 총 19개. 정상 흐름과 에러 흐름(중복 이메일, 기수 0, 틀린 비밀번호, 로그인 없이 접근, 남의 글·댓글, 마감된 글)을 모두 포함한다. 계정과 비밀번호는 실행할 때마다 새로 만들어 코드에 남기지 않는다.

## 3. API 명세

13개 API 의 요청, 응답, 에러 케이스는 [api-spec.md](../api-spec.md) 를 따른다. 에러 코드 요약은 아래와 같다.

| HTTP | code | 상황 |
|---|---|---|
| 400 | `COMMON-003` | 입력 검증 실패, 읽을 수 없는 요청, 잘못된 쿼리 값 |
| 401 | `COMMON-004` / `USER-002` | 토큰이 없거나 무효, 로그아웃한 토큰 / 로그인 정보 불일치 |
| 403 | `STUDY-002` / `COMMENT-002` | 모집글·댓글 작성자가 아님 |
| 404 | `STUDY-001` / `COMMENT-001` | 모집글·댓글이 없음 |
| 409 | `USER-001` / `STUDY-003` | 이미 가입된 이메일 / 마감된 글에 댓글 |

## 4. 설계 결정

| 결정 | 이유 | 고민한 다른 선택지 |
|---|---|---|
| 에러 `code` 는 3주차 규칙대로 `도메인-일련번호` 형식을 쓰고, enum 상수 이름은 과제 명세의 이름을 쓴다 | 3주차의 `BOOK_NOT_FOUND` ↔ `BOOK-001` 과 같은 방식이라 기존 코드와 프로젝트 규칙(`exception-handling`)에 일관된다 | `code` 에 상수 이름 문자열(`STUDY_POST_NOT_FOUND`)을 그대로 내리기. 3주차 형식과 달라져서 고르지 않았다. 프론트와 E2E 는 `code` 문자열이 아니라 HTTP 상태와 `message` 를 기준으로 쓴다 |
| 로그아웃은 토큰의 `jti` 를 DB(`revoked_tokens`)에 저장 | 서버를 다시 켜도 기록이 남고, 명세의 "저장 방식은 자유"를 만족한다 | 인메모리 저장(재시작하면 사라짐), 토큰 만료를 매우 짧게 하기 |
| 잘못된 토큰으로 공개 API 를 부르면 익명으로 처리 | 목록·상세는 로그인과 무관하게 볼 수 있어야 한다. 거절은 인증이 필요한 API 에서만 한다 | 모든 요청에서 즉시 401 |
| 로그인 실패는 이메일 없음과 비밀번호 틀림을 같은 에러로 | 가입된 이메일인지 추측하지 못하게 한다 | 각각 다른 메시지 |
| 목록 검색은 `Specification` + `@EntityGraph(user)`, 댓글 수는 현재 페이지 글 id 를 묶어 한 번에 집계 | 조건이 모두 선택이라 값이 없는 조건을 건너뛰기 쉽고, 작성자·댓글 수를 위한 N+1 쿼리를 막는다 | 조건 조합별 메서드, JPQL 의 `:param is null` 패턴, 글마다 댓글 수 조회 |
| `page`, `size` 범위를 서비스에서 검사(`size` 1~100) | 한 번에 너무 많이 가져오지 못하게 한다 | 제한 없음 |
| 모집글과 댓글은 같은 `domain/studypost` 패키지 | 댓글(`StudyPostComment`)은 글에 속한 하위 개념이라 서로의 Repository 를 오가는 순환을 피할 수 있다 | `domain/comment` 를 따로 두기(서비스끼리 순환 참조 위험) |
| 마감된 글의 댓글은 서버가 409 로 막고, 화면은 입력창을 숨긴다 | 서버가 최종 방어선이고, 화면은 불가능한 동작을 보여주지 않는다 | 화면에서만 막기 |
| 프론트의 서버 데이터는 TanStack Query | 로딩·에러 상태와 갱신을 선언적으로 다루고 `useEffect` 로 데이터를 가져오지 않는 규칙을 지킨다. **의존성 1개 추가** | 직접 만든 fetch 훅(코드가 늘고 `useEffect` 가 필요) |
| 토큰은 `localStorage` 한 곳에서 관리 | 새로고침해도 로그인이 유지되는 가장 단순한 방법 | 메모리만(새로고침하면 풀림), HttpOnly 쿠키(백엔드 설정이 더 필요) |
| 응답 인터셉터에서 공통 응답을 한 번만 벗긴다 | API 함수가 `data` 안쪽 타입만 다룬다 | 호출할 때마다 `.data.data` |
| 테스트 전용 H2 추가 | `@DataJpaTest` 로 목록 검색 쿼리를 MySQL 없이 검증한다. 실행 환경에는 들어가지 않는다 | Repository 테스트 생략 |

## 5. 검증

실행한 명령과 결과다. (2026-10-24, 로컬)

| 대상 | 명령 | 결과 |
|---|---|---|
| 백엔드 단위·웹 계층·Repository 테스트 | `cd backend && ./gradlew test` | 73개 모두 통과 (`contextLoads` 포함, 로컬 MySQL 접속) |
| 프론트 빌드 | `cd frontend && npm run build` | 통과 (타입 검사 포함) |
| 프론트 린트 | `cd frontend && npm run lint` | 통과 (종료 코드 0) |
| E2E (메모리 DB H2 로 띄운 백엔드) | `cd frontend && npx playwright test` | 19개 통과, 연속 2회 실행해도 모두 통과 |
| E2E (로컬 MySQL 로 띄운 백엔드) | `cd frontend && npx playwright test` | 19개 통과 |
| 모바일 폭 | 375px 에서 목록·회원가입 화면 확인 | 가로 스크롤 없음, 레이아웃 정상 |

테스트 구성: `AuthServiceTest` 8, `UserServiceTest` 2, `JwtProviderTest` 8, `StudyPostServiceTest` 13, `StudyPostCommentServiceTest` 12, `StudyPostRepositoryTest` 10(H2), `ApiSecurityTest` 19(공개/로그인 필요 경로, 401·400 에러 형식, `isAuthor` 필드 이름), `Backend14thApplicationTests` 1.

**참고**
- `Backend14thApplicationTests.contextLoads` 는 처음에 `backend/` 에 환경 파일이 없어 실패했다(`DB_URL` 이 `${DB_URL}` 그대로 쓰여 DB 주소로 인식되지 않음). 환경 파일을 `backend/` 로 복사한 뒤 통과했다.
- 이 테스트는 `jwt.secret` 을 테스트 전용 임시 값으로 받는다. 서버를 실제로 띄우려면 환경 파일에 `JWT_SECRET` 이 있어야 한다. MySQL E2E 는 이 값을 환경 변수로 임시로 넘겨서 돌렸다.
- E2E 는 개발 DB 에 `e2e-` 로 시작하는 계정과 글을 남긴다. 실제 MySQL 스키마는 `ddl-auto: update` 로 만들어진다.

## 6. 남은 이슈

- 환경 파일에 `JWT_SECRET`(32바이트 이상)을 추가해야 서버가 뜬다. 값은 `backend/.env.example` 에 이름만 있다.
- 만료된 `revoked_tokens` 기록을 정리하는 작업이 없다. 데이터가 크게 쌓이면 만료 시각 기준으로 지우는 작업이 필요하다.
- 같은 이메일로 동시에 가입 요청이 오면 DB 유니크 제약 오류가 `500` 으로 나갈 수 있다. (사전 확인 `existsByEmail` 과 저장 사이의 경쟁)
- 리프레시 토큰이 없어 토큰이 만료되면(1시간) 다시 로그인해야 한다. `localStorage` 저장은 XSS 에 취약하다.
- 프론트가 쓰는 백엔드 주소는 환경 변수 `VITE_API_URL` 이고, 없으면 `http://localhost:8080` 을 쓴다.
- E2E 는 개발 DB 에 `e2e-` 로 시작하는 이메일의 계정과 글을 남긴다.

closes #9
