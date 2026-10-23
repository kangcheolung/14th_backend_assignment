import { Link, useNavigate } from 'react-router-dom'

import { useAuth } from '../auth/useAuth'
import Button from './Button'

const LINK_CLASS = 'text-sm text-gray-700 hover:text-indigo-600 focus-visible:outline-2 focus-visible:outline-indigo-600'

// 로그인 상태에 따라 메뉴가 달라진다
function Header() {
  const { isLoggedIn, meQuery, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = async () => {
    await logout()
    navigate('/login')
  }

  return (
    <header className="border-b border-gray-200 bg-white">
      <nav className="mx-auto flex max-w-3xl flex-wrap items-center justify-between gap-2 px-4 py-3">
        <Link to="/studies" className="text-lg font-bold text-indigo-600">
          코테이토 스터디
        </Link>
        <div className="flex items-center gap-3">
          {isLoggedIn ? (
            <>
              <span data-testid="header-nickname" className="text-sm font-medium text-gray-900">
                {meQuery.data?.nickname}
              </span>
              <Link to="/mypage" className={LINK_CLASS}>
                마이페이지
              </Link>
              <Button variant="secondary" onClick={handleLogout}>
                로그아웃
              </Button>
            </>
          ) : (
            <>
              <Link to="/login" className={LINK_CLASS}>
                로그인
              </Link>
              <Link to="/signup" className={LINK_CLASS}>
                회원가입
              </Link>
            </>
          )}
        </div>
      </nav>
    </header>
  )
}

export default Header
