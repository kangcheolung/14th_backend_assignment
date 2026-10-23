import { client } from './client'
import type { PageResponse, StudyComment } from './types'

// 응답 인터셉터가 공통 응답을 벗겨 주므로 response.data 가 곧 명세의 data 이다

export async function fetchComments(postId: number, page: number, size: number): Promise<PageResponse<StudyComment>> {
  const response = await client.get<PageResponse<StudyComment>>(`/api/study-posts/${postId}/comments`, {
    params: { page, size },
  })
  return response.data
}

export async function createComment(postId: number, content: string): Promise<number> {
  const response = await client.post<{ commentId: number }>(`/api/study-posts/${postId}/comments`, { content })
  return response.data.commentId
}

export async function updateComment(commentId: number, content: string): Promise<number> {
  const response = await client.patch<{ commentId: number }>(`/api/comments/${commentId}`, { content })
  return response.data.commentId
}

export async function deleteComment(commentId: number): Promise<void> {
  await client.delete(`/api/comments/${commentId}`)
}
