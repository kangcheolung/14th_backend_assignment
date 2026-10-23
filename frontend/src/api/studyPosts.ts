import { client } from './client'
import type {
  PageResponse,
  StudyPostCreatePayload,
  StudyPostDetail,
  StudyPostListParams,
  StudyPostSummary,
  StudyPostUpdatePayload,
} from './types'

// 응답 인터셉터가 공통 응답을 벗겨 주므로 response.data 가 곧 명세의 data 이다

export async function fetchStudyPosts(params: StudyPostListParams): Promise<PageResponse<StudyPostSummary>> {
  const response = await client.get<PageResponse<StudyPostSummary>>('/api/study-posts', { params })
  return response.data
}

export async function fetchStudyPost(postId: number): Promise<StudyPostDetail> {
  const response = await client.get<StudyPostDetail>(`/api/study-posts/${postId}`)
  return response.data
}

export async function createStudyPost(payload: StudyPostCreatePayload): Promise<number> {
  const response = await client.post<{ postId: number }>('/api/study-posts', payload)
  return response.data.postId
}

export async function updateStudyPost(postId: number, payload: StudyPostUpdatePayload): Promise<number> {
  const response = await client.patch<{ postId: number }>(`/api/study-posts/${postId}`, payload)
  return response.data.postId
}

export async function deleteStudyPost(postId: number): Promise<void> {
  await client.delete(`/api/study-posts/${postId}`)
}
