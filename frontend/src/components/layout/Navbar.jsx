import { useEffect, useRef, useState } from 'react'
import { Link, NavLink, useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import {
  ArrowRight,
  BookOpenText,
  MagnifyingGlass,
  ShieldCheck,
  UploadSimple,
  UserCircle,
  SignOut,
  ListBullets,
} from '@phosphor-icons/react'
import { useAuth } from '../../context/AuthContext'
import { Logo } from '../ui/Logo'
import { Avatar } from '../ui/Avatar'
import { Button } from '../ui/Button'
import { Modal } from '../ui/Modal'
import { cn } from '../../lib/cn'
import { ROLES } from '../../lib/constants'

const publicLinks = [
  { to: '/', label: 'Inicio', end: true },
  { to: '/explorar', label: 'Explorar' },
  { to: '/comunidad', label: 'Comunidad' },
]

function UserMenu() {
  const { profile, session, isMaster, logout } = useAuth()
  const [open, setOpen] = useState(false)
  const ref = useRef(null)
  const navigate = useNavigate()

  useEffect(() => {
    if (!open) return undefined
    const handlePointer = (event) => {
      if (!ref.current?.contains(event.target)) setOpen(false)
    }
    const handleKey = (event) => {
      if (event.key === 'Escape') setOpen(false)
    }
    document.addEventListener('pointerdown', handlePointer)
    document.addEventListener('keydown', handleKey)
    return () => {
      document.removeEventListener('pointerdown', handlePointer)
      document.removeEventListener('keydown', handleKey)
    }
  }, [open])

  const handleLogout = async () => {
    setOpen(false)
    await logout()
    navigate('/')
  }

  return (
    <div ref={ref} className="relative">
      <button
        type="button"
        onClick={() => setOpen((value) => !value)}
        aria-haspopup="menu"
        aria-expanded={open}
        aria-label={`Cuenta de ${profile?.username || session?.username || 'usuario'}`}
        className="flex items-center gap-2 rounded-full border border-white/70 bg-white/45 p-1 pr-2.5 transition-colors hover:bg-white/80"
      >
        <Avatar
          name={profile?.username || session?.username}
          src={profile?.profilePictureUrl}
          size="sm"
        />
        <span className="hidden max-w-24 truncate text-xs font-semibold text-ink sm:block">
          {profile?.username || session?.username}
        </span>
      </button>

      {open && (
        <div
          role="menu"
          className="glass absolute right-0 top-[calc(100%+0.65rem)] z-50 w-64 overflow-hidden rounded-3xl p-2"
        >
          <div className="border-b border-ink/8 px-3 py-3">
            <p className="truncate text-sm font-semibold text-ink">
              {profile?.username || session?.username}
            </p>
            <p className="mt-0.5 text-[0.7rem] text-ink-faint">
              {ROLES[(profile?.role || session?.role || '').toUpperCase()] || 'Estudiante'} ·{' '}
              {Number(profile?.reputation || 0).toFixed(1)} de reputación
            </p>
          </div>
          <div className="py-1">
            <Link
              to="/perfil"
              role="menuitem"
              onClick={() => setOpen(false)}
              className="flex items-center gap-2.5 rounded-2xl px-3 py-2.5 text-sm text-ink-soft transition-colors hover:bg-white/70 hover:text-ink"
            >
              <UserCircle size={18} aria-hidden="true" /> Mi perfil
            </Link>
            <Link
              to="/propuestas"
              role="menuitem"
              onClick={() => setOpen(false)}
              className="flex items-center gap-2.5 rounded-2xl px-3 py-2.5 text-sm text-ink-soft transition-colors hover:bg-white/70 hover:text-ink"
            >
              <ListBullets size={18} aria-hidden="true" /> Propuestas
            </Link>
            <Link
              to="/subir"
              role="menuitem"
              onClick={() => setOpen(false)}
              className="flex items-center gap-2.5 rounded-2xl px-3 py-2.5 text-sm text-ink-soft transition-colors hover:bg-white/70 hover:text-ink"
            >
              <UploadSimple size={18} aria-hidden="true" /> Compartir material
            </Link>
            {isMaster && (
              <Link
                to="/administracion"
                role="menuitem"
                onClick={() => setOpen(false)}
                className="flex items-center gap-2.5 rounded-2xl px-3 py-2.5 text-sm text-ink-soft transition-colors hover:bg-white/70 hover:text-ink"
              >
                <ShieldCheck size={18} aria-hidden="true" /> Administración
              </Link>
            )}
          </div>
          <div className="border-t border-ink/8 pt-1">
            <button
              type="button"
              role="menuitem"
              onClick={handleLogout}
              className="flex w-full items-center gap-2.5 rounded-2xl px-3 py-2.5 text-sm text-[#8b4b43] transition-colors hover:bg-[#f8e9e7]"
            >
              <SignOut size={18} aria-hidden="true" /> Cerrar sesión
            </button>
          </div>
        </div>
      )}
    </div>
  )
}

function SearchDialog({ open, onClose }) {
  const [query, setQuery] = useState('')
  const navigate = useNavigate()
  const inputRef = useRef(null)

  useEffect(() => {
    if (open) {
      const timer = window.setTimeout(() => inputRef.current?.focus(), 80)
      return () => window.clearTimeout(timer)
    }
    return undefined
  }, [open])

  const submit = (event) => {
    event.preventDefault()
    const value = query.trim()
    navigate(value ? `/explorar?q=${encodeURIComponent(value)}` : '/explorar')
    onClose()
  }

  const quick = ['Programación', 'Bases de Datos', 'Exámenes', 'Inteligencia Artificial']

  return (
    <Modal
      open={open}
      onClose={onClose}
      title="Encuentra lo que necesitas"
      description="Busca por asignatura, título o tipo de material."
      className="sm:max-w-2xl"
    >
      <form onSubmit={submit}>
        <div className="flex items-center gap-3 rounded-2xl border border-ink/12 bg-white/65 px-4">
          <MagnifyingGlass size={20} className="text-aqua-700" aria-hidden="true" />
          <input
            ref={inputRef}
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder="Ej. examen de Bases de Datos"
            aria-label="Buscar material"
            className="h-14 w-full bg-transparent text-base text-ink outline-none placeholder:text-ink-faint"
          />
          <kbd className="hidden rounded-lg border border-ink/10 bg-paper-50 px-2 py-1 text-[0.65rem] text-ink-faint sm:block">
            ↵
          </kbd>
        </div>
        <div className="mt-5">
          <p className="eyebrow text-ink-faint">Búsquedas frecuentes</p>
          <div className="mt-3 flex flex-wrap gap-2">
            {quick.map((item) => (
              <button
                key={item}
                type="button"
                onClick={() => {
                  navigate(`/explorar?q=${encodeURIComponent(item)}`)
                  onClose()
                }}
                className="chip transition-colors hover:border-aqua-200 hover:bg-aqua-50 hover:text-aqua-800"
              >
                {item}
              </button>
            ))}
          </div>
        </div>
        <div className="mt-7 flex flex-wrap items-center justify-between gap-3 border-t border-ink/8 pt-5">
          <Link
            to="/explorar"
            onClick={onClose}
            className="inline-flex items-center gap-1.5 text-xs font-semibold text-ink-soft hover:text-ink"
          >
            <BookOpenText size={15} aria-hidden="true" /> Ver todo el catálogo
          </Link>
          <Button type="submit" icon={<ArrowRight size={16} weight="bold" />}>
            Buscar
          </Button>
        </div>
      </form>
    </Modal>
  )
}

export function Navbar() {
  const { isAuthenticated, isMaster, logout } = useAuth()
  const navigate = useNavigate()
  const [searchOpen, setSearchOpen] = useState(false)
  const [mobileOpen, setMobileOpen] = useState(false)

  useEffect(() => {
    const handler = (event) => {
      if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === 'k') {
        event.preventDefault()
        setSearchOpen(true)
      }
    }
    window.addEventListener('keydown', handler)
    return () => window.removeEventListener('keydown', handler)
  }, [])

  useEffect(() => {
    if (mobileOpen) {
      document.body.style.overflow = 'hidden'
      return () => {
        document.body.style.overflow = ''
      }
    }
    return undefined
  }, [mobileOpen])

  const links = isAuthenticated
    ? [
        ...publicLinks,
        { to: '/propuestas', label: 'Propuestas' },
        { to: '/subir', label: 'Subir' },
      ]
    : publicLinks

  const handleMobileLogout = async () => {
    setMobileOpen(false)
    await logout()
    toast.success('Sesión cerrada')
    navigate('/')
  }

  return (
    <>
      <header className="sticky top-0 z-50 pt-3">
        <div className="site-shell">
          <div className="glass-nav flex h-[4.25rem] items-center justify-between gap-3 rounded-full px-4 pl-5 sm:px-5">
            <Logo />

            <nav aria-label="Navegación principal" className="hidden items-center gap-1 lg:flex">
              {links.map((link) => (
                <NavLink key={link.to} to={link.to} end={link.end} className="nav-link">
                  {link.label}
                </NavLink>
              ))}
              {isMaster && (
                <NavLink to="/administracion" className="nav-link">
                  Administración
                </NavLink>
              )}
            </nav>

            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={() => setSearchOpen(true)}
                className="hidden items-center gap-2 rounded-full border border-ink/8 bg-white/40 py-2 pl-3 pr-2 text-xs text-ink-soft transition-colors hover:bg-white/75 hover:text-ink sm:flex"
                aria-label="Abrir búsqueda"
              >
                <MagnifyingGlass size={15} aria-hidden="true" />
                <span>Buscar</span>
                <kbd className="rounded-md border border-ink/8 bg-white/55 px-1.5 py-0.5 text-[0.6rem] text-ink-faint">
                  ⌘ K
                </kbd>
              </button>

              {isAuthenticated ? (
                <UserMenu />
              ) : (
                <div className="hidden items-center gap-2 sm:flex">
                  <Button to="/acceso" variant="ghost" className="min-h-9 px-4 py-2 text-xs">
                    Entrar
                  </Button>
                  <Button to="/registro" className="min-h-9 px-4 py-2 text-xs">
                    Crear cuenta
                  </Button>
                </div>
              )}

              <button
                type="button"
                className="grid size-9 place-items-center rounded-full border border-ink/8 bg-white/45 text-ink lg:hidden"
                onClick={() => setMobileOpen((value) => !value)}
                aria-expanded={mobileOpen}
                aria-controls="mobile-nav"
                aria-label={mobileOpen ? 'Cerrar menú' : 'Abrir menú'}
              >
                <span className="relative block h-3.5 w-4">
                  <span
                    className={cn(
                      'absolute left-0 top-0.5 h-px w-full bg-current transition-transform duration-300',
                      mobileOpen && 'translate-y-[5px] rotate-45',
                    )}
                  />
                  <span
                    className={cn(
                      'absolute bottom-0.5 left-0 h-px w-full bg-current transition-transform duration-300',
                      mobileOpen && '-translate-y-[5px] -rotate-45',
                    )}
                  />
                </span>
              </button>
            </div>
          </div>
        </div>
      </header>

      {mobileOpen && (
        <div
          id="mobile-nav"
          className="glass-nav fixed inset-x-3 top-[5.35rem] z-40 rounded-4xl p-6 lg:hidden"
        >
          <nav aria-label="Navegación móvil" className="flex flex-col">
            {links.map((link) => (
              <NavLink
                key={link.to}
                to={link.to}
                end={link.end}
                onClick={() => setMobileOpen(false)}
                className={({ isActive }) =>
                  cn(
                    'border-b border-ink/8 py-4 text-2xl tracking-tight text-ink-soft last:border-0',
                    isActive && 'text-ink',
                  )
                }
              >
                {link.label}
              </NavLink>
            ))}
            {isMaster && (
              <NavLink
                to="/administracion"
                onClick={() => setMobileOpen(false)}
                className="border-b border-ink/8 py-4 text-2xl tracking-tight text-ink-soft"
              >
                Administración
              </NavLink>
            )}
          </nav>
          <div className="mt-6 flex flex-col gap-2">
            <Button
              onClick={() => {
                setMobileOpen(false)
                setSearchOpen(true)
              }}
              variant="ghost"
              icon={<MagnifyingGlass size={17} />}
            >
              Buscar material
            </Button>
            {isAuthenticated ? (
              <div className="mt-2 grid grid-cols-2 gap-2 border-t border-ink/8 pt-4">
                <Link
                  to="/perfil"
                  onClick={() => setMobileOpen(false)}
                  className="rounded-2xl border border-ink/8 bg-white/38 px-3 py-3 text-center text-xs font-semibold text-ink-soft transition-colors hover:bg-white/70 hover:text-ink"
                >
                  Mi perfil
                </Link>
                <Link
                  to="/subir"
                  onClick={() => setMobileOpen(false)}
                  className="rounded-2xl border border-ink/8 bg-white/38 px-3 py-3 text-center text-xs font-semibold text-ink-soft transition-colors hover:bg-white/70 hover:text-ink"
                >
                  Compartir
                </Link>
                <button
                  type="button"
                  onClick={handleMobileLogout}
                  className="col-span-2 rounded-2xl px-3 py-2 text-xs font-semibold text-[#8b4b43] transition-colors hover:bg-[#f8e9e7]"
                >
                  Cerrar sesión
                </button>
              </div>
            ) : (
              <>
                <Button to="/registro" onClick={() => setMobileOpen(false)}>
                  Crear cuenta USC
                </Button>
                <Button
                  to="/acceso"
                  variant="ghost"
                  onClick={() => setMobileOpen(false)}
                >
                  Ya tengo cuenta
                </Button>
              </>
            )}
          </div>
        </div>
      )}

      <SearchDialog
        key={searchOpen ? 'search-open' : 'search-closed'}
        open={searchOpen}
        onClose={() => setSearchOpen(false)}
      />
    </>
  )
}
