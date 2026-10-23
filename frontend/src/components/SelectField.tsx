import { useId } from 'react'
import type { SelectHTMLAttributes } from 'react'

interface SelectOption {
  value: string
  label: string
}

interface SelectFieldProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label: string
  options: SelectOption[]
  placeholder?: string
}

function SelectField({ label, options, placeholder, ...rest }: SelectFieldProps) {
  const id = useId()
  return (
    <div className="flex flex-col gap-1">
      <label htmlFor={id} className="text-sm font-medium text-gray-700">
        {label}
      </label>
      <select
        id={id}
        className="rounded-md border border-gray-300 bg-white px-3 py-2 text-sm focus-visible:outline-2 focus-visible:outline-indigo-600"
        {...rest}
      >
        {placeholder !== undefined && <option value="">{placeholder}</option>}
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
    </div>
  )
}

export default SelectField
