import { STUDY_STATUS_LABEL } from '../api/types'
import type { StudyStatus } from '../api/types'

interface StudyStatusBadgeProps {
  status: StudyStatus
}

const BADGE_CLASS: Record<StudyStatus, string> = {
  RECRUITING: 'bg-green-100 text-green-800',
  CLOSED: 'bg-gray-200 text-gray-700',
}

// 색과 함께 글자로도 상태를 알려 준다
function StudyStatusBadge({ status }: StudyStatusBadgeProps) {
  return (
    <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${BADGE_CLASS[status]}`}>
      {STUDY_STATUS_LABEL[status]}
    </span>
  )
}

export default StudyStatusBadge
