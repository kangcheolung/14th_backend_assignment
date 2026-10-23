import { Link } from 'react-router-dom'

function NotFoundPage() {
  return (
    <section className="py-12 text-center">
      <h1 className="mb-2 text-2xl font-bold">페이지를 찾을 수 없어요</h1>
      <Link to="/studies" className="text-sm text-indigo-600 hover:underline">
        스터디 목록으로 가기
      </Link>
    </section>
  )
}

export default NotFoundPage
