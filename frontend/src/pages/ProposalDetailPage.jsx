import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import {
  ArrowLeft,
  CheckCircle,
  ClockCountdown,
  DownloadSimple,
  FilePdf,
  Info,
  Trash,
  User,
  XCircle,
} from '@phosphor-icons/react'
import { api } from '../lib/api'
import { useAuth } from '../context/AuthContext'
import { statusMeta, typeMeta, visibilityMeta } from '../lib/constants'
import { formatDate } from '../lib/format'
import { Button, TextLink } from '../components/ui/Button'
import { Badge, StatusBadge, TypeBadge } from '../components/ui/Badge'
import { Skeleton } from '../components/ui/Spinner'
import { NotFoundInline } from '../components/ui/States'
import { Modal } from '../components/ui/Modal'
import { VoteBox } from '../components/proposals/VoteBox'

export function ProposalDetailPage() {
  const { id } = useParams()
  const proposalId = Number(id)
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { profile } = useAuth()
  const [cancelOpen, setCancelOpen] = useState(false)

  const proposalQuery = useQuery({
    queryKey: ['proposal', String(proposalId)],
    queryFn: () => api.proposals.get(proposalId),
    enabled: Number.isFinite(proposalId),
  })

  const proposal = proposalQuery.data
  const status = statusMeta(proposal?.status)
  const type = typeMeta(proposal?.type)
  const visibility = visibilityMeta(proposal?.visibilityLevel)

  const downloadMutation = useMutation({
    mutationFn: () => api.proposals.download(proposalId),
    onSuccess: (data) => {
      const anchor = document.createElement('a')
      anchor.href = data.url
      anchor.rel = 'noopener noreferrer'
      anchor.click()
      toast.success('Descarga preparada', {
        description: 'Enlace privado válido durante 2 minutos.',
      })
    },
    onError: (error) => toast.error(error.message),
  })

  const cancelMutation = useMutation({
    mutationFn: () => api.proposals.remove(proposalId),
    onSuccess: () => {
      toast.success('Propuesta retirada')
      queryClient.invalidateQueries({ queryKey: ['proposals'] })
      navigate('/propuestas')
    },
    onError: (error) => toast.error(error.message),
  })

  if (proposalQuery.isLoading) {
    return (
      <div className="site-shell pt-10">
        <Skeleton className="h-4 w-32" />
        <div className="mt-8 grid gap-6 lg:grid-cols-[1fr_22rem]">
          <Skeleton className="h-[32rem] rounded-5xl" />
          <Skeleton className="h-72 rounded-4xl" />
        </div>
      </div>
    )
  }

  if (proposalQuery.isError || !proposal) {
    return (
      <div className="site-shell py-16">
        <NotFoundInline
          message="La propuesta no existe, se ha retirado o no tienes acceso a ella."
          action={{ to: '/propuestas', label: 'Volver a propuestas' }}
        />
      </div>
    )
  }

  const isOwner = profile?.username && profile.username === proposal.proposer
  const pending = proposal.status === 'PENDING'

  return (
    <div className="site-shell">
      <div className="pt-8 sm:pt-12">
        <Link
          to="/propuestas"
          className="inline-flex items-center gap-2 text-xs font-semibold text-ink-soft transition-colors hover:text-ink"
        >
          <ArrowLeft size={14} weight="bold" aria-hidden="true" />
          Volver a propuestas
        </Link>
      </div>

      <div className="mt-7 grid gap-6 lg:grid-cols-[minmax(0,1fr)_22rem] lg:items-start">
        <article className="glass relative overflow-hidden rounded-3xl p-6 sm:p-9">
          <div className="relative flex flex-wrap items-center gap-2">
            <TypeBadge meta={type} />
            <StatusBadge meta={status} />
            <Badge>{visibility.label}</Badge>
          </div>

          <p className="relative mt-8 text-[0.68rem] font-semibold uppercase tracking-[0.14em] text-aqua-700">
            Propuesta de subida
          </p>
          <h1 className="display-tight relative mt-3 text-balance text-[clamp(2.3rem,5vw,4rem)] text-ink">
            {proposal.title}
          </h1>
          <p className="text-pretty relative mt-5 max-w-2xl text-sm leading-7 text-ink-soft sm:text-base sm:leading-8">
            {proposal.description}
          </p>

          {proposal.status === 'APPROVED' && (
            <div className="relative mt-7 flex items-start gap-3 rounded-3xl border border-aqua-200/60 bg-aqua-50/65 p-4">
              <CheckCircle size={21} weight="duotone" className="mt-0.5 shrink-0 text-aqua-700" aria-hidden="true" />
              <div>
                <p className="text-sm font-semibold text-ink">Propuesta aprobada</p>
                <p className="mt-1 text-xs leading-5 text-ink-soft">Ya está publicada en el catálogo.</p>
              </div>
            </div>
          )}
          {proposal.status === 'REJECTED' && (
            <div className="relative mt-7 flex items-start gap-3 rounded-3xl border border-[#e7c7c2] bg-[#fbefed] p-4">
              <XCircle size={21} weight="duotone" className="mt-0.5 shrink-0 text-[#934b43]" aria-hidden="true" />
              <div>
                <p className="text-sm font-semibold text-[#7a3d35]">La propuesta no alcanzó el consenso</p>
                <p className="mt-1 text-xs leading-5 text-[#934b43]/75">
                  El autor puede volver a proponerla más adelante con cambios.
                </p>
              </div>
            </div>
          )}

          <div className="relative mt-8 rounded-4xl border border-ink/8 bg-white/38 p-5 sm:p-6">
            <div className="flex flex-wrap items-start justify-between gap-4">
              <div className="flex items-start gap-3">
                <span className="grid size-12 shrink-0 place-items-center rounded-xl bg-paper-100 text-ink-soft">
                  <FilePdf size={24} weight="duotone" aria-hidden="true" />
                </span>
                <div>
                  <p className="text-sm font-semibold text-ink">{proposal.title}</p>
                  <p className="mt-1 text-[0.65rem] text-ink-faint">
                    {proposal.subjectName} · {proposal.course} · PDF
                  </p>
                </div>
              </div>
              <Button
                variant="ghost"
                onClick={() => downloadMutation.mutate()}
                loading={downloadMutation.isPending}
                icon={<DownloadSimple size={15} weight="bold" />}
                className="min-h-9 px-4 py-2 text-xs"
              >
                Revisar archivo
              </Button>
            </div>

            <div className="mt-5 grid gap-3 border-t border-ink/8 pt-4 sm:grid-cols-2">
              <div>
                <p className="text-[0.6rem] uppercase tracking-[0.1em] text-ink-faint">Propone</p>
                <p className="mt-1 flex items-center gap-1.5 text-xs font-semibold text-ink">
                  <User size={13} className="text-aqua-700" aria-hidden="true" />
                  {proposal.proposer || 'Anónimo'}
                </p>
              </div>
              <div>
                <p className="text-[0.6rem] uppercase tracking-[0.1em] text-ink-faint">Visibilidad</p>
                <p className="mt-1 text-xs font-semibold text-ink">{visibility.label}</p>
              </div>
            </div>
          </div>

          <div className="relative mt-5 flex items-start gap-2.5 rounded-3xl bg-white/30 px-4 py-3">
            <Info size={16} className="mt-0.5 shrink-0 text-aqua-700" aria-hidden="true" />
            <p className="text-[0.68rem] leading-5 text-ink-soft">Abre el archivo antes de votar.</p>
          </div>

          {pending && isOwner && (
            <div className="relative mt-7 flex flex-col justify-between gap-4 border-t border-ink/8 pt-5 sm:flex-row sm:items-center">
              <div>
                <p className="text-xs font-semibold text-ink">¿Quieres retirar tu propuesta?</p>
                <p className="mt-1 text-[0.65rem] text-ink-faint">
                  Solo puedes hacerlo mientras siga en votación.
                </p>
              </div>
              <Button
                variant="danger"
                onClick={() => setCancelOpen(true)}
                icon={<Trash size={14} />}
                className="min-h-9 px-4 py-2 text-xs"
              >
                Retirar propuesta
              </Button>
            </div>
          )}
        </article>

        <aside className="space-y-4 lg:sticky lg:top-24">
          <div className="glass rounded-4xl p-5">
            <div className="mb-4 flex items-center justify-between">
              <p className="eyebrow text-ink-faint">Votación</p>
              {pending && (
                <span className="chip chip-aqua py-1 text-[0.55rem]">
                  <ClockCountdown size={11} aria-hidden="true" /> Abierta
                </span>
              )}
            </div>
            <VoteBox proposalId={proposalId} status={proposal.status} compact />
          </div>

          <div className="surface rounded-4xl p-5">
            <p className="eyebrow text-ink-faint">Ficha</p>
            <div className="mt-4 space-y-2.5 text-xs">
              <div className="flex justify-between gap-3">
                <span className="text-ink-faint">Tipo</span>
                <span className="font-semibold text-ink">{type.label}</span>
              </div>
              <div className="flex justify-between gap-3">
                <span className="text-ink-faint">Asignatura</span>
                <span className="text-right font-semibold text-ink">{proposal.subjectName}</span>
              </div>
              <div className="flex justify-between gap-3">
                <span className="text-ink-faint">Curso</span>
                <span className="font-semibold text-ink">{proposal.course}</span>
              </div>
              <div className="flex justify-between gap-3">
                <span className="text-ink-faint">Estado</span>
                <span className="font-semibold text-ink">{status.label}</span>
              </div>
            </div>
          </div>

          <div className="rounded-4xl border border-white/60 bg-white/28 p-5">
            <p className="text-xs font-semibold text-ink">¿Cómo se decide?</p>
            <p className="mt-2 text-[0.65rem] leading-5 text-ink-soft">
              Se necesita quórum y un porcentaje mínimo de apoyo ponderado. La reputación de cada
              persona ajusta el peso de su voto.
            </p>
            <TextLink to="/comunidad#gobernanza" className="mt-3 text-[0.68rem]">
              Leer las reglas
            </TextLink>
          </div>
        </aside>
      </div>

      <Modal
        open={cancelOpen}
        onClose={() => setCancelOpen(false)}
        title="Retirar la propuesta"
        description="La propuesta dejará de estar disponible para votación. Esta acción no se puede deshacer."
      >
        <div className="flex flex-wrap justify-end gap-2">
          <Button variant="ghost" onClick={() => setCancelOpen(false)}>
            Mantenerla
          </Button>
          <Button
            variant="danger"
            loading={cancelMutation.isPending}
            onClick={() => cancelMutation.mutate()}
          >
            Sí, retirar
          </Button>
        </div>
      </Modal>
    </div>
  )
}

export function PromotionDetailPage() {
  const { id } = useParams()
  const promotionId = Number(id)
  const promotionQuery = useQuery({
    queryKey: ['promotion', String(promotionId)],
    queryFn: () => api.promotions.get(promotionId),
    enabled: Number.isFinite(promotionId),
  })

  if (promotionQuery.isLoading) {
    return (
      <div className="site-shell pt-10">
        <Skeleton className="h-4 w-32" />
        <div className="mt-8 grid gap-6 lg:grid-cols-[1fr_22rem]">
          <Skeleton className="h-[26rem] rounded-5xl" />
          <Skeleton className="h-72 rounded-4xl" />
        </div>
      </div>
    )
  }

  if (promotionQuery.isError || !promotionQuery.data) {
    return (
      <div className="site-shell py-16">
        <NotFoundInline
          message="La candidatura no existe o ya se ha cerrado."
          action={{ to: '/propuestas', label: 'Volver a propuestas' }}
        />
      </div>
    )
  }

  const promotion = promotionQuery.data
  const status = statusMeta(promotion.status)

  return (
    <div className="site-shell">
      <div className="pt-8 sm:pt-12">
        <Link
          to="/propuestas"
          className="inline-flex items-center gap-2 text-xs font-semibold text-ink-soft transition-colors hover:text-ink"
        >
          <ArrowLeft size={14} weight="bold" aria-hidden="true" />
          Volver a propuestas
        </Link>
      </div>

      <div className="mt-7 grid gap-6 lg:grid-cols-[minmax(0,1fr)_22rem] lg:items-start">
        <article className="glass relative overflow-hidden rounded-3xl p-6 sm:p-9">
          <div className="relative flex items-center gap-2">
            <span className="chip chip-aqua">Candidatura a master</span>
            <StatusBadge meta={status} />
          </div>
          <h1 className="display-tight relative mt-8 text-[clamp(2.4rem,5vw,4rem)] text-ink">
            {promotion.candidate}
          </h1>
          <p className="relative mt-4 max-w-xl text-sm leading-7 text-ink-soft">
            Candidatura presentada por{' '}
            <span className="font-semibold text-ink">{promotion.proposer}</span>. Los masters
            moderan y su voto tiene más peso.
          </p>

          <div className="relative mt-8 grid gap-3 sm:grid-cols-2">
            <div className="rounded-3xl border border-ink/8 bg-white/38 p-4">
              <p className="text-[0.6rem] uppercase tracking-[0.1em] text-ink-faint">Presentada</p>
              <p className="mt-1.5 text-sm font-semibold text-ink">{formatDate(promotion.createdAt)}</p>
            </div>
            <div className="rounded-3xl border border-ink/8 bg-white/38 p-4">
              <p className="text-[0.6rem] uppercase tracking-[0.1em] text-ink-faint">Cierre</p>
              <p className="mt-1.5 text-sm font-semibold text-ink">{formatDate(promotion.endsAt)}</p>
            </div>
          </div>

          <div className="relative mt-8 rounded-3xl bg-white/30 p-5">
            <p className="text-sm font-semibold text-ink">Qué implica ser master</p>
            <ul className="mt-3 space-y-2 text-xs leading-5 text-ink-soft">
              <li>· Revisar propuestas y moderar conversaciones.</li>
              <li>· Votar con más peso, siempre dentro del sistema común.</li>
              <li>· Responder ante la comunidad por sus decisiones.</li>
            </ul>
          </div>
        </article>

        <aside className="glass rounded-4xl p-5 lg:sticky lg:top-24">
          <p className="eyebrow text-ink-faint">Votación</p>
          <div className="mt-4">
            <VoteBox proposalId={promotionId} status={promotion.status} compact />
          </div>
        </aside>
      </div>
    </div>
  )
}
