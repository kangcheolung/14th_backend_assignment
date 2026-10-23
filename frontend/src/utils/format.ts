// 서버가 내려준 "2026-11-08T12:00:00" 형식의 시각을 화면용 문자열로 바꾼다
export function formatDateTime(value: string): string {
  return new Date(value).toLocaleString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}
