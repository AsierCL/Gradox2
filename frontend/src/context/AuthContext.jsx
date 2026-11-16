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

  const value = useMemo(
    () => ({
      session,
      profile: profileQuery.data || null,
      profileLoading: profileQuery.isLoading,
      profileError: profileQuery.error,
      isAuthenticated: Boolean(session?.token),
      isMaster: (session?.role || '').toUpperCase() === 'MASTER',
      login,
      logout,
      refreshProfile,
      updateProfile,
    }),
    [
      session,
      profileQuery.data,
      profileQuery.isLoading,
      profileQuery.error,
      login,
      logout,
      refreshProfile,
      updateProfile,
    ],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth debe usarse dentro de AuthProvider')
  }
  return context
}
