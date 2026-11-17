import { Link } from 'react-router-dom'
import { cn } from '../../lib/cn'
import { degreePath } from '../../lib/degree'

export function DegreeSwitcher({ degrees = [], activeCode, className }) {
  if (!degrees.length) return null

  return (
    <nav
      aria-label="Cambiar de grado"
      className={cn(
        'mx-auto grid w-full max-w-3xl grid-cols-1 gap-1 rounded-4xl border border-white/75 bg-white/42 p-1 backdrop-blur-xl sm:grid-cols-3',
        className,
      )}
    >
      {degrees.map((degree) => {
        const active =
          String(degree.code).toUpperCase() === String(activeCode).toUpperCase()
        return (
          <Link
            key={degree.id}
            to={degreePath(degree)}
            aria-current={active ? 'page' : undefined}
            className={cn(
              'w-full rounded-2xl px-4 py-2.5 text-center text-xs font-semibold transition-all duration-300 sm:rounded-full',
              active
                ? 'bg-white text-ink shadow-soft'
                : 'text-ink-soft hover:bg-white/50 hover:text-ink',
            )}
          >
            {degree.name}
            <span className={cn('ml-1.5 text-[0.6rem]', active ? 'text-aqua-700' : 'text-ink-faint')}>
              {degree.code}
            </span>
          </Link>
        )
      })}
    </nav>
  )
}
