import { useState, useEffect, useCallback } from 'react'
import { Search, ShieldAlert } from 'lucide-react'
import toast from 'react-hot-toast'
import AdminLayout from '../../components/layout/AdminLayout'
import Pagination from '../../components/ui/Pagination'
import LoadingSpinner from '../../components/ui/LoadingSpinner'
import Button from '../../components/ui/Button'
import type { User, PageResponse } from '../../types'
import { adminApi } from '../../api/admin'

export default function AdminUsersPage() {
  const [response, setResponse] = useState<PageResponse<User> | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const [togglingId, setTogglingId] = useState<number | null>(null)

  const load = useCallback(() => {
    setIsLoading(true)
    adminApi.getUsers({ page, size: 20, search }).then(r => setResponse(r.data)).finally(() => setIsLoading(false))
  }, [page, search])

  useEffect(load, [load])

  const handleToggle = async (user: User) => {
    if (user.role === 'ROLE_ADMIN') { toast.error('Cannot disable admin accounts'); return }
    try {
      setTogglingId(user.id)
      const res = await adminApi.toggleUserStatus(user.id)
      setResponse(prev => prev ? { ...prev, content: prev.content.map(u => u.id === user.id ? res.data : u) } : prev)
      toast.success(res.data.enabled ? 'User activated' : 'User disabled')
    } catch (err: any) {
      toast.error(err.response?.data?.message ?? 'Failed')
    } finally {
      setTogglingId(null)
    }
  }

  return (
    <AdminLayout>
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Users</h1>
      <div className="mb-4 relative">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
        <input value={search} onChange={e => { setSearch(e.target.value); setPage(0) }} placeholder="Search by name or email..."
          className="w-full sm:w-80 pl-9 pr-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary-400" />
      </div>
      {isLoading ? <div className="flex justify-center py-12"><LoadingSpinner size="lg" /></div> : (
        <div className="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 border-b border-gray-100">
              <tr>
                {['Name', 'Email', 'Role', 'Status', 'Joined', 'Actions'].map(h => (
                  <th key={h} className="text-left px-4 py-3 text-xs font-medium text-gray-500 uppercase">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {response?.content.map(user => (
                <tr key={user.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3 font-medium text-gray-900">{user.firstName} {user.lastName}</td>
                  <td className="px-4 py-3 text-gray-500">{user.email}</td>
                  <td className="px-4 py-3">
                    {user.role === 'ROLE_ADMIN' ? (
                      <span className="inline-flex items-center gap-1 px-2 py-0.5 bg-purple-100 text-purple-700 rounded-full text-xs font-medium">
                        <ShieldAlert className="h-3 w-3" /> Admin
                      </span>
                    ) : (
                      <span className="px-2 py-0.5 bg-gray-100 text-gray-600 rounded-full text-xs">User</span>
                    )}
                  </td>
                  <td className="px-4 py-3">
                    <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${user.enabled ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                      {user.enabled ? 'Active' : 'Disabled'}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-gray-500">{new Date(user.createdAt).toLocaleDateString()}</td>
                  <td className="px-4 py-3">
                    {user.role !== 'ROLE_ADMIN' && (
                      <Button variant={user.enabled ? 'danger' : 'primary'} size="sm"
                        isLoading={togglingId === user.id} onClick={() => handleToggle(user)}>
                        {user.enabled ? 'Disable' : 'Enable'}
                      </Button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {!response?.content.length && <p className="text-center text-gray-500 py-12">No users found.</p>}
        </div>
      )}
      {response && <Pagination currentPage={response.page} totalPages={response.totalPages} onPageChange={setPage} />}
    </AdminLayout>
  )
}
