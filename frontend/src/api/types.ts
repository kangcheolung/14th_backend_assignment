export type Part = 'PM' | 'DE' | 'BE' | 'FE'

export const PART_OPTIONS: { value: Part; label: string }[] = [
  { value: 'PM', label: '기획 (PM)' },
  { value: 'DE', label: '디자인 (DE)' },
  { value: 'BE', label: '백엔드 (BE)' },
  { value: 'FE', label: '프론트엔드 (FE)' },
]

export type StudyStatus = 'RECRUITING' | 'CLOSED'

export const STUDY_STATUS_LABEL: Record<StudyStatus, string> = {
  RECRUITING: '모집 중',
  CLOSED: '모집 마감',
}

// 서버의 공통 성공 응답. 명세의 응답 예시는 data 안쪽만 적은 것이다
export interface ApiResponse<T> {
  success: boolean
  status: number
  data: T
}

// 서버의 공통 에러 응답. 화면에는 message 를 보여준다
export interface ApiErrorBody {
  code: string
  message: string
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface Author {
  nickname: string
  generation: number
  part: Part
}

export interface SignupPayload {
  email: string
  password: string
  name: string
  nickname: string
  generation?: number
  part?: Part
}

export interface LoginPayload {
  email: string
  password: string
}

export interface LoginResult {
  accessToken: string
  tokenType: string
}

export interface UserMe {
  userId: number
  email: string
  name: string
  nickname: string
  generation: number
  part: Part
}

export interface StudyPostSummary {
  postId: number
  title: string
  studyStatus: StudyStatus
  author: Author
  commentCount: number
  createdAt: string
}

export interface StudyPostDetail {
  postId: number
  title: string
  content: string
  studyStatus: StudyStatus
  author: Author
  isAuthor: boolean
  createdAt: string
  updatedAt: string
}

export interface StudyPostListParams {
  keyword?: string
  status?: StudyStatus
  part?: Part
  page: number
  size: number
}

export interface StudyPostCreatePayload {
  title: string
  content: string
}

export interface StudyPostUpdatePayload {
  title?: string
  content?: string
  studyStatus?: StudyStatus
}

export interface StudyComment {
  commentId: number
  content: string
  author: Author
  isAuthor: boolean
  createdAt: string
}
