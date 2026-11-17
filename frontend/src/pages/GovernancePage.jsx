import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Gauge, ShieldCheck, SlidersHorizontal, UsersFour } from '@phosphor-icons/react'
import { api } from '../lib/api'
import { useAuth } from '../context/AuthContext'
import { PageHeader } from '../components/ui/PageHeader'
import { Button, TextLink } from '../components/ui/Button'
import { Reveal } from '../components/ui/Reveal'
import { Skeleton } from '../components/ui/Spinner'
import { EmptyState, ErrorState } from '../components/ui/States'
import { PromotionCard } from '../components/proposals/ProposalCard'
import { ConfigProposalModal } from '../components/governance/ConfigProposalModal'

export function GovernancePage() {
  const { degreeName, degreeCode } = useAuth()
  const [configOpen, setConfigOpen] = useState(false)

  const configQuery = useQuery({
    queryKey: ['vote-config'],
    queryFn: () => api.config.get(),
    staleTime: 60_000,
  })
  const promotionsQuery = useQuery({
    queryKey: ['promotions', 'pending'],
    queryFn: () => api.promotions.pending(),
    staleTime: 20_000,
  })

  const config = configQuery.data
  const promotions = promotionsQuery.data || []
  const missingDegreeMetadata = promotions.some(
    (promotion) => !promotion.candidateDegreeName && !promotion.candidateDegreeCode,
  )

  return (
    <div className="site-shell">
      <PageHeader
        eyebrow="Gobernanza"
        title="Tu grado, tus reglas"
        description={
          degreeName
            ? `Gobiernas ${degreeName}. Las promociones y expulsiones se deciden dentro del grado; la configuración de votaciones es global.`
            : 'Las promociones y expulsiones se deciden dentro de cada grado; la configuración de votaciones es global.'
        }
        actions={
          <Button
            variant="ghost"
            onClick={() => setConfigOpen(true)}
            icon={<SlidersHorizontal size={16} />}
          >
            Proponer cambio global
          </Button>
        }
      />

      <div className="grid gap-4 lg:grid-cols-[1.1fr_0.9fr]">
        <Reveal>
          <section className="glass rounded-4xl p-6 sm:p-8">
            <div className="flex items-start justify-between gap-4">
              <div>
                <p className="eyebrow text-aqua-700">Configuración global</p>
                <h2 className="mt-2 text-2xl font-semibold tracking-tight text-ink">
                  Reglas de votación
                </h2>
                <p className="mt-2 max-w-lg text-xs leading-6 text-ink-soft">
                  Quórum, umbral, cuota de subidas y pesos son iguales para los tres grados. Una
                  propuesta de cambio la votan todos los masters de la plataforma.
                </p>
              </div>
              <Gauge size={22} className="shrink-0 text-aqua-700" aria-hidden="true" />
            </div>

            {configQuery.isLoading ? (
              <div className="mt-5 space-y-2">
                {Array.from({ length: 5 }).map((_, index) => (
                  <Skeleton key={index} className="h-10" />
                ))}
              </div>
            ) : configQuery.isError ? (
              <div className="mt-5">
                <ErrorState compact onRetry={() => configQuery.refetch()} />
              </div>
            ) : config ? (
              <div className="mt-5 divide-y divide-ink/8">
                {[
                  ['Quórum mínimo', `${config.quorumRequired} votos`],
                  ['Umbral de aprobación', `${Math.round(config.approvalThreshold * 100)}%`],
                  ['Subidas pendientes máx.', config.maxPendingUploads],
                  ['Peso del voto master', `×${config.masterVoteWeight}`],
                  ['Peso del voto usuario', `×${config.userVoteWeight}`],
                ].map(([label, value]) => (
                  <div key={label} className="flex items-center justify-between gap-4 py-3">
                    <span className="text-xs text-ink-faint">{label}</span>
                    <span className="text-xs font-semibold text-ink">{value}</span>
                  </div>
                ))}
              </div>
            ) : (
              <p className="mt-5 text-xs text-ink-soft">No se pudo leer la configuración.</p>
            )}

            <Button
              variant="ghost"
              onClick={() => setConfigOpen(true)}
              className="mt-4 w-full"
              icon={<SlidersHorizontal size={15} />}
            >
              Proponer cambio de política
            </Button>
          </section>
        </Reveal>

        <Reveal delay={0.05}>
          <section className="glass-soft rounded-4xl p-6 sm:p-8">
            <span className="grid size-11 place-items-center rounded-full bg-white/65 text-aqua-700">
              <ShieldCheck size={22} weight="duotone" aria-hidden="true" />
            </span>
            <p className="eyebrow mt-5 text-ink-faint">Tu ámbito</p>
            <h2 className="mt-2 text-2xl font-semibold tracking-tight text-ink">
              {degreeName || 'Grado sin asignar'}
            </h2>
            {degreeCode && <p className="mt-1 text-xs text-ink-faint">{degreeCode}</p>}
            <ul className="mt-5 space-y-2 border-t border-ink/8 pt-4 text-xs leading-5 text-ink-soft">
              <li>· Puedes votar propuestas de material de tu grado.</li>
              <li>· Puedes votar promociones y expulsiones de tu grado.</li>
              <li>· Puedes proponer cambios en la configuración global.</li>
              <li>· No puedes moderar ni votar en otros grados.</li>
            </ul>
            <TextLink to="/propuestas" className="mt-5 text-[0.68rem]">
              Ir a propuestas de material
            </TextLink>
          </section>
        </Reveal>
      </div>

      <section className="mt-8">
        <Reveal>
          <div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-end">
            <div>
              <p className="eyebrow text-aqua-700">Comunidad</p>
              <h2 className="display-tight mt-2 text-2xl text-ink sm:text-3xl">
                Promociones y expulsiones
              </h2>
              <p className="mt-1 max-w-2xl text-sm text-ink-soft">
                Candidaturas pendientes de decisión. Solo los masters del grado de la persona
                candidata pueden votar.
              </p>
            </div>
            <span className="chip shrink-0">
              <UsersFour size={12} aria-hidden="true" /> {promotions.length} pendientes
            </span>
          </div>
        </Reveal>

        {missingDegreeMetadata && promotions.length > 0 && (
          <p className="mt-4 rounded-2xl border border-ink/8 bg-white/40 px-4 py-3 text-[0.65rem] leading-5 text-ink-soft">
            La API todavía no etiqueta el grado de cada candidatura. Si intentas votar una de otro
            grado, el servidor la rechazará con un aviso neutro.
          </p>
        )}

        <div className="mt-5">
          {promotionsQuery.isLoading ? (
            <div className="space-y-3">
              {Array.from({ length: 3 }).map((_, index) => (
                <Skeleton key={index} className="h-24 rounded-3xl" />
              ))}
            </div>
          ) : promotionsQuery.isError ? (
            <ErrorState compact onRetry={() => promotionsQuery.refetch()} />
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
              description="Cuando alguien se presente para master en tu grado, aparecerá aquí para que la comunidad lo valore."
            />
          )}
        </div>
      </section>

      <ConfigProposalModal
        key={config?.id ?? 'config-default'}
        open={configOpen}
        onClose={() => setConfigOpen(false)}
        config={config}
      />
    </div>
  )
}
