---
paths:
  - ".env.example"
  - "**/application*.yml"
  - "**/application*.yaml"
  - "**/application*.properties"
  - "**/config/**/*.java"
  - "**/*SecurityConfig.java"
---

# 설정 · 비밀 정보 규칙

## 비밀 정보
- 비밀번호, 토큰, API 키, DB 접속 정보를 `application*.yml` 이나 코드에 쓰지 않는다. 환경변수로 주입한다.
  - 예: `url: ${DB_URL}`, `password: ${DB_PASSWORD:}`
- `.env` 는 Git 에 올리지 않는다. 필요한 변수 **이름만** 적은 `.env.example` 을 레포에 둔다. (값은 비운다)
- `.env` 를 읽거나 고치지 않는다. 값이 필요하면 사용자에게 알려 달라고 한다. (훅이 막는다)

## 프로파일
- 공통 설정은 `application.yml`, 환경별 설정은 `application-local.yml`, `application-prod.yml` 로 나눈다.
- 로컬 전용 편의 설정(`ddl-auto: update`, `show-sql: true`)을 운영 프로파일에 넣지 않는다.

## 보안 설정
- `SecurityConfig` 의 `permitAll()` 은 최소한으로 둔다. 이유가 분명한 경로만 연다.
- 새 API 를 추가하면 `SecurityConfig` 에서 그 경로의 인증 설정을 반드시 확인한다.
- CORS 허용 출처는 필요한 주소만 명시한다. `allowedOrigins("*")` 와 `allowCredentials(true)` 는 함께 쓸 수 없다.
- 설정을 바꿀 때는 영향 범위(어떤 API 가 열리거나 막히는지)를 사용자에게 설명한다.
