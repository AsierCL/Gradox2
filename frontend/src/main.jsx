import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { Toaster } from 'sonner'
import { CheckCircle, Info, WarningCircle } from '@phosphor-icons/react'
import App from './App'
import { AuthProvider } from './context/AuthContext'
import './index.css'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
      staleTime: 15_000,
    },
  },
})

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <AuthProvider>
          <App />
          <Toaster
            position="top-right"
            offset={88}
            icons={{
              success: <CheckCircle size={18} weight="fill" className="text-ink" />,
              error: <WarningCircle size={18} weight="fill" className="text-[#8b4b43]" />,
              info: <Info size={18} weight="fill" className="text-ink-soft" />,
              warning: <WarningCircle size={18} weight="fill" className="text-ink-soft" />,
            }}
            toastOptions={{
              style: {
                background: '#ffffff',
                border: '1px solid rgba(0, 0, 0, 0.08)',
                color: '#1d1d1f',
                boxShadow: '0 20px 40px -28px rgba(0, 0, 0, 0.28)',
                borderRadius: '16px',
              },
              className: 'font-sans',
            }}
          />
        </AuthProvider>
      </BrowserRouter>
    </QueryClientProvider>
  </StrictMode>,
)
