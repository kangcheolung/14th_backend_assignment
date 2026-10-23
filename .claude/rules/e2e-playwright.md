---
paths:
  - "e2e/**"
  - "**/e2e/**"
  - "**/*.spec.ts"
  - "playwright.config.*"
---

# Playwright E2E 규칙

- E2E 는 **핵심 사용자 흐름**만 다룬다. (가입 → 로그인 → 주요 기능 → 로그아웃) 모든 경우의 수는 단위 테스트가 맡는다.
- 요소는 `getByRole`, `getByLabel`, `getByText` 로 찾고, 안 되면 `data-testid` 를 쓴다. CSS 선택자나 XPath 에 기대지 않는다.
- Playwright 의 자동 대기를 쓴다. `waitForTimeout` 같은 고정 대기는 쓰지 않는다.
- 테스트 한 개는 독립적으로 돈다. 이메일처럼 중복되면 안 되는 값은 매번 새로 만들고(타임스탬프 등), 만든 데이터에 기대어 다음 테스트를 돌리지 않는다.
- 정상 흐름과 에러 흐름(잘못된 입력, 권한 없음)을 각각 하나 이상 넣는다.
- `playwright.config` 에 `baseURL` 과 프론트 `webServer` 를 두고, 백엔드는 먼저 떠 있어야 한다. 기동 방법은 프로젝트 `CLAUDE.md` 에 적는다.
- 실패했을 때 원인을 볼 수 있게 `trace: 'on-first-retry'`, `screenshot: 'only-on-failure'` 를 켠다.
- 테스트 계정·비밀번호는 환경변수나 시드로 주입한다. 코드에 쓰지 않는다.
- 실행: `npx playwright test`, 리포트: `npx playwright show-report`
