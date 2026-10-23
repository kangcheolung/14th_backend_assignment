---
paths:
  - "**/api/**/*.ts"
  - "**/api/**/*.tsx"
  - "**/services/**/*.ts"
  - "**/lib/api*.ts"
---

# 프론트 API 클라이언트 규칙

- HTTP 클라이언트(axios 인스턴스 등)는 한 곳에서 만들고 어디서든 그것을 쓴다. 컴포넌트에서 `fetch` 나 `axios` 를 직접 호출하지 않는다.
- `baseURL` 은 환경변수(`import.meta.env.VITE_API_URL` 등)로 받는다. 주소를 코드에 박지 않는다.
- 요청 인터셉터에서 `Authorization: Bearer 토큰` 헤더를 붙인다.
- 응답 인터셉터에서 서버의 공통 응답(`success`, `data` …)을 한 번 벗겨서 `data` 만 돌려준다.
- `401` 이 오면 저장한 토큰을 지우고 로그인 화면으로 보낸다. (로그아웃·만료·무효 토큰 모두)
- 서버의 에러 응답(`code`, `message`)을 타입으로 정의해서 화면에 메시지를 보여줄 수 있게 한다.
- 요청·응답 타입을 API 함수마다 정의한다. 백엔드 명세(`docs/api-spec.md`)와 필드 이름을 맞춘다.
- API 함수는 도메인별 파일로 나눈다. (`api/auth.ts`, `api/studyPosts.ts`)
- 토큰은 저장 위치와 만료를 한 곳에서 관리한다. 화면 곳곳에서 `localStorage` 를 직접 읽지 않는다.
