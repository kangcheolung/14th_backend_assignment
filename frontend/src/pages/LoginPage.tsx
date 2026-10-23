import { useMutation } from '@tanstack/react-query'
import { useState } from 'react'
import type { ChangeEvent, FormEvent } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'

import { login as requestLogin } from '../api/auth'
import { getErrorMessage } from '../api/client'
import { useAuth } from '../auth/useAuth'
import Button from '../components/Button'
import ErrorMessage from '../components/ErrorMessage'
import TextField from '../components/TextField'

interface LoginLocationState {
  from?: string
  signedUp?: boolean
}

const DEFAULT_REDIRECT = '/studies'

function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { login } = useAuth()
  const state = location.state as LoginLocationState | null
  const [form, setForm] = useState({ email: '', password: '' })

  const mutation = useMutation({
    mutationFn: requestLogin,
    onSuccess: (result) => {
      login(result.accessToken)
      navigate(state?.from ?? DEFAULT_REDIRECT, { replace: true })
    },
  })

  const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = event.target
    setForm((prev) => ({ ...prev, [name]: value }))
  }

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault()
    mutation.mutate(form)
  }

  return (
    <section className="mx-auto max-w-md">
      <h1 className="mb-4 text-2xl font-bold">로그인</h1>
      {state?.signedUp && (
        <p role="status" className="mb-4 rounded-md bg-green-50 px-3 py-2 text-sm text-green-700">
          가입이 완료됐어요. 로그인해 주세요.
        </p>
      )}
      <form onSubmit={handleSubmit} noValidate className="flex flex-col gap-4">
        <TextField label="이메일" name="email" type="email" value={form.email} onChange={handleChange} />
        <TextField label="비밀번호" name="password" type="password" value={form.password} onChange={handleChange} />
        {mutation.isError && <ErrorMessage message={getErrorMessage(mutation.error)} />}
        <Button type="submit" disabled={mutation.isPending}>
          {mutation.isPending ? '로그인 중...' : '로그인'}
        </Button>
      </form>
      <p className="mt-4 text-sm text-gray-600">
        아직 계정이 없나요?{' '}
        <Link to="/signup" className="text-indigo-600 hover:underline">
          회원가입
        </Link>
      </p>
    </section>
  )
}

export default LoginPage
