import { Info, LockKey } from '@phosphor-icons/react'
import { cn } from '../../lib/cn'

export function ReadOnlyNotice({
  title = 'Estás en modo lectura',
  description = 'Puedes consultar y descargar el material. Solo los estudiantes de este grado pueden votar, comentar o proponer cambios.',
  icon = 'info',
  className,
}) {
  const Icon = icon === 'lock' ? LockKey : Info

  return (
    <div
      className={cn(
        'flex items-start gap-3 rounded-3xl border border-ink/8 bg-white/45 px-4 py-3.5',
        className,
      )}
      role="status"
    >
      <span className="mt-0.5 grid size-8 shrink-0 place-items-center rounded-full bg-white/70 text-aqua-700">
        <Icon size={16} weight="duotone" aria-hidden="true" />
      </span>
      <div>
        <p className="text-xs font-semibold text-ink">{title}</p>
        <p className="mt-1 text-[0.7rem] leading-5 text-ink-soft">{description}</p>
      </div>
    </div>
  )
}
