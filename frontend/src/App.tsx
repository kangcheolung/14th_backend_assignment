import { Navigate, Route, Routes } from 'react-router-dom'

import ProtectedRoute from './auth/ProtectedRoute'
import Layout from './components/Layout'
import LoginPage from './pages/LoginPage'
import MyPage from './pages/MyPage'
import NotFoundPage from './pages/NotFoundPage'
import SignupPage from './pages/SignupPage'
import StudyDetailPage from './pages/StudyDetailPage'
import StudyFormPage from './pages/StudyFormPage'
import StudyListPage from './pages/StudyListPage'

function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<Navigate to="/studies" replace />} />
        <Route path="signup" element={<SignupPage />} />
        <Route path="login" element={<LoginPage />} />
        <Route path="studies" element={<StudyListPage />} />
        <Route path="studies/:id" element={<StudyDetailPage />} />
        <Route element={<ProtectedRoute />}>
          <Route path="mypage" element={<MyPage />} />
          <Route path="studies/new" element={<StudyFormPage />} />
          <Route path="studies/:id/edit" element={<StudyFormPage />} />
        </Route>
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}

export default App
