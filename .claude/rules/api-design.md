---
paths:
  - "**/controller/**/*.java"
  - "**/*Controller.java"
---

# API 설계 규칙 (Controller)

## URL과 메서드
- 주소는 `/api/…` 로 시작하고 복수 명사를 쓴다. 예: `/api/study-posts/{postId}/comments`
- 메서드 의미를 지킨다: `GET` 조회, `POST` 생성, `PATCH` 부분 수정, `PUT` 전체 교체, `DELETE` 삭제
- 동사를 URL 에 넣지 않는다. (`/getUser` 대신 `GET /users/{id}`)

## 응답
- 성공 응답은 프로젝트의 공통 응답 래퍼(`ResponseUtils` 등)로 감싼다. 새 형식을 만들지 않는다.
- 상태 코드: 생성 `201`, 본문 없는 성공 `204`, 검증 실패 `400`, 인증 없음 `401`, 권한 없음 `403`, 대상 없음 `404`, 중복·상태 충돌 `409`
- 목록은 페이지 응답 형식을 통일한다. (`content`, `page`, `size`, `totalElements`, `totalPages`)
- 응답 DTO 에 비밀번호 같은 민감 정보를 넣지 않는다.

## Controller 의 책임
- 요청 받기, 검증(`@Valid`), 서비스 호출, 응답 만들기까지만 한다. 비즈니스 로직은 Service 에 둔다.
- Controller 에서 `Repository` 를 직접 쓰지 않는다. (학습용 초기 코드는 예외일 수 있으니 기존 패턴을 따른다)
- 인증·작성자 확인이 필요한 API 는 권한 설정에 빠지지 않았는지 `SecurityConfig` 를 함께 확인한다.

## Swagger 문서
- 클래스: `@Tag(name = "도메인", description = "…")`
- 메서드: `@Operation(summary = "동사+목적어", description = "호출 시점, 조건, 주의사항")` — description 도 항상 쓴다.
- 요청·응답 DTO 필드: `@Schema(description = "…")`
