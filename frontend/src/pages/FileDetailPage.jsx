import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import {
  ArrowLeft,
  ArrowSquareOut,
  Check,
  DownloadSimple,
  Eye,
  EyeSlash,
  FilePdf,
  Flag,
  Star,
  ThumbsDown,
  ThumbsUp,
  User,
  ShieldCheck,
} from '@phosphor-icons/react'
import { api } from '../lib/api'
import { useAuth } from '../context/AuthContext'
import { VISIBILITY, typeMeta, visibilityMeta } from '../lib/constants'
import { scoreLabel } from '../lib/format'
import { Badge, TypeBadge } from '../components/ui/Badge'
import { Button, TextLink } from '../components/ui/Button'
import { Skeleton } from '../components/ui/Spinner'
import { ErrorState } from '../components/ui/States'
import { Modal } from '../components/ui/Modal'
import { Avatar } from '../components/ui/Avatar'
import { Thread } from '../components/file/Thread'
import { cn } from '../lib/cn'

function DocumentPreview({ file, type }) {
  return (
    <div className="rounded-3xl border border-ink/8 bg-paper-100 p-5 sm:p-7">
      <div className="flex items-center justify-between gap-4">
        <span className="inline-flex items-center gap-2 text-[0.65rem] font-semibold uppercase tracking-[0.13em] text-ink-faint">
          <FilePdf size={15} weight="fill" className="text-aqua-700" aria-hidden="true" />
          Documento PDF
        </span>
        <span className="chip py-0.5 text-[0.55rem]">Descarga segura</span>
      </div>

      <div className="relative mt-6 rounded-3xl border border-ink/8 bg-white/70 px-5 py-6 sm:px-7 sm:py-8">
        <p className="text-[0.6rem] font-semibold uppercase tracking-[0.14em] text-aqua-700">
          {file.subject}
        </p>
        <p className="mt-2 max-w-lg text-xl font-semibold leading-tight tracking-[-0.035em] text-ink sm:text-2xl">
          {file.fileName}
        </p>
        <div aria-hidden="true" className="mt-6 space-y-2.5">
          <div className="h-1.5 w-[92%] rounded-full bg-ink/[0.055]" />
          <div className="h-1.5 w-[84%] rounded-full bg-ink/[0.045]" />
          <div className="h-1.5 w-[88%] rounded-full bg-ink/[0.04]" />
          <div className="h-1.5 w-[62%] rounded-full bg-ink/[0.035]" />
          <div className="h-1.5 w-[76%] rounded-full bg-ink/[0.04]" />
          <div className="h-1.5 w-[48%] rounded-full bg-ink/[0.03]" />
        </div>
        <div className="mt-6 flex items-center justify-between border-t border-ink/8 pt-3">
          <span className="text-[0.6rem] text-ink-faint">{type.label}</span>
          <span className="text-[0.6rem] tabular-nums text-ink-faint">Gradox · 2.0</span>
        </div>
      </div>
    </div>
  )
}

export function FileDetailPage() {
  const { id } = useParams()
  const fileId = Number(id)
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { isAuthenticated, isMaster, profile } = useAuth()
  const [myVote, setMyVote] = useState(null)
  const [deleteOpen, setDeleteOpen] = useState(false)

  const fileQuery = useQuery({
    queryKey: ['file', String(fileId)],
    queryFn: ({ signal }) => api.files.get(fileId, signal),
    enabled: Number.isFinite(fileId),
  })

  const subjectsQuery = useQuery({
    queryKey: ['subjects'],
    queryFn: () => api.subjects.list(),
    staleTime: 5 * 60_000,
  })

  const file = fileQuery.data
  const subjectInfo = (subjectsQuery.data || []).find(
    (subject) => subject.name === file?.subject,
  )

  useEffect(() => {
    if (file?.fileName) {
      document.title = `${file.fileName} · Gradox`
    }
  }, [file?.fileName])

  const downloadMutation = useMutation({
    mutationFn: () => api.files.download(fileId),
    onSuccess: (data) => {
      const anchor = document.createElement('a')
      anchor.href = data.url
      anchor.rel = 'noopener noreferrer'
      anchor.click()
      toast.success('Descarga preparada', {
        description: 'El enlace es privado y caduca en 2 minutos.',
      })
    },
    onError: (error) => toast.error(error.message),
  })

  const voteMutation = useMutation({
    mutationFn: (upvote) => api.files.vote(fileId, upvote),
    onSuccess: (data) => {
      setMyVote(Boolean(data?.inFavor))
      toast.success(data?.inFavor ? 'Voto positivo registrado' : 'Voto negativo registrado')
      queryClient.invalidateQueries({ queryKey: ['file', String(fileId)] })
      queryClient.invalidateQueries({ queryKey: ['files'] })
    },
    onError: (error) => toast.error(error.message),
  })

  const retractMutation = useMutation({
    mutationFn: () => api.files.retractVote(fileId),
    onSuccess: () => {
      setMyVote(null)
      toast.success('Voto retirado')
      queryClient.invalidateQueries({ queryKey: ['file', String(fileId)] })
      queryClient.invalidateQueries({ queryKey: ['files'] })
    },
    onError: (error) => toast.error(error.message),
  })

  const visibilityMutation = useMutation({
    mutationFn: (level) => api.files.updateVisibility(fileId, level),
    onSuccess: (updated) => {
      queryClient.setQueryData(['file', String(fileId)], updated)
      queryClient.invalidateQueries({ queryKey: ['files'] })
      toast.success('Visibilidad actualizada')
    },
    onError: (error) => toast.error(error.message),
  })

  const deleteMutation = useMutation({
    mutationFn: () => api.files.requestDeletion(fileId),
    onSuccess: () => {
      setDeleteOpen(false)
      toast.success('Propuesta de eliminación creada', {
        description: 'La comunidad podrá votarla desde Propuestas.',
        action: { label: 'Ver', onClick: () => navigate('/propuestas') },
      })
    },
    onError: (error) => toast.error(error.message),
  })

  const handleVote = (upvote) => {
    if (!isAuthenticated) {
      navigate('/acceso', { state: { from: `/material/${fileId}` } })
      return
    }
    voteMutation.mutate(upvote)
  }

  if (fileQuery.isLoading) {
    return (
      <div className="site-shell pt-10">
        <Skeleton className="h-4 w-36" />
        <div className="mt-8 grid gap-6 lg:grid-cols-[1fr_22rem]">
          <div className="space-y-6">
            <Skeleton className="h-[30rem] rounded-5xl" />
            <Skeleton className="h-80 rounded-4xl" />
          </div>
          <div className="space-y-4">
            <Skeleton className="h-72 rounded-4xl" />
            <Skeleton className="h-44 rounded-4xl" />
          </div>
        </div>
      </div>
    )
  }

  if (fileQuery.isError || !file) {
    return (
      <div className="site-shell py-20">
        <ErrorState
          title="Este material no está disponible"
          description="Puede que haya cambiado de dirección, que ya no esté publicado o que no tengas acceso."
          onRetry={() => fileQuery.refetch()}
        />
        <div className="mt-4 text-center">
          <TextLink to="/explorar">Volver al catálogo</TextLink>
        </div>
      </div>
    )
  }

  const type = typeMeta(file.fileType)
  const visibility = visibilityMeta(file.visibilityLevel)
  const isPublic = (file.visibilityLevel || '').toUpperCase() === 'PUBLIC'
  const anonymous = !file.uploaderUsername || file.uploaderUsername === 'anonymous'
  const canManage =
    isMaster || Boolean(profile?.username && profile.username === file.uploaderUsername)

  return (
    <div className="site-shell">
      <div className="pt-8 sm:pt-12">
        <Link
          to="/explorar"
          className="inline-flex items-center gap-2 text-xs font-semibold text-ink-soft transition-colors hover:text-ink"
        >
          <ArrowLeft size={14} weight="bold" aria-hidden="true" />
          Volver al catálogo
        </Link>
      </div>

      <div className="mt-7 grid gap-6 lg:grid-cols-[minmax(0,1fr)_22rem] lg:items-start">
          <article className="glass relative min-w-0 overflow-hidden rounded-3xl p-6 sm:p-9 lg:col-start-1 lg:row-start-1">
            <div className="relative flex flex-wrap items-center gap-2">
              <TypeBadge meta={type} />
              {isPublic ? (
                <Badge>
                  <Eye size={12} aria-hidden="true" /> {visibility.label}
                </Badge>
              ) : (
                <Badge>
                  <EyeSlash size={12} aria-hidden="true" /> {visibility.label}
                </Badge>
              )}
              <span className="chip border-transparent bg-white/45 text-[0.62rem]">
                PDF · descarga directa
              </span>
            </div>

            <p className="mt-8 text-[0.68rem] font-semibold uppercase tracking-[0.14em] text-aqua-700">
              {file.subject || 'Sin asignatura'}
              {subjectInfo?.courseName && (
                <span className="text-ink-faint"> · {subjectInfo.courseName}</span>
              )}
            </p>
            <h1 className="display-tight mt-3 text-balance text-[clamp(2.4rem,5vw,4.2rem)] text-ink">
              {file.fileName}
            </h1>
            {file.description && (
              <p className="text-pretty mt-5 max-w-2xl text-[0.95rem] leading-7 text-ink-soft sm:text-base sm:leading-8">
                {file.description}
              </p>
            )}

            <div className="mt-8 flex flex-wrap items-center gap-3">
              <Button
                onClick={() => downloadMutation.mutate()}
                loading={downloadMutation.isPending}
                icon={<DownloadSimple size={17} weight="bold" />}
              >
                Descargar PDF
              </Button>
              <div className="flex items-center gap-1 rounded-full border border-ink/8 bg-white/45 p-1">
                <button
                  type="button"
                  onClick={() => handleVote(true)}
                  disabled={voteMutation.isPending || retractMutation.isPending}
                  aria-label="Me ha servido"
                  aria-pressed={myVote === true}
                  className={cn(
                    'inline-flex items-center gap-1.5 rounded-full px-3 py-2 text-[0.7rem] font-semibold transition-colors',
                    myVote === true
                      ? 'bg-aqua-700 text-white'
                      : 'text-ink-soft hover:bg-white hover:text-ink',
                  )}
                >
                  <ThumbsUp size={14} weight={myVote === true ? 'fill' : 'regular'} aria-hidden="true" />
                  Me sirve
                </button>
                <button
                  type="button"
                  onClick={() => handleVote(false)}
                  disabled={voteMutation.isPending || retractMutation.isPending}
                  aria-label="No me ha servido"
                  aria-pressed={myVote === false}
                  className={cn(
                    'inline-flex items-center gap-1.5 rounded-full px-3 py-2 text-[0.7rem] font-semibold transition-colors',
                    myVote === false
                      ? 'bg-ink text-white'
                      : 'text-ink-soft hover:bg-white hover:text-ink',
                  )}
                >
                  <ThumbsDown size={14} weight={myVote === false ? 'fill' : 'regular'} aria-hidden="true" />
                  No me sirve
                </button>
              </div>
              {myVote !== null && (
                <button
                  type="button"
                  onClick={() => retractMutation.mutate()}
                  className="text-[0.68rem] font-semibold text-ink-faint transition-colors hover:text-ink"
                >
                  Retirar voto
                </button>
              )}
            </div>

            <div className="mt-8">
              <DocumentPreview file={file} type={type} />
            </div>
          </article>

        <aside className="order-2 space-y-4 lg:sticky lg:top-24 lg:col-start-2 lg:row-span-2 lg:row-start-1 lg:order-none">
          <div className="glass rounded-4xl p-6">
            <p className="eyebrow text-aqua-700">Ficha del material</p>
            <div className="mt-5 space-y-0 divide-y divide-ink/8">
              {[
                ['Asignatura', file.subject || 'Sin asignatura'],
                ['Curso', subjectInfo?.courseName || '—'],
                ['Tipo', type.label],
                ['Formato', 'PDF'],
                ['Visibilidad', visibility.label],
              ].map(([label, value]) => (
                <div key={label} className="flex items-start justify-between gap-4 py-3 first:pt-0">
                  <span className="text-xs text-ink-faint">{label}</span>
                  <span className="text-right text-xs font-semibold text-ink">{value}</span>
                </div>
              ))}
            </div>

            <div className="mt-5 flex items-center gap-3 rounded-3xl border border-ink/8 bg-white/40 p-3.5">
              <Avatar name={anonymous ? '' : file.uploaderUsername} size="md" />
              <div className="min-w-0">
                <p className="text-[0.62rem] uppercase tracking-[0.1em] text-ink-faint">Compartido por</p>
                <p className="mt-0.5 truncate text-xs font-semibold text-ink">
                  {anonymous ? 'Miembro anónimo' : file.uploaderUsername}
                </p>
              </div>
              {anonymous ? (
                <EyeSlash size={15} className="ml-auto text-ink-faint" aria-label="Identidad protegida" />
              ) : (
                <User size={15} className="ml-auto text-aqua-700" aria-label="Uploader visible" />
              )}
            </div>
            {canManage ? (
              <div className="mt-4 border-t border-ink/8 pt-4">
                <label
                  htmlFor="file-visibility"
                  className="text-[0.62rem] font-semibold uppercase tracking-[0.1em] text-ink-faint"
                >
                  Privacidad del uploader
                </label>
                <select
                  id="file-visibility"
                  value={file.visibilityLevel || 'PUBLIC'}
                  disabled={visibilityMutation.isPending}
                  onChange={(event) => visibilityMutation.mutate(event.target.value)}
                  className="mt-2 h-9 w-full rounded-full border border-ink/10 bg-white/55 px-3 text-[0.7rem] font-semibold text-ink-soft outline-none disabled:opacity-50"
                >
                  {Object.entries(VISIBILITY).map(([value, meta]) => (
                    <option key={value} value={value}>
                      {meta.label}
                    </option>
                  ))}
                </select>
                <p className="mt-2 flex items-start gap-1.5 text-[0.62rem] leading-5 text-ink-faint">
                  <ShieldCheck size={13} className="mt-0.5 shrink-0 text-aqua-700" aria-hidden="true" />
                  {visibility.description}
                </p>
              </div>
            ) : (
              <p className="mt-3 flex items-start gap-1.5 text-[0.62rem] leading-5 text-ink-faint">
                <ShieldCheck size={13} className="mt-0.5 shrink-0 text-aqua-700" aria-hidden="true" />
                {visibility.description}
              </p>
            )}
          </div>

          <div className="glass-soft rounded-3xl p-6">
            <div className="flex items-start justify-between gap-4">
              <div>
                <p className="eyebrow text-ink-faint">Valoración media</p>
                <p className="mt-3 flex items-baseline gap-1 text-ink">
                  <span className="display text-5xl tabular-nums">{scoreLabel(file.score)}</span>
                  <span className="text-sm text-ink-faint">/ 10</span>
                </p>
              </div>
              <Star size={19} weight="fill" className="text-ink-soft" aria-hidden="true" />
            </div>
            <div className="mt-4 h-1.5 overflow-hidden rounded-full bg-ink/8">
              <div
                className="h-full rounded-full bg-ink"
                style={{ width: `${Math.min(Math.max(Number(file.score || 0) * 10, 0), 100)}%` }}
              />
            </div>
            <p className="mt-3 text-[0.62rem] leading-5 text-ink-faint">
              Media de los votos de la comunidad.
            </p>
          </div>

          <div className="surface rounded-4xl p-6">
            <p className="text-sm font-semibold text-ink">¿Algo no encaja?</p>
            <p className="mt-2 text-xs leading-5 text-ink-soft">
              Si el material está incompleto, duplicado o no debería estar aquí, puedes proponer
              su retirada.
            </p>
            {isAuthenticated ? (
              <button
                type="button"
                onClick={() => setDeleteOpen(true)}
                className="mt-4 inline-flex items-center gap-1.5 text-[0.68rem] font-semibold text-[#8b4b43] transition-colors hover:text-[#6f352f]"
              >
                <Flag size={13} aria-hidden="true" /> Proponer eliminación
              </button>
            ) : (
              <Link
                to="/acceso"
                state={{ from: `/material/${fileId}` }}
                className="mt-4 inline-flex items-center gap-1.5 text-[0.68rem] font-semibold text-aqua-800"
              >
                Inicia sesión para proponer cambios
              </Link>
            )}
          </div>

          <a
            href="#hilo"
            className="group flex items-center justify-between rounded-3xl border border-white/65 bg-white/32 px-5 py-4 text-xs font-semibold text-ink-soft transition-colors hover:bg-white/60 hover:text-ink"
          >
            Ir a la conversación
            <ArrowSquareOut
              size={15}
              aria-hidden="true"
              className="transition-transform duration-300 group-hover:-translate-y-0.5 group-hover:translate-x-0.5"
            />
          </a>
        </aside>

        <div className="order-3 min-w-0 glass-soft rounded-5xl p-6 sm:p-9 lg:col-start-1 lg:row-start-2 lg:order-none">
          <Thread fileId={fileId} />
        </div>
      </div>

      <Modal
        open={deleteOpen}
        onClose={() => setDeleteOpen(false)}
        title="Proponer la retirada de este material"
        description="No se elimina de inmediato. Se abre una propuesta pública con un periodo de votación."
      >
        <div className="rounded-2xl border border-ink/8 bg-white/45 p-4">
          <p className="text-xs font-semibold text-ink">{file.fileName}</p>
          <p className="mt-1 text-[0.68rem] text-ink-soft">
            {file.subject} · {type.label}
          </p>
        </div>
        <ul className="mt-4 space-y-2 text-xs leading-5 text-ink-soft">
          <li className="flex gap-2">
            <Check size={14} className="mt-0.5 shrink-0 text-aqua-700" aria-hidden="true" />
            La comunidad podrá revisar el motivo y votar.
          </li>
          <li className="flex gap-2">
            <Check size={14} className="mt-0.5 shrink-0 text-aqua-700" aria-hidden="true" />
            El material seguirá disponible mientras la propuesta esté abierta.
          </li>
        </ul>
        <div className="mt-6 flex flex-wrap justify-end gap-2">
          <Button variant="ghost" onClick={() => setDeleteOpen(false)}>
            Cancelar
          </Button>
          <Button
            variant="danger"
            loading={deleteMutation.isPending}
            onClick={() => deleteMutation.mutate()}
            icon={<Flag size={16} />}
          >
            Crear propuesta
          </Button>
        </div>
      </Modal>
    </div>
  )
}
