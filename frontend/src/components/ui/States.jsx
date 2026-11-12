import { Link } from 'react-router-dom'
import { ArrowClockwise, Compass } from '@phosphor-icons/react'
import { Button } from './Button'

export function ErrorState({
  title = 'No se pudo cargar',
  description = 'Comprueba la conexión con la API e inténtalo de nuevo.',
  onRetry,
  compact = false,
}) {
  return (
    <div
      className={`glass-soft flex flex-col items-center rounded-4xl px-6 text-center ${
        compact ? 'py-8' : 'py-14'
      }`}
      role="alert"
    >
      <span className="grid size-11 place-items-center rounded-full bg-white/70 text-aqua-700">
        <Compass size={22} weight="duotone" aria-hidden="true" />
      </span>
      <h3 className="mt-4 text-lg font-semibold tracking-tight text-ink">{title}</h3>
      <p className="mt-2 max-w-md text-sm leading-6 text-ink-soft">{description}</p>
      {onRetry && (
        <Button
          onClick={onRetry}
          variant="ghost"
          className="mt-5"
          icon={<ArrowClockwise size={16} weight="bold" />}
        >
          Reintentar
        </Button>
      )}
    </div>
  )
}

export function EmptyState({ icon, title, description, action }) {
  return (
    <div className="surface flex flex-col items-center rounded-4xl border-dashed px-6 py-14 text-center">
      {icon && (
        <span className="grid size-12 place-items-center rounded-full bg-white/70 text-aqua-700">
          {icon}
        </span>
      )}
      <h3 className="mt-4 text-lg font-semibold tracking-tight text-ink">{title}</h3>
      <p className="mt-2 max-w-md text-sm leading-6 text-ink-soft">{description}</p>
      {action}
    </div>
  )
}

export function NotFoundInline({ message, action }) {
  return (
    <div className="py-20 text-center">
      <p className="eyebrow text-ink-faint">No encontrado</p>
      <h2 className="display-tight mt-3 text-3xl text-ink">Contenido no disponible</h2>
      <p className="mx-auto mt-3 max-w-md text-sm leading-6 text-ink-soft">{message}</p>
      <Button to={action?.to || '/explorar'} className="mt-6" icon={<Compass size={16} />}>
        {action?.label || 'Volver a explorar'}
      </Button>
      <p className="mt-4 text-xs text-ink-faint">
        <Link to="/" className="link-underline">
          Ir al inicio
        </Link>
      </p>
    </div>
  )
}
