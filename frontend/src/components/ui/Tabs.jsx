import { cn } from '../../lib/cn'

export function Tabs({ items, value, onChange, className }) {
  return (
    <div
      className={cn(
        'hide-scrollbar inline-flex max-w-full gap-1 overflow-x-auto rounded-full border border-white/75 bg-white/42 p-1 backdrop-blur-xl',
        className,
      )}
      role="tablist"
    >
      {items.map((item) => {
        const active = item.value === value
        return (
          <button
            key={item.value}
            type="button"
            role="tab"
            aria-selected={active}
            onClick={() => onChange(item.value)}
            className={cn(
              'rounded-full px-4 py-2 text-xs font-semibold transition-all duration-300',
              active
                ? 'bg-white text-ink shadow-soft'
                : 'text-ink-soft hover:bg-white/50 hover:text-ink',
            )}
          >
            {item.label}
            {item.count !== undefined && (
              <span className={cn('ml-1.5 tabular-nums', active ? 'text-aqua-700' : 'text-ink-faint')}>
                {item.count}
              </span>
            )}
          </button>
        )
      })}
    </div>
  )
}
