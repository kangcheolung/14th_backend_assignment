import { Navigate, Outlet, useLocation } from 'react-router-dom'

import { useAuth } from './useAuth'

// 로그인이 필요한 화면을 감싼다. 로그인 전이거나 토큰이 무효가 되어 지워지면 로그인 화면으로 보낸다
function ProtectedRoute() {
  const { isLoggedIn } = useAuth()
  const location = useLocation()

  if (!isLoggedIn) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }
  return <Outlet />
}

export default ProtectedRoute
