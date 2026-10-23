import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'

import { getErrorMessage } from '../api/client'
import { deleteStudyPost, fetchStudyPost, updateStudyPost } from '../api/studyPosts'
import AuthorLabel from '../components/AuthorLabel'
import Button from '../components/Button'
import CommentSection from '../components/CommentSection'
import ErrorMessage from '../components/ErrorMessage'
import LinkButton from '../components/LinkButton'
import LoadingMessage from '../components/LoadingMessage'
import StudyStatusBadge from '../components/StudyStatusBadge'
import { formatDateTime } from '../utils/format'

function StudyDetailPage() {
  const { id } = useParams()
  const postId = Number(id)
  const isValidId = Number.isInteger(postId)
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [isConfirmingDelete, setIsConfirmingDelete] = useState(false)

  const postQuery = useQuery({
    queryKey: ['study-post', postId],
    queryFn: () => fetchStudyPost(postId),
    enabled: isValidId,
  })

  const refreshPost = () => {
    void queryClient.invalidateQueries({ queryKey: ['study-post', postId] })
    void queryClient.invalidateQueries({ queryKey: ['study-posts'] })
  }

  // 모집 마감과 모집 재개는 상태만 바꾸는 수정이다
  const statusMutation = useMutation({
    mutationFn: (studyStatus: 'RECRUITING' | 'CLOSED') => updateStudyPost(postId, { studyStatus }),
    onSuccess: refreshPost,
  })

  const deleteMutation = useMutation({
    mutationFn: () => deleteStudyPost(postId),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['study-posts'] })
      queryClient.removeQueries({ queryKey: ['study-post', postId] })
      queryClient.removeQueries({ queryKey: ['comments', postId] })
      navigate('/studies')
    },
  })

  if (!isValidId) {
    return <ErrorMessage message="올바르지 않은 주소예요." />
  }
  if (postQuery.isPending) {
    return <LoadingMessage />
  }
  if (postQuery.isError) {
    return <ErrorMessage message={getErrorMessage(postQuery.error)} />
  }

  const post = postQuery.data
  const isClosed = post.studyStatus === 'CLOSED'
  const actionError = statusMutation.isError
    ? getErrorMessage(statusMutation.error)
    : deleteMutation.isError
      ? getErrorMessage(deleteMutation.error)
      : undefined

  return (
    <article>
      <div className="mb-2 flex items-center gap-2">
        <StudyStatusBadge status={post.studyStatus} />
        <h1 className="text-2xl font-bold">{post.title}</h1>
      </div>
      <div className="mb-4 flex flex-wrap items-center gap-x-3 text-sm text-gray-500">
        <AuthorLabel author={post.author} />
        <span>{formatDateTime(post.createdAt)}</span>
      </div>
      <p className="whitespace-pre-wrap rounded-md border border-gray-200 bg-white p-4 text-sm text-gray-900">
        {post.content}
      </p>

      {actionError && (
        <div className="mt-3">
          <ErrorMessage message={actionError} />
        </div>
      )}

      {post.isAuthor && (
        <div className="mt-4 flex flex-wrap items-center gap-2">
          <LinkButton variant="secondary" to={`/studies/${post.postId}/edit`}>
            수정
          </LinkButton>
          <Button
            variant="secondary"
            disabled={statusMutation.isPending}
            onClick={() => statusMutation.mutate(isClosed ? 'RECRUITING' : 'CLOSED')}
          >
            {isClosed ? '모집 재개' : '모집 마감'}
          </Button>
          {isConfirmingDelete ? (
            <>
              <span className="text-sm text-red-700">정말 삭제할까요? 댓글도 함께 삭제돼요.</span>
              <Button variant="danger" disabled={deleteMutation.isPending} onClick={() => deleteMutation.mutate()}>
                삭제 확인
              </Button>
              <Button variant="secondary" onClick={() => setIsConfirmingDelete(false)}>
                취소
              </Button>
            </>
          ) : (
            <Button variant="danger" onClick={() => setIsConfirmingDelete(true)}>
              삭제
            </Button>
          )}
        </div>
      )}

      <CommentSection postId={post.postId} isClosed={isClosed} />
    </article>
  )
}

export default StudyDetailPage
