import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useNavigate, useParams } from 'react-router-dom'

import { getErrorMessage } from '../api/client'
import { createStudyPost, fetchStudyPost, updateStudyPost } from '../api/studyPosts'
import ErrorMessage from '../components/ErrorMessage'
import LoadingMessage from '../components/LoadingMessage'
import StudyForm from '../components/StudyForm'
import type { StudyFormValues } from '../components/StudyForm'

const EMPTY_VALUES: StudyFormValues = { title: '', content: '', studyStatus: 'RECRUITING' }

// /studies/new 는 작성, /studies/:id/edit 는 수정이다. 둘 다 로그인이 필요하다 (ProtectedRoute)
function StudyFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const postId = id === undefined ? undefined : Number(id)
  const isEdit = postId !== undefined

  const postQuery = useQuery({
    queryKey: ['study-post', postId],
    queryFn: () => fetchStudyPost(postId as number),
    enabled: isEdit && Number.isInteger(postId),
  })

  const saveMutation = useMutation({
    mutationFn: (values: StudyFormValues) =>
      isEdit
        ? updateStudyPost(postId, values)
        : createStudyPost({ title: values.title, content: values.content }),
    onSuccess: (savedPostId) => {
      void queryClient.invalidateQueries({ queryKey: ['study-posts'] })
      void queryClient.invalidateQueries({ queryKey: ['study-post', savedPostId] })
      navigate(`/studies/${savedPostId}`)
    },
  })

  if (isEdit && !Number.isInteger(postId)) {
    return <ErrorMessage message="올바르지 않은 주소예요." />
  }

  if (!isEdit) {
    return (
      <section>
        <h1 className="mb-4 text-2xl font-bold">모집글 쓰기</h1>
        <StudyForm
          initialValues={EMPTY_VALUES}
          submitLabel="등록하기"
          pendingLabel="등록 중..."
          isPending={saveMutation.isPending}
          errorMessage={saveMutation.isError ? getErrorMessage(saveMutation.error) : undefined}
          showStatus={false}
          onSubmit={(values) => saveMutation.mutate(values)}
        />
      </section>
    )
  }

  if (postQuery.isPending) {
    return <LoadingMessage />
  }
  if (postQuery.isError) {
    return <ErrorMessage message={getErrorMessage(postQuery.error)} />
  }
  if (!postQuery.data.isAuthor) {
    return <ErrorMessage message="모집글 작성자만 수정할 수 있어요." />
  }

  const post = postQuery.data
  return (
    <section>
      <h1 className="mb-4 text-2xl font-bold">모집글 수정</h1>
      <StudyForm
        key={post.postId}
        initialValues={{ title: post.title, content: post.content, studyStatus: post.studyStatus }}
        submitLabel="수정하기"
        pendingLabel="수정 중..."
        isPending={saveMutation.isPending}
        errorMessage={saveMutation.isError ? getErrorMessage(saveMutation.error) : undefined}
        showStatus
        onSubmit={(values) => saveMutation.mutate(values)}
      />
    </section>
  )
}

export default StudyFormPage
