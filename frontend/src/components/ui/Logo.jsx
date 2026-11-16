import { Link } from 'react-router-dom'
import { cn } from '../../lib/cn'

export function Logomark({ className = 'size-9' }) {
  return (
    <svg
      viewBox="0 0 48 48"
      fill="none"
      aria-hidden="true"
      className={cn('shrink-0', className)}
    >
      <defs>
        <linearGradient id="gradox-mark" x1="10" y1="4" x2="41" y2="44" gradientUnits="userSpaceOnUse">
          <stop stopColor="#636366" />
          <stop offset="1" stopColor="#1D1D1F" />
        </linearGradient>
      </defs>
      <path
        d="M11 30.5C11 19.8 19.2 10.4 29.6 6c0 10.2 1 17 5.7 21.6 3.5 3.4 7.1 4.5 10.7 4.6C41.6 40.3 34 46 25.5 46 17 46 11 40.1 11 30.5Z"
        fill="url(#gradox-mark)"
      />
      <path
        d="M19.5 36c4.5-8.2 11.6-11.9 20.3-13.7"
        stroke="#F5F5F7"
        strokeWidth="2.6"
        strokeLinecap="round"
        opacity=".88"
      />
    </svg>
  )
}

export function Logo({ light = false, compact = false }) {
  return (
    <Link
      to="/"
      aria-label="Gradox, ir al inicio"
      className="group inline-flex items-center gap-2.5 rounded-full"
    >
      <Logomark className="size-9 transition-transform duration-500 ease-silk group-hover:rotate-[-6deg]" />
      {!compact && (
        <span
          className={cn(
            'text-[1.35rem] tracking-[-0.055em]',
            light ? 'text-white' : 'text-ink',
          )}
          style={{ fontVariationSettings: "'wght' 650" }}
        >
          gradox
          <span className={light ? 'text-aqua-300' : 'text-aqua-500'}>.</span>
        </span>
      )}
    </Link>
  )
}
