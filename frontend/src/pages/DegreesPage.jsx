import { useQuery } from '@tanstack/react-query'
import { GraduationCap, MagnifyingGlass } from '@phosphor-icons/react'
import { api } from '../lib/api'
import { useAuth } from '../context/AuthContext'
import { Button } from '../components/ui/Button'
import { PageHeader } from '../components/ui/PageHeader'
import { Reveal } from '../components/ui/Reveal'
import { Skeleton } from '../components/ui/Spinner'
import { EmptyState, ErrorState } from '../components/ui/States'
import { DegreeCard } from '../components/degrees/DegreeCard'

export function DegreesPage() {
  const { degreeId, isAuthenticated } = useAuth()

  const degreesQuery = useQuery({
    queryKey: ['degrees'],
    queryFn: () => api.degrees.list(),
    staleTime: 10 * 60_000,
  })
  const filesQuery = useQuery({
    queryKey: ['files'],
    queryFn: ({ signal }) => api.files.list(signal),
    staleTime: 30_000,
  })

  const degrees = degreesQuery.data || []
  const files = filesQuery.data || []
  const filesByDegree = files.reduce((acc, file) => {
    if (file.degreeId === null || file.degreeId === undefined) return acc
    const key = String(file.degreeId)
    acc[key] = (acc[key] || 0) + 1
    return acc
  }, {})

  return (
    <div className="site-shell">
      <PageHeader
        eyebrow="Escuela de Ingeniería"
        title="Grados"
        description="Cada grado es una comunidad de escritura. Puedes leer y descargar material de cualquiera; para votar, comentar o proponer necesitas pertenecer a ese grado."
        actions={
          <Button to="/explorar" variant="ghost" icon={<MagnifyingGlass size={16} />}>
            Explorar materiales
          </Button>
        }
      />

      {degreesQuery.isLoading ? (
        <div className="grid gap-4 md:grid-cols-3">
          {Array.from({ length: 3 }).map((_, index) => (
            <Skeleton key={index} className="h-64 rounded-4xl" />
          ))}
        </div>
      ) : degreesQuery.isError ? (
        <ErrorState onRetry={() => degreesQuery.refetch()} />
      ) : degrees.length ? (
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {degrees.map((degree, index) => (
            <Reveal key={degree.id} delay={Math.min(index * 0.04, 0.12)}>
              <DegreeCard
                degree={degree}
                own={isAuthenticated && degreeId !== null && String(degree.id) === String(degreeId)}
                fileCount={filesByDegree[String(degree.id)] || 0}
              />
            </Reveal>
          ))}
        </div>
      ) : (
        <EmptyState
          icon={<GraduationCap size={22} weight="duotone" />}
          title="No hay grados disponibles"
          description="El catálogo de grados no ha devuelto resultados. Vuelve a intentarlo en unos minutos."
        />
      )}

      <Reveal delay={0.08}>
        <div className="glass-soft mt-8 flex flex-col items-start justify-between gap-5 rounded-4xl p-6 sm:flex-row sm:items-center sm:p-8">
          <div className="flex items-start gap-4">
            <span className="grid size-11 shrink-0 place-items-center rounded-full bg-white/65 text-aqua-700">
              <GraduationCap size={22} weight="duotone" aria-hidden="true" />
            </span>
            <div>
              <p className="text-sm font-semibold text-ink">Un grado, una comunidad</p>
              <p className="mt-1 max-w-xl text-xs leading-6 text-ink-soft">
                El grado se elige al registrarse y es permanente. Define dónde puedes participar,
                no lo que puedes leer. Si necesitas corregirlo, la administración de plataforma
                puede hacerlo de forma auditada.
              </p>
            </div>
          </div>
          {!isAuthenticated && (
            <Button to="/registro" className="shrink-0">
              Crear cuenta
            </Button>
          )}
        </div>
      </Reveal>
    </div>
  )
}
