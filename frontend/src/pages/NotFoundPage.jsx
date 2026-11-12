import { ArrowLeft, Compass, MagnifyingGlass } from '@phosphor-icons/react'
import { Button } from '../components/ui/Button'
import { Logomark } from '../components/ui/Logo'

export function NotFoundPage() {
  return (
    <div className="site-shell flex min-h-[60vh] flex-col items-center justify-center py-20 text-center">
      <Logomark className="size-14 opacity-80" />
      <p className="eyebrow mt-7 text-ink-faint">Error 404</p>
      <h1 className="display-tight mt-3 max-w-xl text-balance text-[clamp(2.4rem,5vw,3.8rem)] text-ink">
        Página no encontrada
      </h1>
      <p className="mt-4 max-w-md text-sm leading-6 text-ink-soft">
        La página que buscas no existe o ha cambiado de dirección.
      </p>
      <div className="mt-7 flex flex-wrap justify-center gap-2">
        <Button to="/explorar" icon={<Compass size={16} />}>
          Explorar materiales
        </Button>
        <Button to="/" variant="ghost" icon={<ArrowLeft size={16} />}>
          Volver al inicio
        </Button>
      </div>
      <p className="mt-7 inline-flex items-center gap-1.5 text-[0.65rem] text-ink-faint">
        <MagnifyingGlass size={13} aria-hidden="true" />
        También puedes buscar con ⌘ K
      </p>
    </div>
  )
}
