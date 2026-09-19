import { useState, useEffect, useCallback } from 'react'
import toast from 'react-hot-toast'
import AdminLayout from '../../components/layout/AdminLayout'
import Badge from '../../components/ui/Badge'
import Pagination from '../../components/ui/Pagination'
import LoadingSpinner from '../../components/ui/LoadingSpinner'
import Modal from '../../components/ui/Modal'
import type { Order, PageResponse, OrderStatus } from '../../types'
import { adminApi } from '../../api/admin'

const ORDER_STATUSES: OrderStatus[] = ['PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED']

export default function AdminOrdersPage() {
  const [response, setResponse] = useState<PageResponse<Order> | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [page, setPage] = useState(0)
  const [statusFilter, setStatusFilter] = useState('')
  const [viewOrder, setViewOrder] = useState<Order | null>(null)
  const [updatingId, setUpdatingId] = useState<number | null>(null)

  const load = useCallback(() => {
    setIsLoading(true)
    adminApi.getAllOrders({ page, size: 20, status: statusFilter || undefined }).then(r => setResponse(r.data)).finally(() => setIsLoading(false))
  }, [page, statusFilter])

  useEffect(load, [load])

  const handleStatusUpdate = async (orderId: number, status: string) => {
    try {
      setUpdatingId(orderId)
      const res = await adminApi.updateOrderStatus(orderId, status)
      setResponse(prev => prev ? { ...prev, content: prev.content.map(o => o.id === orderId ? res.data : o) } : prev)
      if (viewOrder?.id === orderId) setViewOrder(res.data)
      toast.success('Order status updated')
    } catch (err: any) {
      toast.error(err.response?.data?.message ?? 'Failed to update status')
    } finally {
      setUpdatingId(null)
    }
  }

  return (
    <AdminLayout>
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Orders</h1>
      <div className="mb-4">
        <select value={statusFilter} onChange={e => { setStatusFilter(e.target.value); setPage(0) }}
          className="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400">
          <option value="">All Statuses</option>
          {ORDER_STATUSES.map(s => <option key={s} value={s}>{s}</option>)}
        </select>
      </div>

      {isLoading ? <div className="flex justify-center py-12"><LoadingSpinner size="lg" /></div> : (
        <div className="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 border-b border-gray-100">
              <tr>
                {['Order ID', 'Customer', 'Status', 'Total', 'Date', 'Update Status', ''].map(h => (
                  <th key={h} className="text-left px-4 py-3 text-xs font-medium text-gray-500 uppercase">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {response?.content.map(order => (
                <tr key={order.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3 font-mono font-medium text-gray-900">#{order.id}</td>
                  <td className="px-4 py-3 text-gray-600 max-w-[150px] truncate">{order.userEmail}</td>
                  <td className="px-4 py-3"><Badge status={order.status} /></td>
                  <td className="px-4 py-3 font-semibold text-gray-900">${order.totalAmount.toFixed(2)}</td>
                  <td className="px-4 py-3 text-gray-500">{new Date(order.createdAt).toLocaleDateString()}</td>
                  <td className="px-4 py-3">
                    <select
                      value={order.status}
                      onChange={e => handleStatusUpdate(order.id, e.target.value)}
                      disabled={updatingId === order.id || order.status === 'CANCELLED' || order.status === 'DELIVERED'}
                      className="border border-gray-300 rounded-lg px-2 py-1 text-xs focus:outline-none focus:ring-2 focus:ring-primary-400 disabled:opacity-50">
                      {ORDER_STATUSES.map(s => <option key={s} value={s}>{s}</option>)}
                    </select>
                  </td>
                  <td className="px-4 py-3">
                    <button onClick={() => setViewOrder(order)}
                      className="text-primary-600 hover:underline text-xs font-medium">Details</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {!response?.content.length && <p className="text-center text-gray-500 py-12">No orders found.</p>}
        </div>
      )}
      {response && <Pagination currentPage={response.page} totalPages={response.totalPages} onPageChange={setPage} />}

      <Modal isOpen={!!viewOrder} onClose={() => setViewOrder(null)} title={`Order #${viewOrder?.id}`} size="lg">
        {viewOrder && (
          <div className="space-y-4">
            <div className="flex items-center gap-3">
              <Badge status={viewOrder.status} />
              <span className="text-sm text-gray-500">Customer: {viewOrder.userEmail}</span>
            </div>
            <div>
              <h4 className="font-medium text-gray-900 mb-2">Items</h4>
              {viewOrder.items.map(item => (
                <div key={item.id} className="flex justify-between text-sm py-1.5 border-b border-gray-50 last:border-0">
                  <span>{item.productName} x{item.quantity}</span>
                  <span className="font-medium">${item.subtotal.toFixed(2)}</span>
                </div>
              ))}
              <div className="flex justify-between font-bold mt-2 pt-2 border-t">
                <span>Total</span>
                <span>${viewOrder.totalAmount.toFixed(2)}</span>
              </div>
            </div>
            <div>
              <h4 className="font-medium text-gray-900 mb-2">Shipping</h4>
              <address className="not-italic text-sm text-gray-600 leading-relaxed">
                {viewOrder.shippingAddress.firstName} {viewOrder.shippingAddress.lastName}<br />
                {viewOrder.shippingAddress.street}<br />
                {viewOrder.shippingAddress.city}, {viewOrder.shippingAddress.state} {viewOrder.shippingAddress.zipCode}<br />
                {viewOrder.shippingAddress.country}
              </address>
            </div>
          </div>
        )}
      </Modal>
    </AdminLayout>
  )
}
