import { useMemo, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import {
  ArrowRight,
  CheckSquareOffset,
  MagnifyingGlass,
  Plus,
  ShieldCheck,
  UsersFour,
} from '@phosphor-icons/react'
import { api } from '../lib/api'
import { useAuth } from '../context/AuthContext'
import { PageHeader } from '../components/ui/PageHeader'
import { Button, TextLink } from '../components/ui/Button'
import { Tabs } from '../components/ui/Tabs'
import { Reveal } from '../components/ui/Reveal'
import { ProposalCard, PromotionCard } from '../components/proposals/ProposalCard'
import { Skeleton } from '../components/ui/Spinner'
import { EmptyState, ErrorState } from '../components/ui/States'
import { formatNumber } from '../lib/format'

const filters = [
  { value: 'PENDING', label: 'En votación' },
  { value: 'APPROVED', label: 'Aprobadas' },
  { value: 'REJECTED', label: 'Rechazadas' },
  { value: '', label: 'Todas' },
]

export function ProposalsPage() {
  const { isMaster } = useAuth()
  const [tab, setTab] = useState('materiales')
  const [status, setStatus] = useState('PENDING')
  const [query, setQuery] = useState('')

  const proposalsQuery = useQuery({
    queryKey: ['proposals'],
    queryFn: () => api.proposals.list(),
    staleTime: 20_000,
  })

  const promotionsQuery = useQuery({
    queryKey: ['promotions', 'pending'],
    queryFn: () => api.promotions.pending(),
    enabled: isMaster,
    staleTime: 20_000,
  })

  const proposals = useMemo(() => proposalsQuery.data || [], [proposalsQuery.data])
  const pending = proposals.filter((proposal) => proposal.status === 'PENDING').length
  const approved = proposals.filter((proposal) => proposal.status === 'APPROVED').length
  const rejected = proposals.filter((proposal) => proposal.status === 'REJECTED').length

  const filtered = useMemo(() => {
    const normalized = query.trim().toLowerCase()
    return proposals
      .filter((proposal) => (status ? proposal.status === status : true))
      .filter((proposal) => {
        if (!normalized) return true
        return [proposal.title, proposal.description, proposal.subjectName, proposal.course, proposal.proposer]
          .filter(Boolean)
          .join(' ')
          .toLowerCase()
          .includes(normalized)
      })
  }, [proposals, status, query])

  const promotions = promotionsQuery.data || []

  return (
    <div className="site-shell">
      <PageHeader
        title="Propuestas abiertas"
        description="Materiales pendientes de revisión. Cada voto pesa según la reputación."
        actions={
          <Button to="/subir" icon={<Plus size={16} weight="bold" />}>
            Proponer material
          </Button>
        }
      />

      <Reveal>
        <div className="grid gap-3 sm:grid-cols-3">
          {[
            ['En votación', pending, 'Esperando consenso'],
            ['Aprobadas', approved, 'Publicadas'],
            ['Rechazadas', rejected, 'Revisadas por la comunidad'],
          ].map(([label, value, copy]) => (
            <div key={label} className="glass-soft rounded-3xl p-5">
              <p className="eyebrow text-ink-faint">{label}</p>
              <p className="display-tight mt-2 text-3xl tabular-nums text-ink">
                {formatNumber(value)}
              </p>
              <p className="mt-1 text-[0.65rem] text-ink-faint">{copy}</p>
            </div>
          ))}
        </div>
      </Reveal>

      <div className="mt-8 flex flex-col gap-4">
        <div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-center">
          <Tabs
            value={tab}
            onChange={setTab}
            items={[
              { value: 'materiales', label: 'Materiales', count: proposals.length },
              ...(isMaster
                ? [{ value: 'promociones', label: 'Promociones', count: promotions.length }]
                : []),
            ]}
          />

          {tab === 'materiales' && (
            <div className="flex items-center gap-2">
              <div className="flex items-center gap-2 rounded-full border border-ink/8 bg-white/45 px-3 py-2">
                <MagnifyingGlass size={14} className="text-ink-faint" aria-hidden="true" />
                <input
                  value={query}
                  onChange={(event) => setQuery(event.target.value)}
                  placeholder="Filtrar propuestas"
                  aria-label="Filtrar propuestas"
                  className="w-36 bg-transparent text-xs text-ink outline-none placeholder:text-ink-faint sm:w-48"
                />
              </div>
            </div>
          )}
        </div>

        {tab === 'materiales' ? (
          <>
            <div className="hide-scrollbar flex gap-1.5 overflow-x-auto">
              {filters.map((filter) => (
                <button
                  key={filter.value}
                  type="button"
                  onClick={() => setStatus(filter.value)}
                  className={`shrink-0 rounded-full px-3.5 py-2 text-[0.7rem] font-semibold transition-colors ${
                    status === filter.value
                      ? 'bg-ink text-white'
                      : 'text-ink-soft hover:bg-white/65 hover:text-ink'
                  }`}
                >
                  {filter.label}
                  {filter.value === 'PENDING' && pending > 0 && (
                    <span className="ml-1.5 tabular-nums opacity-60">{pending}</span>
                  )}
                </button>
              ))}
            </div>

            {proposalsQuery.isLoading ? (
              <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
                {Array.from({ length: 6 }).map((_, index) => (
                  <Skeleton key={index} className="h-60 rounded-4xl" />
                ))}
              </div>
            ) : proposalsQuery.isError ? (
              <ErrorState onRetry={() => proposalsQuery.refetch()} />
            ) : filtered.length ? (
              <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
                {filtered.map((proposal, index) => (
                  <Reveal key={proposal.id} delay={Math.min(index * 0.025, 0.2)}>
                    <ProposalCard proposal={proposal} />
                  </Reveal>
                ))}
              </div>
            ) : (
              <EmptyState
                icon={<CheckSquareOffset size={22} weight="duotone" />}
                title={query ? 'Ninguna propuesta coincide' : 'No hay propuestas en este estado'}
                description={
                  query
                    ? 'Prueba con otro título, asignatura o nombre de quien propone.'
                    : status === 'PENDING'
                      ? 'No hay materiales esperando decisión. Buen momento para proponer algo tú.'
                      : 'Cuando haya propuestas en este estado aparecerán aquí.'
                }
                action={
                  status === 'PENDING' ? (
                    <Button to="/subir" variant="ghost" className="mt-5">
                      Proponer un material
                    </Button>
                  ) : undefined
                }
              />
            )}
          </>
        ) : (
          <>
            {promotionsQuery.isLoading ? (
              <div className="space-y-3">
                {Array.from({ length: 3 }).map((_, index) => (
                  <Skeleton key={index} className="h-24 rounded-3xl" />
                ))}
              </div>
            ) : promotionsQuery.isError ? (
              <ErrorState onRetry={() => promotionsQuery.refetch()} />
            ) : promotions.length ? (
              <div className="space-y-3">
                {promotions.map((proposal) => (
                  <PromotionCard key={proposal.id} proposal={proposal} />
                ))}
              </div>
            ) : (
              <EmptyState
                icon={<UsersFour size={22} weight="duotone" />}
                title="No hay candidaturas pendientes"
                description="Cuando alguien se presente para master, aparecerá aquí para que la comunidad lo valore."
              />
            )}
          </>
        )}
      </div>

      <Reveal className="mt-10">
        <div className="glass-soft flex flex-col justify-between gap-5 rounded-4xl p-6 sm:flex-row sm:items-center sm:p-7">
          <div className="flex items-start gap-4">
            <span className="grid size-11 shrink-0 place-items-center rounded-full bg-white/65 text-aqua-700">
              <ShieldCheck size={22} weight="duotone" aria-hidden="true" />
            </span>
            <div>
              <p className="text-sm font-semibold text-ink">Cómo votar</p>
              <p className="mt-1 max-w-xl text-xs leading-6 text-ink-soft">
                Abre el archivo antes de votar. Un voto acertado suma reputación; uno contrario resta.
              </p>
            </div>
          </div>
          <TextLink to="/comunidad#gobernanza" className="shrink-0">
            Ver las reglas
            <ArrowRight size={13} weight="bold" aria-hidden="true" />
          </TextLink>
        </div>
      </Reveal>
    </div>
  )
}
