import {
  Check,
  Eye,
  EyeSlash,
  LockKey,
  Medal,
  Scales,
  ShieldCheck,
  Timer,
  UserCircle,
  UsersFour,
} from '@phosphor-icons/react'
import { PageHeader } from '../components/ui/PageHeader'
import { Reveal } from '../components/ui/Reveal'
import { Button } from '../components/ui/Button'
import { ROLES } from '../lib/constants'

const reputation = [
  ['Emitir un voto', '+1'],
  ['Voto que coincide con el resultado', '+4'],
  ['Voto que no coincide', '−2'],
  ['Proponer un archivo', '+2'],
  ['Archivo aceptado', '+10'],
  ['Archivo rechazado', '−5'],
  ['Eliminación tras publicarse', '−15'],
]

const faqs = [
  {
    question: '¿Quién puede usar Gradox?',
    answer:
      'Cualquier persona puede consultar el catálogo público. Para votar, comentar o proponer material necesitas una cuenta verificada con correo @rai.usc.es o @rai.usc.gal.',
  },
  {
    question: '¿Quién decide si un material se publica?',
    answer:
      'La comunidad, mediante una votación ponderada. Se necesita un mínimo de votos y un porcentaje de apoyo. Los masters tienen más peso, pero no pueden aprobar solos.',
  },
  {
    question: '¿Se muestra mi nombre al subir material?',
    answer:
      'Depende de la visibilidad que elijas. En público se muestra tu alias; en comunidad, solo a usuarios registrados; en privado, tu identidad queda oculta salvo para ti y los masters.',
  },
  {
    question: '¿Cómo se calcula la reputación?',
    answer:
      'Con los puntos de la tabla superior. La reputación nunca baja de cero. Los masters reciben un multiplicador de ×1,5 en recompensas positivas.',
  },
  {
    question: '¿Qué datos guarda Gradox?',
    answer:
      'Los necesarios para operar la cuenta: correo, reputación, participaciones y registro de auditoría de moderación. La exposición pública depende de la visibilidad de cada aportación.',
  },
]

function Accordion() {
  return (
    <div className="divide-y divide-ink/8 border-y border-ink/8">
      {faqs.map((faq) => (
        <details key={faq.question} className="group">
          <summary className="flex cursor-pointer list-none items-center justify-between gap-4 py-5 text-sm font-semibold text-ink marker:content-none">
            {faq.question}
            <span className="relative grid size-6 shrink-0 place-items-center rounded-full border border-ink/10">
              <span className="absolute h-px w-2.5 bg-ink-soft" />
              <span className="absolute h-2.5 w-px bg-ink-soft transition-transform duration-200 group-open:scale-y-0" />
            </span>
          </summary>
          <p className="max-w-2xl pb-5 text-sm leading-7 text-ink-soft">{faq.answer}</p>
        </details>
      ))}
    </div>
  )
}

export function CommunityPage() {
  return (
    <div className="site-shell">
      <PageHeader
        eyebrow="Comunidad"
        title="Funcionamiento y reglas"
        description="Cómo se publica material, cómo se vota y qué permisos tiene cada rol."
      />

      <section id="gobernanza" className="scroll-mt-24 py-4 sm:py-8">
        <div className="grid gap-4 lg:grid-cols-[1.15fr_0.85fr]">
          <Reveal>
            <div className="glass rounded-3xl p-6 sm:p-8">
              <h2 className="display-tight mt-3 text-2xl text-ink sm:text-3xl">Ciclo de una propuesta</h2>
              <div className="mt-7 space-y-6">
                {[
                  ['01', 'Se presenta', 'El autor propone el material e indica asignatura, tipo y visibilidad.'],
                  ['02', 'Se revisa', 'La propuesta queda abierta durante siete días.'],
                  ['03', 'Se vota', 'Cada voto pesa según la reputación y el rol de quien vota.'],
                  ['04', 'Se resuelve', 'Si alcanza el quórum y el apoyo necesarios, se publica.'],
                ].map(([number, title, copy]) => (
                  <div key={number} className="grid grid-cols-[2rem_1fr] gap-4">
                    <span className="pt-0.5 text-[0.68rem] font-semibold tabular-nums text-ink-faint">
                      {number}
                    </span>
                    <div>
                      <p className="text-sm font-semibold text-ink">{title}</p>
                      <p className="mt-1 text-sm leading-6 text-ink-soft">{copy}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </Reveal>

          <Reveal delay={0.06}>
            <div className="glass rounded-3xl p-6 sm:p-8">
              <h2 className="display-tight mt-3 text-2xl text-ink">Umbrales</h2>
              <div className="mt-6 divide-y divide-ink/8">
                {[
                  [Timer, 'Duración', '7 días'],
                  [UsersFour, 'Participación mínima', '10 votos'],
                  [Scales, 'Apoyo necesario', '60%'],
                  [ShieldCheck, 'Quórum de masters', '20%'],
                ].map(([Icon, label, value]) => (
                  <div key={label} className="flex items-center justify-between gap-4 py-3.5">
                    <span className="flex items-center gap-2 text-sm text-ink-soft">
                      <Icon size={15} aria-hidden="true" /> {label}
                    </span>
                    <span className="text-sm font-semibold tabular-nums text-ink">{value}</span>
                  </div>
                ))}
              </div>
              <p className="mt-5 text-xs leading-5 text-ink-faint">
                Los umbrales pueden modificarse mediante una propuesta de política.
              </p>
            </div>
          </Reveal>
        </div>
      </section>

      <section id="reputacion" className="scroll-mt-24 py-12 sm:py-16">
        <Reveal>
          <div className="grid gap-8 lg:grid-cols-[0.8fr_1.2fr] lg:gap-16">
            <div>
              <h2 className="display-tight mt-3 text-[clamp(1.9rem,3.2vw,2.7rem)] text-ink">
                Puntos por actividad
              </h2>
              <p className="mt-4 max-w-md text-sm leading-7 text-ink-soft">
                La reputación determina el peso del voto. Las penalizaciones se aplican completas;
                las recompensas de los masters se multiplican por ×1,5.
              </p>
              <p className="mt-4 text-xs text-ink-faint">La reputación nunca es inferior a cero.</p>
            </div>

            <div className="glass rounded-3xl p-6 sm:p-8">
              <div className="divide-y divide-ink/8">
                {reputation.map(([action, points]) => (
                  <div key={action} className="flex items-center justify-between gap-4 py-3.5">
                    <span className="text-sm text-ink-soft">{action}</span>
                    <span className="text-sm font-semibold tabular-nums text-ink">{points}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </Reveal>
      </section>

      <section className="py-12 sm:py-16">
        <Reveal>
          <div className="max-w-2xl">
            <h2 className="display-tight mt-3 text-[clamp(1.9rem,3.2vw,2.7rem)] text-ink">
              Permisos por cuenta
            </h2>
          </div>
        </Reveal>
        <div className="mt-8 grid gap-4 md:grid-cols-3">
          {[
            {
              icon: Eye,
              role: ROLES.GUEST,
              tag: 'Solo lectura',
              copy: 'Consulta el catálogo y descarga materiales visibles.',
              points: ['Acceso al catálogo', 'Descargas disponibles', 'Sin votos ni comentarios'],
            },
            {
              icon: UserCircle,
              role: ROLES.USER,
              tag: 'Participa',
              copy: 'Propone materiales, vota y participa en los hilos.',
              points: ['Proponer material', 'Votar y comentar', 'Acumular reputación'],
            },
            {
              icon: Medal,
              role: ROLES.MASTER,
              tag: 'Modera',
              copy: 'Revisa propuestas y modera conversaciones.',
              points: ['Voto con más peso', 'Moderación', 'Acceso a administración'],
            },
          ].map((item, index) => (
            <Reveal key={item.role} delay={index * 0.04}>
              <div className="flex h-full flex-col glass-soft rounded-3xl p-6">
                <div className="flex items-center justify-between">
                  <item.icon size={20} className="text-ink-soft" aria-hidden="true" />
                  <span className="chip py-1 text-[0.55rem]">{item.tag}</span>
                </div>
                <h3 className="mt-6 text-xl font-semibold tracking-tight text-ink">{item.role}</h3>
                <p className="mt-2 flex-1 text-sm leading-6 text-ink-soft">{item.copy}</p>
                <ul className="mt-5 space-y-2 border-t border-ink/8 pt-4">
                  {item.points.map((point) => (
                    <li key={point} className="flex items-center gap-2 text-xs text-ink-soft">
                      <Check size={12} className="text-ink" aria-hidden="true" />
                      {point}
                    </li>
                  ))}
                </ul>
              </div>
            </Reveal>
          ))}
        </div>
      </section>

      <section className="py-4 sm:py-8">
        <Reveal>
          <div className="glass rounded-3xl p-6 sm:p-8">
            <div className="grid gap-8 lg:grid-cols-[0.9fr_1.1fr] lg:items-center">
              <div>
                <h2 className="display-tight mt-3 text-2xl text-ink">Visibilidad del autor</h2>
                <p className="mt-3 max-w-md text-sm leading-6 text-ink-soft">
                  Puedes elegir qué parte de tu identidad se muestra en cada archivo.
                </p>
              </div>
              <div className="space-y-3">
                {[
                  [Eye, 'Público', 'Cualquier persona ve tu alias.'],
                  [UsersFour, 'Comunidad', 'Solo usuarios registrados ven tu alias.'],
                  [EyeSlash, 'Privado', 'Solo tú y los masters podéis verlo.'],
                ].map(([Icon, title, copy]) => (
                  <div key={title} className="flex items-start gap-3 border-b border-ink/8 pb-3 last:border-0 last:pb-0">
                    <Icon size={16} className="mt-0.5 shrink-0 text-ink-soft" aria-hidden="true" />
                    <div>
                      <p className="text-sm font-semibold text-ink">{title}</p>
                      <p className="mt-0.5 text-xs leading-5 text-ink-soft">{copy}</p>
                    </div>
                  </div>
                ))}
                <p className="flex items-start gap-2 pt-2 text-[0.62rem] leading-5 text-ink-faint">
                  <LockKey size={12} className="mt-0.5 shrink-0" aria-hidden="true" />
                  La actividad de moderación se conserva para auditoría.
                </p>
              </div>
            </div>
          </div>
        </Reveal>
      </section>

      <section className="py-12 sm:py-16">
        <div className="grid gap-8 lg:grid-cols-[0.6fr_1.4fr]">
          <Reveal>
            <div>
              <h2 className="display-tight mt-3 text-[clamp(1.9rem,3.2vw,2.7rem)] text-ink">Dudas</h2>
              <Button
                href="https://github.com/AsierCL/Gradox2"
                variant="ghost"
                className="mt-6"
              >
                Documentación en GitHub
              </Button>
            </div>
          </Reveal>
          <Reveal delay={0.06}>
            <Accordion />
          </Reveal>
        </div>
      </section>

      <section className="pb-16 sm:pb-20">
        <Reveal>
          <div className="flex flex-col items-start justify-between gap-5 glass-soft rounded-3xl p-6 sm:flex-row sm:items-center sm:p-8">
            <div>
              <p className="text-base font-semibold text-ink">Comparte material</p>
              <p className="mt-1 text-sm text-ink-soft">Puedes proponer apuntes, exámenes o ejercicios.</p>
            </div>
            <Button to="/subir">Compartir material</Button>
          </div>
        </Reveal>
      </section>
    </div>
  )
}
