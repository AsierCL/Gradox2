import { useQuery } from '@tanstack/react-query'
import { BookOpenText, GraduationCap } from '@phosphor-icons/react'
import { api } from '../lib/api'
import { useAuth } from '../context/AuthContext'
import { Hero } from '../components/home/Hero'
import { FileRow } from '../components/file/FileRow'
import { DegreeCard } from '../components/degrees/DegreeCard'
import { Reveal } from '../components/ui/Reveal'
import { Skeleton } from '../components/ui/Spinner'
import { EmptyState, ErrorState } from '../components/ui/States'
import { Button, TextLink } from '../components/ui/Button'

export function HomePage() {
  const { degreeId, isAuthenticated } = useAuth()
  const statsQuery = useQuery({
    queryKey: ['stats'],
    queryFn: ({ signal }) => api.stats.get(signal),
    staleTime: 60_000,
  })
  const filesQuery = useQuery({
    queryKey: ['files'],
    queryFn: ({ signal }) => api.files.list(signal),
    staleTime: 30_000,
  })
  const degreesQuery = useQuery({
    queryKey: ['degrees'],
    queryFn: () => api.degrees.list(),
    staleTime: 10 * 60_000,
  })

  const files = filesQuery.data || []
  const degrees = degreesQuery.data || []
  const latestFiles = files.slice(0, 8)
  const filesByDegree = files.reduce((acc, file) => {
    if (file.degreeId === null || file.degreeId === undefined) return acc
    const key = String(file.degreeId)
    acc[key] = (acc[key] || 0) + 1
    return acc
  }, {})

  return (
    <>
      <Hero stats={statsQuery.data} degrees={degrees} />

      <section className="site-shell pb-14 sm:pb-20">
        <div className="flex items-end justify-between gap-4">
          <div>
            <h2 className="display-tight text-2xl text-ink sm:text-3xl">Últimos archivos</h2>
            <p className="mt-1 text-sm text-ink-soft">
              Material reciente de todos los grados de la Escuela.
            </p>
          </div>
          <TextLink to="/explorar" className="shrink-0">
            Ver todos
          </TextLink>
        </div>

        <div className="glass mt-5 overflow-hidden rounded-2xl">
          {filesQuery.isLoading ? (
            <div className="divide-y divide-ink/8">
              {Array.from({ length: 6 }).map((_, index) => (
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
            <div className="p-5">
              <ErrorState compact onRetry={() => filesQuery.refetch()} />
            </div>
          ) : latestFiles.length ? (
            <div className="divide-y divide-ink/8">
              {latestFiles.map((file) => (
                <FileRow key={file.id} file={file} showDegree />
              ))}
            </div>
          ) : (
            <div className="p-5">
              <EmptyState
                icon={<BookOpenText size={22} weight="duotone" />}
                title="No hay archivos publicados"
                description="Puedes compartir el primero desde tu grado."
                action={
                  <Button to="/grados" className="mt-5">
                    Elegir mi grado
                  </Button>
                }
              />
            </div>
          )}
        </div>
      </section>

      <section className="site-shell pb-20 sm:pb-24">
        <Reveal>
          <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
            <div>
              <p className="eyebrow text-aqua-700">Catálogo</p>
              <h2 className="display-tight mt-2 text-2xl text-ink sm:text-3xl">
                Elige tu grado
              </h2>
              <p className="mt-1 max-w-2xl text-sm text-ink-soft">
                Tres planes de la Escuela de Ingeniería. El grado define dónde puedes participar;
                el material de cualquier grado se puede leer.
              </p>
            </div>
            <TextLink to="/grados" className="shrink-0">
              Ver todos
            </TextLink>
          </div>

          <div className="mt-6">
            {degreesQuery.isLoading ? (
              <div className="grid gap-4 md:grid-cols-3">
                {Array.from({ length: 3 }).map((_, index) => (
                  <Skeleton key={index} className="h-64 rounded-4xl" />
                ))}
              </div>
            ) : degreesQuery.isError ? (
              <ErrorState
                compact
                title="No se pudo cargar el catálogo de grados"
                onRetry={() => degreesQuery.refetch()}
              />
            ) : degrees.length ? (
              <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
                {degrees.map((degree, index) => (
                  <Reveal key={degree.id} delay={Math.min(index * 0.04, 0.12)}>
                    <DegreeCard
                      degree={degree}
                      own={
                        isAuthenticated &&
                        degreeId !== null &&
                        String(degree.id) === String(degreeId)
                      }
                      fileCount={filesByDegree[String(degree.id)] || 0}
                    />
                  </Reveal>
                ))}
              </div>
            ) : (
              <EmptyState
                icon={<GraduationCap size={22} weight="duotone" />}
                title="No hay grados disponibles"
                description="El catálogo no ha devuelto resultados."
              />
            )}
          </div>
        </Reveal>
      </section>
    </>
  )
}
