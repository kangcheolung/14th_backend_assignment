import { expect, test } from '@playwright/test'

import {
  createPostViaApi,
  createUserWithToken,
  newTestUser,
  signupAndLoginViaUi,
  signupViaApi,
  uniqueTitle,
} from './helpers.ts'

test.describe('스터디 모집글', () => {
  test('작성 → 검색·필터 → 수정 → 모집 마감 → 삭제', async ({ page }) => {
    const user = newTestUser('BE')
    const title = uniqueTitle('알고리즘 스터디')
    const editedTitle = `${title} (수정)`
    await signupAndLoginViaUi(page, user)

    // 작성
    await page.getByRole('link', { name: '글쓰기' }).click()
    await page.getByLabel('제목 (50자 이하)').fill(title)
    await page.getByLabel('내용 (2000자 이하)').fill('주 2회 백준 풀이를 같이 해요.')
    await page.getByRole('button', { name: '등록하기' }).click()
    await expect(page.getByRole('heading', { level: 1, name: title })).toBeVisible()
    await expect(page.getByText('모집 중', { exact: true })).toBeVisible()

    // 목록에서 키워드 + 상태 + 파트 필터로 찾기
    await page.goto('/studies')
    await page.getByLabel('검색어').fill(title)
    await page.getByLabel('모집 상태').selectOption('RECRUITING')
    await page.getByLabel('작성자 파트').selectOption('BE')
    await page.getByRole('button', { name: '검색' }).click()
    const result = page.getByRole('link', { name: title })
    await expect(result).toHaveCount(1)
    await expect(result).toContainText(user.nickname)

    // 다른 파트로 거르면 사라진다
    await page.getByLabel('작성자 파트').selectOption('FE')
    await expect(page.getByText('조건에 맞는 모집글이 없어요.')).toBeVisible()
    await page.getByLabel('작성자 파트').selectOption('BE')
    await expect(result).toHaveCount(1)

    // 수정
    await result.click()
    await page.getByRole('link', { name: '수정' }).click()
    await expect(page.getByRole('heading', { name: '모집글 수정' })).toBeVisible()
    await page.getByLabel('제목 (50자 이하)').fill(editedTitle)
    await page.getByRole('button', { name: '수정하기' }).click()
    await expect(page.getByRole('heading', { level: 1, name: editedTitle })).toBeVisible()

    // 모집 마감
    await page.getByRole('button', { name: '모집 마감' }).click()
    await expect(page.getByText('모집 마감', { exact: true })).toBeVisible()
    await expect(page.getByRole('button', { name: '모집 재개' })).toBeVisible()

    // 마감 상태 필터로 찾기
    await page.goto('/studies')
    await page.getByLabel('검색어').fill(editedTitle)
    await page.getByLabel('모집 상태').selectOption('CLOSED')
    await page.getByRole('button', { name: '검색' }).click()
    await expect(page.getByRole('link', { name: editedTitle })).toHaveCount(1)

    // 삭제
    await page.getByRole('link', { name: editedTitle }).click()
    await page.getByRole('button', { name: '삭제', exact: true }).click()
    await expect(page.getByText('정말 삭제할까요?')).toBeVisible()
    await page.getByRole('button', { name: '삭제 확인' }).click()
    await expect(page).toHaveURL(/\/studies$/)

    await page.getByLabel('검색어').fill(editedTitle)
    await page.getByRole('button', { name: '검색' }).click()
    await expect(page.getByText('조건에 맞는 모집글이 없어요.')).toBeVisible()
  })

  test('제목 없이 등록하면 서버의 에러 메시지를 보여준다', async ({ page }) => {
    await signupAndLoginViaUi(page, newTestUser())

    await page.goto('/studies/new')
    await page.getByLabel('내용 (2000자 이하)').fill('내용만 있어요.')
    await page.getByRole('button', { name: '등록하기' }).click()

    await expect(page.getByRole('alert')).toHaveText('제목을 입력해 주세요.')
  })

  test('로그인하지 않고 글쓰기에 들어가면 로그인 화면으로 보내고, 로그인하면 글쓰기로 돌아온다', async ({ page, request }) => {
    const user = newTestUser()
    await signupViaApi(request, user)

    await page.goto('/studies/new')
    await expect(page).toHaveURL(/\/login$/)

    await page.getByLabel('이메일', { exact: true }).fill(user.email)
    await page.getByLabel('비밀번호', { exact: true }).fill(user.password)
    await page.getByRole('button', { name: '로그인' }).click()

    await expect(page).toHaveURL(/\/studies\/new$/)
    await expect(page.getByRole('heading', { name: '모집글 쓰기' })).toBeVisible()
  })

  test('남이 쓴 글에는 수정·삭제·마감 버튼이 보이지 않는다', async ({ page, request }) => {
    const writer = await createUserWithToken(request, 'BE')
    const title = uniqueTitle('남의 스터디')
    const postId = await createPostViaApi(request, writer.token, title)
    await signupAndLoginViaUi(page, newTestUser('FE'))

    await page.goto(`/studies/${postId}`)

    await expect(page.getByRole('heading', { level: 1, name: title })).toBeVisible()
    await expect(page.getByRole('link', { name: '수정' })).toHaveCount(0)
    await expect(page.getByRole('button', { name: '모집 마감' })).toHaveCount(0)
    await expect(page.getByRole('button', { name: '삭제', exact: true })).toHaveCount(0)
  })

  test('남이 쓴 글의 수정 주소로 들어가면 작성자만 수정할 수 있다고 알려 준다', async ({ page, request }) => {
    const writer = await createUserWithToken(request, 'BE')
    const postId = await createPostViaApi(request, writer.token, uniqueTitle('남의 스터디'))
    await signupAndLoginViaUi(page, newTestUser('FE'))

    await page.goto(`/studies/${postId}/edit`)

    await expect(page.getByRole('alert')).toHaveText('모집글 작성자만 수정할 수 있어요.')
  })

  test('없는 글을 열면 서버의 에러 메시지를 보여준다', async ({ page }) => {
    await page.goto('/studies/999999999')

    await expect(page.getByRole('alert')).toHaveText('존재하지 않는 스터디 모집글이에요.')
  })
})
