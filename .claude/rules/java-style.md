---
paths:
  - "**/*.java"
---

# Java 코드 스타일

## 문서화와 주석
- 새로 만드는 클래스·인터페이스·record 에는 클래스 Javadoc 으로 **역할, 책임, 경계**(무엇을 하고 무엇을 하지 않는지)를 적는다. 기존 파일에 소급 적용하지 않는다.
- 파일을 고칠 때는 그 변경과 관련된 기존 주석이 틀려지지 않게 같이 고친다.
- 주석은 코드가 하는 일을 반복하지 말고 **왜 필요한지**를 적는다. 순서가 중요한 흐름은 `1.`, `2.` 번호를 붙인다.

## 구조
- 의존 방향은 Controller → Service → Repository 한 방향이다. 순환 의존은 만들지 않는다.
- Entity 를 Controller 계층에 노출하지 않는다. 반드시 DTO 로 바꾼다.
- 외부 API 연동은 별도 `client/` 패키지로 분리한다.
- 의존성은 생성자 주입으로 받는다. (`@RequiredArgsConstructor`)

## 코딩 컨벤션
- Lombok: `@RequiredArgsConstructor`, `@Getter`, `@Builder`, `@Slf4j`. 엔티티에 `@Setter` 를 쓰지 않는다.
- DTO 는 `record` 를 우선한다.
- 상수는 `enum` 이나 `static final` 로 둔다. 매직 넘버·매직 문자열을 쓰지 않는다.
- `null` 을 반환하지 않는다. 값이 없을 수 있으면 `Optional` 이나 예외를 쓴다.
- 사용하지 않는 import, 주석 처리한 코드, 디버그용 출력(`System.out`)을 남기지 않는다.
