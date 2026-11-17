export const DEGREE_SCOPE_MESSAGE =
  'Solo los estudiantes de este grado pueden participar.'

export const DEGREE_REQUIRED_MESSAGE =
  'Tu cuenta no tiene un grado asignado. Contacta con administración.'

export const sameDegreeId = (left, right) =>
  left !== null &&
  left !== undefined &&
  right !== null &&
  right !== undefined &&
  String(left) === String(right)

export const degreePath = (degree) =>
  degree?.code ? `/grados/${encodeURIComponent(degree.code)}` : '/grados'

export const degreeLabel = (degree) =>
  degree ? `${degree.name}${degree.code ? ` · ${degree.code}` : ''}` : 'Sin grado'

export const semesterLabel = (semester) => {
  const value = String(semester || '').trim()
  return value || 'Anual'
}

export const creditsLabel = (credits) => {
  const value = Number(credits)
  return Number.isFinite(value) ? `${value} ECTS` : '—'
}

export const isDegreeScopeError = (error) =>
  error?.code === 'DEGREE_SCOPE_FORBIDDEN'

export const isDegreeRequiredError = (error) =>
  error?.code === 'DEGREE_REQUIRED'

export const degreeErrorMessage = (error) => {
  switch (error?.code) {
    case 'DEGREE_REQUIRED':
      return DEGREE_REQUIRED_MESSAGE
    case 'DEGREE_NOT_FOUND':
      return 'El grado seleccionado no existe.'
    case 'DEGREE_IMMUTABLE':
      return 'El grado de un usuario no se puede modificar desde su perfil.'
    case 'DEGREE_SCOPE_FORBIDDEN':
      return DEGREE_SCOPE_MESSAGE
    case 'DEGREE_LAST_MASTER':
      return error.message || 'No puedes dejar un grado sin ningún master activo.'
    default:
      return error?.message || 'La operación no se pudo completar.'
  }
}
