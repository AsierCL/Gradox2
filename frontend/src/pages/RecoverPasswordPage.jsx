import { useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { CheckCircle, Key, PaperPlaneTilt } from '@phosphor-icons/react'
import { api } from '../lib/api'
import { AuthLayout, PasswordToggle } from '../components/auth/AuthLayout'
import { Button } from '../components/ui/Button'
import { Input } from '../components/ui/Field'

export function RecoverPasswordPage() {
  const [params] = useSearchParams()
  const token = params.get('token') || ''
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [done, setDone] = useState(false)

  const submitRequest = async (event) => {
    event.preventDefault()
    setError('')
    setLoading(true)
    try {
      await api.auth.requestPasswordReset(email.trim())
      setDone(true)
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setLoading(false)
    }
  }

  const submitReset = async (event) => {
    event.preventDefault()
    setError('')
    if (password.length < 8) {
      setError('La nueva contraseña debe tener al menos 8 caracteres.')
      return
    }
    if (password !== confirm) {
      setError('Las contraseñas no coinciden.')
      return
    }
    setLoading(true)
    try {
      await api.auth.resetPassword({ token, newPassword: password })
      setDone(true)
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthLayout
      eyebrow={token ? 'Nueva contraseña' : 'Recuperar acceso'}
      title={done ? 'Contraseña actualizada' : token ? 'Nueva contraseña' : 'Recuperar contraseña'}
      description={
        done
          ? token
            ? 'Ya puedes iniciar sesión con la nueva contraseña.'
            : 'Si el correo existe, recibirás un enlace para restablecer la contraseña. Revisa también la carpeta de spam.'
          : token
            ? 'Elige una contraseña de entre 8 y 72 caracteres.'
            : 'Introduce el correo con el que te registraste.'
      }
      footer={
        <p className="text-sm text-ink-soft">
          <Link to="/acceso" className="font-semibold text-ink hover:opacity-65">
            Volver al acceso
          </Link>
        </p>
      }
    >
      {done ? (
        <div className="rounded-2xl border border-ink/8 bg-paper-100 p-6">
          <span className="grid size-10 place-items-center rounded-xl bg-white text-ink-soft">
            <CheckCircle size={21} weight="duotone" aria-hidden="true" />
          </span>
          <p className="mt-4 text-sm font-semibold text-ink">
            {token ? 'Contraseña actualizada' : 'Revisa tu correo'}
          </p>
          <p className="mt-2 text-sm leading-6 text-ink-soft">
            {token
              ? 'La nueva contraseña ya está activa.'
              : 'El enlace caduca por seguridad. Si no llega en unos minutos, puedes volver a intentarlo.'}
          </p>
          <Button to="/acceso" className="mt-5 w-full">
            Ir al acceso
          </Button>
        </div>
      ) : token ? (
        <form onSubmit={submitReset} className="space-y-4" noValidate>
          <Input
            label="Nueva contraseña"
            type={showPassword ? 'text' : 'password'}
            autoComplete="new-password"
            placeholder="Mínimo 8 caracteres"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            required
            minLength={8}
            maxLength={72}
            rightAdornment={
              <PasswordToggle
                visible={showPassword}
                onToggle={() => setShowPassword((value) => !value)}
              />
            }
          />
          <Input
            label="Repite la contraseña"
            type={showPassword ? 'text' : 'password'}
            autoComplete="new-password"
            placeholder="Otra vez"
            value={confirm}
            onChange={(event) => setConfirm(event.target.value)}
            required
            minLength={8}
            maxLength={72}
          />
          {error && (
            <div
              role="alert"
              className="rounded-2xl border border-[#e7c7c2] bg-[#fbefed] px-4 py-3 text-xs leading-5 text-[#86483f]"
            >
              {error}
            </div>
          )}
          <Button
            type="submit"
            loading={loading}
            className="w-full"
            icon={!loading ? <Key size={16} weight="bold" /> : undefined}
          >
            Guardar nueva contraseña
          </Button>
        </form>
      ) : (
        <form onSubmit={submitRequest} className="space-y-4" noValidate>
          <Input
            label="Correo institucional"
            type="email"
            autoComplete="email"
            placeholder="nombre@rai.usc.es"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            required
          />
          {error && (
            <div
              role="alert"
              className="rounded-2xl border border-[#e7c7c2] bg-[#fbefed] px-4 py-3 text-xs leading-5 text-[#86483f]"
            >
              {error}
            </div>
          )}
          <Button
            type="submit"
            loading={loading}
            className="w-full"
            icon={!loading ? <PaperPlaneTilt size={16} weight="bold" /> : undefined}
          >
            Enviar enlace
          </Button>
          <p className="text-[0.65rem] leading-5 text-ink-faint">
            Por seguridad, no revelamos si un correo está registrado. Si existe, recibirás el
            enlace.
          </p>
        </form>
      )}
    </AuthLayout>
  )
}
