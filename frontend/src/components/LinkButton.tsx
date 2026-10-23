import { Link } from 'react-router-dom'
import type { LinkProps } from 'react-router-dom'

import { BUTTON_BASE_CLASS, BUTTON_VARIANT_CLASS } from './buttonStyles'
import type { ButtonVariant } from './buttonStyles'

interface LinkButtonProps extends LinkProps {
  variant?: ButtonVariant
}

// 모양은 버튼이지만 화면을 이동시키는 링크다 (클릭 가능한 div 대신 진짜 링크를 쓴다)
function LinkButton({ variant = 'primary', className = '', ...rest }: LinkButtonProps) {
  return <Link className={`${BUTTON_BASE_CLASS} ${BUTTON_VARIANT_CLASS[variant]} ${className}`} {...rest} />
}

export default LinkButton
