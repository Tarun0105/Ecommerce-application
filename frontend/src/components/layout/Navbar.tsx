import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ShoppingCart, User, Menu, X, Package, LogOut, LayoutDashboard, ChevronDown } from 'lucide-react'
import { useAuth } from '../../context/AuthContext'
import { useCart } from '../../context/CartContext'

export default function Navbar() {
  const { user, isAuthenticated, isAdmin, logout } = useAuth()
  const { cartItemCount } = useCart()
  const navigate = useNavigate()
  const [menuOpen, setMenuOpen] = useState(false)
  const [userMenuOpen, setUserMenuOpen] = useState(false)

  const handleLogout = () => {
    logout()
    navigate('/')
    setUserMenuOpen(false)
  }

  return (
    <nav className="bg-white shadow-sm border-b border-gray-100 sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          <div className="flex items-center gap-8">
            <Link to="/" className="flex items-center gap-2">
              <Package className="h-7 w-7 text-primary-600" />
              <span className="text-xl font-bold text-primary-700">ShopVerse</span>
            </Link>
            <div className="hidden md:flex items-center gap-6">
              <Link to="/" className="text-gray-600 hover:text-primary-600 font-medium transition-colors">Home</Link>
              <Link to="/products" className="text-gray-600 hover:text-primary-600 font-medium transition-colors">Products</Link>
            </div>
          </div>

          <div className="flex items-center gap-4">
            <Link to="/cart" className="relative p-2 text-gray-600 hover:text-primary-600 transition-colors">
              <ShoppingCart className="h-6 w-6" />
              {cartItemCount > 0 && (
                <span className="absolute -top-1 -right-1 bg-primary-600 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center font-medium">
                  {cartItemCount > 99 ? '99+' : cartItemCount}
                </span>
              )}
            </Link>

            {isAuthenticated ? (
              <div className="relative hidden md:block">
                <button
                  onClick={() => setUserMenuOpen(!userMenuOpen)}
                  className="flex items-center gap-2 text-gray-700 hover:text-primary-600 font-medium transition-colors"
                >
                  <User className="h-5 w-5" />
                  <span className="max-w-24 truncate">{user?.firstName}</span>
                  <ChevronDown className="h-4 w-4" />
                </button>
                {userMenuOpen && (
                  <div className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-gray-100 py-1 z-50">
                    <Link to="/profile" onClick={() => setUserMenuOpen(false)}
                      className="flex items-center gap-2 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50">
                      <User className="h-4 w-4" /> Profile
                    </Link>
                    <Link to="/orders" onClick={() => setUserMenuOpen(false)}
                      className="flex items-center gap-2 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50">
                      <ShoppingCart className="h-4 w-4" /> My Orders
                    </Link>
                    {isAdmin && (
                      <Link to="/admin" onClick={() => setUserMenuOpen(false)}
                        className="flex items-center gap-2 px-4 py-2 text-sm text-primary-600 hover:bg-primary-50 font-medium">
                        <LayoutDashboard className="h-4 w-4" /> Admin Dashboard
                      </Link>
                    )}
                    <hr className="my-1" />
                    <button onClick={handleLogout}
                      className="flex items-center gap-2 px-4 py-2 text-sm text-red-600 hover:bg-red-50 w-full text-left">
                      <LogOut className="h-4 w-4" /> Logout
                    </button>
                  </div>
                )}
              </div>
            ) : (
              <div className="hidden md:flex items-center gap-3">
                <Link to="/login" className="text-gray-600 hover:text-primary-600 font-medium">Login</Link>
                <Link to="/register" className="bg-primary-600 hover:bg-primary-700 text-white px-4 py-2 rounded-lg font-medium transition-colors">
                  Register
                </Link>
              </div>
            )}

            <button onClick={() => setMenuOpen(!menuOpen)} className="md:hidden p-2 text-gray-600">
              {menuOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
            </button>
          </div>
        </div>
      </div>

      {menuOpen && (
        <div className="md:hidden bg-white border-t border-gray-100 py-3 px-4 space-y-2">
          <Link to="/" onClick={() => setMenuOpen(false)} className="block py-2 text-gray-700 font-medium">Home</Link>
          <Link to="/products" onClick={() => setMenuOpen(false)} className="block py-2 text-gray-700 font-medium">Products</Link>
          {isAuthenticated ? (
            <>
              <Link to="/profile" onClick={() => setMenuOpen(false)} className="block py-2 text-gray-700">Profile</Link>
              <Link to="/orders" onClick={() => setMenuOpen(false)} className="block py-2 text-gray-700">My Orders</Link>
              {isAdmin && <Link to="/admin" onClick={() => setMenuOpen(false)} className="block py-2 text-primary-600 font-medium">Admin Dashboard</Link>}
              <button onClick={handleLogout} className="block py-2 text-red-600 font-medium w-full text-left">Logout</button>
            </>
          ) : (
            <>
              <Link to="/login" onClick={() => setMenuOpen(false)} className="block py-2 text-gray-700">Login</Link>
              <Link to="/register" onClick={() => setMenuOpen(false)} className="block py-2 text-primary-600 font-medium">Register</Link>
            </>
          )}
        </div>
      )}
    </nav>
  )
}
