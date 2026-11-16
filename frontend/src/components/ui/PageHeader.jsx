import { cn } from '../../lib/cn'
import { Reveal } from './Reveal'

export function PageHeader({ eyebrow, title, description, actions, className }) {
  return (
    <header className={cn('relative pb-8 pt-8 sm:pb-10 sm:pt-12', className)}>
      <Reveal>
        {eyebrow && <p className="eyebrow text-ink-faint">{eyebrow}</p>}
        <div className="mt-4 flex flex-col gap-6 lg:flex-row lg:items-end lg:justify-between">
          <div className="max-w-3xl">
            <h1 className="display text-balance text-[clamp(2.4rem,4.8vw,4.1rem)] text-ink">
              {title}
            </h1>
            {description && (
              <p className="text-pretty mt-4 max-w-2xl text-base leading-7 text-ink-soft">
                {description}
              </p>
            )}
          </div>
          {actions && <div className="flex shrink-0 flex-wrap items-center gap-3">{actions}</div>}
        </div>
      </Reveal>
    </header>
  )
}

export function SectionHeading({ eyebrow, title, description, action, className }) {
  return (
    <div
      className={cn(
        'flex flex-col gap-6 sm:flex-row sm:items-end sm:justify-between',
        className,
      )}
    >
      <div className="max-w-2xl">
        {eyebrow && <p className="eyebrow text-ink-faint">{eyebrow}</p>}
        <h2 className="display-tight mt-3 text-balance text-[clamp(1.9rem,3.2vw,2.7rem)] text-ink">
          {title}
        </h2>
        {description && (
          <p className="mt-2 max-w-xl text-sm leading-6 text-ink-soft">{description}</p>
        )}
      </div>
      {action && <div className="shrink-0">{action}</div>}
    </div>
  )
}
