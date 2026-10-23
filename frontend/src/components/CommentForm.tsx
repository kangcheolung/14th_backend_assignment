import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import type { FormEvent } from 'react'

import { createComment } from '../api/comments'
import { getErrorMessage } from '../api/client'
import Button from './Button'
import ErrorMessage from './ErrorMessage'
import TextArea from './TextArea'

interface CommentFormProps {
  postId: number
}

// 댓글을 쓰는 것이 곧 스터디 신청이다
function CommentForm({ postId }: CommentFormProps) {
  const queryClient = useQueryClient()
  const [content, setContent] = useState('')

  const mutation = useMutation({
    mutationFn: () => createComment(postId, content),
    onSuccess: () => {
      setContent('')
      void queryClient.invalidateQueries({ queryKey: ['comments', postId] })
      void queryClient.invalidateQueries({ queryKey: ['study-posts'] }) // 목록의 댓글 수
    },
  })

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault()
    mutation.mutate()
  }

  return (
    <form onSubmit={handleSubmit} noValidate className="mb-4 flex flex-col gap-3">
      <TextArea
        label="댓글로 스터디 신청하기 (500자 이하)"
        name="comment"
        rows={3}
        value={content}
        onChange={(event) => setContent(event.target.value)}
      />
      {mutation.isError && <ErrorMessage message={getErrorMessage(mutation.error)} />}
      <div>
        <Button type="submit" disabled={mutation.isPending}>
          {mutation.isPending ? '등록 중...' : '댓글 등록'}
        </Button>
      </div>
    </form>
  )
}

export default CommentForm
