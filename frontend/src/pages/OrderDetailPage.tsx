import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { ArrowLeft, Package, MapPin, X } from 'lucide-react'
import toast from 'react-hot-toast'
import Layout from '../components/layout/Layout'
import Badge from '../components/ui/Badge'
import Button from '../components/ui/Button'
import LoadingSpinner from '../components/ui/LoadingSpinner'
import ConfirmDialog from '../components/ui/ConfirmDialog'
import { ordersApi } from '../api/orders'
import type { Order } from '../types'

export default function OrderDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [order, setOrder] = useState<Order | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [showCancel, setShowCancel] = useState(false)
  const [isCancelling, setIsCancelling] = useState(false)

  useEffect(() => {
    if (!id) return
    ordersApi.getOrderById(Number(id))
      .then(r => setOrder(r.data))
      .catch(() => navigate('/orders'))
      .finally(() => setIsLoading(false))
  }, [id])

  const handleCancel = async () => {
    if (!order) return
    try {
      setIsCancelling(true)
      const res = await ordersApi.cancelOrder(order.id)
      setOrder(res.data)
      toast.success('Order cancelled')
      setShowCancel(false)
    } catch (err: any) {
      toast.error(err.response?.data?.message ?? 'Failed to cancel order')
    } finally {
      setIsCancelling(false)
    }
  }

  if (isLoading) {
    return <Layout><div className="flex justify-center py-20"><LoadingSpinner size="lg" /></div></Layout>
  }

  if (!order) return null

  const canCancel = ['PENDING', 'CONFIRMED'].includes(order.status)

  return (
    <Layout>
      <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <button onClick={() => navigate('/orders')}
          className="flex items-center gap-1.5 text-sm text-gray-500 hover:text-gray-700 mb-6 transition-colors">
          <ArrowLeft className="h-4 w-4" /> Back to Orders
        </button>

        <div className="flex items-start justify-between mb-6">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Order #{order.id}</h1>
            <p className="text-sm text-gray-500 mt-1">{new Date(order.createdAt).toLocaleString()}</p>
          </div>
          <div className="flex items-center gap-3">
            <Badge status={order.status} />
            {canCancel && (
              <Button variant="danger" size="sm" onClick={() => setShowCancel(true)}>
                <X className="h-4 w-4" /> Cancel Order
              </Button>
            )}
          </div>
        </div>

        <div className="space-y-5">
          {/* Items */}
          <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-6">
            <h2 className="font-semibold text-gray-900 mb-4 flex items-center gap-2">
              <Package className="h-4 w-4" /> Order Items
            </h2>
            <div className="divide-y divide-gray-100">
              {order.items.map(item => (
                <div key={item.id} className="py-3 flex justify-between text-sm">
                  <div>
                    <p className="font-medium text-gray-900">{item.productName}</p>
                    <p className="text-gray-400 text-xs">SKU: {item.productSku} · Qty: {item.quantity}</p>
                  </div>
                  <p className="font-semibold text-gray-900">${item.subtotal.toFixed(2)}</p>
                </div>
              ))}
            </div>
            <div className="border-t border-gray-100 pt-4 mt-2 flex justify-between font-bold text-gray-900">
              <span>Total</span>
              <span>${order.totalAmount.toFixed(2)}</span>
            </div>
          </div>

          {/* Shipping */}
          <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-6">
            <h2 className="font-semibold text-gray-900 mb-3 flex items-center gap-2">
              <MapPin className="h-4 w-4" /> Shipping Address
            </h2>
            <p className="text-sm text-gray-600 leading-relaxed">
              {order.shippingAddress.firstName} {order.shippingAddress.lastName}<br />
              {order.shippingAddress.street}<br />
              {order.shippingAddress.city}, {order.shippingAddress.state} {order.shippingAddress.zipCode}<br />
              {order.shippingAddress.country}
            </p>
          </div>

          {order.notes && (
            <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-6">
              <h2 className="font-semibold text-gray-900 mb-2">Notes</h2>
              <p className="text-sm text-gray-600">{order.notes}</p>
            </div>
          )}
        </div>
      </div>

      <ConfirmDialog
        isOpen={showCancel}
        onClose={() => setShowCancel(false)}
        onConfirm={handleCancel}
        title="Cancel Order"
        message={`Are you sure you want to cancel order #${order.id}? This action cannot be undone.`}
        confirmText="Cancel Order"
        isLoading={isCancelling}
      />
    </Layout>
  )
}
