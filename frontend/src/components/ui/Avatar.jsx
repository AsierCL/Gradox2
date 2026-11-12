import { User } from '@phosphor-icons/react'
import { initials } from '../../lib/format'
import { cn } from '../../lib/cn'

const sizes = {
  sm: 'size-8 text-[0.65rem]',
  md: 'size-10 text-xs',
  lg: 'size-14 text-base',
  xl: 'size-24 text-2xl',
}

export function Avatar({ name, src, size = 'md', className, ring = false }) {
  return (
    <span
      className={cn(
        'relative inline-grid shrink-0 place-items-center overflow-hidden rounded-full bg-gradient-to-br from-aqua-200 via-aqua-100 to-white text-aqua-900',
        ring && 'ring-4 ring-white/55',
        sizes[size],
        className,
      )}
      aria-hidden="true"
    >
      {src ? (
        <img src={src} alt="" className="size-full object-cover" />
      ) : name ? (
        <span style={{ fontVariationSettings: "'wght' 700" }}>{initials(name)}</span>
      ) : (
        <User size={16} weight="fill" className="text-aqua-700" />
      )}
    </span>
  )
}
