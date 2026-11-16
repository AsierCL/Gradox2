import { Component } from 'react'
import { WarningCircle } from '@phosphor-icons/react'

export class AppErrorBoundary extends Component {
  state = { hasError: false }

  static getDerivedStateFromError() {
    return { hasError: true }
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="site-shell flex min-h-[60vh] flex-col items-center justify-center py-20 text-center">
          <span className="grid size-14 place-items-center rounded-full bg-white/60 text-aqua-700">
            <WarningCircle size={28} weight="duotone" aria-hidden="true" />
          </span>
          <p className="eyebrow mt-7 text-ink-faint">Error</p>
          <h1 className="display-tight mt-3 max-w-xl text-4xl text-ink">
            No se pudo mostrar esta página
          </h1>
          <p className="mt-4 max-w-md text-sm leading-6 text-ink-soft">
            Recarga la página o vuelve al inicio.
          </p>
          <div className="mt-7 flex flex-wrap justify-center gap-2">
            <button
              type="button"
              onClick={() => window.location.reload()}
              className="btn btn-primary"
            >
              Recargar
            </button>
            <a href="/" className="btn btn-ghost">
              Volver al inicio
            </a>
          </div>
        </div>
      )
    }

    return this.props.children
  }
}
