import { Link } from 'react-router-dom'
import { cn } from '../../lib/cn'
import { degreePath } from '../../lib/degree'

export function DegreeSwitcher({ degrees = [], activeCode, className }) {
  if (!degrees.length) return null

  return (
    <nav
      aria-label="Cambiar de grado"
      className={cn(
        'flex max-w-full flex-wrap gap-1 rounded-4xl border border-white/75 bg-white/42 p-1 backdrop-blur-xl',
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
              'shrink-0 rounded-full px-4 py-2 text-xs font-semibold transition-all duration-300',
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
