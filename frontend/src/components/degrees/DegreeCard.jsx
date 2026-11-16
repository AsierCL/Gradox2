import { Link } from 'react-router-dom'
import {
  ArrowUpRight,
  BookOpenText,
  Brain,
  Code,
  Flask,
  GraduationCap,
} from '@phosphor-icons/react'
import { cn } from '../../lib/cn'
import { degreePath } from '../../lib/degree'
import { formatNumber } from '../../lib/format'

const DEGREE_ICONS = {
  G4012: Code,
  G4022: Flask,
  G4121: Brain,
}

export function DegreeCard({ degree, own = false, fileCount = 0, subjectCount }) {
  const DegreeIcon = DEGREE_ICONS[degree.code] || GraduationCap

  return (
    <Link
      to={degreePath(degree)}
      className={cn(
        'group relative flex h-full flex-col overflow-hidden rounded-4xl border p-6 transition-all duration-500 ease-silk',
        own
          ? 'border-aqua-300/70 bg-aqua-50/65 hover:-translate-y-1 hover:border-aqua-400 hover:shadow-float'
          : 'border-white/72 bg-white/45 backdrop-blur-xl hover:-translate-y-1 hover:border-white hover:bg-white/70 hover:shadow-float',
      )}
    >
      <div className="flex items-start justify-between gap-3">
        <div className="flex flex-wrap items-center gap-2">
          <span className="chip py-1 text-[0.58rem]">{degree.code}</span>
          {own && <span className="chip chip-aqua py-1 text-[0.55rem]">Tu grado</span>}
        </div>
        <span className="grid size-9 shrink-0 place-items-center rounded-full border border-ink/8 bg-white/55 text-ink-faint transition-transform duration-500 group-hover:-translate-y-0.5 group-hover:translate-x-0.5">
          <ArrowUpRight size={16} weight="bold" aria-hidden="true" />
        </span>
      </div>

      <span className="mt-7 grid size-11 place-items-center rounded-2xl bg-white/65 text-aqua-700">
        <DegreeIcon size={22} weight="duotone" aria-hidden="true" />
      </span>

      <h3 className="mt-5 text-balance text-xl font-semibold leading-snug tracking-[-0.03em] text-ink">
        {degree.name}
      </h3>

      <p className="mt-2 flex flex-1 flex-wrap items-center gap-x-3 gap-y-1 text-[0.68rem] text-ink-faint">
        <span>{formatNumber(degree.coursesCount)} cursos</span>
        {subjectCount !== undefined && <span>{formatNumber(subjectCount)} asignaturas</span>}
        <span className="inline-flex items-center gap-1.5">
          <BookOpenText size={12} aria-hidden="true" />
          {formatNumber(fileCount)} {fileCount === 1 ? 'material' : 'materiales'}
        </span>
      </p>

      <span className="mt-6 inline-flex items-center gap-1.5 text-xs font-semibold text-ink">
        Ver el grado
        <ArrowUpRight size={13} weight="bold" aria-hidden="true" />
      </span>
    </Link>
  )
}
