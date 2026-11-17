import { useState } from 'react'
import { Link } from 'react-router-dom'
import { ArrowRight } from '@phosphor-icons/react'
import { cn } from '../../lib/cn'
import { creditsLabel, semesterLabel } from '../../lib/degree'
import { SUBJECT_TYPES } from '../../lib/constants'
import { formatNumber } from '../../lib/format'

export function CourseBrowser({
  courses = [],
  fileCountBySubject = {},
  selectedCourseId,
  onSelectCourse,
}) {
  const [internalId, setInternalId] = useState(() => courses[0]?.id ?? null)
  const activeId = selectedCourseId !== undefined ? selectedCourseId : internalId
  const selected =
    courses.find((course) => String(course.id) === String(activeId)) || courses[0]

  const selectCourse = (courseId) => {
    setInternalId(courseId)
    onSelectCourse?.(courseId)
  }

  if (!courses.length) {
    return (
      <div className="glass-soft rounded-3xl p-6 text-sm text-ink-soft">
        Este grado todavía no tiene cursos publicados.
      </div>
    )
  }

  const subjects = selected?.subjects || []

  return (
    <div className="glass min-w-0 rounded-3xl p-5 sm:p-7">
      <div className="hide-scrollbar flex gap-1.5 overflow-x-auto border-b border-ink/8 pb-4">
        {courses.map((course) => {
          const active = String(course.id) === String(selected?.id)
          return (
            <button
              key={course.id}
              type="button"
              onClick={() => selectCourse(course.id)}
              aria-label={course.name}
              aria-pressed={active}
              className={cn(
                'shrink-0 rounded-full px-4 py-2 text-xs font-semibold transition-colors',
                active ? 'bg-ink text-white' : 'text-ink-soft hover:bg-white hover:text-ink',
              )}
            >
              <span className="sm:hidden">{course.code}º</span>
              <span className="hidden sm:inline">{course.name}</span>
            </button>
          )
        })}
      </div>

      {subjects.length ? (
        <div className="mt-2 grid min-w-0 gap-x-10 sm:grid-cols-2 lg:grid-cols-3">
          {subjects.map((subject) => {
            const count = fileCountBySubject[subject.id] || 0
            return (
              <Link
                key={subject.id}
                to={`/explorar?grado=${selected.degreeId}&curso=${selected.id}&asignatura=${subject.id}`}
                className="group flex min-w-0 flex-col gap-1 border-b border-ink/8 py-3.5 transition-colors hover:text-ink"
              >
                <span className="flex min-w-0 items-center justify-between gap-3">
                  <span className="truncate text-sm text-ink-soft transition-colors group-hover:text-ink">
                    {subject.name}
                  </span>
                  <span className="flex shrink-0 items-center gap-2 text-[0.65rem] text-ink-faint">
                    {count > 0
                      ? `${formatNumber(count)} ${count === 1 ? 'archivo' : 'archivos'}`
                      : null}
                    <ArrowRight
                      size={13}
                      weight="bold"
                      aria-hidden="true"
                      className="transition-transform duration-200 group-hover:translate-x-0.5"
                    />
                  </span>
                </span>
                <span className="flex flex-wrap items-center gap-x-2 gap-y-1 text-[0.6rem] text-ink-faint">
                  <span>{subject.code}</span>
                  <span aria-hidden="true">·</span>
                  <span>{creditsLabel(subject.credits)}</span>
                  <span aria-hidden="true">·</span>
                  <span>{semesterLabel(subject.semester)}</span>
                  {subject.subjectType && (
                    <>
                      <span aria-hidden="true">·</span>
                      <span
                        className={cn(
                          'rounded-full px-2 py-0.5',
                          SUBJECT_TYPES[subject.subjectType] || 'bg-white/60',
                        )}
                      >
                        {subject.subjectType}
                      </span>
                    </>
                  )}
                </span>
              </Link>
            )
          })}
        </div>
      ) : (
        <p className="py-8 text-center text-sm text-ink-soft">
          Este curso todavía no tiene asignaturas publicadas.
        </p>
      )}
    </div>
  )
}
