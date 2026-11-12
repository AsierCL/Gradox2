const numberFormatter = new Intl.NumberFormat('es-ES')
const compactFormatter = new Intl.NumberFormat('es-ES', {
  notation: 'compact',
  maximumFractionDigits: 1,
})

export const formatNumber = (value) => numberFormatter.format(Number(value || 0))
export const formatCompact = (value) => compactFormatter.format(Number(value || 0))

export const formatDate = (value, options = {}) => {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '—'
  return new Intl.DateTimeFormat('es-ES', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
    ...options,
  }).format(date)
}

export const formatDateTime = (value) => {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '—'
  return new Intl.DateTimeFormat('es-ES', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date)
}

export const timeAgo = (value) => {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  const diff = date.getTime() - Date.now()
  const minutes = Math.round(diff / 60000)
  const formatter = new Intl.RelativeTimeFormat('es', { numeric: 'auto' })
  if (Math.abs(minutes) < 60) return formatter.format(minutes, 'minute')
  const hours = Math.round(minutes / 60)
  if (Math.abs(hours) < 24) return formatter.format(hours, 'hour')
  const days = Math.round(hours / 24)
  if (Math.abs(days) < 30) return formatter.format(days, 'day')
  return formatDate(value)
}

export const formatBytes = (bytes) => {
  const value = Number(bytes || 0)
  if (!value) return '0 MB'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  const index = Math.min(Math.floor(Math.log(value) / Math.log(1024)), units.length - 1)
  return `${(value / 1024 ** index).toFixed(index > 1 ? 1 : 0)} ${units[index]}`
}

export const initials = (name = '') =>
  name
    .replace(/^anonymous$/i, 'Anónimo')
    .split(/[\s._-]+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0])
    .join('')
    .toUpperCase() || 'G'

export const scoreLabel = (score) => {
  if (score === null || score === undefined) return 'Sin valorar'
  return Number(score).toFixed(1)
}

export const plural = (count, singular, pluralName) =>
  `${formatNumber(count)} ${count === 1 ? singular : pluralName}`
