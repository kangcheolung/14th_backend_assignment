import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { isAxiosError } from 'axios'
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'

import App from './App.tsx'
import './index.css'

const MAX_RETRY_COUNT = 2

// 서버가 "없다(404)", "권한 없다(403)", "로그인 필요(401)" 처럼 요청 자체를 거절한 경우는 다시 물어봐도 같은 답이라 재시도하지 않는다.
// 네트워크 오류나 5xx 처럼 잠깐의 문제일 수 있는 경우만 재시도한다
function shouldRetry(failureCount: number, error: unknown): boolean {
  if (isAxiosError(error) && error.response && error.response.status < 500) {
    return false
  }
  return failureCount < MAX_RETRY_COUNT
}

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: shouldRetry } },
})

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </QueryClientProvider>
  </StrictMode>,
)
