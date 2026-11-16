import { cn } from '../../lib/cn'

export function Spinner({ className, label = 'Cargando' }) {
  return (
    <span
      role="status"
      aria-label={label}
      className={cn(
        'inline-block size-5 animate-spin rounded-full border-2 border-aqua-600/25 border-r-aqua-700',
        className,
      )}
    />
  )
}

export function PageLoader({ label = 'Cargando' }) {
  return (
    <div className="flex min-h-[45vh] flex-col items-center justify-center gap-4 text-ink-soft">
      <Spinner className="size-7" label={label} />
      <p className="text-sm">{label}…</p>
    </div>
  )
}

export function Skeleton({ className }) {
  return <div aria-hidden="true" className={cn('skeleton rounded-2xl', className)} />
}
