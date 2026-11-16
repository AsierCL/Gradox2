import { Link } from 'react-router-dom'
import { ArrowUpRight, FileArrowUp, User, CalendarBlank } from '@phosphor-icons/react'
import { statusMeta, typeMeta } from '../../lib/constants'
import { formatDate } from '../../lib/format'
import { StatusBadge, TypeBadge } from '../ui/Badge'

export function ProposalCard({ proposal }) {
  const type = typeMeta(proposal.type)
  const status = statusMeta(proposal.status)

  return (
    <Link
      to={`/propuestas/${proposal.id}`}
      className="group relative flex flex-col overflow-hidden rounded-4xl border border-white/72 bg-white/45 p-5 backdrop-blur-xl transition-all duration-500 ease-silk hover:-translate-y-1 hover:border-white hover:bg-white/70 hover:shadow-float sm:p-6"
    >
      <div className="flex items-start justify-between gap-3">
        <div className="flex flex-wrap items-center gap-2">
          <TypeBadge meta={type} />
          <StatusBadge meta={status} />
        </div>
        <span className="grid size-8 shrink-0 place-items-center rounded-full border border-ink/8 bg-white/50 text-ink-faint transition-transform duration-500 group-hover:-translate-y-0.5 group-hover:translate-x-0.5">
          <ArrowUpRight size={15} weight="bold" aria-hidden="true" />
        </span>
      </div>

      <div className="mt-5 flex-1">
        <p className="text-[0.65rem] font-semibold uppercase tracking-[0.12em] text-aqua-700">
          {proposal.subjectName || 'Sin asignatura'}
          {proposal.course && <span className="text-ink-faint"> · {proposal.course}</span>}
        </p>
        <h3 className="mt-2 text-balance text-[1.25rem] font-semibold leading-snug tracking-[-0.03em] text-ink">
          {proposal.title}
        </h3>
        {proposal.description && (
          <p className="mt-2 line-clamp-2 text-xs leading-5 text-ink-soft">{proposal.description}</p>
        )}
      </div>

      <div className="mt-5 flex items-center justify-between gap-3 border-t border-ink/8 pt-3.5 text-[0.62rem] text-ink-faint">
        <span className="inline-flex items-center gap-1.5 truncate">
          <User size={12} aria-hidden="true" />
          {proposal.proposer || 'Anónimo'}
        </span>
        <span className="inline-flex shrink-0 items-center gap-1.5">
          <FileArrowUp size={12} aria-hidden="true" />
          Subida
        </span>
      </div>
    </Link>
  )
}

export function PromotionCard({ proposal }) {
  const status = statusMeta(proposal.status)
  return (
    <Link
      to={`/propuestas/promocion/${proposal.id}`}
      className="group flex items-center justify-between gap-4 rounded-3xl border border-white/72 bg-white/45 p-5 transition-all duration-500 hover:border-white hover:bg-white/70"
    >
      <div className="min-w-0">
        <div className="flex flex-wrap items-center gap-2">
          <span className="chip chip-aqua py-1 text-[0.55rem]">Promoción</span>
          <StatusBadge meta={status} />
        </div>
        <p className="mt-2 truncate text-sm font-semibold text-ink">
          {proposal.candidate} aspira a master
        </p>
        <p className="mt-1 flex items-center gap-3 text-[0.62rem] text-ink-faint">
          <span>Propone {proposal.proposer}</span>
          {proposal.endsAt && (
            <span className="inline-flex items-center gap-1">
              <CalendarBlank size={11} aria-hidden="true" /> cierra {formatDate(proposal.endsAt)}
            </span>
          )}
        </p>
      </div>
      <span className="grid size-8 shrink-0 place-items-center rounded-full border border-ink/8 bg-white/50 text-ink-faint">
        <ArrowUpRight size={15} weight="bold" aria-hidden="true" />
      </span>
    </Link>
  )
}
