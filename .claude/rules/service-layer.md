---
paths:
  - "**/service/**/*.java"
---

# Service 규칙

## 트랜잭션
- 조회 서비스는 클래스에 `@Transactional(readOnly = true)`, 변경 서비스는 `@Transactional` 을 붙인다.
- 프로젝트가 이미 Command(쓰기)/Query(읽기) 로 나눠져 있으면 그 구조를 따른다. 없으면 억지로 나누지 않는다.
- 외부 API 호출·파일 처리처럼 오래 걸리는 일은 트랜잭션 안에 넣지 않는다.
- 변경은 영속 상태 엔티티의 Dirty Checking 을 활용한다. `save()` 는 새 엔티티에만 쓴다.

## 예외와 로깅
- 없는 대상은 `orElseThrow(() -> new 도메인예외(ErrorCode.XXX_NOT_FOUND))` 패턴을 쓴다.
- 단순 Not Found 는 로그를 남기지 않는다. 중요한 비즈니스 실패, 설정 오류, 외부 연동 실패는 예외를 던지기 전에 `log.error` 를 남긴다.

## 구조
- 한 메서드는 한 가지 일만 한다. 길어지면 private 메서드로 나눈다.
- 권한 검사(작성자 본인 여부 등)는 Service 에서 한다. 실패하면 `403` 에 해당하는 예외를 던진다.
- 다른 도메인의 Repository 를 직접 쓰지 말고 그 도메인의 Service 를 호출한다.
