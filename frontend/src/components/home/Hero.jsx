import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import { ArrowRight, MagnifyingGlass } from '@phosphor-icons/react'
import { Button } from '../ui/Button'
import { formatNumber } from '../../lib/format'

export function Hero({ stats }) {
  const [query, setQuery] = useState('')
  const navigate = useNavigate()

  const handleSubmit = (event) => {
    event.preventDefault()
    navigate(query.trim() ? `/explorar?q=${encodeURIComponent(query.trim())}` : '/explorar')
  }

  return (
    <section className="site-shell relative isolate pb-12 pt-12 sm:pb-16 sm:pt-20">
      <div
        aria-hidden="true"
        className="pointer-events-none absolute left-1/2 top-6 -z-10 h-[22rem] w-[min(92vw,58rem)] -translate-x-1/2 rounded-[50%] bg-[radial-gradient(ellipse_at_42%_28%,rgba(255,255,255,0.8),rgba(255,255,255,0.18)_52%,transparent_74%)] blur-2xl sm:h-[28rem]"
      />

      <motion.div
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.45, ease: [0.16, 1, 0.3, 1] }}
        className="relative z-10 mx-auto max-w-3xl text-center"
      >
        <p className="text-xs font-semibold text-ink-soft">Ingeniería Informática · USC</p>
        <h1 className="display mt-3 text-balance text-[clamp(2.4rem,5vw,4rem)] text-ink">
          Material de Ingeniería Informática
        </h1>
        <p className="mx-auto mt-4 max-w-xl text-base leading-7 text-ink-soft">
          Busca apuntes, exámenes y ejercicios por asignatura o título.
        </p>

        <form onSubmit={handleSubmit} className="mx-auto mt-7 max-w-2xl">
          <div className="glass glass-sheen flex items-center gap-2 rounded-2xl p-1.5 pl-4 transition-shadow focus-within:shadow-float">
            <MagnifyingGlass size={18} className="shrink-0 text-ink-soft" aria-hidden="true" />
            <input
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              placeholder="Buscar material"
              aria-label="Buscar material en Gradox"
              className="h-11 min-w-0 flex-1 bg-transparent text-sm text-ink outline-none placeholder:text-ink-faint"
            />
            <Button type="submit" className="min-h-11 shrink-0 rounded-xl px-5" aria-label="Buscar">
              <span className="hidden sm:inline">Buscar</span>
              <ArrowRight size={16} weight="bold" aria-hidden="true" />
            </Button>
          </div>
        </form>

        <div className="mt-4 flex flex-wrap items-center justify-center gap-x-5 gap-y-2 text-[0.7rem]">
          {['Apuntes', 'Exámenes', 'Ejercicios'].map((label) => (
            <Link
              key={label}
              to={`/explorar?q=${encodeURIComponent(label)}`}
              className="font-semibold text-ink-soft underline decoration-ink/20 underline-offset-4 transition-colors hover:text-ink"
            >
              {label}
            </Link>
          ))}
          {stats && (
            <span className="text-ink-faint">
              {formatNumber(stats.totalFiles)} archivos · {formatNumber(stats.totalUsers)} estudiantes
            </span>
          )}
        </div>
      </motion.div>
    </section>
  )
}
