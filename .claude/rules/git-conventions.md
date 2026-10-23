# Git 컨벤션

## 브랜치
- 기본 브랜치는 `develop` (없으면 `main`). 작업 브랜치는 항상 최신 기본 브랜치에서 만든다.
- 이름: `feat/{이슈번호}-{영문-설명}`, `fix/…`, `chore/…`, `docs/…`, `refactor/…`, `hotfix/{설명}`
  - 예: `feat/7-harness`, `fix/12-token-expire`
- 이슈 번호가 없으면 먼저 이슈를 만든다. (`start-issue` 스킬)
- `main`, `develop` 에 직접 커밋하거나 push 하지 않는다. push 는 훅이 막는다.

## 커밋
- 메시지: `타입: 요약`, 범위가 필요하면 `타입(범위): 요약`
  - 타입: `feat`, `fix`, `chore`, `refactor`, `docs`, `test`, `style`, `perf`, `build`, `ci`, `revert`
- 요약은 한국어로 짧게 쓴다. 본문이 필요하면 "무엇"보다 "왜"를 적는다.
- 프로덕션 코드와 테스트 코드는 **별도 커밋**으로 나눈다.
- 이슈 하나도 논리 단위로 나눈다. 권장 순서: Repository → Service → Controller → 테스트 → 문서
- `git add` 는 파일을 하나씩 지정한다. `git add -A`, `git add .` 는 쓰지 않는다. (비밀 파일이 섞일 수 있다)
- 이미 push 한 커밋은 `--amend` 하지 않는다. `--no-verify`, `push --force` 는 쓰지 않는다.

## 확인 규칙
- 코드를 다 만들었다고 바로 커밋하지 않는다. 사용자가 확인할 시간을 주고, **커밋해도 된다는 말을 들은 뒤에만** 커밋한다.
- push, 이슈 생성, PR 생성도 마찬가지다. 초안을 먼저 보여주고 확인받는다.
- 이슈·PR 제목 형식은 `pr-conventions.md` 를 따른다.
