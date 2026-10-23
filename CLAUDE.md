# CLAUDE.md — backend14th

코테이토 14기 백엔드 과제 프로젝트. 일하는 방식과 컨벤션은 개인 하네스(`~/.claude/CLAUDE.md`, `~/.claude/rules/`, `~/.claude/skills/`)를 따르고, 이 파일에는 이 프로젝트만의 내용을 적는다.

## 프로젝트 개요
- Spring Boot 3.5.16, Java 17, Gradle
- Spring Web, Spring Data JPA, Spring Security, Validation, springdoc(Swagger) 2.8.5, Lombok
- DB: MySQL (`com.mysql:mysql-connector-j`), `ddl-auto: update` (마이그레이션 도구 없음)
- 베이스 패키지: `com.cotato.backend14th`
- 원격: `kangcheolung/14th_backend_assignment`, 기본 브랜치 `develop`

## 구조
```
src/main/java/com/cotato/backend14th/
├── Backend14thApplication.java
├── HelloController.java
├── domain/
│   └── book/{controller, dto, entity, repository}     # 아직 service 계층 없음
└── global/
    ├── common/entity/BaseEntity.java                  # createdAt, updatedAt
    ├── common/response/{ApiResponse, ErrorResponse, ResponseUtils}
    ├── config/{CorsConfig, SecurityConfig, SwaggerConfig}
    └── exception/{Backend14thException, ErrorCode, GlobalExceptionHandler}
src/main/resources/{application.yml, application-local.yml}
src/test/java/…/Backend14thApplicationTests.java
```

## 이 프로젝트의 약속
- 성공 응답은 `ResponseUtils.ok/created` 로 `ApiResponse` 에 감싼다. (204 용 헬퍼는 없다)
- 에러는 `Backend14thException(ErrorCode)` 을 던지고 `GlobalExceptionHandler` 가 `ErrorResponse` 로 바꾼다.
  - 검증 실패(`@Valid`) 핸들러는 아직 없다. 추가하기 전에는 400 이 500 으로 나갈 수 있다.
- 에러 코드는 `ErrorCode` enum 하나에 모은다. 형식 `도메인-일련번호` (예: `BOOK-001`)
- `SecurityConfig` 는 `anyRequest().permitAll()` 이고 JWT 필터는 아직 없다.
- `CorsConfig` 는 `http://localhost:3000`, `http://localhost:8080` 만 허용한다.

## 환경 설정
- `application.yml` 이 작업 폴더의 `.env` 를 읽는다. (`optional:file:.env[.properties]`) 필요한 변수: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- 기본 프로파일은 `local` (`SPRING_PROFILES_ACTIVE` 로 바꾼다)
- `.env` 는 Git 에 올리지 않고 읽거나 고치지 않는다. 값이 필요하면 사용자에게 묻는다.

## 주요 명령어
```bash
./gradlew build            # 빌드 + 테스트
./gradlew test             # 테스트
./gradlew bootRun          # 실행 (MySQL 과 .env 필요)
```
Swagger UI: `/swagger-ui/index.html`

## 읽을 곳
- 작업 규칙(브랜치·커밋·이슈·PR): `~/.claude/rules/git-conventions.md`, `pr-conventions.md`
- 백엔드 코드 규칙: `~/.claude/rules/` 의 `java-style`, `api-design`, `exception-handling`, `testing-backend` 등
- 과제 진행 방식(브랜치 `이름/basic` → `이름/harness`, PR 대상)은 과제 안내(Notion)를 따른다.

## 영구 금지
- `git add -A` / `git add .`, `git push --force`, `--no-verify`
- `main`, `develop` 직접 push (PR 로만)
- 시크릿을 `application*.yml` 이나 코드에 하드코딩
- Entity 를 Controller 에서 직접 반환
