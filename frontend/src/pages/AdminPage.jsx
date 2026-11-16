import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import {
  ArrowLeft,
  ArrowRight,
  ArrowsClockwise,
  Check,
  DownloadSimple,
  FileText,
  Gauge,
  HardDrives,
  ListMagnifyingGlass,
  ShieldCheck,
  SlidersHorizontal,
  UserMinus,
  UsersThree,
  WarningCircle,
} from '@phosphor-icons/react'
import { api } from '../lib/api'
import { ACTION_TYPES, PROPOSAL_STATUS } from '../lib/constants'
import { formatBytes, formatDate, formatDateTime, formatNumber } from '../lib/format'
import { PageHeader } from '../components/ui/PageHeader'
import { Button } from '../components/ui/Button'
import { Tabs } from '../components/ui/Tabs'
import { Reveal } from '../components/ui/Reveal'
import { Modal } from '../components/ui/Modal'
import { Input } from '../components/ui/Field'
import { Skeleton } from '../components/ui/Spinner'
import { EmptyState, ErrorState } from '../components/ui/States'
import { StatusBadge } from '../components/ui/Badge'

const actionOptions = [
  { value: '', label: 'Todas' },
  { value: 'UPLOAD', label: 'Subidas' },
  { value: 'DELETE', label: 'Eliminaciones' },
  { value: 'PROMOTION', label: 'Promociones' },
  { value: 'EXPULSION', label: 'Expulsiones' },
  { value: 'POLICY_CHANGE', label: 'Políticas' },
]

function Pager({ page, totalPages, onChange }) {
  if (!totalPages || totalPages <= 1) return null
  return (
    <div className="flex items-center justify-between gap-4 border-t border-ink/8 pt-4">
      <p className="text-[0.65rem] text-ink-faint">
        Página {page + 1} de {totalPages}
      </p>
      <div className="flex gap-1.5">
        <button
          type="button"
          disabled={page <= 0}
          onClick={() => onChange(page - 1)}
          className="inline-flex items-center gap-1 rounded-full border border-ink/10 bg-white/45 px-3 py-1.5 text-[0.65rem] font-semibold text-ink-soft disabled:opacity-35"
        >
          <ArrowLeft size={12} /> Anterior
        </button>
        <button
          type="button"
          disabled={page >= totalPages - 1}
          onClick={() => onChange(page + 1)}
          className="inline-flex items-center gap-1 rounded-full border border-ink/10 bg-white/45 px-3 py-1.5 text-[0.65rem] font-semibold text-ink-soft disabled:opacity-35"
        >
          Siguiente <ArrowRight size={12} />
        </button>
      </div>
    </div>
  )
}

function ConfigModal({ open, onClose, config }) {
  const [form, setForm] = useState({
    quorumRequired: config?.quorumRequired || 10,
    approvalThreshold: config?.approvalThreshold ?? 0.6,
    maxPendingUploads: config?.maxPendingUploads || 3,
    masterVoteWeight: config?.masterVoteWeight ?? 2,
    userVoteWeight: config?.userVoteWeight ?? 1,
  })
  const [error, setError] = useState('')

  const mutation = useMutation({
    mutationFn: () =>
      api.config.propose({
        quorumRequired: Number(form.quorumRequired),
        approvalThreshold: Number(form.approvalThreshold),
        maxPendingUploads: Number(form.maxPendingUploads),
        masterVoteWeight: Number(form.masterVoteWeight),
        userVoteWeight: Number(form.userVoteWeight),
      }),
    onSuccess: () => {
      toast.success('Propuesta de política creada', {
        description: 'Necesita aprobación reforzada de los masters.',
      })
      onClose()
    },
    onError: (requestError) => setError(requestError.message),
  })

  return (
    <Modal
      open={open}
      onClose={onClose}
      title="Proponer un cambio de política"
      description="No se aplica de inmediato. Se abre una votación reforzada entre los masters."
    >
      <form
        onSubmit={(event) => {
          event.preventDefault()
          setError('')
          mutation.mutate()
        }}
        className="space-y-4"
      >
        <div className="grid gap-4 sm:grid-cols-2">
          <Input
            label="Quórum mínimo"
            type="number"
            min={1}
            max={10000}
            value={form.quorumRequired}
            onChange={(event) => setForm((prev) => ({ ...prev, quorumRequired: event.target.value }))}
          />
          <Input
            label="Umbral de aprobación (0–1)"
            type="number"
            min={0.01}
            max={1}
            step={0.01}
            value={form.approvalThreshold}
            onChange={(event) => setForm((prev) => ({ ...prev, approvalThreshold: event.target.value }))}
          />
          <Input
            label="Máx. subidas pendientes"
            type="number"
            min={1}
            value={form.maxPendingUploads}
            onChange={(event) => setForm((prev) => ({ ...prev, maxPendingUploads: event.target.value }))}
          />
          <Input
            label="Peso del voto master"
            type="number"
            min={0.1}
            step={0.1}
            value={form.masterVoteWeight}
            onChange={(event) => setForm((prev) => ({ ...prev, masterVoteWeight: event.target.value }))}
          />
          <Input
            label="Peso del voto usuario"
            type="number"
            min={0.1}
            step={0.1}
            value={form.userVoteWeight}
            onChange={(event) => setForm((prev) => ({ ...prev, userVoteWeight: event.target.value }))}
          />
        </div>
        {error && (
          <div role="alert" className="rounded-2xl border border-[#e7c7c2] bg-[#fbefed] px-4 py-3 text-xs text-[#86483f]">
            {error}
          </div>
        )}
        <div className="flex justify-end gap-2 pt-2">
          <Button type="button" variant="ghost" onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" loading={mutation.isPending}>
            Crear propuesta
          </Button>
        </div>
      </form>
    </Modal>
  )
}

export function AdminPage() {
  const [tab, setTab] = useState('resumen')
  const [page, setPage] = useState(0)
  const [auditPage, setAuditPage] = useState(0)
  const [actionFilter, setActionFilter] = useState('')
  const [configOpen, setConfigOpen] = useState(false)
  const [banOpen, setBanOpen] = useState(false)
  const [banForm, setBanForm] = useState({ userId: '', reason: '' })
  const queryClient = useQueryClient()

  const statsQuery = useQuery({
    queryKey: ['stats'],
    queryFn: ({ signal }) => api.stats.get(signal),
    staleTime: 60_000,
  })
  const configQuery = useQuery({
    queryKey: ['vote-config'],
    queryFn: () => api.config.get(),
    staleTime: 60_000,
  })
  const proposalsQuery = useQuery({
    queryKey: ['admin-proposals', page],
    queryFn: () => api.admin.proposals(page, 20),
    enabled: tab === 'propuestas' || tab === 'resumen',
  })
  const bannedQuery = useQuery({
    queryKey: ['banned-users'],
    queryFn: () => api.admin.banned(0, 30),
    enabled: tab === 'usuarios',
  })
  const auditQuery = useQuery({
    queryKey: ['admin-audit', auditPage],
    queryFn: () => api.admin.audit({ page: auditPage, size: 20 }),
    enabled: tab === 'auditoria',
  })

  const unbanMutation = useMutation({
    mutationFn: (userId) => api.admin.unban(userId),
    onSuccess: () => {
      toast.success('Usuario rehabilitado')
      queryClient.invalidateQueries({ queryKey: ['banned-users'] })
    },
    onError: (error) => toast.error(error.message),
  })

  const banMutation = useMutation({
    mutationFn: () => api.admin.ban(Number(banForm.userId), banForm.reason.trim()),
    onSuccess: () => {
      toast.success('Usuario baneado')
      setBanOpen(false)
      setBanForm({ userId: '', reason: '' })
      queryClient.invalidateQueries({ queryKey: ['banned-users'] })
    },
    onError: (error) => toast.error(error.message),
  })

  const stats = statsQuery.data
  const config = configQuery.data
  const proposalsPage = proposalsQuery.data
  const proposals = (proposalsPage?.content || []).filter(
    (proposal) => !actionFilter || proposal.actionType === actionFilter,
  )
  const bannedUsers = bannedQuery.data?.content || []
  const auditPageData = auditQuery.data
  const auditLogs = auditPageData?.content || []

  return (
    <div className="site-shell">
      <PageHeader
        title="Panel de administración"
        description="Propuestas, usuarios, auditoría y reglas de votación."
        actions={
          <Button
            variant="ghost"
            onClick={() => setConfigOpen(true)}
            icon={<SlidersHorizontal size={16} />}
          >
            Cambiar política
          </Button>
        }
      />

      <Reveal>
        <Tabs
          value={tab}
          onChange={(value) => {
            setTab(value)
            setPage(0)
            setAuditPage(0)
          }}
          items={[
            { value: 'resumen', label: 'Resumen' },
            { value: 'propuestas', label: 'Propuestas' },
            { value: 'usuarios', label: 'Usuarios' },
            { value: 'auditoria', label: 'Auditoría' },
          ]}
        />
      </Reveal>

      <div className="mt-6">
        {tab === 'resumen' && (
          <div className="space-y-4">
            <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
              {[
                ['Materiales', stats?.totalFiles, FileText],
                ['Usuarios', stats?.totalUsers, UsersThree],
                ['Descargas', stats?.totalDownloads, DownloadSimple],
                ['Almacenamiento', stats ? formatBytes(stats.totalStorageBytes) : '—', HardDrives],
              ].map(([label, value, Icon]) => (
                <div key={label} className="glass-soft rounded-3xl p-5">
                  <span className="grid size-9 place-items-center rounded-full bg-white/60 text-aqua-700">
                    <Icon size={17} weight="duotone" aria-hidden="true" />
                  </span>
                  {statsQuery.isLoading ? (
                    <Skeleton className="mt-4 h-8 w-24" />
                  ) : (
                    <p className="display-tight mt-4 text-3xl tabular-nums text-ink">
                      {typeof value === 'number' ? formatNumber(value) : value || '—'}
                    </p>
                  )}
                  <p className="mt-1 text-[0.65rem] text-ink-faint">{label}</p>
                </div>
              ))}
            </div>

            <div className="grid gap-4 lg:grid-cols-[1fr_1fr]">
              <div className="glass rounded-4xl p-6">
                <div className="flex items-center justify-between gap-4">
                  <div>
                    <p className="eyebrow text-aqua-700">Configuración activa</p>
                    <h2 className="mt-2 text-xl font-semibold tracking-tight text-ink">
                      Reglas de votación
                    </h2>
                  </div>
                  <Gauge size={22} className="text-aqua-700" aria-hidden="true" />
                </div>
                {configQuery.isLoading ? (
                  <div className="mt-5 space-y-2">
                    {Array.from({ length: 4 }).map((_, index) => (
                      <Skeleton key={index} className="h-10" />
                    ))}
                  </div>
                ) : config ? (
                  <div className="mt-4 divide-y divide-ink/8">
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
                  <p className="mt-4 text-xs text-ink-soft">No se pudo leer la configuración.</p>
                )}
                <Button
                  variant="ghost"
                  onClick={() => setConfigOpen(true)}
                  className="mt-4 w-full"
                  icon={<SlidersHorizontal size={15} />}
                >
                  Proponer cambio
                </Button>
              </div>

              <div className="glass rounded-4xl p-6">
                <div className="flex items-center justify-between gap-4">
                  <div>
                    <p className="eyebrow text-aqua-700">Actividad reciente</p>
                    <h2 className="mt-2 text-xl font-semibold tracking-tight text-ink">
                      Últimas propuestas
                    </h2>
                  </div>
                  <ListMagnifyingGlass size={22} className="text-aqua-700" aria-hidden="true" />
                </div>
                {proposalsQuery.isLoading ? (
                  <div className="mt-5 space-y-3">
                    {Array.from({ length: 4 }).map((_, index) => (
                      <Skeleton key={index} className="h-12" />
                    ))}
                  </div>
                ) : proposalsPage?.content?.length ? (
                  <div className="mt-4 space-y-2">
                    {proposalsPage.content.slice(0, 4).map((proposal) => (
                      <div key={proposal.id} className="rounded-2xl border border-ink/8 bg-white/35 p-3">
                        <div className="flex items-center justify-between gap-3">
                          <p className="truncate text-xs font-semibold text-ink">
                            {proposal.title || proposal.candidate || ACTION_TYPES[proposal.actionType]}
                          </p>
                          <StatusBadge
                            meta={PROPOSAL_STATUS[proposal.status] || {
                              label: proposal.status,
                              tone: 'bg-white/50 text-ink-soft',
                            }}
                          />
                        </div>
                        <p className="mt-1 text-[0.6rem] text-ink-faint">
                          {ACTION_TYPES[proposal.actionType] || proposal.actionType} · {proposal.proposer}
                        </p>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="mt-5 text-xs text-ink-soft">No hay propuestas todavía.</p>
                )}
                <button
                  type="button"
                  onClick={() => setTab('propuestas')}
                  className="mt-4 inline-flex items-center gap-1 text-[0.68rem] font-semibold text-aqua-800"
                >
                  Ver todas <ArrowRight size={12} />
                </button>
              </div>
            </div>
          </div>
        )}

        {tab === 'propuestas' && (
          <Reveal>
            <div className="glass rounded-4xl p-5 sm:p-6">
              <div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-center">
                <div>
                  <p className="eyebrow text-aqua-700">Solo lectura</p>
                  <h2 className="mt-1 text-xl font-semibold tracking-tight text-ink">
                    Todas las propuestas
                  </h2>
                </div>
                <select
                  value={actionFilter}
                  onChange={(event) => setActionFilter(event.target.value)}
                  className="h-9 rounded-full border border-ink/10 bg-white/45 px-3 text-[0.68rem] font-semibold text-ink-soft outline-none"
                  aria-label="Filtrar por tipo de acción"
                >
                  {actionOptions.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </div>

              {proposalsQuery.isLoading ? (
                <div className="mt-5 space-y-2">
                  {Array.from({ length: 6 }).map((_, index) => (
                    <Skeleton key={index} className="h-16" />
                  ))}
                </div>
              ) : proposalsQuery.isError ? (
                <div className="mt-5">
                  <ErrorState compact onRetry={() => proposalsQuery.refetch()} />
                </div>
              ) : proposals.length ? (
                <div className="mt-5 overflow-x-auto">
                  <table className="w-full min-w-[44rem] border-collapse text-left">
                    <thead>
                      <tr className="border-b border-ink/8 text-[0.58rem] uppercase tracking-[0.1em] text-ink-faint">
                        <th className="pb-3 pr-4 font-semibold">Propuesta</th>
                        <th className="pb-3 pr-4 font-semibold">Tipo</th>
                        <th className="pb-3 pr-4 font-semibold">Estado</th>
                        <th className="pb-3 pr-4 font-semibold">Quórum</th>
                        <th className="pb-3 font-semibold">Creada</th>
                      </tr>
                    </thead>
                    <tbody>
                      {proposals.map((proposal) => (
                        <tr key={proposal.id} className="border-b border-ink/6 last:border-0">
                          <td className="max-w-xs py-3 pr-4">
                            <p className="truncate text-xs font-semibold text-ink">
                              {proposal.title || proposal.candidate || `Propuesta #${proposal.id}`}
                            </p>
                            <p className="mt-0.5 truncate text-[0.6rem] text-ink-faint">
                              {proposal.proposer} · {proposal.subjectName || 'Sin asignatura'}
                            </p>
                          </td>
                          <td className="py-3 pr-4 text-[0.65rem] text-ink-soft">
                            {ACTION_TYPES[proposal.actionType] || proposal.actionType}
                          </td>
                          <td className="py-3 pr-4">
                            <StatusBadge
                              meta={PROPOSAL_STATUS[proposal.status] || {
                                label: proposal.status,
                                tone: 'bg-white/50 text-ink-soft',
                              }}
                            />
                          </td>
                          <td className="py-3 pr-4 text-[0.65rem] tabular-nums text-ink-soft">
                            {proposal.quorumRequired} · {Math.round(proposal.approvalThreshold * 100)}%
                          </td>
                          <td className="py-3 text-[0.65rem] text-ink-faint">
                            {formatDate(proposal.createdAt)}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              ) : (
                <div className="mt-5">
                  <EmptyState
                    icon={<ListMagnifyingGlass size={20} />}
                    title="No hay propuestas con ese filtro"
                    description="Cambia el tipo de acción o vuelve más tarde."
                  />
                </div>
              )}

              <Pager
                page={proposalsPage?.number || 0}
                totalPages={proposalsPage?.totalPages || 1}
                onChange={setPage}
              />
            </div>
          </Reveal>
        )}

        {tab === 'usuarios' && (
          <Reveal>
            <div className="grid gap-4 lg:grid-cols-[1fr_20rem]">
              <div className="glass rounded-4xl p-5 sm:p-6">
                <div className="flex items-center justify-between gap-4">
                  <div>
                    <p className="eyebrow text-aqua-700">Moderación</p>
                    <h2 className="mt-1 text-xl font-semibold tracking-tight text-ink">
                      Usuarios baneados
                    </h2>
                  </div>
                  <span className="chip text-[0.6rem]">{bannedUsers.length} en esta página</span>
                </div>

                {bannedQuery.isLoading ? (
                  <div className="mt-5 space-y-2">
                    {Array.from({ length: 5 }).map((_, index) => (
                      <Skeleton key={index} className="h-14" />
                    ))}
                  </div>
                ) : bannedQuery.isError ? (
                  <div className="mt-5">
                    <ErrorState compact onRetry={() => bannedQuery.refetch()} />
                  </div>
                ) : bannedUsers.length ? (
                  <div className="mt-5 divide-y divide-ink/8">
                    {bannedUsers.map((user) => (
                      <div key={user.id} className="flex items-center justify-between gap-4 py-3.5">
                        <div className="flex min-w-0 items-center gap-3">
                          <span className="grid size-9 shrink-0 place-items-center rounded-full bg-[#f8e9e7] text-[0.62rem] font-bold text-[#934b43]">
                            {user.username?.slice(0, 2).toUpperCase()}
                          </span>
                          <div className="min-w-0">
                            <p className="truncate text-xs font-semibold text-ink">{user.username}</p>
                            <p className="truncate text-[0.6rem] text-ink-faint">{user.email}</p>
                          </div>
                        </div>
                        <button
                          type="button"
                          onClick={() => unbanMutation.mutate(user.id)}
                          disabled={unbanMutation.isPending}
                          className="shrink-0 rounded-full border border-aqua-200 bg-aqua-50 px-3 py-1.5 text-[0.62rem] font-semibold text-aqua-800 transition-colors hover:bg-aqua-100"
                        >
                          Rehabilitar
                        </button>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="mt-5">
                    <EmptyState
                      icon={<ShieldCheck size={20} />}
                      title="Nadie está baneado"
                      description="No hay cuentas deshabilitadas."
                    />
                  </div>
                )}
              </div>

              <div className="glass-soft rounded-4xl p-5">
                <p className="flex items-center gap-2 text-xs font-semibold text-ink">
                  <UserMinus size={15} className="text-[#8b4b43]" aria-hidden="true" />
                  Banear usuario
                </p>
                <p className="mt-2 text-[0.65rem] leading-5 text-ink-soft">
                  Introduce el ID interno y un motivo. La acción queda registrada en auditoría.
                </p>
                <Button
                  variant="danger"
                  className="mt-4 w-full"
                  onClick={() => setBanOpen(true)}
                  icon={<UserMinus size={15} />}
                >
                  Abrir formulario
                </Button>
                <div className="mt-5 flex items-start gap-2 rounded-2xl bg-white/45 p-3 text-[0.6rem] leading-5 text-ink-faint">
                  <WarningCircle size={14} className="mt-0.5 shrink-0 text-[#8b4b43]" aria-hidden="true" />
                  El baneo deshabilita la cuenta. Úsalo solo ante comportamiento perjudicial
                  reiterado.
                </div>
              </div>
            </div>
          </Reveal>
        )}

        {tab === 'auditoria' && (
          <Reveal>
            <div className="glass rounded-4xl p-5 sm:p-6">
              <div className="flex items-center justify-between gap-4">
                <div>
                  <p className="eyebrow text-aqua-700">Trazabilidad</p>
                  <h2 className="mt-1 text-xl font-semibold tracking-tight text-ink">
                    Registro de acciones
                  </h2>
                </div>
                <button
                  type="button"
                  onClick={() => auditQuery.refetch()}
                  className="inline-flex items-center gap-1.5 rounded-full border border-ink/10 bg-white/45 px-3 py-1.5 text-[0.65rem] font-semibold text-ink-soft"
                >
                  <ArrowsClockwise size={12} /> Actualizar
                </button>
              </div>

              {auditQuery.isLoading ? (
                <div className="mt-5 space-y-2">
                  {Array.from({ length: 6 }).map((_, index) => (
                    <Skeleton key={index} className="h-14" />
                  ))}
                </div>
              ) : auditQuery.isError ? (
                <div className="mt-5">
                  <ErrorState compact onRetry={() => auditQuery.refetch()} />
                </div>
              ) : auditLogs.length ? (
                <div className="mt-5 divide-y divide-ink/8">
                  {auditLogs.map((log) => (
                    <div key={log.id} className="flex items-start gap-3 py-3.5">
                      <span className="mt-0.5 grid size-7 shrink-0 place-items-center rounded-full bg-white/55 text-aqua-700">
                        <Check size={12} weight="bold" aria-hidden="true" />
                      </span>
                      <div className="min-w-0 flex-1">
                        <p className="text-xs font-semibold text-ink">
                          {ACTION_TYPES[log.actionType] || log.actionType}{' '}
                          <span className="font-normal text-ink-faint">por {log.actor}</span>
                        </p>
                        <p className="mt-0.5 text-[0.62rem] text-ink-soft">
                          {log.details || `${log.targetEntity || 'Entidad'} #${log.targetId || '—'}`}
                        </p>
                      </div>
                      <span className="shrink-0 text-[0.6rem] text-ink-faint">
                        {formatDateTime(log.timestamp)}
                      </span>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="mt-5">
                  <EmptyState
                    icon={<ListMagnifyingGlass size={20} />}
                    title="Sin registros en esta página"
                    description="La actividad de moderación aparecerá aquí cuando ocurra."
                  />
                </div>
              )}

              <Pager
                page={auditPageData?.number || 0}
                totalPages={auditPageData?.totalPages || 1}
                onChange={setAuditPage}
              />
            </div>
          </Reveal>
        )}
      </div>

      <ConfigModal
        key={config?.id ?? 'config-default'}
        open={configOpen}
        onClose={() => setConfigOpen(false)}
        config={config}
      />

      <Modal
        open={banOpen}
        onClose={() => setBanOpen(false)}
        title="Banear un usuario"
        description="La cuenta quedará deshabilitada y la acción se registrará con tu nombre."
      >
        <form
          onSubmit={(event) => {
            event.preventDefault()
            if (!banForm.userId || Number(banForm.userId) < 1) {
              toast.error('Introduce un ID de usuario válido.')
              return
            }
            banMutation.mutate()
          }}
          className="space-y-4"
        >
          <Input
            label="ID del usuario"
            type="number"
            min={1}
            value={banForm.userId}
            onChange={(event) => setBanForm((prev) => ({ ...prev, userId: event.target.value }))}
            placeholder="Ej. 42"
            required
          />
          <Input
            label="Motivo"
            value={banForm.reason}
            onChange={(event) => setBanForm((prev) => ({ ...prev, reason: event.target.value }))}
            placeholder="Breve explicación para el registro"
          />
          <div className="flex justify-end gap-2 pt-2">
            <Button type="button" variant="ghost" onClick={() => setBanOpen(false)}>
              Cancelar
            </Button>
            <Button type="submit" variant="danger" loading={banMutation.isPending}>
              Banear cuenta
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
