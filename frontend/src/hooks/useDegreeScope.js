import { useAuth } from '../context/AuthContext'
import { sameDegreeId } from '../lib/degree'

/**
 * Decide la UI según el ámbito de grado de un recurso.
 *
 * El backend sigue siendo la autoridad: si una acción se escapa, responde
 * 403 DEGREE_SCOPE_FORBIDDEN. Esto solo evita ofrecer acciones imposibles.
 */
export function useDegreeScope(resourceDegreeId) {
  const { degreeId, isAuthenticated, isPlatformAdmin, profileLoading } = useAuth()
  const hasResourceDegree =
    resourceDegreeId !== null && resourceDegreeId !== undefined
  const profileDegreeId = degreeId ?? null
  const hasDegree = profileDegreeId !== null && profileDegreeId !== undefined
  const sameDegree = hasResourceDegree
    ? sameDegreeId(profileDegreeId, resourceDegreeId)
    : true
  const isReadOnly = Boolean(
    isAuthenticated && hasResourceDegree && hasDegree && !sameDegree,
  )
  const needsDegree = Boolean(isAuthenticated && !hasDegree && !profileLoading)
  const canParticipate = Boolean(
    isAuthenticated && hasDegree && sameDegree && !isPlatformAdmin,
  )

  return {
    profileDegreeId,
    hasDegree,
    hasResourceDegree,
    sameDegree,
    isReadOnly,
    needsDegree,
    canParticipate,
    isPlatformAdmin,
  }
}
