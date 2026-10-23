import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import type { ChangeEvent, FormEvent } from 'react'
import { Link, useSearchParams } from 'react-router-dom'

import { getErrorMessage } from '../api/client'
import { fetchStudyPosts } from '../api/studyPosts'
import { PART_OPTIONS, STUDY_STATUS_LABEL } from '../api/types'
import type { Part, StudyStatus } from '../api/types'
import AuthorLabel from '../components/AuthorLabel'
import Button from '../components/Button'
import ErrorMessage from '../components/ErrorMessage'
import LinkButton from '../components/LinkButton'
import LoadingMessage from '../components/LoadingMessage'
import Pagination from '../components/Pagination'
import SelectField from '../components/SelectField'
import StudyStatusBadge from '../components/StudyStatusBadge'
import TextField from '../components/TextField'
import { formatDateTime } from '../utils/format'

const PAGE_SIZE = 10

const STATUS_OPTIONS = (Object.keys(STUDY_STATUS_LABEL) as StudyStatus[]).map((value) => ({
  value,
  label: STUDY_STATUS_LABEL[value],
}))

// 주소창의 값은 사용자가 바꿀 수 있으므로 허용된 값만 받아들인다
function parseStatus(value: string | null): StudyStatus | undefined {
  return STATUS_OPTIONS.find((option) => option.value === value)?.value
}

function parsePart(value: string | null): Part | undefined {
  return PART_OPTIONS.find((option) => option.value === value)?.value
}

function parsePage(value: string | null): number {
  const page = Number(value)
  return Number.isInteger(page) && page > 0 ? page : 0
}

// 검색 조건을 주소(?keyword=…&status=…&part=…&page=…)에 담아서 새로고침, 뒤로가기, 링크 공유가 되게 한다
function StudyListPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const keyword = searchParams.get('keyword') ?? ''
  const status = parseStatus(searchParams.get('status'))
  const part = parsePart(searchParams.get('part'))
  const page = parsePage(searchParams.get('page'))
  const [keywordInput, setKeywordInput] = useState(keyword)

  const listQuery = useQuery({
    queryKey: ['study-posts', { keyword, status, part, page }],
    queryFn: () => fetchStudyPosts({ keyword: keyword || undefined, status, part, page, size: PAGE_SIZE }),
    placeholderData: keepPreviousData,
  })

  // 조건을 바꾸면 첫 페이지부터 다시 본다
  const updateParams = (changes: Record<string, string | undefined>) => {
    const next = new URLSearchParams(searchParams)
    Object.entries({ page: undefined, ...changes }).forEach(([key, value]) => {
      if (value) {
        next.set(key, value)
      } else {
        next.delete(key)
      }
    })
    setSearchParams(next)
  }

  const handleSearch = (event: FormEvent) => {
    event.preventDefault()
    updateParams({ keyword: keywordInput.trim() || undefined })
  }

  const handleStatusChange = (event: ChangeEvent<HTMLSelectElement>) => {
    updateParams({ status: event.target.value || undefined })
  }

  const handlePartChange = (event: ChangeEvent<HTMLSelectElement>) => {
    updateParams({ part: event.target.value || undefined })
  }

  return (
    <section>
      <div className="mb-4 flex items-center justify-between">
        <h1 className="text-2xl font-bold">스터디 모집</h1>
        <LinkButton to="/studies/new">글쓰기</LinkButton>
      </div>

      <form onSubmit={handleSearch} className="mb-4 grid gap-3 rounded-md border border-gray-200 bg-white p-4 sm:grid-cols-2">
        <div className="sm:col-span-2">
          <TextField
            label="검색어"
            name="keyword"
            value={keywordInput}
            onChange={(event) => setKeywordInput(event.target.value)}
            placeholder="제목이나 내용으로 찾아 보세요"
          />
        </div>
        <SelectField
          label="모집 상태"
          name="status"
          value={status ?? ''}
          onChange={handleStatusChange}
          options={STATUS_OPTIONS}
          placeholder="전체"
        />
        <SelectField
          label="작성자 파트"
          name="part"
          value={part ?? ''}
          onChange={handlePartChange}
          options={PART_OPTIONS}
          placeholder="전체"
        />
        <div className="sm:col-span-2">
          <Button type="submit">검색</Button>
        </div>
      </form>

      {listQuery.isPending && <LoadingMessage />}
      {listQuery.isError && <ErrorMessage message={getErrorMessage(listQuery.error)} />}
      {listQuery.isSuccess &&
        (listQuery.data.content.length === 0 ? (
          <p className="py-8 text-center text-sm text-gray-500">조건에 맞는 모집글이 없어요.</p>
        ) : (
          <>
            <ul className="flex flex-col gap-3">
              {listQuery.data.content.map((post) => (
                <li key={post.postId}>
                  <Link
                    to={`/studies/${post.postId}`}
                    className="block rounded-md border border-gray-200 bg-white p-4 hover:border-indigo-300 focus-visible:outline-2 focus-visible:outline-indigo-600"
                  >
                    <div className="mb-1 flex items-center gap-2">
                      <StudyStatusBadge status={post.studyStatus} />
                      <h2 className="font-medium text-gray-900">{post.title}</h2>
                    </div>
                    <div className="flex flex-wrap items-center gap-x-3 text-sm text-gray-500">
                      <AuthorLabel author={post.author} />
                      <span>댓글 {post.commentCount}</span>
                      <span>{formatDateTime(post.createdAt)}</span>
                    </div>
                  </Link>
                </li>
              ))}
            </ul>
            <Pagination
              page={listQuery.data.page}
              totalPages={listQuery.data.totalPages}
              onPageChange={(nextPage) => {
                const next = new URLSearchParams(searchParams)
                next.set('page', String(nextPage))
                setSearchParams(next)
              }}
            />
          </>
        ))}
    </section>
  )
}

export default StudyListPage
