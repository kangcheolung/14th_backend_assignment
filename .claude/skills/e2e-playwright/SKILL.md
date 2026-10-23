---
name: e2e-playwright
description: 프론트와 백엔드를 연동한 주요 사용자 흐름의 Playwright E2E 테스트를 구성하고 작성하고 실행한다. "e2e 돌려줘", "playwright 구성", "프론트 연동 검증"처럼 화면에서 백엔드까지 이어진 흐름을 검증할 때 사용한다. 단위 테스트에는 사용하지 않는다.
argument-hint: "[검증할 사용자 흐름]"
---

# Playwright E2E

규칙은 `~/.claude/rules/e2e-playwright.md` 를 따른다.

## 절차
1. **준비 확인**: `package.json` 에 `@playwright/test` 가 있는지, `npx playwright --version` 이 되는지 확인한다.
   - 없으면 설치(`npm i -D @playwright/test`, `npx playwright install`)가 필요하다고 알리고 **확인받은 뒤** 진행한다.
2. **환경 확인**: 백엔드 기동 방법과 포트, 프론트 개발 서버 포트를 프로젝트 `CLAUDE.md` 에서 찾는다. 없으면 묻는다. `playwright.config` 에 `baseURL`, `webServer` 를 맞춘다.
3. **시나리오 합의**: 핵심 흐름 1~3개를 정상/에러로 나눠 목록으로 보여주고 확인받는다.
4. **작성**: `e2e/*.spec.ts`. 역할 기반 로케이터를 쓰고 고정 대기는 쓰지 않는다. 중복되면 안 되는 값은 매번 새로 만든다.
5. **실행**: 백엔드와 프론트가 떠 있는 상태에서 `npx playwright test`. 서버를 켜지 못했으면 그렇게 보고한다.
6. **실패 분석**: trace, screenshot 으로 원인을 구분한다. 앱 버그인지, 테스트가 잘못된 건지, 환경 문제인지 가른다.
7. **보고**: 시나리오별 결과와 `npx playwright show-report` 안내를 적는다.

## 지키는 것
- 테스트 계정과 비밀번호는 환경변수나 시드로 넣고 코드나 리포트에 남기지 않는다.
