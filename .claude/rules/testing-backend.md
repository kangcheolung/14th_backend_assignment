---
paths:
  - "**/src/test/**/*.java"
---

# 백엔드 테스트 규칙

## 종류 선택
| 종류 | 기반 | 언제 |
|---|---|---|
| 단위 | `@ExtendWith(MockitoExtension.class)` | Service 의 비즈니스 로직 검증 (기본) |
| Repository | `@DataJpaTest` | 쿼리를 실제로 검증해야 할 때 |
| 통합 | `@SpringBootTest` + `@Tag("integration")` | API 전체 흐름 검증 |

단위 테스트를 먼저 쓰고, DB 나 전체 흐름이 꼭 필요할 때만 통합 테스트를 더한다.

## 시나리오는 3가지를 갖춘다
- **정상 케이스**: 기대한 결과가 나오는가
- **예외 케이스**: 없는 대상, 권한 없음, 검증 실패, 중복 같은 에러가 올바른 `ErrorCode` 로 나오는가
- **경계 케이스**: 0, 빈 값, 최대 길이, 마지막 페이지

## 작성 방식
- `@DisplayName` 은 한국어로 "무엇을 하면 어떻게 된다" 가 보이게 쓴다.
- `// Given / When / Then` 구조로 나눈다.
- 검증은 결과(반환값, 상태)를 우선하고, 호출 횟수 검증(`then().should()`)은 꼭 필요한 곳에만 쓴다.
- 테스트 데이터는 Fixture 클래스(`fixture/XxxFixture`)의 `public static` 팩토리로 만든다.
- 테스트끼리 데이터를 공유하지 않는다. 시간, 난수는 고정해서 쓴다.
- 통합 테스트는 `./gradlew test -Dgroups=integration` 처럼 분리해서 실행할 수 있게 `@Tag("integration")` 을 붙인다.

## 지키는 것
- 실패하는 테스트를 지우거나 단언을 약하게 바꿔서 통과시키지 않는다. 원인이 코드인지 테스트인지 먼저 가른다.
- 프로덕션 코드와 테스트 코드는 커밋을 나눈다.
- 작업을 끝내기 전에 `./gradlew test` 가 통과하는지 실제로 실행해서 확인한다.
