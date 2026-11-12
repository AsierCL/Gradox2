import { Link } from 'react-router-dom'
import { ArrowUpRight } from '@phosphor-icons/react'
import { cn } from '../../lib/cn'

const variants = {
  primary: 'btn-primary',
  aqua: 'btn-aqua',
  ghost: 'btn-ghost',
  light: 'btn-light',
  danger: 'btn-danger',
  quiet: 'text-ink-soft hover:text-ink hover:bg-white/60',
}

export function Button({
  children,
  variant = 'primary',
  className,
  loading = false,
  icon,
  iconRight,
  to,
  href,
  type = 'button',
  disabled,
  ...props
}) {
  const content = (
    <>
      {loading ? (
        <span
          aria-hidden="true"
          className="size-4 animate-spin rounded-full border-2 border-current border-r-transparent opacity-70"
        />
      ) : (
        icon && <span className="shrink-0">{icon}</span>
      )}
      <span>{children}</span>
      {iconRight && <span className="shrink-0">{iconRight}</span>}
    </>
  )

  const classes = cn('btn', variants[variant] || variants.primary, className)

  if (to) {
    return (
      <Link to={to} className={classes} aria-disabled={disabled || loading} {...props}>
        {content}
      </Link>
    )
  }

  if (href) {
    return (
      <a
        href={href}
        className={classes}
        target="_blank"
        rel="noreferrer"
        aria-disabled={disabled || loading}
        {...props}
      >
        {content}
      </a>
    )
  }

  return (
    <button type={type} className={classes} disabled={disabled || loading} {...props}>
      {content}
    </button>
  )
}

export function TextLink({ to, children, className, ...props }) {
  return (
    <Link to={to} className={cn('link-underline text-aqua-800', className)} {...props}>
      {children}
      <ArrowUpRight size={14} weight="bold" aria-hidden="true" />
    </Link>
  )
}
