import { useQuery } from '@tanstack/react-query'
import { BookOpenText } from '@phosphor-icons/react'
import { api } from '../lib/api'
import { Hero } from '../components/home/Hero'
import { CourseBrowser } from '../components/home/CourseBrowser'
import { FileRow } from '../components/file/FileRow'
import { Reveal } from '../components/ui/Reveal'
import { Skeleton } from '../components/ui/Spinner'
import { EmptyState, ErrorState } from '../components/ui/States'
import { Button, TextLink } from '../components/ui/Button'

export function HomePage() {
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

  const files = filesQuery.data || []
  const courses = [...(coursesQuery.data || [])].sort((a, b) => Number(a.code) - Number(b.code))
  const subjects = subjectsQuery.data || []
  const latestFiles = files.slice(0, 8)

  const fileCountBySubject = files.reduce((acc, file) => {
    if (file.subject) acc[file.subject] = (acc[file.subject] || 0) + 1
    return acc
  }, {})

  return (
    <>
      <Hero stats={statsQuery.data} />

      <section className="site-shell pb-14 sm:pb-20">
        <div className="flex items-end justify-between gap-4">
          <div>
            <h2 className="display-tight text-2xl text-ink sm:text-3xl">Últimos archivos</h2>
            <p className="mt-1 text-sm text-ink-soft">Los materiales publicados más recientemente.</p>
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
                <FileRow key={file.id} file={file} />
              ))}
            </div>
          ) : (
            <div className="p-5">
              <EmptyState
                icon={<BookOpenText size={22} weight="duotone" />}
                title="No hay archivos publicados"
                description="Puedes compartir el primero."
                action={
                  <Button to="/subir" className="mt-5">
                    Compartir material
                  </Button>
                }
              />
            </div>
          )}
        </div>
      </section>

      <section className="site-shell pb-20 sm:pb-24">
        <Reveal>
          <div>
            <h2 className="display-tight text-2xl text-ink sm:text-3xl">Cursos y asignaturas</h2>
            <p className="mt-1 text-sm text-ink-soft">
              Selecciona un curso para ver sus asignaturas.
            </p>
          </div>

          <div className="mt-6">
            {coursesQuery.isLoading || subjectsQuery.isLoading ? (
              <div className="space-y-3">
                <Skeleton className="h-9 w-72 rounded-full" />
                <Skeleton className="h-48 rounded-2xl" />
              </div>
            ) : coursesQuery.isError || subjectsQuery.isError ? (
              <ErrorState
                compact
                title="No se pudo cargar el catálogo"
                onRetry={() => {
                  coursesQuery.refetch()
                  subjectsQuery.refetch()
                }}
              />
            ) : (
              <CourseBrowser
                courses={courses}
                subjects={subjects}
                fileCountBySubject={fileCountBySubject}
              />
            )}
          </div>
        </Reveal>
      </section>
    </>
  )
}
