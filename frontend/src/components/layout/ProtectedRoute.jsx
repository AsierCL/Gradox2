import { Navigate, useLocation } from 'react-router-dom'
import { ShieldWarning } from '@phosphor-icons/react'
import { useAuth } from '../../context/AuthContext'
import { PageLoader } from '../ui/Spinner'
import { PageHeader } from '../ui/PageHeader'
import { Button } from '../ui/Button'
import { Reveal } from '../ui/Reveal'

export function ProtectedRoute({ children, masterOnly = false, adminOnly = false }) {
  const { isAuthenticated, isMaster, isPlatformAdmin, profileLoading } = useAuth()
  const location = useLocation()

  if (!isAuthenticated) {
    return <Navigate to="/acceso" replace state={{ from: location.pathname }} />
  }

  const restricted = masterOnly || adminOnly
  const allowed = adminOnly ? isPlatformAdmin : isMaster

  if (restricted && profileLoading) {
    return <PageLoader label="Comprobando permisos" />
  }

  if (restricted && !allowed) {
    const isAdminGate = adminOnly
    return (
      <div className="site-shell">
        <PageHeader
          eyebrow="Acceso restringido"
          title={
            isAdminGate
              ? 'Esta sala es solo para administración.'
              : 'Esta sala es solo para masters.'
          }
          description={
            isAdminGate
              ? 'La administración de plataforma gestiona el catálogo, los usuarios y la auditoría.'
              : 'La gobernanza de cada grado está reservada a sus masters.'
          }
        />
        <Reveal>
          <div className="glass-soft flex flex-col items-start gap-5 rounded-4xl p-8">
            <span className="grid size-12 place-items-center rounded-full bg-white/70 text-aqua-700">
              <ShieldWarning size={24} weight="duotone" aria-hidden="true" />
            </span>
            <p className="max-w-xl text-sm leading-6 text-ink-soft">
              {isAdminGate
                ? 'Un master de grado no hereda el panel de toda la plataforma. Si crees que deberías tener acceso, revisa tu sesión o contacta con administración.'
                : 'Si crees que deberías tener acceso, revisa tu sesión o contacta con un master de tu grado.'}
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
