import { useState, useEffect } from 'react'
import { Users, Package, ShoppingBag, Clock, DollarSign } from 'lucide-react'
import AdminLayout from '../../components/layout/AdminLayout'
import type { DashboardStats } from '../../types'
import { adminApi } from '../../api/admin'

function StatCard({ label, value, icon: Icon, color }: { label: string; value: string | number; icon: React.ElementType; color: string }) {
  return (
    <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-6 flex items-center gap-4">
      <div className={`p-3 rounded-xl ${color}`}>
        <Icon className="h-6 w-6 text-white" />
      </div>
      <div>
        <p className="text-2xl font-bold text-gray-900">{value}</p>
        <p className="text-sm text-gray-500">{label}</p>
      </div>
    </div>
  )
}

function SkeletonCard() {
  return <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-6 animate-pulse h-24" />
}

export default function AdminDashboardPage() {
  const [stats, setStats] = useState<DashboardStats | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    adminApi.getDashboardStats().then(r => setStats(r.data)).finally(() => setIsLoading(false))
  }, [])

  return (
    <AdminLayout>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>
        <p className="text-gray-500 text-sm mt-1">Welcome to ShopVerse Admin Panel</p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-4 mb-8">
        {isLoading ? (
          Array.from({ length: 5 }).map((_, i) => <SkeletonCard key={i} />)
        ) : stats ? (
          <>
            <StatCard label="Total Users" value={stats.totalUsers} icon={Users} color="bg-blue-500" />
            <StatCard label="Active Products" value={stats.totalProducts} icon={Package} color="bg-green-500" />
            <StatCard label="Total Orders" value={stats.totalOrders} icon={ShoppingBag} color="bg-purple-500" />
            <StatCard label="Pending Orders" value={stats.pendingOrders} icon={Clock} color="bg-yellow-500" />
            <StatCard label="Total Revenue" value={`$${(stats.totalRevenue ?? 0).toFixed(2)}`} icon={DollarSign} color="bg-indigo-500" />
          </>
        ) : null}
      </div>
    </AdminLayout>
  )
}
