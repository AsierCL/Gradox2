import { useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMutation, useQuery } from '@tanstack/react-query'
import { toast } from 'sonner'
import {
  ArrowRight,
  Check,
  Eye,
  EyeSlash,
  FilePdf,
  Info,
  ShieldCheck,
  UploadSimple,
  UsersThree,
  X,
} from '@phosphor-icons/react'
import { api } from '../lib/api'
import { formatBytes } from '../lib/format'
import { FILE_TYPES, VISIBILITY } from '../lib/constants'
import { PageHeader } from '../components/ui/PageHeader'
import { Reveal } from '../components/ui/Reveal'
import { Button } from '../components/ui/Button'
import { Input, Select, Textarea } from '../components/ui/Field'
import { Skeleton } from '../components/ui/Spinner'
import { cn } from '../lib/cn'

const MAX_SIZE = 10 * 1024 * 1024

const visibilityOptions = [
  {
    value: 'PUBLIC',
    icon: Eye,
    title: 'Público',
    copy: 'Cualquier persona puede ver que lo compartiste.',
  },
  {
    value: 'RESTRICTED',
    icon: UsersThree,
    title: 'Solo comunidad',
    copy: 'Solo estudiantes registrados ven tu identidad.',
  },
  {
    value: 'PRIVATE',
    icon: EyeSlash,
    title: 'Privado',
    copy: 'Tu nombre queda oculto para el resto de la comunidad.',
  },
]

export function UploadPage() {
  const navigate = useNavigate()
  const fileInput = useRef(null)
  const [dragging, setDragging] = useState(false)
  const [file, setFile] = useState(null)
  const [form, setForm] = useState({
    title: '',
    description: '',
    type: 'APUNTES',
    subjectId: '',
    visibilityLevel: 'PUBLIC',
  })
  const [errors, setErrors] = useState({})
  const [serverError, setServerError] = useState('')

  const coursesQuery = useQuery({
    queryKey: ['courses'],
    queryFn: () => api.courses.list(),
    staleTime: 5 * 60_000,
  })
  const subjectsQuery = useQuery({
    queryKey: ['subjects'],
    queryFn: () => api.subjects.list(),
    staleTime: 5 * 60_000,
  })

  const courses = [...(coursesQuery.data || [])].sort((a, b) => Number(a.code) - Number(b.code))
  const subjects = subjectsQuery.data || []

  const uploadMutation = useMutation({
    mutationFn: async () => {
      const data = new FormData()
      data.append('title', form.title.trim())
      data.append('description', form.description.trim())
      data.append('type', form.type)
      data.append('subjectId', form.subjectId)
      data.append('visibilityLevel', form.visibilityLevel)
      data.append('file', file)
      return api.proposals.create(data)
    },
    onSuccess: (proposal) => {
      toast.success('Propuesta creada', {
        description: 'Ya está abierta a la revisión de la comunidad.',
      })
      navigate(`/propuestas/${proposal.id}`)
    },
    onError: (error) => setServerError(error.message),
  })

  const update = (key, value) => {
    setForm((prev) => ({ ...prev, [key]: value }))
    setErrors((prev) => ({ ...prev, [key]: '' }))
    setServerError('')
  }

  const pickFile = (nextFile) => {
    if (!nextFile) return
    const looksPdf =
      nextFile.type === 'application/pdf' || nextFile.name.toLowerCase().endsWith('.pdf')
    if (!looksPdf) {
      setErrors((prev) => ({ ...prev, file: 'El material debe ser un archivo PDF.' }))
      return
    }
    if (nextFile.size > MAX_SIZE) {
      setErrors((prev) => ({ ...prev, file: 'El PDF no puede superar los 10 MB.' }))
      return
    }
    setFile(nextFile)
    setErrors((prev) => ({ ...prev, file: '' }))
  }

  const validate = () => {
    const next = {}
    if (!form.title.trim()) next.title = 'Ponle un título reconocible.'
    else if (form.title.trim().length > 150) next.title = 'Máximo 150 caracteres.'
    if (!form.description.trim()) next.description = 'Explica brevemente qué contiene.'
    else if (form.description.trim().length > 2000) next.description = 'Máximo 2000 caracteres.'
    if (!form.subjectId) next.subjectId = 'Elige la asignatura.'
    if (!file) next.file = 'Adjunta el PDF que quieres proponer.'
    setErrors(next)
    return Object.keys(next).length === 0
  }

  const submit = (event) => {
    event.preventDefault()
    setServerError('')
    if (!validate()) return
    uploadMutation.mutate()
  }

  return (
    <div className="site-shell">
      <PageHeader
        eyebrow="Compartir material"
        title="Nuevo material"
        description="La propuesta se revisa antes de publicarse en el catálogo."
      />

      <form onSubmit={submit} className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_21rem] lg:items-start">
        <Reveal>
          <div className="glass rounded-5xl p-6 sm:p-8">
            <div className="space-y-5">
              <Input
                label="Título del material"
                placeholder="Ej. Apuntes del Tema 4"
                value={form.title}
                onChange={(event) => update('title', event.target.value)}
                maxLength={150}
                error={errors.title}
                hint={`${form.title.length} / 150 caracteres`}
                required
              />
              <Textarea
                label="Descripción"
                placeholder="Qué contiene, para qué asignatura sirve, si incluye ejercicios resueltos…"
                value={form.description}
                onChange={(event) => update('description', event.target.value)}
                maxLength={2000}
                error={errors.description}
                hint={`${form.description.length} / 2000 caracteres`}
                rows={5}
                required
              />

              <div className="grid gap-4 sm:grid-cols-2">
                <Select
                  label="Tipo de material"
                  value={form.type}
                  onChange={(event) => update('type', event.target.value)}
                  required
                >
                  {Object.entries(FILE_TYPES).map(([value, meta]) => (
                    <option key={value} value={value}>
                      {meta.label}
                    </option>
                  ))}
                </Select>

                <Select
                  label="Asignatura"
                  value={form.subjectId}
                  onChange={(event) => update('subjectId', event.target.value)}
                  error={errors.subjectId}
                  required
                >
                  <option value="">Elige asignatura</option>
                  {courses.map((course) => {
                    const courseSubjects = subjects.filter(
                      (subject) => String(subject.courseId) === String(course.id),
                    )
                    if (!courseSubjects.length) return null
                    return (
                      <optgroup key={course.id} label={course.name}>
                        {courseSubjects.map((subject) => (
                          <option key={subject.id} value={subject.id}>
                            {subject.name} · {subject.code}
                          </option>
                        ))}
                      </optgroup>
                    )
                  })}
                </Select>
              </div>

              <div>
                <p className="field-label">Archivo PDF</p>
                <input
                  ref={fileInput}
                  type="file"
                  accept=".pdf,application/pdf"
                  onChange={(event) => pickFile(event.target.files?.[0])}
                  className="sr-only"
                  aria-label="Seleccionar archivo PDF"
                />
                {file ? (
                  <div className="flex items-center gap-4 rounded-3xl border border-aqua-200/70 bg-aqua-50/55 p-4">
                    <span className="grid size-11 shrink-0 place-items-center rounded-2xl bg-white/75 text-aqua-700">
                      <FilePdf size={23} weight="duotone" aria-hidden="true" />
                    </span>
                    <div className="min-w-0 flex-1">
                      <p className="truncate text-sm font-semibold text-ink">{file.name}</p>
                      <p className="mt-0.5 text-[0.65rem] text-ink-soft">{formatBytes(file.size)}</p>
                    </div>
                    <button
                      type="button"
                      onClick={() => {
                        setFile(null)
                        if (fileInput.current) fileInput.current.value = ''
                      }}
                      aria-label="Quitar archivo"
                      className="grid size-8 place-items-center rounded-full bg-white/70 text-ink-soft transition-colors hover:text-[#8b4b43]"
                    >
                      <X size={15} weight="bold" aria-hidden="true" />
                    </button>
                  </div>
                ) : (
                  <button
                    type="button"
                    onClick={() => fileInput.current?.click()}
                    onDragOver={(event) => {
                      event.preventDefault()
                      setDragging(true)
                    }}
                    onDragLeave={() => setDragging(false)}
                    onDrop={(event) => {
                      event.preventDefault()
                      setDragging(false)
                      pickFile(event.dataTransfer.files?.[0])
                    }}
                    className={cn(
                      'flex w-full flex-col items-center rounded-3xl border border-dashed px-6 py-9 text-center transition-all duration-300',
                      dragging
                        ? 'border-aqua-500 bg-aqua-50/75'
                        : 'border-ink/15 bg-white/30 hover:border-aqua-300 hover:bg-white/50',
                    )}
                  >
                    <span className="grid size-11 place-items-center rounded-full border border-ink/8 bg-white/65 text-aqua-700">
                      <UploadSimple size={21} weight="duotone" aria-hidden="true" />
                    </span>
                    <span className="mt-3 text-sm font-semibold text-ink">
                      Arrastra el PDF aquí o haz clic para elegirlo
                    </span>
                    <span className="mt-1 text-[0.65rem] text-ink-faint">PDF · máximo 10 MB</span>
                  </button>
                )}
                {errors.file && <p className="mt-1.5 text-xs text-[#934b43]">{errors.file}</p>}
              </div>

              <div>
                <p className="field-label">Visibilidad de tu identidad</p>
                <div className="grid gap-2 sm:grid-cols-3">
                  {visibilityOptions.map((option) => {
                    const active = form.visibilityLevel === option.value
                    return (
                      <button
                        key={option.value}
                        type="button"
                        onClick={() => update('visibilityLevel', option.value)}
                        aria-pressed={active}
                        className={cn(
                          'rounded-2xl border p-4 text-left transition-all duration-300',
                          active
                            ? 'border-aqua-300 bg-aqua-50/75 shadow-soft'
                            : 'border-ink/8 bg-white/25 hover:border-aqua-200 hover:bg-white/50',
                        )}
                      >
                        <div className="flex items-center justify-between">
                          <option.icon
                            size={17}
                            className={active ? 'text-aqua-700' : 'text-ink-faint'}
                            aria-hidden="true"
                          />
                          {active && (
                            <span className="grid size-4 place-items-center rounded-full bg-aqua-700 text-white">
                              <Check size={10} weight="bold" aria-hidden="true" />
                            </span>
                          )}
                        </div>
                        <p className="mt-3 text-xs font-semibold text-ink">{option.title}</p>
                        <p className="mt-1 text-[0.62rem] leading-5 text-ink-faint">{option.copy}</p>
                      </button>
                    )
                  })}
                </div>
                <p className="mt-2 flex items-start gap-1.5 text-[0.62rem] leading-5 text-ink-faint">
                  <ShieldCheck size={13} className="mt-0.5 shrink-0 text-aqua-700" aria-hidden="true" />
                  {VISIBILITY[form.visibilityLevel].description}
                </p>
              </div>

              {serverError && (
                <div
                  role="alert"
                  className="rounded-2xl border border-[#e7c7c2] bg-[#fbefed] px-4 py-3 text-xs leading-5 text-[#86483f]"
                >
                  {serverError}
                </div>
              )}

              <div className="flex flex-col justify-between gap-3 border-t border-ink/8 pt-5 sm:flex-row sm:items-center">
                <p className="text-[0.65rem] leading-5 text-ink-faint">
                  Al proponerlo confirmas que tienes derecho a compartir este contenido.
                </p>
                <Button
                  type="submit"
                  loading={uploadMutation.isPending}
                  icon={!uploadMutation.isPending ? <ArrowRight size={16} weight="bold" /> : undefined}
                  className="shrink-0"
                >
                  Enviar a revisión
                </Button>
              </div>
            </div>
          </div>
        </Reveal>

        <aside className="space-y-4 lg:sticky lg:top-24">
          <Reveal delay={0.08}>
            <div className="rounded-3xl border border-ink/8 bg-white p-6">
              <p className="eyebrow text-ink-faint">Qué ocurre después</p>
              <div className="mt-5 space-y-4">
                {[
                  ['01', 'Revisión', 'La propuesta queda abierta durante siete días.'],
                  ['02', 'Consenso', 'Necesita quórum y un porcentaje mínimo de apoyo.'],
                  ['03', 'Publicación', 'Si se aprueba, aparece en el catálogo.'],
                ].map(([number, title, copy]) => (
                  <div key={number} className="flex gap-3">
                    <span className="mt-0.5 text-[0.58rem] font-semibold tabular-nums text-ink-faint">
                      {number}
                    </span>
                    <div>
                      <p className="text-xs font-semibold text-ink">{title}</p>
                      <p className="mt-1 text-[0.65rem] leading-5 text-ink-soft">{copy}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </Reveal>

          <Reveal delay={0.12}>
            <div className="rounded-3xl border border-ink/8 bg-white p-5">
              <p className="flex items-center gap-2 text-xs font-semibold text-ink">
                <Info size={15} className="text-ink-soft" aria-hidden="true" />
                Antes de enviarlo
              </p>
              <ul className="mt-3 space-y-2 text-[0.65rem] leading-5 text-ink-soft">
                <li>· Comprueba que el PDF se abre y está completo.</li>
                <li>· Usa un título que permita encontrarlo después.</li>
                <li>· No incluyas datos personales de terceros.</li>
                <li>· Si ya existe, mejora su descripción en el hilo.</li>
              </ul>
            </div>
          </Reveal>

          {subjectsQuery.isLoading && (
            <Skeleton className="h-24 rounded-4xl" />
          )}
        </aside>
      </form>
    </div>
  )
}
