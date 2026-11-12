import { Suspense, useEffect } from 'react'
import { Outlet, useLocation } from 'react-router-dom'
import { motion, useReducedMotion } from 'framer-motion'
import { AmbientBackground } from './AmbientBackground'
import { Navbar } from './Navbar'
import { Footer } from './Footer'
import { AppErrorBoundary } from './AppErrorBoundary'
import { PageLoader } from '../ui/Spinner'

const pageTitles = [
  [/^\/$/, 'Gradox · Material de Ingeniería Informática USC'],
  [/^\/explorar/, 'Explorar materiales · Gradox'],
  [/^\/material\//, 'Material · Gradox'],
  [/^\/comunidad/, 'Comunidad y reglas · Gradox'],
  [/^\/acceso/, 'Iniciar sesión · Gradox'],
  [/^\/registro/, 'Crear cuenta · Gradox'],
  [/^\/verificar/, 'Verificar cuenta · Gradox'],
  [/^\/recuperar/, 'Recuperar contraseña · Gradox'],
  [/^\/perfil/, 'Tu perfil · Gradox'],
  [/^\/propuestas\/promocion\//, 'Candidatura a master · Gradox'],
  [/^\/propuestas\//, 'Propuesta · Gradox'],
  [/^\/propuestas/, 'Propuestas · Gradox'],
  [/^\/subir/, 'Compartir material · Gradox'],
  [/^\/administracion/, 'Administración · Gradox'],
]

function ScrollManager() {
  const { pathname, hash } = useLocation()

  useEffect(() => {
    if (hash) {
      const element = document.querySelector(hash)
      if (element) {
        element.scrollIntoView({ behavior: 'smooth', block: 'start' })
        return
      }
    }
    window.scrollTo(0, 0)
  }, [pathname, hash])

  useEffect(() => {
    const match = pageTitles.find(([pattern]) => pattern.test(pathname))
    document.title = match ? match[1] : 'Página no encontrada · Gradox'
  }, [pathname])

  return null
}

export function AppShell() {
  const location = useLocation()
  const reduceMotion = useReducedMotion()

  return (
    <>
      <a
        href="#contenido"
        className="sr-only focus:not-sr-only focus:fixed focus:left-4 focus:top-4 focus:z-[100] focus:rounded-full focus:bg-ink focus:px-4 focus:py-2 focus:text-sm focus:text-white"
      >
        Saltar al contenido
      </a>
      <AmbientBackground />
      <ScrollManager />
      <Navbar />
      <main id="contenido" className="relative z-0 min-h-[70vh]">
        <motion.div
          key={location.pathname}
          initial={reduceMotion ? false : { opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.45, ease: [0.16, 1, 0.3, 1] }}
        >
          <AppErrorBoundary key={location.pathname}>
            <Suspense fallback={<PageLoader />}>
              <Outlet />
            </Suspense>
          </AppErrorBoundary>
        </motion.div>
      </main>
      <Footer />
    </>
  )
}
