import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { ClipboardList, ChevronRight } from 'lucide-react'
import Layout from '../components/layout/Layout'
import Badge from '../components/ui/Badge'
import LoadingSpinner from '../components/ui/LoadingSpinner'
import EmptyState from '../components/ui/EmptyState'
import Pagination from '../components/ui/Pagination'
import { ordersApi } from '../api/orders'
import type { Order, PageResponse } from '../types'

export default function OrderHistoryPage() {
  const [response, setResponse] = useState<PageResponse<Order> | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [page, setPage] = useState(0)

  useEffect(() => {
    setIsLoading(true)
    ordersApi.getMyOrders({ page, size: 10 })
      .then(r => setResponse(r.data))
      .catch(() => {})
      .finally(() => setIsLoading(false))
  }, [page])

  return (
    <Layout>
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <h1 className="text-2xl font-bold text-gray-900 mb-6 flex items-center gap-2">
          <ClipboardList className="h-6 w-6" /> Order History
        </h1>

        {isLoading ? (
          <div className="flex justify-center py-20"><LoadingSpinner size="lg" /></div>
        ) : response?.content.length === 0 ? (
          <EmptyState icon={ClipboardList} title="No orders yet"
            description="Once you place an order, it will appear here."
            actionLabel="Start Shopping" onAction={() => window.location.href = '/products'} />
        ) : (
          <>
            <div className="space-y-4">
              {response!.content.map(order => (
                <Link key={order.id} to={`/orders/${order.id}`}
                  className="block bg-white rounded-xl border border-gray-100 shadow-sm hover:shadow-md transition-shadow p-5">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-4">
                      <div>
                        <p className="font-semibold text-gray-900">Order #{order.id}</p>
                        <p className="text-xs text-gray-500 mt-0.5">
                          {new Date(order.createdAt).toLocaleDateString()} · {order.items.length} item{order.items.length !== 1 ? 's' : ''}
                        </p>
                      </div>
                      <Badge status={order.status} />
                    </div>
                    <div className="flex items-center gap-3">
                      <span className="font-bold text-gray-900">${order.totalAmount.toFixed(2)}</span>
                      <ChevronRight className="h-5 w-5 text-gray-400" />
                    </div>
                  </div>
                  <div className="mt-3 text-sm text-gray-500 truncate">
                    {order.items.map(i => i.productName).join(', ')}
                  </div>
                </Link>
              ))}
            </div>
            {response && (
              <Pagination currentPage={response.page} totalPages={response.totalPages} onPageChange={setPage} />
            )}
          </>
        )}
      </div>
    </Layout>
  )
}
