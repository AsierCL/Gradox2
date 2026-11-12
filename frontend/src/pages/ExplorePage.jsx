import { useMemo } from 'react'
import { useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import {
  BookOpenText,
  FunnelSimple,
  MagnifyingGlass,
  SlidersHorizontal,
  X,
} from '@phosphor-icons/react'
import { api } from '../lib/api'
import { PageHeader } from '../components/ui/PageHeader'
import { FileRow } from '../components/file/FileRow'
import { Reveal } from '../components/ui/Reveal'
import { Skeleton } from '../components/ui/Spinner'
import { EmptyState, ErrorState } from '../components/ui/States'
import { Button } from '../components/ui/Button'
import { formatNumber } from '../lib/format'
import { FILE_TYPES } from '../lib/constants'
import { cn } from '../lib/cn'

const types = [
  { value: '', label: 'Todo' },
  { value: 'APUNTES', label: 'Apuntes' },
  { value: 'EXAMEN', label: 'Exámenes' },
  { value: 'EJERCICIO', label: 'Ejercicios' },
  { value: 'OTRO', label: 'Otros' },
]

const searchAliases = {
  APUNTES: 'apuntes apunte teoria temario',
  EXAMEN: 'examen examenes prueba',
  EJERCICIO: 'ejercicio ejercicios practica practicas resuelto resueltos',
  OTRO: 'otro material',
}

const normalizeSearch = (value = '') =>
  value
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()

export function ExplorePage() {
  const [params, setParams] = useSearchParams()
  const query = params.get('q') || ''
  const type = params.get('tipo') || ''
  const course = params.get('curso') || ''
  const subjectParam = params.get('asignatura') || ''
  const sort = params.get('orden') || 'relevancia'

  const filesQuery = useQuery({
    queryKey: ['files'],
    queryFn: ({ signal }) => api.files.list(signal),
    staleTime: 30_000,
  })
  const subjectsQuery = useQuery({
    queryKey: ['subjects'],
    queryFn: () => api.subjects.list(),
    staleTime: 5 * 60_000,
  })
  const coursesQuery = useQuery({
    queryKey: ['courses'],
    queryFn: () => api.courses.list(),
    staleTime: 5 * 60_000,
  })

  const files = useMemo(() => filesQuery.data || [], [filesQuery.data])
  const subjects = useMemo(() => subjectsQuery.data || [], [subjectsQuery.data])
  const courses = [...(coursesQuery.data || [])].sort((a, b) => Number(a.code) - Number(b.code))

  const subjectsById = useMemo(
    () => new Map(subjects.map((subject) => [String(subject.id), subject])),
    [subjects],
  )
  const subjectsByName = useMemo(
    () => new Map(subjects.map((subject) => [subject.name, subject])),
    [subjects],
  )

  const availableSubjects = course
    ? subjects.filter((subject) => String(subject.courseId) === String(course))
    : subjects

  const update = (key, value) => {
    const next = new URLSearchParams(params)
    if (value) next.set(key, value)
    else next.delete(key)
    if (key === 'curso') next.delete('asignatura')
    setParams(next, { replace: true })
  }

  const clearFilters = () => {
    const next = new URLSearchParams()
    if (query) next.set('q', query)
    setParams(next, { replace: true })
  }

  const filtered = useMemo(() => {
    const normalized = normalizeSearch(query.trim())
    let list = files.filter((file) => {
      if (type && file.fileType !== type) return false
      const subject = subjectsByName.get(file.subject)
      if (course && String(subject?.courseId) !== String(course)) return false
      if (subjectParam && String(subject?.id) !== String(subjectParam)) return false
      if (!normalized) return true
      const haystack = normalizeSearch(
        [
          file.fileName,
          file.description,
          file.subject,
          file.uploaderUsername,
          FILE_TYPES[file.fileType]?.label,
          searchAliases[file.fileType],
        ]
          .filter(Boolean)
          .join(' '),
      )
      return haystack.includes(normalized)
    })

    if (sort === 'valoracion') {
      list = [...list].sort((a, b) => Number(b.score || 0) - Number(a.score || 0))
    } else if (sort === 'nombre') {
      list = [...list].sort((a, b) => (a.fileName || '').localeCompare(b.fileName || '', 'es'))
    } else if (sort === 'asignatura') {
      list = [...list].sort((a, b) => (a.subject || '').localeCompare(b.subject || '', 'es'))
    }
    return list
  }, [files, type, course, subjectParam, query, sort, subjectsByName])

  const activeFilters = [type, course, subjectParam].filter(Boolean).length

  return (
    <div className="site-shell">
      <PageHeader
        eyebrow="Catálogo"
        title="Explorar materiales"
        description="Apuntes, exámenes y ejercicios de Ingeniería Informática."
      />

      <Reveal>
        <div className="glass rounded-4xl p-3 sm:p-4">
          <div className="flex flex-col gap-3 lg:flex-row lg:items-center">
            <div className="flex min-w-0 flex-1 items-center gap-3 rounded-3xl border border-white/70 bg-white/50 px-4">
              <MagnifyingGlass size={19} className="shrink-0 text-aqua-700" aria-hidden="true" />
              <input
                value={query}
                onChange={(event) => update('q', event.target.value)}
                placeholder="Buscar material"
                aria-label="Buscar material"
                className="h-12 w-full min-w-0 bg-transparent text-sm text-ink outline-none placeholder:text-ink-faint"
              />
              {query && (
                <button
                  type="button"
                  onClick={() => update('q', '')}
                  aria-label="Borrar búsqueda"
                  className="grid size-7 shrink-0 place-items-center rounded-full text-ink-faint transition-colors hover:bg-white hover:text-ink"
                >
                  <X size={14} weight="bold" aria-hidden="true" />
                </button>
              )}
            </div>
            <div className="flex items-center gap-2">
              <span className="hidden shrink-0 items-center gap-1.5 px-2 text-xs text-ink-faint sm:flex">
                <SlidersHorizontal size={15} aria-hidden="true" /> Ordenar
              </span>
              <select
                value={sort}
                onChange={(event) => update('orden', event.target.value)}
                aria-label="Ordenar resultados"
                className="h-12 rounded-full border border-ink/10 bg-white/55 px-4 text-xs font-semibold text-ink-soft outline-none transition-colors hover:bg-white/80"
              >
                <option value="relevancia">Relevancia</option>
                <option value="valoracion">Mejor valorados</option>
                <option value="nombre">Título A–Z</option>
                <option value="asignatura">Asignatura A–Z</option>
              </select>
            </div>
          </div>

          <div className="mt-3 flex flex-col gap-3 border-t border-ink/8 pt-3 lg:flex-row lg:items-center lg:justify-between">
            <div className="hide-scrollbar flex flex-wrap items-center gap-1.5 sm:flex-nowrap sm:overflow-x-auto">
              {types.map((item) => (
                <button
                  key={item.value}
                  type="button"
                  onClick={() => update('tipo', item.value)}
                  className={cn(
                    'shrink-0 rounded-full px-3.5 py-2 text-[0.72rem] font-semibold transition-all duration-300',
                    type === item.value
                      ? 'bg-ink text-white shadow-soft'
                      : 'text-ink-soft hover:bg-white/65 hover:text-ink',
                  )}
                >
                  {item.label}
                </button>
              ))}
            </div>

            <div className="grid gap-2 sm:grid-cols-2 lg:flex lg:items-center">
              <div className="flex items-center gap-2">
                <FunnelSimple size={15} className="text-ink-faint" aria-hidden="true" />
                <select
                  value={course}
                  onChange={(event) => update('curso', event.target.value)}
                  aria-label="Filtrar por curso"
                  className="h-10 min-w-0 flex-1 rounded-full border border-ink/10 bg-white/45 px-3 text-[0.72rem] font-semibold text-ink-soft outline-none hover:bg-white/70 lg:min-w-36"
                >
                  <option value="">Todos los cursos</option>
                  {courses.map((item) => (
                    <option key={item.id} value={item.id}>
                      {item.name}
                    </option>
                  ))}
                </select>
              </div>
              <select
                value={subjectParam}
                onChange={(event) => update('asignatura', event.target.value)}
                aria-label="Filtrar por asignatura"
                className="h-10 min-w-0 rounded-full border border-ink/10 bg-white/45 px-3 text-[0.72rem] font-semibold text-ink-soft outline-none hover:bg-white/70 lg:min-w-44"
              >
                <option value="">Todas las asignaturas</option>
                {availableSubjects.map((item) => (
                  <option key={item.id} value={item.id}>
                    {item.name}
                  </option>
                ))}
              </select>
              {(activeFilters > 0 || query) && (
                <button
                  type="button"
                  onClick={clearFilters}
                  className="inline-flex items-center justify-center gap-1.5 rounded-full px-3 py-2 text-[0.7rem] font-semibold text-ink-soft transition-colors hover:text-ink"
                >
                  <X size={13} weight="bold" aria-hidden="true" /> Limpiar
                </button>
              )}
            </div>
          </div>
        </div>
      </Reveal>

      <div className="mt-8 flex items-end justify-between gap-4 pb-5">
        <div>
          <p className="eyebrow text-aqua-700">Resultados</p>
          <p className="mt-2 text-sm text-ink-soft">
            {filesQuery.isLoading
              ? 'Abriendo el catálogo…'
              : `${formatNumber(filtered.length)} ${
                  filtered.length === 1 ? 'material encontrado' : 'materiales encontrados'
                }`}
            {subjectParam && subjectsById.get(subjectParam) && (
              <>
                {' '}
                en{' '}
                <span className="font-semibold text-ink">
                  {subjectsById.get(subjectParam).name}
                </span>
              </>
            )}
          </p>
        </div>
        {sort !== 'relevancia' && (
          <p className="hidden text-[0.68rem] text-ink-faint sm:block">
            Ordenado por {sort === 'valoracion' ? 'valoración' : sort === 'nombre' ? 'título' : 'asignatura'}
          </p>
        )}
      </div>

      {filesQuery.isLoading ? (
        <div className="divide-y divide-ink/8 overflow-hidden rounded-2xl border border-ink/8 bg-white">
          {Array.from({ length: 8 }).map((_, index) => (
            <div key={index} className="flex items-center gap-4 px-5 py-4">
              <Skeleton className="h-6 w-16 rounded-full" />
              <div className="flex-1 space-y-2">
                <Skeleton className="h-4 w-2/3" />
                <Skeleton className="h-3 w-1/3" />
              </div>
            </div>
          ))}
        </div>
      ) : filesQuery.isError ? (
        <ErrorState onRetry={() => filesQuery.refetch()} />
      ) : filtered.length ? (
        <div className="glass divide-y divide-ink/8 overflow-hidden rounded-2xl">
          {filtered.map((file) => (
            <FileRow key={file.id} file={file} />
          ))}
        </div>
      ) : (
        <EmptyState
          icon={<BookOpenText size={22} weight="duotone" />}
          title="No hay nada con esos filtros"
          description={
            query
              ? `No encontramos materiales para “${query}”. Prueba con una asignatura o elimina algún filtro.`
              : 'Todavía no hay materiales que coincidan con esta combinación.'
          }
          action={
            <Button onClick={clearFilters} variant="ghost" className="mt-5">
              Limpiar filtros
            </Button>
          }
        />
      )}

      <div className="mt-16 flex flex-col items-start justify-between gap-5 rounded-3xl border border-ink/8 bg-white px-6 py-6 sm:flex-row sm:items-center">
        <div>
          <p className="text-sm font-semibold text-ink">Comparte material</p>
          <p className="mt-1 text-sm text-ink-soft">Publica apuntes, exámenes o ejercicios.</p>
        </div>
        <Button to="/subir">Compartir material</Button>
      </div>
    </div>
  )
}
