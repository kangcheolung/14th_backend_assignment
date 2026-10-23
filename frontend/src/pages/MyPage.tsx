import { getErrorMessage } from '../api/client'
import { PART_OPTIONS } from '../api/types'
import { useAuth } from '../auth/useAuth'
import ErrorMessage from '../components/ErrorMessage'
import LoadingMessage from '../components/LoadingMessage'

function MyPage() {
  const { meQuery } = useAuth()

  if (meQuery.isPending) {
    return <LoadingMessage />
  }
  if (meQuery.isError) {
    return <ErrorMessage message={getErrorMessage(meQuery.error)} />
  }

  const me = meQuery.data
  const partLabel = PART_OPTIONS.find((option) => option.value === me.part)?.label ?? me.part
  const rows = [
    { label: '이메일', value: me.email },
    { label: '이름', value: me.name },
    { label: '닉네임', value: me.nickname },
    { label: '기수', value: `${me.generation}기` },
    { label: '파트', value: partLabel },
  ]

  return (
    <section>
      <h1 className="mb-4 text-2xl font-bold">마이페이지</h1>
      <dl className="divide-y divide-gray-200 rounded-md border border-gray-200 bg-white">
        {rows.map((row) => (
          <div key={row.label} className="flex gap-4 px-4 py-3 text-sm">
            <dt className="w-20 shrink-0 font-medium text-gray-500">{row.label}</dt>
            <dd className="text-gray-900">{row.value}</dd>
          </div>
        ))}
      </dl>
    </section>
  )
}

export default MyPage
