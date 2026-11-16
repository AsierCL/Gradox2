import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import {
  ArrowRight,
  CheckCircle,
  EnvelopeSimple,
  Info,
  PaperPlaneTilt,
} from '@phosphor-icons/react'
import { api } from '../lib/api'
import { AuthLayout, PasswordToggle } from '../components/auth/AuthLayout'
import { Button } from '../components/ui/Button'
import { Input, Select } from '../components/ui/Field'

const validEmail = /^[A-Za-z0-9._%+-]+@rai\.usc\.(es|gal)$/

export function RegisterPage() {
  const [form, setForm] = useState({
    email: '',
    username: '',
    password: '',
    confirm: '',
    degreeId: '',
  })
  const [errors, setErrors] = useState({})
  const [serverError, setServerError] = useState('')
  const [loading, setLoading] = useState(false)
  const [sentTo, setSentTo] = useState('')
  const [showPassword, setShowPassword] = useState(false)

  const degreesQuery = useQuery({
    queryKey: ['degrees'],
    queryFn: () => api.degrees.list(),
    staleTime: 10 * 60_000,
  })
  const degrees = degreesQuery.data || []

  const update = (key, value) => {
    setForm((prev) => ({ ...prev, [key]: value }))
    setErrors((prev) => ({ ...prev, [key]: '' }))
    setServerError('')
  }

  const validate = () => {
    const next = {}
    if (!validEmail.test(form.email.trim())) {
      next.email = 'Usa un correo @rai.usc.es o @rai.usc.gal.'
    }
    if (form.username.trim().length < 3 || form.username.trim().length > 50) {
      next.username = 'El alias debe tener entre 3 y 50 caracteres.'
    }
    if (form.password.length < 8 || form.password.length > 72) {
      next.password = 'La contraseña debe tener entre 8 y 72 caracteres.'
    }
    if (form.password !== form.confirm) {
      next.confirm = 'Las contraseñas no coinciden.'
    }
    if (!form.degreeId) {
      next.degreeId = 'Selecciona el grado que vas a cursar.'
    }
    setErrors(next)
    return Object.keys(next).length === 0
  }

  const submit = async (event) => {
    event.preventDefault()
    if (!validate()) return
    setLoading(true)
    setServerError('')
    try {
      await api.auth.register({
        email: form.email.trim(),
        username: form.username.trim(),
        password: form.password,
        degreeId: Number(form.degreeId),
      })
      setSentTo(form.email.trim())
    } catch (error) {
      if (error.code === 'DEGREE_REQUIRED') {
        setErrors((prev) => ({ ...prev, degreeId: 'Selecciona el grado que vas a cursar.' }))
      } else if (error.code === 'DEGREE_NOT_FOUND') {
        setErrors((prev) => ({
          ...prev,
          degreeId: 'El grado seleccionado ya no existe. Elige otro.',
        }))
      } else {
        setServerError(error.message)
      }
    } finally {
      setLoading(false)
    }
  }

  if (sentTo) {
    return (
      <AuthLayout
        eyebrow="Registro"
        title="Revisa tu correo"
        description="Hemos enviado un enlace de verificación. La cuenta se activará cuando lo abras."
        footer={
          <p className="text-sm text-ink-soft">
            ¿Ya lo has verificado?{' '}
            <Link to="/acceso" className="font-semibold text-ink">
              Inicia sesión
            </Link>
          </p>
        }
      >
        <div className="rounded-2xl border border-ink/8 bg-paper-100 p-6">
          <span className="grid size-10 place-items-center rounded-xl bg-white text-ink-soft">
            <PaperPlaneTilt size={21} weight="duotone" aria-hidden="true" />
          </span>
          <p className="mt-4 text-sm font-semibold text-ink">{sentTo}</p>
          <p className="mt-2 text-sm leading-6 text-ink-soft">
            Si no llega en unos minutos, revisa la carpeta de spam.
          </p>
          <div className="mt-4 space-y-2 border-t border-ink/8 pt-4 text-xs leading-5 text-ink-soft">
            {[
              'El enlace te devuelve a Gradox y activa tu cuenta.',
              'Después podrás iniciar sesión con tu alias y contraseña.',
            ].map((item) => (
              <p key={item} className="flex gap-2">
                <CheckCircle size={14} className="mt-0.5 shrink-0 text-ink-soft" aria-hidden="true" />
                {item}
              </p>
            ))}
          </div>
        </div>
        <Button to="/acceso" variant="ghost" className="mt-5 w-full">
          Volver al acceso
        </Button>
      </AuthLayout>
    )
  }

  return (
    <AuthLayout
      eyebrow="Nueva cuenta"
      title="Crea tu cuenta"
      description="Necesitas un correo @rai.usc.es o @rai.usc.gal. El alias será visible cuando compartas o comentes."
      footer={
        <p className="text-sm text-ink-soft">
          ¿Ya tienes cuenta?{' '}
          <Link to="/acceso" className="font-semibold text-ink hover:opacity-65">
            Inicia sesión
          </Link>
        </p>
      }
    >
      <form onSubmit={submit} className="space-y-4" noValidate>
        <Input
          label="Correo institucional"
          type="email"
          autoComplete="email"
          placeholder="nombre.apellido@rai.usc.es"
          value={form.email}
          onChange={(event) => update('email', event.target.value)}
          error={errors.email}
          required
          rightAdornment={
            <span className="pointer-events-none absolute right-4 top-1/2 -translate-y-1/2 text-ink-faint">
              <EnvelopeSimple size={16} aria-hidden="true" />
            </span>
          }
        />
        <Input
          label="Alias en Gradox"
          autoComplete="username"
          placeholder="como quieres aparecer"
          value={form.username}
          onChange={(event) => update('username', event.target.value)}
          error={errors.username}
          hint="Entre 3 y 50 caracteres. Puedes cambiarlo más adelante."
          required
          minLength={3}
          maxLength={50}
        />

        <Select
          label="Grado"
          value={form.degreeId}
          onChange={(event) => update('degreeId', event.target.value)}
          error={errors.degreeId}
          hint={
            degreesQuery.isError
              ? 'No se pudo cargar el catálogo de grados. Vuelve a intentarlo.'
              : 'Selecciona el plan de estudios al que perteneces.'
          }
          disabled={degreesQuery.isLoading || degreesQuery.isError}
          required
        >
          <option value="" disabled>
            {degreesQuery.isLoading ? 'Cargando grados…' : 'Elige tu grado'}
          </option>
          {degrees.map((degree) => (
            <option key={degree.id} value={degree.id}>
              {degree.name} · {degree.code}
            </option>
          ))}
        </Select>

        {degreesQuery.isError && (
          <button
            type="button"
            onClick={() => degreesQuery.refetch()}
            className="text-left text-xs font-semibold text-aqua-800 underline decoration-aqua-300 underline-offset-4"
          >
            Reintentar la carga de grados
          </button>
        )}

        <div className="flex items-start gap-2.5 rounded-2xl border border-ink/8 bg-white/40 px-4 py-3">
          <Info size={16} className="mt-0.5 shrink-0 text-aqua-700" aria-hidden="true" />
          <p className="text-[0.68rem] leading-5 text-ink-soft">
            El grado no se puede cambiar después. Elígelo con cuidado: define en qué comunidad
            podrás votar, comentar y proponer material.
          </p>
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          <Input
            label="Contraseña"
            type={showPassword ? 'text' : 'password'}
            autoComplete="new-password"
            placeholder="Mínimo 8 caracteres"
            value={form.password}
            onChange={(event) => update('password', event.target.value)}
            error={errors.password}
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
            value={form.confirm}
            onChange={(event) => update('confirm', event.target.value)}
            error={errors.confirm}
            required
            minLength={8}
            maxLength={72}
          />
        </div>

        {serverError && (
          <div
            role="alert"
            className="rounded-2xl border border-[#e7c7c2] bg-[#fbefed] px-4 py-3 text-xs leading-5 text-[#86483f]"
          >
            {serverError}
          </div>
        )}

        <p className="text-[0.65rem] leading-5 text-ink-faint">
          Al crear la cuenta aceptas las normas de convivencia y el tratamiento de datos
          descrito en la política del proyecto.
        </p>

        <Button
          type="submit"
          loading={loading}
          className="w-full"
          icon={!loading ? <ArrowRight size={16} weight="bold" /> : undefined}
        >
          Crear mi cuenta
        </Button>
      </form>
    </AuthLayout>
  )
}
