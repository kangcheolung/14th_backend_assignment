import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import { Link } from 'react-router-dom'

import { fetchComments } from '../api/comments'
import { getErrorMessage } from '../api/client'
import { useAuth } from '../auth/useAuth'
import CommentForm from './CommentForm'
import CommentItem from './CommentItem'
import ErrorMessage from './ErrorMessage'
import LoadingMessage from './LoadingMessage'
import Pagination from './Pagination'

const COMMENT_PAGE_SIZE = 20

interface CommentSectionProps {
  postId: number
  isClosed: boolean
}

// 모집글의 댓글(스터디 신청) 영역. 마감된 글에는 새 댓글을 쓸 수 없다
function CommentSection({ postId, isClosed }: CommentSectionProps) {
  const { isLoggedIn } = useAuth()
  const [page, setPage] = useState(0)

  const commentsQuery = useQuery({
    queryKey: ['comments', postId, page],
    queryFn: () => fetchComments(postId, page, COMMENT_PAGE_SIZE),
    placeholderData: keepPreviousData,
  })

  const total = commentsQuery.data?.totalElements ?? 0

  return (
    <section aria-labelledby="comments-heading" className="mt-8">
      <h2 id="comments-heading" className="mb-3 text-lg font-bold">
        댓글 (신청) {total}
      </h2>

      {isClosed ? (
        <p className="mb-4 rounded-md bg-gray-100 px-3 py-2 text-sm text-gray-700">
          모집이 마감된 글이라 댓글을 쓸 수 없어요.
        </p>
      ) : isLoggedIn ? (
        <CommentForm postId={postId} />
      ) : (
        <p className="mb-4 rounded-md bg-gray-100 px-3 py-2 text-sm text-gray-700">
          <Link to="/login" state={{ from: `/studies/${postId}` }} className="text-indigo-600 hover:underline">
            로그인
          </Link>
          하고 댓글로 스터디에 신청해 보세요.
        </p>
      )}

      {commentsQuery.isPending && <LoadingMessage />}
      {commentsQuery.isError && <ErrorMessage message={getErrorMessage(commentsQuery.error)} />}
      {commentsQuery.isSuccess &&
        (commentsQuery.data.content.length === 0 ? (
          <p className="py-4 text-center text-sm text-gray-500">아직 신청한 사람이 없어요.</p>
        ) : (
          <>
            <ul className="flex flex-col gap-3">
              {commentsQuery.data.content.map((comment) => (
                <CommentItem key={comment.commentId} postId={postId} comment={comment} />
              ))}
            </ul>
            <Pagination page={commentsQuery.data.page} totalPages={commentsQuery.data.totalPages} onPageChange={setPage} />
          </>
        ))}
    </section>
  )
}

export default CommentSection
