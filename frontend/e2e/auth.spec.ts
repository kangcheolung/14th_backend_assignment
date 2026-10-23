import { expect, test } from '@playwright/test'

import { API_URL, bearer, loginViaUi, newTestUser, signupAndLoginViaUi, signupViaApi, signupViaUi } from './helpers.ts'

test.describe('인증', () => {
  test('가입 → 로그인 → 헤더 닉네임 → 마이페이지의 기수·파트 → 로그아웃', async ({ page }) => {
    const user = newTestUser('FE')

    await signupViaUi(page, user)
    await expect(page).toHaveURL(/\/login$/)
    await expect(page.getByText('가입이 완료됐어요. 로그인해 주세요.')).toBeVisible()

    await loginViaUi(page, user)
    await expect(page).toHaveURL(/\/studies$/)

    await page.getByRole('link', { name: '마이페이지' }).click()
    await expect(page.getByRole('heading', { name: '마이페이지' })).toBeVisible()
    await expect(page.getByText(user.email)).toBeVisible()
    await expect(page.getByText('14기')).toBeVisible()
    await expect(page.getByText('프론트엔드 (FE)')).toBeVisible()

    await page.getByRole('button', { name: '로그아웃' }).click()
    await expect(page).toHaveURL(/\/login$/)
    // 로그인 화면 본문에도 회원가입 링크가 있어서 헤더 안에서만 찾는다
    await expect(page.getByRole('navigation').getByRole('link', { name: '회원가입' })).toBeVisible()
    await expect(page.getByTestId('header-nickname')).toHaveCount(0)
  })

  test('이미 가입된 이메일로 가입하면 서버의 에러 메시지를 보여준다', async ({ page, request }) => {
    const user = newTestUser()
    await signupViaApi(request, user)

    await signupViaUi(page, user)

    await expect(page.getByRole('alert')).toHaveText('이미 가입된 이메일이에요.')
    await expect(page).toHaveURL(/\/signup$/)
  })

  test('기수에 0 을 넣으면 기수 안내 메시지를 보여준다', async ({ page }) => {
    const user = { ...newTestUser(), generation: 0 }

    await signupViaUi(page, user)

    await expect(page.getByRole('alert')).toHaveText('기수는 1 이상이어야 해요.')
  })

  test('파트를 고르지 않으면 파트 안내 메시지를 보여준다', async ({ page }) => {
    const user = newTestUser()

    await page.goto('/signup')
    await page.getByLabel('이메일', { exact: true }).fill(user.email)
    await page.getByLabel('비밀번호 (8자 이상)', { exact: true }).fill(user.password)
    await page.getByLabel('이름', { exact: true }).fill(user.name)
    await page.getByLabel('닉네임 (2~10자)', { exact: true }).fill(user.nickname)
    await page.getByLabel('기수', { exact: true }).fill('14')
    await page.getByRole('button', { name: '가입하기' }).click()

    await expect(page.getByRole('alert')).toHaveText('파트를 선택해 주세요.')
  })

  test('비밀번호가 틀리면 로그인에 실패하고 에러 메시지를 보여준다', async ({ page, request }) => {
    const user = newTestUser()
    await signupViaApi(request, user)

    await page.goto('/login')
    await page.getByLabel('이메일', { exact: true }).fill(user.email)
    await page.getByLabel('비밀번호', { exact: true }).fill(`${user.password}-wrong`)
    await page.getByRole('button', { name: '로그인' }).click()

    await expect(page.getByRole('alert')).toHaveText('이메일 또는 비밀번호가 올바르지 않아요.')
    await expect(page.getByTestId('header-nickname')).toHaveCount(0)
  })

  test('로그인하지 않고 마이페이지에 들어가면 로그인 화면으로 보낸다', async ({ page }) => {
    await page.goto('/mypage')

    await expect(page).toHaveURL(/\/login$/)
  })

  test('로그아웃한 토큰은 서버가 더 이상 받아주지 않는다', async ({ page, request }) => {
    const user = newTestUser()
    await signupAndLoginViaUi(page, user)
    const token = await page.evaluate(() => localStorage.getItem('accessToken'))
    expect(token).not.toBeNull()

    const before = await request.get(`${API_URL}/api/users/me`, { headers: bearer(token as string) })
    expect(before.status()).toBe(200)

    await page.getByRole('button', { name: '로그아웃' }).click()
    await expect(page).toHaveURL(/\/login$/)

    const after = await request.get(`${API_URL}/api/users/me`, { headers: bearer(token as string) })
    expect(after.status()).toBe(401)
  })
})
