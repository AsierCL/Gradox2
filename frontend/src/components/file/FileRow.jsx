import { Link } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { toast } from 'sonner'
import { ArrowUpRight, DownloadSimple, Eye, EyeSlash, Star } from '@phosphor-icons/react'
import { api } from '../../lib/api'
import { typeMeta, visibilityMeta } from '../../lib/constants'
import { scoreLabel } from '../../lib/format'
import { TypeBadge } from '../ui/Badge'

export function FileRow({ file }) {
  const meta = typeMeta(file.fileType)
  const visibility = visibilityMeta(file.visibilityLevel)
  const isPrivate = (file.visibilityLevel || '').toUpperCase() !== 'PUBLIC'

  const downloadMutation = useMutation({
    mutationFn: () => api.files.download(file.id),
    onSuccess: (data) => {
      const anchor = document.createElement('a')
      anchor.href = data.url
      anchor.rel = 'noopener noreferrer'
      anchor.click()
      toast.success('Descarga preparada')
    },
    onError: (error) => toast.error(error.message),
  })

  return (
    <div className="row-sheen group flex items-center gap-3 px-4 py-3.5 transition-colors hover:bg-white/40 sm:gap-4 sm:px-5">
      <TypeBadge meta={meta} className="hidden shrink-0 sm:inline-flex" />

      <div className="min-w-0 flex-1">
        <Link
          to={`/material/${file.id}`}
          className="block truncate text-sm font-semibold text-ink hover:underline hover:underline-offset-4"
        >
          {file.fileName}
        </Link>
        <p className="mt-0.5 flex min-w-0 items-center gap-x-2 whitespace-nowrap text-[0.68rem] text-ink-soft">
          <span className="shrink-0 font-semibold text-ink-faint sm:hidden">{meta.label}</span>
          <span className="shrink-0 text-ink-faint sm:hidden" aria-hidden="true">·</span>
          <span className="truncate">{file.subject || 'Sin asignatura'}</span>
          <span className="hidden shrink-0 items-center gap-1 sm:inline-flex">
            <span className="text-ink-faint" aria-hidden="true">·</span>
            {isPrivate ? <EyeSlash size={11} aria-hidden="true" /> : <Eye size={11} aria-hidden="true" />}
            {visibility.label}
          </span>
          {file.uploaderUsername && file.uploaderUsername !== 'anonymous' && (
            <>
              <span className="hidden text-ink-faint sm:inline" aria-hidden="true">·</span>
              <span className="hidden shrink-0 sm:inline">{file.uploaderUsername}</span>
            </>
          )}
        </p>
      </div>

      <span className="hidden w-20 items-center justify-end gap-1 text-xs font-semibold tabular-nums text-ink sm:flex">
        <Star size={11} weight="fill" aria-hidden="true" />
        {scoreLabel(file.score)}
      </span>

      <button
        type="button"
        onClick={() => downloadMutation.mutate()}
        disabled={downloadMutation.isPending}
        aria-label={`Descargar ${file.fileName}`}
        title="Descargar"
        className="grid size-9 shrink-0 place-items-center rounded-full text-ink-soft transition-colors hover:bg-white hover:text-ink disabled:opacity-40"
      >
        {downloadMutation.isPending ? (
          <span className="size-3.5 animate-spin rounded-full border-2 border-ink/20 border-r-ink" />
        ) : (
          <DownloadSimple size={16} aria-hidden="true" />
        )}
      </button>

      <Link
        to={`/material/${file.id}`}
        aria-label={`Ver ${file.fileName}`}
        className="grid size-9 shrink-0 place-items-center rounded-full text-ink-faint transition-colors hover:bg-white hover:text-ink"
      >
        <ArrowUpRight size={15} weight="bold" aria-hidden="true" />
      </Link>
    </div>
  )
}
