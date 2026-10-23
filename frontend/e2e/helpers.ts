import { randomUUID } from 'node:crypto'

import { expect } from '@playwright/test'
import type { APIRequestContext, Page } from '@playwright/test'

export const API_URL = process.env.E2E_API_URL ?? 'http://localhost:8080'

export interface TestUser {
  email: string
  password: string
  name: string
  nickname: string
  generation: number
  part: 'PM' | 'DE' | 'BE' | 'FE'
}

// 테스트 계정은 실행할 때마다 새로 만든다. 이메일이 겹치지 않고, 비밀번호도 코드에 남지 않는다.
// e2e- 로 시작하는 이메일은 테스트가 만든 데이터라는 표시다
export function newTestUser(part: TestUser['part'] = 'BE'): TestUser {
  const id = randomUUID()
  return {
    email: `e2e-${id}@example.com`,
    password: `Pw-${randomUUID()}`,
    name: '이투이',
    nickname: `e2e${id.slice(0, 6)}`,
    generation: 14,
    part,
  }
}

// 글 제목은 검색으로 딱 한 건만 찾을 수 있도록 매번 고유한 값을 붙인다
export function uniqueTitle(prefix: string): string {
  return `${prefix} ${randomUUID().slice(0, 8)}`
}

export async function signupViaUi(page: Page, user: TestUser) {
  await page.goto('/signup')
  await page.getByLabel('이메일', { exact: true }).fill(user.email)
  await page.getByLabel('비밀번호 (8자 이상)', { exact: true }).fill(user.password)
  await page.getByLabel('이름', { exact: true }).fill(user.name)
  await page.getByLabel('닉네임 (2~10자)', { exact: true }).fill(user.nickname)
  await page.getByLabel('기수', { exact: true }).fill(String(user.generation))
  await page.getByLabel('파트', { exact: true }).selectOption(user.part)
  await page.getByRole('button', { name: '가입하기' }).click()
}

export async function loginViaUi(page: Page, user: TestUser) {
  await page.goto('/login')
  await page.getByLabel('이메일', { exact: true }).fill(user.email)
  await page.getByLabel('비밀번호', { exact: true }).fill(user.password)
  await page.getByRole('button', { name: '로그인' }).click()
  await expect(page.getByTestId('header-nickname')).toHaveText(user.nickname)
}

export async function signupAndLoginViaUi(page: Page, user: TestUser) {
  await signupViaUi(page, user)
  await expect(page).toHaveURL(/\/login$/)
  await loginViaUi(page, user)
}

// 화면 흐름이 아닌 준비 단계(다른 사람의 계정, 글, 댓글)는 API 로 빠르게 만든다

export async function signupViaApi(request: APIRequestContext, user: TestUser) {
  const response = await request.post(`${API_URL}/api/auth/signup`, { data: user })
  expect(response.status()).toBe(201)
}

export async function loginViaApi(request: APIRequestContext, user: TestUser): Promise<string> {
  const response = await request.post(`${API_URL}/api/auth/login`, {
    data: { email: user.email, password: user.password },
  })
  expect(response.status()).toBe(200)
  const body = (await response.json()) as { data: { accessToken: string } }
  return body.data.accessToken
}

export async function createUserWithToken(request: APIRequestContext, part: TestUser['part'] = 'BE') {
  const user = newTestUser(part)
  await signupViaApi(request, user)
  return { user, token: await loginViaApi(request, user) }
}

export function bearer(token: string) {
  return { Authorization: `Bearer ${token}` }
}

export async function createPostViaApi(
  request: APIRequestContext,
  token: string,
  title: string,
  content = '주 2회 모여서 같이 풀어요.',
): Promise<number> {
  const response = await request.post(`${API_URL}/api/study-posts`, {
    headers: bearer(token),
    data: { title, content },
  })
  expect(response.status()).toBe(201)
  const body = (await response.json()) as { data: { postId: number } }
  return body.data.postId
}

export async function closePostViaApi(request: APIRequestContext, token: string, postId: number) {
  const response = await request.patch(`${API_URL}/api/study-posts/${postId}`, {
    headers: bearer(token),
    data: { studyStatus: 'CLOSED' },
  })
  expect(response.status()).toBe(200)
}

export async function createCommentViaApi(
  request: APIRequestContext,
  token: string,
  postId: number,
  content: string,
): Promise<number> {
  const response = await request.post(`${API_URL}/api/study-posts/${postId}/comments`, {
    headers: bearer(token),
    data: { content },
  })
  expect(response.status()).toBe(201)
  const body = (await response.json()) as { data: { commentId: number } }
  return body.data.commentId
}
