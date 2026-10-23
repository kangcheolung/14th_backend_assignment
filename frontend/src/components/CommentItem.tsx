import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import type { FormEvent } from 'react'

import { deleteComment, updateComment } from '../api/comments'
import { getErrorMessage } from '../api/client'
import type { StudyComment } from '../api/types'
import { formatDateTime } from '../utils/format'
import AuthorLabel from './AuthorLabel'
import Button from './Button'
import ErrorMessage from './ErrorMessage'
import TextArea from './TextArea'

interface CommentItemProps {
  postId: number
  comment: StudyComment
}

// 댓글 한 개. 내가 쓴 댓글(isAuthor)에만 수정, 삭제 버튼이 보인다
function CommentItem({ postId, comment }: CommentItemProps) {
  const queryClient = useQueryClient()
  const [isEditing, setIsEditing] = useState(false)
  const [draft, setDraft] = useState(comment.content)

  const refresh = () => {
    void queryClient.invalidateQueries({ queryKey: ['comments', postId] })
    void queryClient.invalidateQueries({ queryKey: ['study-posts'] }) // 목록의 댓글 수
  }

  const updateMutation = useMutation({
    mutationFn: () => updateComment(comment.commentId, draft),
    onSuccess: () => {
      setIsEditing(false)
      refresh()
    },
  })

  const deleteMutation = useMutation({
    mutationFn: () => deleteComment(comment.commentId),
    onSuccess: refresh,
  })

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault()
    updateMutation.mutate()
  }

  const handleCancel = () => {
    setDraft(comment.content)
    setIsEditing(false)
    updateMutation.reset()
  }

  const errorMessage = updateMutation.isError
    ? getErrorMessage(updateMutation.error)
    : deleteMutation.isError
      ? getErrorMessage(deleteMutation.error)
      : undefined

  return (
    <li className="rounded-md border border-gray-200 bg-white p-4">
      <div className="mb-2 flex flex-wrap items-center justify-between gap-2">
        <AuthorLabel author={comment.author} />
        <span className="text-xs text-gray-500">{formatDateTime(comment.createdAt)}</span>
      </div>

      {isEditing ? (
        <form onSubmit={handleSubmit} noValidate className="flex flex-col gap-3">
          <TextArea label="댓글 수정" rows={3} value={draft} onChange={(event) => setDraft(event.target.value)} />
          <div className="flex gap-2">
            <Button type="submit" disabled={updateMutation.isPending}>
              저장
            </Button>
            <Button variant="secondary" onClick={handleCancel}>
              취소
            </Button>
          </div>
        </form>
      ) : (
        <p className="whitespace-pre-wrap text-sm text-gray-900">{comment.content}</p>
      )}

      {errorMessage && (
        <div className="mt-2">
          <ErrorMessage message={errorMessage} />
        </div>
      )}

      {comment.isAuthor && !isEditing && (
        <div className="mt-3 flex gap-2">
          <Button variant="secondary" onClick={() => setIsEditing(true)}>
            수정
          </Button>
          <Button variant="danger" disabled={deleteMutation.isPending} onClick={() => deleteMutation.mutate()}>
            삭제
          </Button>
        </div>
      )}
    </li>
  )
}

export default CommentItem
