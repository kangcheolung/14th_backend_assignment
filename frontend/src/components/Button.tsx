import type { ButtonHTMLAttributes } from 'react'

import { BUTTON_BASE_CLASS, BUTTON_VARIANT_CLASS } from './buttonStyles'
import type { ButtonVariant } from './buttonStyles'

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant
}

function Button({ variant = 'primary', type = 'button', className = '', ...rest }: ButtonProps) {
  return <button type={type} className={`${BUTTON_BASE_CLASS} ${BUTTON_VARIANT_CLASS[variant]} ${className}`} {...rest} />
}

export default Button
