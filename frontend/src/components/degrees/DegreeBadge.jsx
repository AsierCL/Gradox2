import { GraduationCap } from '@phosphor-icons/react'
import { cn } from '../../lib/cn'

export function DegreeBadge({ degree, name, code, className }) {
  const label = degree?.name || name
  const degreeCode = degree?.code || code

  if (!label && !degreeCode) return null

  return (
    <span className={cn('chip py-1 text-[0.58rem]', className)}>
      <GraduationCap size={11} aria-hidden="true" />
      <span className="truncate">{label || degreeCode}</span>
      {label && degreeCode && <span className="text-ink-faint">· {degreeCode}</span>}
    </span>
  )
}
