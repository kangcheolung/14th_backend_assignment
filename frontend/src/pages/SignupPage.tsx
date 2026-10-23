import { useMutation } from '@tanstack/react-query'
import { useState } from 'react'
import type { ChangeEvent, FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'

import { signup } from '../api/auth'
import { getErrorMessage } from '../api/client'
import { PART_OPTIONS } from '../api/types'
import Button from '../components/Button'
import ErrorMessage from '../components/ErrorMessage'
import SelectField from '../components/SelectField'
import TextField from '../components/TextField'

const INITIAL_FORM = { email: '', password: '', name: '', nickname: '', generation: '', part: '' }

function SignupPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState(INITIAL_FORM)

  const mutation = useMutation({
    mutationFn: signup,
    onSuccess: () => navigate('/login', { state: { signedUp: true } }),
  })

  const handleChange = (event: ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = event.target
    setForm((prev) => ({ ...prev, [name]: value }))
  }

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault()
    // 값 검증은 서버가 하고, 서버의 안내 문구를 화면에 보여준다
    mutation.mutate({
      email: form.email,
      password: form.password,
      name: form.name,
      nickname: form.nickname,
      generation: form.generation === '' ? undefined : Number(form.generation),
      part: PART_OPTIONS.find((option) => option.value === form.part)?.value,
    })
  }

  return (
    <section className="mx-auto max-w-md">
      <h1 className="mb-4 text-2xl font-bold">회원가입</h1>
      <form onSubmit={handleSubmit} noValidate className="flex flex-col gap-4">
        <TextField label="이메일" name="email" type="email" value={form.email} onChange={handleChange} />
        <TextField
          label="비밀번호 (8자 이상)"
          name="password"
          type="password"
          value={form.password}
          onChange={handleChange}
        />
        <TextField label="이름" name="name" value={form.name} onChange={handleChange} />
        <TextField label="닉네임 (2~10자)" name="nickname" value={form.nickname} onChange={handleChange} />
        <TextField label="기수" name="generation" type="number" value={form.generation} onChange={handleChange} />
        <SelectField
          label="파트"
          name="part"
          value={form.part}
          onChange={handleChange}
          options={PART_OPTIONS}
          placeholder="파트를 선택해 주세요"
        />
        {mutation.isError && <ErrorMessage message={getErrorMessage(mutation.error)} />}
        <Button type="submit" disabled={mutation.isPending}>
          {mutation.isPending ? '가입 중...' : '가입하기'}
        </Button>
      </form>
      <p className="mt-4 text-sm text-gray-600">
        이미 계정이 있나요?{' '}
        <Link to="/login" className="text-indigo-600 hover:underline">
          로그인
        </Link>
      </p>
    </section>
  )
}

export default SignupPage
