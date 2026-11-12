import { Link } from 'react-router-dom'
import { ArrowUpRight, GithubLogo, ChatCircleDots } from '@phosphor-icons/react'
import { Logo } from '../ui/Logo'

const columns = [
  {
    title: 'Biblioteca',
    links: [
      { to: '/explorar', label: 'Explorar materiales' },
      { to: '/subir', label: 'Compartir material' },
      { to: '/propuestas', label: 'Propuestas' },
    ],
  },
  {
    title: 'Comunidad',
    links: [
      { to: '/comunidad', label: 'Funcionamiento' },
      { to: '/comunidad#reputacion', label: 'Reputación' },
      { to: '/comunidad#gobernanza', label: 'Votaciones' },
    ],
  },
]

export function Footer() {
  return (
    <footer className="site-shell mt-20 pb-8 sm:mt-28">
      <div className="border-t border-ink/10 pt-10">
        <div className="grid gap-10 lg:grid-cols-[1.5fr_1fr_1fr_1.2fr]">
          <div>
            <Logo />
            <p className="mt-4 max-w-xs text-sm leading-6 text-ink-soft">
              Material de Ingeniería Informática de la Universidad de Santiago de Compostela.
            </p>
          </div>

          {columns.map((column) => (
            <div key={column.title}>
              <p className="eyebrow text-ink-faint">{column.title}</p>
              <ul className="mt-4 space-y-2.5">
                {column.links.map((link) => (
                  <li key={link.to}>
                    <Link
                      to={link.to}
                      className="text-sm text-ink-soft transition-colors hover:text-ink"
                    >
                      {link.label}
                    </Link>
                  </li>
                ))}
              </ul>
            </div>
          ))}

          <div>
            <p className="eyebrow text-ink-faint">Código abierto</p>
            <p className="mt-4 text-sm leading-6 text-ink-soft">Gradox es software libre bajo GPL-3.0.</p>
            <div className="mt-4 flex items-center gap-3">
              <a
                href="https://github.com/AsierCL/Gradox2"
                target="_blank"
                rel="noreferrer"
                className="text-ink-soft transition-colors hover:text-ink"
                aria-label="Gradox en GitHub"
              >
                <GithubLogo size={19} aria-hidden="true" />
              </a>
              <a
                href="https://github.com/AsierCL/Gradox2/issues"
                target="_blank"
                rel="noreferrer"
                className="text-ink-soft transition-colors hover:text-ink"
                aria-label="Abrir una incidencia"
              >
                <ChatCircleDots size={19} aria-hidden="true" />
              </a>
              <a
                href="https://github.com/AsierCL/Gradox2"
                target="_blank"
                rel="noreferrer"
                className="inline-flex items-center gap-1 text-xs font-semibold text-ink"
              >
                Ver código <ArrowUpRight size={13} weight="bold" aria-hidden="true" />
              </a>
            </div>
          </div>
        </div>

        <div className="mt-10 flex flex-col gap-2 border-t border-ink/8 pt-5 text-[0.7rem] text-ink-faint sm:flex-row sm:items-center sm:justify-between">
          <p>© {new Date().getFullYear()} Gradox</p>
          <p>GPL-3.0 · Universidad de Santiago de Compostela</p>
        </div>
      </div>
    </footer>
  )
}
