import { client } from './client'
import type { LoginPayload, LoginResult, SignupPayload, UserMe } from './types'

// 응답 인터셉터가 공통 응답을 벗겨 주므로 response.data 가 곧 명세의 data 이다

export async function signup(payload: SignupPayload): Promise<number> {
  const response = await client.post<{ userId: number }>('/api/auth/signup', payload)
  return response.data.userId
}

export async function login(payload: LoginPayload): Promise<LoginResult> {
  const response = await client.post<LoginResult>('/api/auth/login', payload)
  return response.data
}

export async function logout(): Promise<void> {
  await client.post('/api/auth/logout')
}

export async function fetchMe(): Promise<UserMe> {
  const response = await client.get<UserMe>('/api/users/me')
  return response.data
}
