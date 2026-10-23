import axios from 'axios'

import { getToken, removeToken } from '../auth/tokenStorage'
import type { ApiErrorBody, ApiResponse } from './types'

const DEFAULT_API_URL = 'http://localhost:8080'
const FALLBACK_ERROR_MESSAGE = '요청을 처리하지 못했어요. 잠시 후 다시 시도해 주세요.'

export const client = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? DEFAULT_API_URL,
})

client.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

client.interceptors.response.use(
  (response) => {
    // 서버의 공통 응답({ success, status, data })을 여기서 한 번만 벗겨 data 만 돌려준다.
    // 본문이 없는 204 응답은 그대로 둔다
    const body: unknown = response.data
    if (typeof body === 'object' && body !== null && 'success' in body && 'data' in body) {
      response.data = (body as ApiResponse<unknown>).data
    }
    return response
  },
  (error: unknown) => {
    // 토큰을 보냈는데 401 이면 만료되었거나 로그아웃된 토큰이다. 지우면 화면이 로그인 상태에서 풀린다
    if (axios.isAxiosError(error) && error.response?.status === 401 && error.config?.headers.Authorization) {
      removeToken()
    }
    return Promise.reject(error)
  },
)

// 서버가 내려준 에러 메시지가 있으면 그대로 보여주고, 없으면 기본 문구를 쓴다
export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError<ApiErrorBody>(error)) {
    return error.response?.data?.message ?? FALLBACK_ERROR_MESSAGE
  }
  return FALLBACK_ERROR_MESSAGE
}
