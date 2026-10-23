---
paths:
  - "**/entity/**/*.java"
  - "**/repository/**/*.java"
---

# JPA Entity · Repository 규칙

## Entity
- 공통 시각(`createdAt`, `updatedAt`)은 `BaseEntity` 를 상속해서 쓴다. (`@EnableJpaAuditing` 필요)
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 를 쓰고, 생성은 `@Builder` 나 정적 팩토리로 한다.
- `@Setter` 를 쓰지 않는다. 상태 변경은 의도가 드러나는 메서드(`close()`, `changeTitle()`)로 한다.
- Enum 필드는 `@Enumerated(EnumType.STRING)` 으로 저장한다.
- 연관관계는 모두 `FetchType.LAZY` 로 한다. 양방향은 꼭 필요할 때만 쓰고, 연관관계 주인을 분명히 한다.
- `@Table`, `@Column` 의 이름·길이·제약은 마이그레이션 SQL 과 정확히 같아야 한다.

## Repository
- N+1 이 생길 수 있는 조회는 `JOIN FETCH` 나 `@EntityGraph` 를 쓴다. 목록 조회는 쿼리 수를 확인한다.
- 페이징 + 컬렉션 fetch join 은 함께 쓰지 않는다. (메모리 페이징 문제)
- `@Modifying` 벌크 쿼리는 영속성 컨텍스트와 어긋날 수 있다. 쓴 뒤에는 이미 조회한 엔티티를 다시 조회한다.
- 조회 조건이 늘어나는 검색은 메서드 이름 대신 `@Query` 나 동적 쿼리로 쓴다.
- 자주 조회하는 컬럼과 FK 에는 인덱스를 둔다.
