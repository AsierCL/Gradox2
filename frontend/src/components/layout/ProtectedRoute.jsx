import { Navigate, useLocation } from 'react-router-dom'
import { ShieldWarning } from '@phosphor-icons/react'
import { useAuth } from '../../context/AuthContext'
import { PageLoader } from '../ui/Spinner'
import { PageHeader } from '../ui/PageHeader'
import { Button } from '../ui/Button'
import { Reveal } from '../ui/Reveal'

export function ProtectedRoute({ children, masterOnly = false }) {
  const { isAuthenticated, isMaster, profileLoading } = useAuth()
  const location = useLocation()

  if (!isAuthenticated) {
    return <Navigate to="/acceso" replace state={{ from: location.pathname }} />
  }

  if (masterOnly && profileLoading) {
    return <PageLoader label="Comprobando permisos" />
  }

  if (masterOnly && !isMaster) {
    return (
      <div className="site-shell">
        <PageHeader
          eyebrow="Acceso restringido"
          title="Esta sala es solo para masters."
          description="La administración de Gradox está reservada a miembros elegidos por la comunidad."
        />
        <Reveal>
          <div className="glass-soft flex flex-col items-start gap-5 rounded-4xl p-8">
            <span className="grid size-12 place-items-center rounded-full bg-white/70 text-aqua-700">
              <ShieldWarning size={24} weight="duotone" aria-hidden="true" />
            </span>
            <p className="max-w-xl text-sm leading-6 text-ink-soft">
              Si crees que deberías tener acceso, revisa tu sesión o contacta con un master.
            </p>
            <Button to="/" variant="ghost">
              Volver al inicio
            </Button>
          </div>
        </Reveal>
      </div>
    )
  }

  return children
}
