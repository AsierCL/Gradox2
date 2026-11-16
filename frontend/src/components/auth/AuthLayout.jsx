import { Link } from 'react-router-dom'
import { motion, useReducedMotion } from 'framer-motion'
import { ArrowLeft, ShieldCheck } from '@phosphor-icons/react'
import { Logo } from '../ui/Logo'

export function AuthLayout({ eyebrow, title, description, children, footer }) {
  const reduceMotion = useReducedMotion()

  return (
    <div className="site-shell py-8 sm:py-14">
      <Link
        to="/"
        className="inline-flex items-center gap-2 text-xs font-semibold text-ink-soft transition-colors hover:text-ink"
      >
        <ArrowLeft size={14} weight="bold" aria-hidden="true" />
        Volver al inicio
      </Link>

      <motion.div
        initial={reduceMotion ? false : { opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.45, ease: [0.16, 1, 0.3, 1] }}
        className="glass glass-sheen mx-auto mt-8 w-full max-w-lg rounded-3xl p-6 sm:p-9"
      >
        <Logo />
        <div className="mt-8">
          <p className="eyebrow text-ink-faint">{eyebrow}</p>
          <h1 className="display-tight mt-3 text-balance text-[clamp(2rem,4vw,2.8rem)] text-ink">
            {title}
          </h1>
          {description && (
            <p className="mt-3 max-w-md text-sm leading-6 text-ink-soft">{description}</p>
          )}
        </div>
        <div className="mt-7">{children}</div>
        {footer && <div className="mt-7 border-t border-ink/8 pt-5">{footer}</div>}
      </motion.div>

      <p className="mx-auto mt-6 flex max-w-lg items-center justify-center gap-1.5 text-[0.62rem] text-ink-faint">
        <ShieldCheck size={13} aria-hidden="true" />
        Acceso con correo institucional · GPL-3.0
      </p>
    </div>
  )
}

export function PasswordToggle({ visible, onToggle }) {
  return (
    <button
      type="button"
      onClick={onToggle}
      className="absolute right-3 top-1/2 -translate-y-1/2 rounded-full px-2 py-1 text-[0.65rem] font-semibold text-ink-faint transition-colors hover:text-ink"
      aria-label={visible ? 'Ocultar contraseña' : 'Mostrar contraseña'}
    >
      {visible ? 'Ocultar' : 'Ver'}
    </button>
  )
}
