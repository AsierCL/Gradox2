import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { api, sessionStore, onSessionChange } from '../lib/api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const queryClient = useQueryClient()
  const [session, setSession] = useState(() => sessionStore.read())

  useEffect(() => onSessionChange(setSession), [])

  const profileQuery = useQuery({
    queryKey: ['me', session?.username],
    queryFn: () => api.users.me(),
    enabled: Boolean(session?.token),
    staleTime: 45_000,
    retry: 1,
  })

  const login = useCallback(
    async (credentials) => {
      const auth = await api.auth.login(credentials)
      sessionStore.write(auth)
      setSession(auth)
      await queryClient.invalidateQueries({ queryKey: ['me'] })
      return auth
    },
    [queryClient],
  )

  const logout = useCallback(async () => {
    const current = sessionStore.read()
    try {
      if (current?.refreshToken) {
        await api.auth.logout(current.refreshToken)
      }
    } catch {
      // La sesión local se cierra incluso si la revocación remota falla.
    } finally {
      sessionStore.clear()
      setSession(null)
      queryClient.clear()
    }
  }, [queryClient])

  const refreshProfile = useCallback(async () => {
    const result = await profileQuery.refetch()
    return result.data
  }, [profileQuery])

  const updateProfile = useCallback(
    (profile) => {
      queryClient.setQueryData(['me', session?.username], profile)
    },
    [queryClient, session?.username],
  )

  const value = useMemo(() => {
    const profile = profileQuery.data || null
    const role = (profile?.role || session?.role || '').toUpperCase()
    const degreeId = profile?.degreeId ?? session?.degreeId ?? null
    const degreeCode = profile?.degreeCode ?? session?.degreeCode ?? null
    const degreeName = profile?.degreeName ?? session?.degreeName ?? null
    const isPlatformAdmin = Boolean(
      profile ? profile.isPlatformAdmin : session?.isPlatformAdmin,
    )

    return {
      session,
      profile,
      profileLoading: profileQuery.isLoading,
      profileError: profileQuery.error,
      isAuthenticated: Boolean(session?.token),
      role,
      isMaster: role === 'MASTER',
      isPlatformAdmin,
      degreeId,
      degreeCode,
      degreeName,
      degree: degreeId || degreeCode || degreeName
        ? { id: degreeId, code: degreeCode, name: degreeName }
        : null,
      belongsToDegree: (candidateId) =>
        degreeId !== null &&
        candidateId !== null &&
        candidateId !== undefined &&
        String(degreeId) === String(candidateId),
      login,
      logout,
      refreshProfile,
      updateProfile,
    }
  }, [
    session,
    profileQuery.data,
    profileQuery.isLoading,
    profileQuery.error,
    login,
    logout,
    refreshProfile,
    updateProfile,
  ])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth debe usarse dentro de AuthProvider')
  }
  return context
}
