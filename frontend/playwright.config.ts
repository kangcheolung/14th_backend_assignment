import { defineConfig, devices } from '@playwright/test'

const FRONTEND_URL = 'http://localhost:3000'

// 백엔드(8080)와 MySQL 은 먼저 떠 있어야 한다. 기동 방법은 루트 CLAUDE.md 에 있다.
// 프론트 개발 서버는 떠 있지 않으면 Playwright 가 직접 띄운다.
export default defineConfig({
  testDir: './e2e',
  // 테스트가 같은 DB 를 쓰므로 한 번에 하나씩 돌린다. 데이터는 테스트마다 새로 만든 값을 쓴다
  fullyParallel: false,
  workers: 1,
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL: FRONTEND_URL,
    locale: 'ko-KR',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  webServer: {
    command: 'npm run dev',
    url: FRONTEND_URL,
    reuseExistingServer: true,
  },
})
