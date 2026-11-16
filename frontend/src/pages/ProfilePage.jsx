import { useRef, useState } from 'react'
import { useMutation, useQuery } from '@tanstack/react-query'
import { toast } from 'sonner'
import {
  ArrowRight,
  Camera,
  Check,
  Crown,
  Medal,
  PencilSimple,
  ShieldCheck,
  Sparkle,
  Trash,
  Trophy,
  UserCircle,
  CalendarBlank,
  SignIn,
} from '@phosphor-icons/react'
import { api } from '../lib/api'
import { useAuth } from '../context/AuthContext'
import { ROLES } from '../lib/constants'
import { formatDate, formatNumber } from '../lib/format'
import { Avatar } from '../components/ui/Avatar'
import { Button, TextLink } from '../components/ui/Button'
import { PageHeader } from '../components/ui/PageHeader'
import { Reveal } from '../components/ui/Reveal'
import { Modal } from '../components/ui/Modal'
import { Input } from '../components/ui/Field'
import { EmptyState, ErrorState } from '../components/ui/States'
import { Skeleton } from '../components/ui/Spinner'

function BadgeCard({ badge }) {
  return (
    <div className="flex items-center gap-3 rounded-2xl border border-ink/8 bg-white/36 p-3">
      <span className="grid size-10 shrink-0 place-items-center overflow-hidden rounded-xl bg-aqua-100/70 text-aqua-800">
        {badge.iconUrl ? (
          <img src={badge.iconUrl} alt="" className="size-full object-cover" />
        ) : (
          <Medal size={20} weight="duotone" aria-hidden="true" />
        )}
      </span>
      <div className="min-w-0">
        <p className="truncate text-xs font-semibold text-ink">
          {badge.description || badge.name}
        </p>
        <p className="mt-0.5 text-[0.6rem] uppercase tracking-[0.08em] text-ink-faint">
          {badge.name}
        </p>
      </div>
    </div>
  )
}

export function ProfilePage() {
  const {
    profile,
    profileLoading,
    profileError,
    refreshProfile,
    updateProfile,
    isMaster,
  } = useAuth()
  const fileInputRef = useRef(null)
  const [editOpen, setEditOpen] = useState(false)
  const [form, setForm] = useState({ username: '', password: '', confirm: '' })
  const [formError, setFormError] = useState('')
  const [photoLoading, setPhotoLoading] = useState(false)
  const [promotionRequested, setPromotionRequested] = useState(false)

  const badgesQuery = useQuery({
    queryKey: ['badges'],
    queryFn: () => api.badges.list(),
    staleTime: 5 * 60_000,
  })

  const editMutation = useMutation({
    mutationFn: async () => {
      const payload = {}
      if (form.username.trim() && form.username.trim() !== profile?.username) {
        payload.username = form.username.trim()
      }
      if (form.password) payload.password = form.password
      if (!Object.keys(payload).length) {
        throw new Error('No has cambiado nada.')
      }
      return api.users.updateMe(payload)
    },
    onSuccess: (nextProfile) => {
      updateProfile(nextProfile)
      toast.success('Perfil actualizado')
      setEditOpen(false)
      setForm({ username: '', password: '', confirm: '' })
    },
    onError: (error) => setFormError(error.message),
  })

  const photoMutation = useMutation({
    mutationFn: (file) => api.users.uploadPhoto(file),
    onSuccess: (nextProfile) => {
      updateProfile(nextProfile)
      toast.success('Foto actualizada')
    },
    onError: (error) => toast.error(error.message),
    onSettled: () => setPhotoLoading(false),
  })

  const deletePhotoMutation = useMutation({
    mutationFn: () => api.users.deletePhoto(),
    onSuccess: (nextProfile) => {
      updateProfile(nextProfile)
      toast.success('Foto eliminada')
    },
    onError: (error) => toast.error(error.message),
  })

  const promotionMutation = useMutation({
    mutationFn: () => api.promotions.request(),
    onSuccess: () => {
      setPromotionRequested(true)
      toast.success('Solicitud enviada', {
        description: 'La comunidad podrá valorarla mediante votación.',
      })
    },
    onError: (error) => toast.error(error.message),
  })

  const openEdit = () => {
    setForm({ username: profile?.username || '', password: '', confirm: '' })
    setFormError('')
    setEditOpen(true)
  }

  const submitEdit = (event) => {
    event.preventDefault()
    setFormError('')
    if (form.password && form.password.length < 8) {
      setFormError('La nueva contraseña debe tener al menos 8 caracteres.')
      return
    }
    if (form.password && form.password !== form.confirm) {
      setFormError('Las contraseñas no coinciden.')
      return
    }
    editMutation.mutate()
  }

  const selectPhoto = (event) => {
    const file = event.target.files?.[0]
    event.target.value = ''
    if (!file) return
    if (!file.type.startsWith('image/')) {
      toast.error('Selecciona un archivo de imagen.')
      return
    }
    setPhotoLoading(true)
    photoMutation.mutate(file)
  }

  if (profileLoading) {
    return (
      <div className="site-shell pt-10">
        <Skeleton className="h-10 w-60" />
        <Skeleton className="mt-6 h-64 rounded-5xl" />
        <div className="mt-4 grid gap-4 lg:grid-cols-2">
          <Skeleton className="h-72 rounded-4xl" />
          <Skeleton className="h-72 rounded-4xl" />
        </div>
      </div>
    )
  }

  if (profileError || !profile) {
    return (
      <div className="site-shell py-20">
        <ErrorState
          title="No pudimos cargar tu perfil"
          description={profileError?.message || 'Vuelve a iniciar sesión para continuar.'}
          onRetry={() => refreshProfile()}
        />
      </div>
    )
  }

  const role = ROLES[(profile.role || '').toUpperCase()] || 'Estudiante'
  const earnedBadges = badgesQuery.data?.filter((badge) => profile.badges?.includes(badge.name)) || []
  const badgeNames = profile.badges || []
  const nextBadge = badgesQuery.data?.find((badge) => !badgeNames.includes(badge.name))

  return (
    <div className="site-shell">
      <PageHeader
        eyebrow="Cuenta"
        title="Tu perfil"
        description="Gestiona tu cuenta, tu reputación y tus insignias."
        actions={
          <Button variant="ghost" onClick={openEdit} icon={<PencilSimple size={16} />}>
            Editar perfil
          </Button>
        }
      />

      <Reveal>
        <section className="glass glass-sheen rounded-3xl p-6 sm:p-9">
          <div className="flex flex-col gap-8 lg:flex-row lg:items-center lg:justify-between">
            <div className="flex flex-col gap-6 sm:flex-row sm:items-center">
              <div className="relative w-fit">
                <Avatar
                  name={profile.username}
                  src={profile.profilePictureUrl}
                  size="xl"
                  ring
                  className="size-28 text-3xl sm:size-32"
                />
                <button
                  type="button"
                  onClick={() => fileInputRef.current?.click()}
                  disabled={photoLoading}
                  className="absolute -bottom-1 -right-1 grid size-9 place-items-center rounded-full bg-ink text-white shadow-float transition-transform hover:scale-105 disabled:opacity-50"
                  aria-label="Cambiar foto de perfil"
                >
                  {photoLoading ? (
                    <span className="size-3.5 animate-spin rounded-full border-2 border-white/30 border-r-white" />
                  ) : (
                    <Camera size={16} weight="fill" aria-hidden="true" />
                  )}
                </button>
                <input
                  ref={fileInputRef}
                  type="file"
                  accept="image/*"
                  onChange={selectPhoto}
                  className="sr-only"
                  aria-label="Seleccionar nueva foto"
                />
              </div>

              <div>
                <div className="flex flex-wrap items-center gap-2">
                  <span className="chip chip-aqua">
                    {isMaster ? (
                      <Crown size={13} weight="fill" aria-hidden="true" />
                    ) : (
                      <UserCircle size={13} weight="fill" aria-hidden="true" />
                    )}
                    {role}
                  </span>
                  <span className="chip border-transparent bg-white/40">
                    <ShieldCheck size={12} aria-hidden="true" /> Cuenta USC
                  </span>
                </div>
                <h2 className="display-tight mt-3 text-3xl text-ink sm:text-4xl">{profile.username}</h2>
                <p className="mt-1 text-sm text-ink-soft">{profile.email}</p>
                <div className="mt-4 flex flex-wrap items-center gap-4 text-[0.65rem] text-ink-faint">
                  <span className="inline-flex items-center gap-1.5">
                    <CalendarBlank size={13} aria-hidden="true" />
                    Miembro desde {formatDate(profile.createdAt)}
                  </span>
                  {profile.lastLogin && (
                    <span className="inline-flex items-center gap-1.5">
                      <SignIn size={13} aria-hidden="true" />
                      Última entrada {formatDate(profile.lastLogin)}
                    </span>
                  )}
                </div>
                {profile.profilePictureUrl && (
                  <button
                    type="button"
                    onClick={() => deletePhotoMutation.mutate()}
                    disabled={deletePhotoMutation.isPending}
                    className="mt-3 inline-flex items-center gap-1 text-[0.62rem] font-semibold text-ink-faint transition-colors hover:text-[#8b4b43]"
                  >
                    <Trash size={11} aria-hidden="true" /> Quitar foto
                  </button>
                )}
              </div>
            </div>

            <div className="flex items-center gap-6 rounded-4xl border border-white/65 bg-white/35 px-6 py-5 sm:gap-9">
              <div>
                <p className="eyebrow text-ink-faint">Reputación</p>
                <p className="display mt-2 flex items-baseline gap-1 text-4xl tabular-nums text-ink">
                  {Number(profile.reputation || 0).toFixed(1)}
                  <span className="text-xs font-normal text-ink-faint">pts</span>
                </p>
              </div>
              <div className="h-12 w-px bg-ink/10" />
              <div>
                <p className="eyebrow text-ink-faint">Insignias</p>
                <p className="display mt-2 flex items-baseline gap-1 text-4xl tabular-nums text-ink">
                  {formatNumber(badgeNames.length)}
                  <span className="text-xs font-normal text-ink-faint">/ {badgesQuery.data?.length || '—'}</span>
                </p>
              </div>
            </div>
          </div>

          <div className="relative mt-8 grid gap-3 border-t border-ink/8 pt-6 sm:grid-cols-3">
            {[
              ['Comparte material', 'Cada aporte aceptado suma 10 puntos.'],
              ['Vota con criterio', 'Un voto acertado suma 4 puntos.'],
              ['Participa', 'La reputación determina el peso de tu voto.'],
            ].map(([title, copy]) => (
              <div key={title} className="flex gap-2.5">
                <span className="mt-0.5 grid size-5 shrink-0 place-items-center rounded-full bg-aqua-100 text-aqua-800">
                  <Check size={11} weight="bold" aria-hidden="true" />
                </span>
                <div>
                  <p className="text-xs font-semibold text-ink">{title}</p>
                  <p className="mt-0.5 text-[0.65rem] leading-5 text-ink-faint">{copy}</p>
                </div>
              </div>
            ))}
          </div>
        </section>
      </Reveal>

      <div className="mt-4 grid gap-4 lg:grid-cols-[1.1fr_0.9fr]">
        <Reveal delay={0.04}>
          <section className="glass-soft h-full rounded-4xl p-6 sm:p-8">
            <div className="flex items-center justify-between gap-4">
              <div>
                <h2 className="flex items-center gap-2 text-2xl font-semibold tracking-tight text-ink">
                  <Trophy size={22} weight="duotone" className="text-sand-400" aria-hidden="true" />
                  Tus insignias
                </h2>
              </div>
              <TextLink to="/comunidad#reputacion">Cómo se ganan</TextLink>
            </div>

            <div className="mt-6">
              {badgesQuery.isLoading ? (
                <div className="grid gap-2 sm:grid-cols-2">
                  {Array.from({ length: 4 }).map((_, index) => (
                    <Skeleton key={index} className="h-16" />
                  ))}
                </div>
              ) : earnedBadges.length ? (
                <div className="grid gap-2 sm:grid-cols-2">
                  {earnedBadges.map((badge) => (
                    <BadgeCard key={badge.id} badge={badge} />
                  ))}
                </div>
              ) : badgeNames.length ? (
                <div className="grid gap-2 sm:grid-cols-2">
                  {badgeNames.map((name) => (
                    <div
                      key={name}
                      className="flex items-center gap-3 rounded-2xl border border-ink/8 bg-white/36 p-3"
                    >
                      <span className="grid size-10 place-items-center rounded-xl bg-aqua-100/70 text-aqua-800">
                        <Medal size={20} weight="duotone" aria-hidden="true" />
                      </span>
                      <div>
                        <p className="text-xs font-semibold text-ink">{name}</p>
                        <p className="mt-0.5 text-[0.6rem] text-ink-faint">Insignia conseguida</p>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <EmptyState
                  icon={<Medal size={22} weight="duotone" />}
                  title="Todavía no tienes insignias"
                  description="Se ganan compartiendo material, participando en votaciones y aportando contexto útil en los hilos."
                  action={
                    <Button to="/subir" variant="ghost" className="mt-5">
                      Compartir mi primer material
                    </Button>
                  }
                />
              )}
            </div>

            {nextBadge && (
              <div className="mt-5 flex items-start gap-3 rounded-2xl border border-dashed border-ink/12 bg-white/22 p-4">
                <span className="grid size-9 shrink-0 place-items-center rounded-xl bg-white/55 text-ink-faint">
                  <Trophy size={17} weight="duotone" aria-hidden="true" />
                </span>
                <div>
                  <p className="text-[0.62rem] font-semibold uppercase tracking-[0.1em] text-ink-faint">
                    Próxima insignia
                  </p>
                  <p className="mt-1 text-xs font-semibold text-ink">
                    {nextBadge.description || nextBadge.name}
                  </p>
                  <p className="mt-0.5 text-[0.62rem] leading-5 text-ink-faint">
                    {nextBadge.name}
                  </p>
                </div>
              </div>
            )}
          </section>
        </Reveal>

        <div className="grid gap-4">
          <Reveal delay={0.08}>
            <section className="surface rounded-4xl p-6 sm:p-7">
              <p className="eyebrow text-aqua-700">Cuenta</p>
              <h2 className="mt-2 text-xl font-semibold tracking-tight text-ink">Cuenta y seguridad</h2>
              <div className="mt-5 space-y-3">
                <button
                  type="button"
                  onClick={openEdit}
                  className="flex w-full items-center justify-between rounded-2xl border border-ink/8 bg-white/38 p-4 text-left transition-colors hover:bg-white/68"
                >
                  <span>
                    <span className="block text-xs font-semibold text-ink">Alias y contraseña</span>
                    <span className="mt-0.5 block text-[0.65rem] text-ink-faint">
                      Cambia tu alias y contraseña.
                    </span>
                  </span>
                  <ArrowRight size={15} className="text-ink-faint" aria-hidden="true" />
                </button>
                <div className="flex items-center justify-between rounded-2xl border border-ink/8 bg-white/38 p-4">
                  <span>
                    <span className="block text-xs font-semibold text-ink">Correo institucional</span>
                    <span className="mt-0.5 block text-[0.65rem] text-ink-faint">{profile.email}</span>
                  </span>
                  <span className="chip chip-aqua py-1 text-[0.55rem]">Verificado</span>
                </div>
              </div>
            </section>
          </Reveal>

          {!isMaster ? (
            <Reveal delay={0.12}>
              <section className="rounded-3xl border border-ink/8 bg-white p-6 sm:p-7">
                <span className="grid size-10 place-items-center rounded-xl bg-paper-100 text-ink-soft">
                  <Sparkle size={20} weight="duotone" aria-hidden="true" />
                </span>
                <p className="mt-5 text-lg font-semibold tracking-tight text-ink">
                  ¿Quieres ser master?
                </p>
                <p className="mt-2 text-xs leading-6 text-ink-soft">
                  Los masters moderan y tienen más peso en las votaciones. La comunidad decide
                  quién accede al rol.
                </p>
                <button
                  type="button"
                  onClick={() => promotionMutation.mutate()}
                  disabled={promotionMutation.isPending || promotionRequested}
                  className="mt-5 inline-flex items-center gap-1.5 rounded-full bg-ink px-4 py-2 text-[0.7rem] font-semibold text-white transition-colors hover:bg-[#333336] disabled:opacity-50"
                >
                  {promotionRequested ? (
                    <>
                      <Check size={13} weight="bold" aria-hidden="true" /> Solicitud enviada
                    </>
                  ) : (
                    <>
                      Presentarme <ArrowRight size={13} weight="bold" aria-hidden="true" />
                    </>
                  )}
                </button>
              </section>
            </Reveal>
          ) : (
            <Reveal delay={0.12}>
              <section className="glass rounded-4xl p-6 sm:p-7">
                <span className="grid size-10 place-items-center rounded-2xl bg-white/60 text-aqua-700">
                  <Crown size={20} weight="duotone" aria-hidden="true" />
                </span>
                <p className="mt-5 text-lg font-semibold tracking-tight text-ink">Administración</p>
                <p className="mt-2 text-xs leading-6 text-ink-soft">
                  Propuestas, auditoría y usuarios baneados.
                </p>
                <Button to="/administracion" className="mt-5" variant="ghost">
                  Abrir administración
                </Button>
              </section>
            </Reveal>
          )}
        </div>
      </div>

      <Modal
        open={editOpen}
        onClose={() => setEditOpen(false)}
        title="Editar tu perfil"
        description="Puedes cambiar tu alias o establecer una nueva contraseña. Los campos vacíos se mantienen como están."
      >
        <form onSubmit={submitEdit} className="space-y-4">
          <Input
            label="Alias"
            autoComplete="username"
            value={form.username}
            onChange={(event) => setForm((prev) => ({ ...prev, username: event.target.value }))}
            minLength={3}
            maxLength={50}
          />
          <div className="grid gap-4 sm:grid-cols-2">
            <Input
              label="Nueva contraseña"
              type="password"
              autoComplete="new-password"
              placeholder="Opcional"
              value={form.password}
              onChange={(event) => setForm((prev) => ({ ...prev, password: event.target.value }))}
              minLength={8}
              maxLength={72}
            />
            <Input
              label="Repite la contraseña"
              type="password"
              autoComplete="new-password"
              placeholder="Opcional"
              value={form.confirm}
              onChange={(event) => setForm((prev) => ({ ...prev, confirm: event.target.value }))}
              minLength={8}
              maxLength={72}
            />
          </div>
          {formError && (
            <div
              role="alert"
              className="rounded-2xl border border-[#e7c7c2] bg-[#fbefed] px-4 py-3 text-xs leading-5 text-[#86483f]"
            >
              {formError}
            </div>
          )}
          <div className="flex justify-end gap-2 pt-2">
            <Button type="button" variant="ghost" onClick={() => setEditOpen(false)}>
              Cancelar
            </Button>
            <Button type="submit" loading={editMutation.isPending}>
              Guardar cambios
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
