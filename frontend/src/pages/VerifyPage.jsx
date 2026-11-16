import { Link, useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { CheckCircle, EnvelopeSimple, WarningCircle } from '@phosphor-icons/react'
import { api } from '../lib/api'
import { AuthLayout } from '../components/auth/AuthLayout'
import { Button } from '../components/ui/Button'
import { Spinner } from '../components/ui/Spinner'

export function VerifyPage() {
  const [params] = useSearchParams()
  const token = params.get('token') || ''

  const verifyQuery = useQuery({
    queryKey: ['verify', token],
    queryFn: () => api.auth.verify(token),
    enabled: Boolean(token),
    retry: false,
    staleTime: Infinity,
  })

  const success = verifyQuery.isSuccess
  const error = !token || verifyQuery.isError

  return (
    <AuthLayout
      eyebrow="Verificación de cuenta"
      title={
        success ? 'Cuenta verificada' : error ? 'Enlace no válido' : 'Verificando cuenta'
      }
      description={
        success
          ? 'Ya puedes iniciar sesión.'
          : error
            ? 'El enlace puede haber caducado o haberse usado ya.'
            : 'Estamos comprobando el enlace.'
      }
      footer={
        <p className="text-sm text-ink-soft">
          <Link to="/acceso" className="font-semibold text-ink hover:opacity-65">
            Ir al acceso
          </Link>
        </p>
      }
    >
      <div
        className={`rounded-2xl border p-6 ${
          success
            ? 'border-ink/8 bg-paper-100'
            : error
              ? 'border-[#e7c7c2] bg-[#fbefed]'
              : 'border-ink/8 bg-paper-100'
        }`}
      >
        {verifyQuery.isLoading ? (
          <div className="flex items-center gap-4">
            <Spinner className="size-7" />
            <div>
              <p className="text-sm font-semibold text-ink">Validando el enlace…</p>
              <p className="mt-1 text-xs text-ink-soft">No cierres esta página.</p>
            </div>
          </div>
        ) : success ? (
          <div>
            <span className="grid size-12 place-items-center rounded-full bg-white/75 text-aqua-700">
              <CheckCircle size={25} weight="duotone" aria-hidden="true" />
            </span>
            <p className="mt-5 text-lg font-semibold text-ink">Cuenta verificada</p>
            <p className="mt-1.5 text-sm leading-6 text-ink-soft">
              Ya puedes iniciar sesión con tu alias y contraseña.
            </p>
            <Button to="/acceso" className="mt-5 w-full">
              Entrar en Gradox
            </Button>
          </div>
        ) : (
          <div>
            <span className="grid size-12 place-items-center rounded-full bg-white/70 text-[#934b43]">
              {token ? (
                <WarningCircle size={25} weight="duotone" aria-hidden="true" />
              ) : (
                <EnvelopeSimple size={25} weight="duotone" aria-hidden="true" />
              )}
            </span>
            <p className="mt-5 text-lg font-semibold text-ink">
              {token ? 'No pudimos verificar el enlace' : 'Falta el enlace de verificación'}
            </p>
            <p className="mt-1.5 text-sm leading-6 text-ink-soft">
              {verifyQuery.error?.message ||
                'Abre el enlace completo desde tu correo o vuelve a solicitar el registro.'}
            </p>
            <div className="mt-5 flex flex-col gap-2">
              <Button to="/registro" variant="ghost" className="w-full">
                Volver al registro
              </Button>
              <Link to="/acceso" className="text-center text-xs font-semibold text-ink-soft">
                Intentar iniciar sesión
              </Link>
            </div>
          </div>
        )}
      </div>
    </AuthLayout>
  )
}
