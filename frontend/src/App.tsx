import { BrowserRouter } from 'react-router-dom'
import { Toaster } from 'react-hot-toast'
import { AuthProvider } from './context/AuthContext'
import { CartProvider } from './context/CartContext'
import AppRouter from './router/AppRouter'

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <CartProvider>
          <AppRouter />
          <Toaster
            position="top-right"
            toastOptions={{
              duration: 4000,
              style: { background: '#333', color: '#fff' },
              success: { style: { background: '#059669' } },
              error: { style: { background: '#dc2626' } },
            }}
          />
        </CartProvider>
      </AuthProvider>
    </BrowserRouter>
  )
}
