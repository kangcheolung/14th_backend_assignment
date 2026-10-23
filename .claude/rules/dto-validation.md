---
paths:
  - "**/dto/**/*.java"
  - "**/*Request.java"
  - "**/*Response.java"
---

# DTO · 입력 검증 규칙

- 요청 DTO(`dto/request`)와 응답 DTO(`dto/response`)를 분리한다. 한 클래스를 둘 다에 쓰지 않는다.
- DTO 는 `record` 를 우선한다.
- 입력 검증은 Controller 경계에서 `@Valid` 와 검증 어노테이션으로 한다.
  - 문자열: `@NotBlank`, `@Size(min, max)`, `@Email`
  - 숫자: `@Min`, `@Max`, `@Positive`
  - 검증 메시지는 사용자에게 보여도 되는 한국어로 쓴다.
- 검증 실패는 `400` 과 공통 에러 응답으로 내려간다. (`exception-handling` 규칙 참고)
- 명세에 적힌 필드 제약(길이, 범위, 허용 값)은 코드의 검증과 문서가 같아야 한다.
- 응답 DTO 에 비밀번호, 토큰 같은 민감 정보를 넣지 않는다.
- Entity → DTO 변환은 정적 팩토리(`from(entity)`) 나 변환기 클래스에서 한다. Entity 에 DTO 의존을 넣지 않는다.
