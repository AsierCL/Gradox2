import { useEffect, useId, useRef } from 'react'
import { createPortal } from 'react-dom'
import { AnimatePresence, motion, useReducedMotion } from 'framer-motion'
import { X } from '@phosphor-icons/react'
import { cn } from '../../lib/cn'

export function Modal({ open, onClose, title, description, children, className }) {
  const reduceMotion = useReducedMotion()
  const panelRef = useRef(null)
  const titleId = useId()

  useEffect(() => {
    if (!open) return undefined
    const previous = document.activeElement
    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'

    const handleKey = (event) => {
      if (event.key === 'Escape') onClose?.()
      if (event.key !== 'Tab' || !panelRef.current) return
      const focusable = panelRef.current.querySelectorAll(
        'a[href], button:not([disabled]), textarea, input, select, [tabindex]:not([tabindex="-1"])',
      )
      if (!focusable.length) return
      const first = focusable[0]
      const last = focusable[focusable.length - 1]
      if (event.shiftKey && document.activeElement === first) {
        event.preventDefault()
        last.focus()
      } else if (!event.shiftKey && document.activeElement === last) {
        event.preventDefault()
        first.focus()
      }
    }

    document.addEventListener('keydown', handleKey)
    const timer = window.setTimeout(() => panelRef.current?.focus(), 30)

    return () => {
      document.removeEventListener('keydown', handleKey)
      document.body.style.overflow = previousOverflow
      window.clearTimeout(timer)
      previous?.focus?.()
    }
  }, [open, onClose])

  return createPortal(
    <AnimatePresence>
      {open && (
        <div className="fixed inset-0 z-[80] flex items-end justify-center p-0 sm:items-center sm:p-6">
          <motion.button
            type="button"
            aria-label="Cerrar"
            className="absolute inset-0 cursor-default bg-black/30 backdrop-blur-sm"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: reduceMotion ? 0 : 0.25 }}
            onClick={onClose}
          />
          <motion.section
            ref={panelRef}
            role="dialog"
            aria-modal="true"
            aria-labelledby={titleId}
            tabIndex={-1}
            className={cn(
              'glass relative z-10 max-h-[92vh] w-full overflow-y-auto rounded-t-4xl border-white/80 p-6 outline-none sm:max-w-xl sm:rounded-4xl sm:p-8',
              className,
            )}
            initial={reduceMotion ? false : { opacity: 0, y: 34, scale: 0.985 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={reduceMotion ? { opacity: 0 } : { opacity: 0, y: 22, scale: 0.99 }}
            transition={{ duration: 0.4, ease: [0.16, 1, 0.3, 1] }}
          >
            <div className="mb-6 flex items-start justify-between gap-6">
              <div>
                <h2 id={titleId} className="display-tight text-2xl text-ink">
                  {title}
                </h2>
                {description && (
                  <p className="mt-2 max-w-md text-sm leading-6 text-ink-soft">{description}</p>
                )}
              </div>
              <button
                type="button"
                onClick={onClose}
                aria-label="Cerrar ventana"
                className="grid size-9 shrink-0 place-items-center rounded-full border border-ink/10 bg-white/55 text-ink-soft transition-colors hover:bg-white hover:text-ink"
              >
                <X size={17} weight="bold" aria-hidden="true" />
              </button>
            </div>
            {children}
          </motion.section>
        </div>
      )}
    </AnimatePresence>,
    document.body,
  )
}
