import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { ArrowRight } from '@phosphor-icons/react'
import { useAuth } from '../context/AuthContext'
import { AuthLayout, PasswordToggle } from '../components/auth/AuthLayout'
import { Button } from '../components/ui/Button'
import { Input } from '../components/ui/Field'

export function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [form, setForm] = useState({ username: '', password: '' })
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const update = (key, value) => setForm((prev) => ({ ...prev, [key]: value }))

  const submit = async (event) => {
    event.preventDefault()
    setError('')
    setLoading(true)
    try {
      await login({ username: form.username.trim(), password: form.password })
      toast.success('Sesión iniciada')
      navigate(location.state?.from || '/', { replace: true })
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthLayout
      eyebrow="Cuenta"
      title="Inicia sesión"
      description="Accede con tu cuenta de la USC para votar, comentar y compartir material."
      footer={
        <p className="text-sm text-ink-soft">
          ¿No tienes cuenta?{' '}
          <Link to="/registro" className="font-semibold text-ink hover:opacity-65">
            Crear cuenta
          </Link>
        </p>
      }
    >
      <form onSubmit={submit} className="space-y-4" noValidate>
        <Input
          label="Nombre de usuario"
          autoComplete="username"
          placeholder="tu_alias"
          value={form.username}
          onChange={(event) => update('username', event.target.value)}
          required
          minLength={3}
          maxLength={50}
          error={error && !form.username ? 'Introduce tu nombre de usuario.' : ''}
        />
        <Input
          label="Contraseña"
          type={showPassword ? 'text' : 'password'}
          autoComplete="current-password"
          placeholder="Tu contraseña"
          value={form.password}
          onChange={(event) => update('password', event.target.value)}
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

        {error && (
          <div
            role="alert"
            className="rounded-2xl border border-[#e7c7c2] bg-[#fbefed] px-4 py-3 text-xs leading-5 text-[#86483f]"
          >
            {error}
          </div>
        )}

        <div className="flex items-center justify-between gap-4 pt-1">
          <Link
            to="/recuperar"
            className="text-xs font-semibold text-ink-soft transition-colors hover:text-ink"
          >
            He olvidado mi contraseña
          </Link>
          <Button
            type="submit"
            loading={loading}
            icon={!loading ? <ArrowRight size={16} weight="bold" /> : undefined}
          >
            Entrar
          </Button>
        </div>
      </form>
    </AuthLayout>
  )
}
