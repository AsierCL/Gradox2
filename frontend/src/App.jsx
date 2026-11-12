import { lazy } from 'react'
import { Route, Routes } from 'react-router-dom'
import { AppShell } from './components/layout/AppShell'
import { ProtectedRoute } from './components/layout/ProtectedRoute'
import { HomePage } from './pages/HomePage'

const lazyPage = (loader, name) =>
  lazy(() => loader().then((module) => ({ default: module[name] })))

const ExplorePage = lazyPage(() => import('./pages/ExplorePage'), 'ExplorePage')
const FileDetailPage = lazyPage(() => import('./pages/FileDetailPage'), 'FileDetailPage')
const CommunityPage = lazyPage(() => import('./pages/CommunityPage'), 'CommunityPage')
const LoginPage = lazyPage(() => import('./pages/LoginPage'), 'LoginPage')
const RegisterPage = lazyPage(() => import('./pages/RegisterPage'), 'RegisterPage')
const VerifyPage = lazyPage(() => import('./pages/VerifyPage'), 'VerifyPage')
const RecoverPasswordPage = lazyPage(
  () => import('./pages/RecoverPasswordPage'),
  'RecoverPasswordPage',
)
const ProfilePage = lazyPage(() => import('./pages/ProfilePage'), 'ProfilePage')
const ProposalsPage = lazyPage(() => import('./pages/ProposalsPage'), 'ProposalsPage')
const ProposalDetailPage = lazyPage(
  () => import('./pages/ProposalDetailPage'),
  'ProposalDetailPage',
)
const PromotionDetailPage = lazyPage(
  () => import('./pages/ProposalDetailPage'),
  'PromotionDetailPage',
)
const UploadPage = lazyPage(() => import('./pages/UploadPage'), 'UploadPage')
const AdminPage = lazyPage(() => import('./pages/AdminPage'), 'AdminPage')
const NotFoundPage = lazyPage(() => import('./pages/NotFoundPage'), 'NotFoundPage')

export default function App() {
  return (
    <Routes>
      <Route element={<AppShell />}>
        <Route index element={<HomePage />} />
        <Route path="explorar" element={<ExplorePage />} />
        <Route path="material/:id" element={<FileDetailPage />} />
        <Route path="comunidad" element={<CommunityPage />} />

        <Route path="acceso" element={<LoginPage />} />
        <Route path="registro" element={<RegisterPage />} />
        <Route path="verificar" element={<VerifyPage />} />
        <Route path="recuperar" element={<RecoverPasswordPage />} />

        <Route
          path="perfil"
          element={
            <ProtectedRoute>
              <ProfilePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="propuestas"
          element={
            <ProtectedRoute>
              <ProposalsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="propuestas/promocion/:id"
          element={
            <ProtectedRoute masterOnly>
              <PromotionDetailPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="propuestas/:id"
          element={
            <ProtectedRoute>
              <ProposalDetailPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="subir"
          element={
            <ProtectedRoute>
              <UploadPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="administracion"
          element={
            <ProtectedRoute masterOnly>
              <AdminPage />
            </ProtectedRoute>
          }
        />

        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}
