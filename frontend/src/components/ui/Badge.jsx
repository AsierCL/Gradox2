import { cn } from '../../lib/cn'

export function Badge({ children, className, dot, tone }) {
  return (
    <span className={cn('chip', tone, className)}>
      {dot && <span className={cn('size-1.5 rounded-full', dot)} aria-hidden="true" />}
      {children}
    </span>
  )
}

export function StatusBadge({ meta }) {
  return (
    <span className={cn('chip border-transparent', meta.tone)}>
      <span className="size-1.5 rounded-full bg-current opacity-60" aria-hidden="true" />
      {meta.label}
    </span>
  )
}

export function TypeBadge({ meta, className }) {
  return (
    <span className={cn('chip', meta.tone, className)}>
      <span className={cn('size-1.5 rounded-full', meta.dot)} aria-hidden="true" />
      {meta.label}
    </span>
  )
}
