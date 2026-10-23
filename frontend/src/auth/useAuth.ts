import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useSyncExternalStore } from 'react'

import { fetchMe, logout as requestLogout } from '../api/auth'
import { getToken, removeToken, setToken, subscribeToken } from './tokenStorage'

export const ME_QUERY_KEY = ['me'] as const

// 로그인 상태와 내 정보를 한곳에서 다룬다. 토큰이 있을 때만 내 정보를 서버에서 가져온다
export function useAuth() {
  const token = useSyncExternalStore(subscribeToken, getToken)
  const queryClient = useQueryClient()
  const meQuery = useQuery({
    queryKey: ME_QUERY_KEY,
    queryFn: fetchMe,
    enabled: token !== null,
    retry: false,
  })

  const login = (accessToken: string) => {
    setToken(accessToken)
    void queryClient.invalidateQueries({ queryKey: ME_QUERY_KEY })
  }

  const logout = async () => {
    try {
      await requestLogout()
    } finally {
      // 서버 호출이 실패해도 이 브라우저에서는 로그아웃 상태로 만든다
      removeToken()
      queryClient.clear()
    }
  }

  return { isLoggedIn: token !== null, meQuery, login, logout }
}
