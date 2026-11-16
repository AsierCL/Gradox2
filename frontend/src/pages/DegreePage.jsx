import { useQuery } from '@tanstack/react-query'
import { BookOpenText, FileText, GraduationCap, UploadSimple } from '@phosphor-icons/react'
import { useParams } from 'react-router-dom'
import { api } from '../lib/api'
import { useAuth } from '../context/AuthContext'
import { useDegreeScope } from '../hooks/useDegreeScope'
import { formatNumber } from '../lib/format'
import { Button } from '../components/ui/Button'
import { PageHeader } from '../components/ui/PageHeader'
import { Reveal } from '../components/ui/Reveal'
import { Skeleton } from '../components/ui/Spinner'
import { ErrorState, NotFoundInline } from '../components/ui/States'
import { DegreeSwitcher } from '../components/degrees/DegreeSwitcher'
import { ReadOnlyNotice } from '../components/common/ReadOnlyNotice'
import { CourseBrowser } from '../components/home/CourseBrowser'

export function DegreePage() {
  const { degreeCode } = useParams()
  const { isAuthenticated, isPlatformAdmin, degreeId, profileLoading } = useAuth()

  const degreesQuery = useQuery({
    queryKey: ['degrees'],
    queryFn: () => api.degrees.list(),
    staleTime: 10 * 60_000,
  })
  const degrees = degreesQuery.data || []
  const degree = degrees.find(
    (item) => String(item.code).toUpperCase() === String(degreeCode).toUpperCase(),
  )
  const scope = useDegreeScope(degree?.id)
  const ownDegree =
    isAuthenticated && degreeId !== null && degree && String(degreeId) === String(degree.id)

  const coursesQuery = useQuery({
    queryKey: ['degree-courses', degree?.id],
    queryFn: () => api.degrees.courses(degree.id),
    enabled: Boolean(degree?.id),
    staleTime: 5 * 60_000,
  })
  const filesQuery = useQuery({
    queryKey: ['files'],
    queryFn: ({ signal }) => api.files.list(signal),
    staleTime: 30_000,
  })

  const courses = coursesQuery.data || []
  const subjects = courses.flatMap((course) =>
    (course.subjects || []).map((subject) => ({
      ...subject,
      courseId: course.id,
      courseName: course.name,
    })),
  )
  const degreeFiles = (filesQuery.data || []).filter(
    (file) => degree && String(file.degreeId) === String(degree.id),
  )
  const fileCountBySubject = degreeFiles.reduce((acc, file) => {
    if (file.subjectId === null || file.subjectId === undefined) return acc
    acc[file.subjectId] = (acc[file.subjectId] || 0) + 1
    return acc
  }, {})

  if (degreesQuery.isLoading) {
    return (
      <div className="site-shell pt-10">
        <Skeleton className="h-10 w-64" />
        <Skeleton className="mt-5 h-5 w-96 max-w-full" />
        <Skeleton className="mt-8 h-72 rounded-4xl" />
      </div>
    )
  }

  if (degreesQuery.isError) {
    return (
      <div className="site-shell py-16">
        <ErrorState onRetry={() => degreesQuery.refetch()} />
      </div>
    )
  }

  if (!degree) {
    return (
      <div className="site-shell py-16">
        <NotFoundInline
          message="El grado no existe o ya no está disponible en el catálogo."
          action={{ to: '/grados', label: 'Ver todos los grados' }}
        />
      </div>
    )
  }

  return (
    <div className="site-shell">
      <PageHeader
        eyebrow={`Grado · ${degree.code}`}
        title={degree.name}
        description="Consulta el plan de estudios y entra en cualquier asignatura para ver su material. La lectura es libre; la participación se limita al grado propio."
        actions={
          ownDegree && !isPlatformAdmin ? (
            <Button to="/subir" icon={<UploadSimple size={16} weight="bold" />}>
              Compartir material
            </Button>
          ) : (
            <Button to={`/explorar?grado=${degree.id}`} variant="ghost" icon={<BookOpenText size={16} />}>
              Explorar asignaturas
            </Button>
          )
        }
      />

      <div className="mb-6">
        <DegreeSwitcher degrees={degrees} activeCode={degree.code} />
      </div>

      {scope.isReadOnly && (
        <ReadOnlyNotice
          className="mb-6"
          title={`Estás viendo ${degree.name} en modo lectura`}
          description="Puedes consultar el catálogo y descargar materiales. Solo los estudiantes de este grado pueden votar, comentar o proponer cambios."
        />
      )}

      {scope.needsDegree && !profileLoading && (
        <ReadOnlyNotice
          className="mb-6"
          icon="lock"
          title="Tu cuenta no tiene grado asignado"
          description="Puedes seguir leyendo, pero necesitas que administración corrija tu grado antes de participar."
        />
      )}

      <Reveal>
        <div className="mb-4 grid gap-3 sm:grid-cols-3">
          {[
            ['Cursos', degree.coursesCount, GraduationCap],
            ['Asignaturas', subjects.length || '—', BookOpenText],
            ['Materiales', degreeFiles.length, FileText],
          ].map(([label, value, Icon]) => (
            <div key={label} className="glass-soft rounded-3xl p-5">
              <span className="grid size-9 place-items-center rounded-full bg-white/60 text-aqua-700">
                <Icon size={17} weight="duotone" aria-hidden="true" />
              </span>
              <p className="display-tight mt-4 text-3xl tabular-nums text-ink">
                {typeof value === 'number' ? formatNumber(value) : value}
              </p>
              <p className="mt-1 text-[0.65rem] text-ink-faint">{label}</p>
            </div>
          ))}
        </div>
      </Reveal>

      <div className="mt-6">
        {coursesQuery.isLoading ? (
          <div className="space-y-3">
            <Skeleton className="h-10 w-80 max-w-full rounded-full" />
            <Skeleton className="h-64 rounded-3xl" />
          </div>
        ) : coursesQuery.isError ? (
          <ErrorState
            title="No se pudo cargar el plan de estudios"
            onRetry={() => coursesQuery.refetch()}
          />
        ) : (
          <CourseBrowser
            key={degree.id}
            courses={courses}
            fileCountBySubject={fileCountBySubject}
          />
        )}
      </div>

      <Reveal delay={0.06}>
        <div className="glass-soft mt-6 flex flex-col items-start justify-between gap-5 rounded-4xl p-6 sm:flex-row sm:items-center sm:p-8">
          <div>
            <p className="text-sm font-semibold text-ink">¿Buscas algo concreto?</p>
            <p className="mt-1 max-w-xl text-xs leading-6 text-ink-soft">
              Puedes filtrar por grado, curso, asignatura y tipo de material. Los nombres de
              asignatura se repiten entre grados: el catálogo siempre usa el identificador.
            </p>
          </div>
          <div className="flex flex-wrap gap-2">
            <Button to={`/explorar?grado=${degree.id}`} variant="ghost">
              Explorar {degree.name}
            </Button>
            {ownDegree && !isPlatformAdmin && <Button to="/subir">Compartir material</Button>}
          </div>
        </div>
      </Reveal>
    </div>
  )
}
