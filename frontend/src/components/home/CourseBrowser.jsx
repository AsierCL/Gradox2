import { useState } from 'react'
import { Link } from 'react-router-dom'
import { ArrowRight } from '@phosphor-icons/react'
import { cn } from '../../lib/cn'

export function CourseBrowser({ courses, subjects, fileCountBySubject }) {
  const [selectedId, setSelectedId] = useState(() => courses[0]?.id || null)
  const selected = courses.find((course) => String(course.id) === String(selectedId)) || courses[0]
  const courseSubjects = selected
    ? subjects.filter((subject) => String(subject.courseId) === String(selected.id))
    : []

  if (!courses.length) return null

  return (
    <div className="glass min-w-0 rounded-3xl p-5 sm:p-7">
      <div className="flex gap-1.5 border-b border-ink/8 pb-4">
        {courses.map((course) => {
          const active = String(course.id) === String(selected?.id)
          return (
            <button
              key={course.id}
              type="button"
              onClick={() => setSelectedId(course.id)}
              aria-label={course.name}
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

      <div className="mt-2 grid min-w-0 gap-x-10 sm:grid-cols-2 lg:grid-cols-3">
        {courseSubjects.map((subject) => {
          const count = fileCountBySubject[subject.name] || 0
          return (
            <Link
              key={subject.id}
              to={`/explorar?curso=${selected.id}&asignatura=${subject.id}`}
              className="group flex min-w-0 items-center justify-between gap-4 border-b border-ink/8 py-3.5 text-sm text-ink-soft transition-colors hover:text-ink"
            >
              <span className="min-w-0 truncate">{subject.name}</span>
              <span className="flex shrink-0 items-center gap-2 text-[0.65rem] text-ink-faint">
                {count > 0 ? `${count} ${count === 1 ? 'archivo' : 'archivos'}` : subject.code}
                <ArrowRight
                  size={13}
                  weight="bold"
                  aria-hidden="true"
                  className="text-ink-faint transition-transform duration-200 group-hover:translate-x-0.5"
                />
              </span>
            </Link>
          )
        })}
      </div>
    </div>
  )
}
