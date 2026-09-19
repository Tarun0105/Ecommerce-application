import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { CheckCircle, Package } from 'lucide-react'
import Layout from '../components/layout/Layout'
import Badge from '../components/ui/Badge'
import LoadingSpinner from '../components/ui/LoadingSpinner'
import Button from '../components/ui/Button'
import { ordersApi } from '../api/orders'
import type { Order } from '../types'

export default function OrderConfirmationPage() {
  const { id } = useParams<{ id: string }>()
  const [order, setOrder] = useState<Order | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    if (!id) return
    ordersApi.getOrderById(Number(id))
      .then(r => setOrder(r.data))
      .catch(() => {})
      .finally(() => setIsLoading(false))
  }, [id])

  if (isLoading) {
    return <Layout><div className="flex justify-center py-20"><LoadingSpinner size="lg" /></div></Layout>
  }

  if (!order) {
    return (
      <Layout>
        <div className="max-w-2xl mx-auto px-4 py-20 text-center">
          <h2 className="text-xl font-bold text-gray-900 mb-4">Order not found</h2>
          <Link to="/orders"><Button>View My Orders</Button></Link>
        </div>
      </Layout>
    )
  }

  return (
    <Layout>
      <div className="max-w-2xl mx-auto px-4 sm:px-6 py-12">
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-green-100 rounded-full mb-4">
            <CheckCircle className="h-9 w-9 text-green-600" />
          </div>
          <h1 className="text-2xl font-bold text-gray-900 mb-2">Order Confirmed!</h1>
          <p className="text-gray-500">Thank you for your purchase. Your order has been placed successfully.</p>
        </div>

        <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-6 space-y-5">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-500">Order #{order.id}</p>
              <p className="text-xs text-gray-400">{new Date(order.createdAt).toLocaleDateString()}</p>
            </div>
            <Badge status={order.status} />
          </div>

          <div className="border-t border-gray-100 pt-4">
            <h3 className="font-semibold text-gray-900 mb-3 flex items-center gap-2">
              <Package className="h-4 w-4" /> Items
            </h3>
            <div className="space-y-2">
              {order.items.map(item => (
                <div key={item.id} className="flex justify-between text-sm text-gray-600">
                  <span>{item.productName} x{item.quantity}</span>
                  <span>${item.subtotal.toFixed(2)}</span>
                </div>
              ))}
            </div>
          </div>

          <div className="border-t border-gray-100 pt-4">
            <h3 className="font-semibold text-gray-900 mb-2">Shipping To</h3>
            <p className="text-sm text-gray-600">
              {order.shippingAddress.firstName} {order.shippingAddress.lastName}<br />
              {order.shippingAddress.street}<br />
              {order.shippingAddress.city}, {order.shippingAddress.state} {order.shippingAddress.zipCode}<br />
              {order.shippingAddress.country}
            </p>
          </div>

          <div className="border-t border-gray-100 pt-4 flex justify-between font-bold text-gray-900">
            <span>Total</span>
            <span>${order.totalAmount.toFixed(2)}</span>
          </div>
        </div>

        <div className="flex gap-3 mt-6">
          <Link to="/orders" className="flex-1">
            <Button variant="outline" fullWidth>View All Orders</Button>
          </Link>
          <Link to="/products" className="flex-1">
            <Button fullWidth>Continue Shopping</Button>
          </Link>
        </div>
      </div>
    </Layout>
  )
}
