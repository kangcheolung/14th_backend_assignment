import { expect, test } from '@playwright/test'

import {
  API_URL,
  bearer,
  closePostViaApi,
  createCommentViaApi,
  createPostViaApi,
  createUserWithToken,
  newTestUser,
  signupAndLoginViaUi,
  uniqueTitle,
} from './helpers.ts'

test.describe('댓글(스터디 신청)', () => {
  test('댓글 작성 → 수정 → 삭제', async ({ page, request }) => {
    const writer = await createUserWithToken(request, 'BE')
    const postId = await createPostViaApi(request, writer.token, uniqueTitle('신청받는 스터디'))
    const applicant = newTestUser('FE')
    await signupAndLoginViaUi(page, applicant)

    await page.goto(`/studies/${postId}`)
    await expect(page.getByText('아직 신청한 사람이 없어요.')).toBeVisible()

    // 작성
    await page.getByLabel('댓글로 스터디 신청하기 (500자 이하)').fill('안녕하세요! 참여하고 싶어요.')
    await page.getByRole('button', { name: '댓글 등록' }).click()
    // 이 글에는 댓글이 하나뿐이다. 수정 모드에서는 본문 글자가 입력창으로 바뀌므로 글자가 아니라 목록 항목으로 찾는다
    const comment = page.getByRole('listitem')
    await expect(comment).toHaveCount(1)
    await expect(comment).toContainText('안녕하세요! 참여하고 싶어요.')
    await expect(comment).toContainText(`${applicant.nickname} · 14기 · FE`)
    await expect(page.getByRole('heading', { name: '댓글 (신청) 1' })).toBeVisible()

    // 수정
    await comment.getByRole('button', { name: '수정' }).click()
    await comment.getByLabel('댓글 수정').fill('수정한 신청 메시지')
    await comment.getByRole('button', { name: '저장' }).click()
    await expect(comment).toContainText('수정한 신청 메시지')
    await expect(comment).not.toContainText('안녕하세요! 참여하고 싶어요.')

    // 삭제
    await comment.getByRole('button', { name: '삭제' }).click()
    await expect(page.getByText('아직 신청한 사람이 없어요.')).toBeVisible()
    await expect(page.getByRole('heading', { name: '댓글 (신청) 0' })).toBeVisible()
  })

  test('내용 없이 댓글을 등록하면 서버의 에러 메시지를 보여준다', async ({ page, request }) => {
    const writer = await createUserWithToken(request, 'BE')
    const postId = await createPostViaApi(request, writer.token, uniqueTitle('신청받는 스터디'))
    await signupAndLoginViaUi(page, newTestUser())

    await page.goto(`/studies/${postId}`)
    await page.getByRole('button', { name: '댓글 등록' }).click()

    await expect(page.getByRole('alert')).toHaveText('내용을 입력해 주세요.')
  })

  test('모집이 마감된 글에는 댓글 입력창 대신 안내가 보이고, 서버도 409 로 거절한다', async ({ page, request }) => {
    const writer = await createUserWithToken(request, 'BE')
    const postId = await createPostViaApi(request, writer.token, uniqueTitle('마감된 스터디'))
    await closePostViaApi(request, writer.token, postId)
    const applicant = await createUserWithToken(request, 'FE')
    await signupAndLoginViaUi(page, newTestUser('FE'))

    await page.goto(`/studies/${postId}`)

    await expect(page.getByText('모집이 마감된 글이라 댓글을 쓸 수 없어요.')).toBeVisible()
    await expect(page.getByRole('button', { name: '댓글 등록' })).toHaveCount(0)

    const response = await request.post(`${API_URL}/api/study-posts/${postId}/comments`, {
      headers: bearer(applicant.token),
      data: { content: '마감 뒤에 신청해 볼게요' },
    })
    expect(response.status()).toBe(409)
    const body = (await response.json()) as { message: string }
    expect(body.message).toBe('모집이 마감된 글에는 댓글을 쓸 수 없어요.')
  })

  test('로그인하지 않으면 댓글 입력창 대신 로그인 안내가 보인다', async ({ page, request }) => {
    const writer = await createUserWithToken(request, 'BE')
    const postId = await createPostViaApi(request, writer.token, uniqueTitle('신청받는 스터디'))
    await createCommentViaApi(request, writer.token, postId, '글쓴이가 먼저 남긴 댓글')

    await page.goto(`/studies/${postId}`)

    await expect(page.getByRole('link', { name: '로그인' }).last()).toBeVisible()
    await expect(page.getByRole('button', { name: '댓글 등록' })).toHaveCount(0)
    // 댓글 목록은 로그인 없이도 보인다
    await expect(page.getByText('글쓴이가 먼저 남긴 댓글')).toBeVisible()
  })

  test('남이 쓴 댓글에는 수정·삭제 버튼이 없고, 서버도 403 으로 거절한다', async ({ page, request }) => {
    const writer = await createUserWithToken(request, 'BE')
    const postId = await createPostViaApi(request, writer.token, uniqueTitle('신청받는 스터디'))
    const commentId = await createCommentViaApi(request, writer.token, postId, '글쓴이의 댓글')
    const other = await createUserWithToken(request, 'FE')
    await signupAndLoginViaUi(page, newTestUser('FE'))

    await page.goto(`/studies/${postId}`)

    const comment = page.getByRole('listitem').filter({ hasText: '글쓴이의 댓글' })
    await expect(comment).toHaveCount(1)
    await expect(comment.getByRole('button', { name: '수정' })).toHaveCount(0)
    await expect(comment.getByRole('button', { name: '삭제' })).toHaveCount(0)

    const patch = await request.patch(`${API_URL}/api/comments/${commentId}`, {
      headers: bearer(other.token),
      data: { content: '가로챈 내용' },
    })
    expect(patch.status()).toBe(403)
    const remove = await request.delete(`${API_URL}/api/comments/${commentId}`, { headers: bearer(other.token) })
    expect(remove.status()).toBe(403)
  })

  test('글을 삭제하면 달린 댓글도 함께 사라진다', async ({ page, request }) => {
    const writer = await createUserWithToken(request, 'BE')
    const postId = await createPostViaApi(request, writer.token, uniqueTitle('지워질 스터디'))
    await createCommentViaApi(request, writer.token, postId, '같이 지워질 댓글')

    const response = await request.delete(`${API_URL}/api/study-posts/${postId}`, { headers: bearer(writer.token) })
    expect(response.status()).toBe(204)

    await page.goto(`/studies/${postId}`)
    await expect(page.getByRole('alert')).toHaveText('존재하지 않는 스터디 모집글이에요.')
    const comments = await request.get(`${API_URL}/api/study-posts/${postId}/comments`)
    expect(comments.status()).toBe(404)
  })
})
