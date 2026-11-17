export const FILE_TYPES = {
  APUNTES: {
    label: 'Apuntes',
    short: 'Apuntes',
    tone: 'bg-[#1d1d1f] text-white border-[#1d1d1f]',
    dot: 'bg-white',
  },
  EXAMEN: {
    label: 'Examen',
    short: 'Examen',
    tone: 'bg-[#e8e8ed] text-[#2c2c2e] border-[#d2d2d7]',
    dot: 'bg-[#48484a]',
  },
  EJERCICIO: {
    label: 'Ejercicio',
    short: 'Ejercicio',
    tone: 'bg-[#f2f2f2] text-[#48484a] border-[#e5e5e5]',
    dot: 'bg-[#6e6e73]',
  },
  OTRO: {
    label: 'Otro material',
    short: 'Otro',
    tone: 'bg-white text-ink-soft border-ink/10',
    dot: 'bg-ink-faint',
  },
}

export const VISIBILITY = {
  PUBLIC: {
    label: 'Público',
    description: 'Cualquier persona puede ver quién lo comparte.',
  },
  RESTRICTED: {
    label: 'Comunidad',
    description: 'Solo usuarios registrados ven quién lo comparte.',
  },
  PRIVATE: {
    label: 'Privado',
    description: 'Solo tú y los masters ven quién lo comparte.',
  },
}

export const PROPOSAL_STATUS = {
  PENDING: { label: 'En votación', tone: 'bg-[#f2f2f2] text-[#48484a]' },
  APPROVED: { label: 'Aprobada', tone: 'bg-[#e8e8ed] text-[#1d1d1f]' },
  REJECTED: { label: 'Rechazada', tone: 'bg-[#f2f2f2] text-[#48484a]' },
}

export const ACTION_TYPES = {
  UPLOAD: 'Subida',
  DELETE: 'Eliminación',
  PROMOTION: 'Promoción',
  EXPULSION: 'Expulsión',
  POLICY_CHANGE: 'Cambio de política',
  VETO: 'Veto',
  BAN: 'Baneo',
  UNBAN: 'Rehabilitación',
  DEGREE_CHANGE: 'Cambio de grado',
}

export const ROLES = {
  GUEST: 'Invitado',
  USER: 'Estudiante',
  MASTER: 'Master',
  ADMIN: 'Administrador',
}

export const SUBJECT_TYPES = {
  'Formación básica': 'bg-[#e8e8ed] text-[#2c2c2e]',
  Obligatorio: 'bg-white text-ink-soft',
  Optativo: 'bg-[#f2f2f2] text-ink-soft',
  'Trabajos Fin de Grado y Máster': 'bg-[#1d1d1f] text-white',
  'Prácticas en Empresas de Grao e Máster': 'bg-[#e8e8ed] text-[#2c2c2e]',
}

export const COURSE_LABELS = {
  1: 'Primer curso',
  2: 'Segundo curso',
  3: 'Tercer curso',
  4: 'Cuarto curso',
}

export const typeMeta = (type) => FILE_TYPES[type] || FILE_TYPES.OTRO

export const statusMeta = (status) =>
  PROPOSAL_STATUS[status] || {
    label: status || 'Sin estado',
    tone: 'bg-white/60 text-ink-soft',
  }

export const visibilityMeta = (visibility) =>
  VISIBILITY[visibility] || VISIBILITY.PUBLIC
