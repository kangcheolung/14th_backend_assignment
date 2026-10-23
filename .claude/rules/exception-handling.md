---
paths:
  - "**/exception/**/*.java"
  - "**/*Exception.java"
  - "**/*ErrorCode.java"
---

# 예외 처리 규칙

## 구조
- `ErrorCode` enum 하나에 HTTP 상태, 코드, 메시지를 모아 관리한다. 예: `BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOK-001", "책을 찾을 수 없습니다.")`
- 코드 형식은 `도메인-일련번호` 다. 새 도메인은 새 그룹을 추가한다.
- 던지는 예외는 프로젝트 공통 예외 하나(`ErrorCode` 를 담는다)를 쓴다. 상황마다 새 예외 클래스를 만들지 않는다.
- `@RestControllerAdvice` 의 전역 핸들러에서 한 번에 처리한다. 컨트롤러마다 try-catch 하지 않는다.

## 핸들러가 꼭 처리해야 하는 것
| 상황 | 상태 |
|---|---|
| 공통 예외(`ErrorCode` 포함) | 코드가 정한 상태 |
| `@Valid` 검증 실패 (`MethodArgumentNotValidException`) | 400 |
| 요청 본문을 읽을 수 없음, 파라미터 타입 불일치 | 400 |
| 인증 실패 / 권한 없음 | 401 / 403 |
| 그 밖의 예상 못 한 예외 | 500 (공통 메시지만) |

핸들러가 없는 예외는 Spring 기본 응답으로 나가 형식이 깨진다. 새 API 를 만들 때 이 표를 점검한다.

## 응답과 로그
- 에러 응답에 스택트레이스, SQL, 내부 경로 같은 내부 정보를 넣지 않는다.
- 단순 Not Found 는 로그를 남기지 않는다. 시스템 설정 오류·외부 연동 실패·중요한 비즈니스 실패는 `log.error` 를 남긴 뒤 던진다.
