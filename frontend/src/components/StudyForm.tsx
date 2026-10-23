import { useState } from 'react'
import type { FormEvent } from 'react'

import { STUDY_STATUS_LABEL } from '../api/types'
import type { StudyStatus } from '../api/types'
import Button from './Button'
import ErrorMessage from './ErrorMessage'
import SelectField from './SelectField'
import TextArea from './TextArea'
import TextField from './TextField'

export interface StudyFormValues {
  title: string
  content: string
  studyStatus: StudyStatus
}

interface StudyFormProps {
  initialValues: StudyFormValues
  submitLabel: string
  pendingLabel: string
  isPending: boolean
  errorMessage?: string
  // 수정 화면에서만 모집 상태를 바꿀 수 있다
  showStatus: boolean
  onSubmit: (values: StudyFormValues) => void
}

const STATUS_OPTIONS = (Object.keys(STUDY_STATUS_LABEL) as StudyStatus[]).map((value) => ({
  value,
  label: STUDY_STATUS_LABEL[value],
}))

// 작성과 수정이 같은 입력 화면을 쓴다. 값 검증은 서버가 하고, 서버의 안내 문구를 그대로 보여준다
function StudyForm({ initialValues, submitLabel, pendingLabel, isPending, errorMessage, showStatus, onSubmit }: StudyFormProps) {
  const [values, setValues] = useState(initialValues)

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault()
    onSubmit(values)
  }

  return (
    <form onSubmit={handleSubmit} noValidate className="flex flex-col gap-4">
      <TextField
        label="제목 (50자 이하)"
        name="title"
        value={values.title}
        onChange={(event) => setValues((prev) => ({ ...prev, title: event.target.value }))}
      />
      <TextArea
        label="내용 (2000자 이하)"
        name="content"
        rows={10}
        value={values.content}
        onChange={(event) => setValues((prev) => ({ ...prev, content: event.target.value }))}
      />
      {showStatus && (
        <SelectField
          label="모집 상태"
          name="studyStatus"
          value={values.studyStatus}
          onChange={(event) => {
            const next = STATUS_OPTIONS.find((option) => option.value === event.target.value)?.value
            if (next) {
              setValues((prev) => ({ ...prev, studyStatus: next }))
            }
          }}
          options={STATUS_OPTIONS}
        />
      )}
      {errorMessage && <ErrorMessage message={errorMessage} />}
      <Button type="submit" disabled={isPending}>
        {isPending ? pendingLabel : submitLabel}
      </Button>
    </form>
  )
}

export default StudyForm
