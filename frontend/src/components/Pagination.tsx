import Button from './Button'

interface PaginationProps {
  page: number
  totalPages: number
  onPageChange: (page: number) => void
}

// page 는 서버와 같이 0부터 시작하고, 화면에는 1부터 보여준다
function Pagination({ page, totalPages, onPageChange }: PaginationProps) {
  if (totalPages <= 1) {
    return null
  }
  return (
    <nav aria-label="페이지 이동" className="mt-4 flex items-center justify-center gap-3">
      <Button variant="secondary" disabled={page <= 0} onClick={() => onPageChange(page - 1)}>
        이전
      </Button>
      <span className="text-sm text-gray-600">
        {page + 1} / {totalPages}
      </span>
      <Button variant="secondary" disabled={page + 1 >= totalPages} onClick={() => onPageChange(page + 1)}>
        다음
      </Button>
    </nav>
  )
}

export default Pagination
