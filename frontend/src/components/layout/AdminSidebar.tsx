import { NavLink, useNavigate } from 'react-router-dom'
import { LayoutDashboard, Package, Tag, Users, ShoppingBag, ArrowLeft, LogOut } from 'lucide-react'
import { useAuth } from '../../context/AuthContext'

const navItems = [
  { to: '/admin', label: 'Dashboard', icon: LayoutDashboard, end: true },
  { to: '/admin/products', label: 'Products', icon: Package },
  { to: '/admin/categories', label: 'Categories', icon: Tag },
  { to: '/admin/users', label: 'Users', icon: Users },
  { to: '/admin/orders', label: 'Orders', icon: ShoppingBag },
]

export default function AdminSidebar() {
  const { logout } = useAuth()
  const navigate = useNavigate()
  const handleLogout = () => { logout(); navigate('/') }

  return (
    <aside className="w-64 bg-gray-900 text-white flex flex-col min-h-screen">
      <div className="p-6 border-b border-gray-700">
        <div className="flex items-center gap-2">
          <Package className="h-6 w-6 text-primary-400" />
          <div>
            <p className="font-bold text-white">ShopVerse</p>
            <p className="text-xs text-gray-400">Admin Panel</p>
          </div>
        </div>
      </div>
      <nav className="flex-1 py-4">
        {navItems.map(({ to, label, icon: Icon, end }) => (
          <NavLink key={to} to={to} end={end}
            className={({ isActive }) =>
              `flex items-center gap-3 px-6 py-3 text-sm font-medium transition-colors ${
                isActive ? 'bg-primary-700 text-white' : 'text-gray-300 hover:bg-gray-800 hover:text-white'
              }`
            }>
            <Icon className="h-5 w-5" />
            {label}
          </NavLink>
        ))}
      </nav>
      <div className="p-4 border-t border-gray-700 space-y-1">
        <NavLink to="/"
          className="flex items-center gap-3 px-3 py-2 text-sm text-gray-300 hover:text-white hover:bg-gray-800 rounded-lg transition-colors">
          <ArrowLeft className="h-4 w-4" /> Back to Store
        </NavLink>
        <button onClick={handleLogout}
          className="flex items-center gap-3 px-3 py-2 text-sm text-red-400 hover:text-red-300 hover:bg-gray-800 rounded-lg transition-colors w-full">
          <LogOut className="h-4 w-4" /> Logout
        </button>
      </div>
    </aside>
  )
}
