import { useId } from 'react'
import { CaretDown } from '@phosphor-icons/react'
import { cn } from '../../lib/cn'

export function Field({ label, hint, error, required, className, children, htmlFor }) {
  return (
    <div className={cn('w-full', className)}>
      {label && (
        <label className="field-label" htmlFor={htmlFor}>
          {label}
          {required && <span className="ml-1 text-aqua-600">*</span>}
        </label>
      )}
      {children}
      {error ? (
        <p className="mt-1.5 text-xs leading-5 text-[#934b43]">{error}</p>
      ) : (
        hint && <p className="mt-1.5 text-xs leading-5 text-ink-faint">{hint}</p>
      )}
    </div>
  )
}

export function Input({ label, hint, error, className, id, rightAdornment, ...props }) {
  const generatedId = useId()
  const inputId = id || generatedId
  return (
    <Field label={label} hint={hint} error={error} required={props.required} htmlFor={inputId}>
      <div className="relative">
        <input
          id={inputId}
          className={cn(
            'field',
            rightAdornment && 'pr-20',
            error && 'border-[#c88982]',
            className,
          )}
          aria-invalid={Boolean(error)}
          {...props}
        />
        {rightAdornment}
      </div>
    </Field>
  )
}

export function Textarea({ label, hint, error, className, id, ...props }) {
  const generatedId = useId()
  const inputId = id || generatedId
  return (
    <Field label={label} hint={hint} error={error} required={props.required} htmlFor={inputId}>
      <textarea
        id={inputId}
        className={cn('field', error && 'border-[#c88982]', className)}
        aria-invalid={Boolean(error)}
        {...props}
      />
    </Field>
  )
}

export function Select({ label, hint, error, className, id, children, ...props }) {
  const generatedId = useId()
  const inputId = id || generatedId
  return (
    <Field label={label} hint={hint} error={error} required={props.required} htmlFor={inputId}>
      <div className="relative">
        <select
          id={inputId}
          className={cn(
            'field appearance-none pr-10',
            error && 'border-[#c88982]',
            className,
          )}
          aria-invalid={Boolean(error)}
          {...props}
        >
          {children}
        </select>
        <CaretDown
          size={14}
          weight="bold"
          aria-hidden="true"
          className="pointer-events-none absolute right-4 top-1/2 -translate-y-1/2 text-ink-faint"
        />
      </div>
    </Field>
  )
}
