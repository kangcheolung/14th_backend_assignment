export type ButtonVariant = 'primary' | 'secondary' | 'danger'

export const BUTTON_BASE_CLASS =
  'inline-block rounded-md px-4 py-2 text-sm font-medium focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600 disabled:cursor-not-allowed disabled:opacity-50'

// 클래스 이름을 조립하지 않고 완성된 문자열을 고른다 (Tailwind 가 인식할 수 있게)
export const BUTTON_VARIANT_CLASS: Record<ButtonVariant, string> = {
  primary: 'bg-indigo-600 text-white hover:bg-indigo-700',
  secondary: 'border border-gray-300 bg-white text-gray-700 hover:bg-gray-50',
  danger: 'bg-red-600 text-white hover:bg-red-700',
}
