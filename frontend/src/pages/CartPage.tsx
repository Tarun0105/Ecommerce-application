import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ShoppingCart, ShoppingBag } from 'lucide-react'
import toast from 'react-hot-toast'
import Layout from '../components/layout/Layout'
import CartItemRow from '../components/cart/CartItemRow'
import Button from '../components/ui/Button'
import LoadingSpinner from '../components/ui/LoadingSpinner'
import EmptyState from '../components/ui/EmptyState'
import ConfirmDialog from '../components/ui/ConfirmDialog'
import { useCart } from '../context/CartContext'

export default function CartPage() {
  const { cart, isLoading, updateItem, removeItem, clearCart } = useCart()
  const navigate = useNavigate()
  const [loadingItemId, setLoadingItemId] = useState<number | null>(null)
  const [showClearConfirm, setShowClearConfirm] = useState(false)
  const [isClearing, setIsClearing] = useState(false)

  const handleUpdateQuantity = async (itemId: number, quantity: number) => {
    if (quantity < 1) return
    try {
      setLoadingItemId(itemId)
      await updateItem(itemId, quantity)
    } catch {
      toast.error('Failed to update quantity')
    } finally {
      setLoadingItemId(null)
    }
  }

  const handleRemove = async (itemId: number) => {
    try {
      setLoadingItemId(itemId)
      await removeItem(itemId)
      toast.success('Item removed')
    } catch {
      toast.error('Failed to remove item')
    } finally {
      setLoadingItemId(null)
    }
  }

  const handleClearCart = async () => {
    try {
      setIsClearing(true)
      await clearCart()
      toast.success('Cart cleared')
      setShowClearConfirm(false)
    } catch {
      toast.error('Failed to clear cart')
    } finally {
      setIsClearing(false)
    }
  }

  if (isLoading) {
    return (
      <Layout>
        <div className="flex justify-center py-20"><LoadingSpinner size="lg" /></div>
      </Layout>
    )
  }

  const isEmpty = !cart || cart.items.length === 0

  return (
    <Layout>
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex items-center justify-between mb-6">
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
            <ShoppingCart className="h-6 w-6" /> Your Cart
            {cart && cart.totalItems > 0 && (
              <span className="text-sm font-normal text-gray-500">({cart.totalItems} item{cart.totalItems !== 1 ? 's' : ''})</span>
            )}
          </h1>
          {!isEmpty && (
            <button onClick={() => setShowClearConfirm(true)}
              className="text-sm text-red-500 hover:text-red-700 hover:underline transition-colors">
              Clear cart
            </button>
          )}
        </div>

        {isEmpty ? (
          <EmptyState
            icon={ShoppingBag}
            title="Your cart is empty"
            description="Browse our products and add items to your cart."
            actionLabel="Continue Shopping"
            onAction={() => navigate('/products')}
          />
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            <div className="lg:col-span-2 bg-white rounded-xl border border-gray-100 shadow-sm p-6">
              {cart!.items.map(item => (
                <CartItemRow
                  key={item.id}
                  item={item}
                  onUpdateQuantity={handleUpdateQuantity}
                  onRemove={handleRemove}
                  isLoading={loadingItemId === item.id}
                />
              ))}
            </div>

            <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-6 h-fit">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">Order Summary</h2>
              <div className="space-y-3 text-sm">
                <div className="flex justify-between text-gray-600">
                  <span>Subtotal ({cart!.totalItems} items)</span>
                  <span>${cart!.totalAmount.toFixed(2)}</span>
                </div>
                <div className="flex justify-between text-gray-600">
                  <span>Shipping</span>
                  <span className="text-green-600">{cart!.totalAmount >= 50 ? 'Free' : '$4.99'}</span>
                </div>
                <div className="border-t border-gray-100 pt-3 flex justify-between font-bold text-gray-900">
                  <span>Total</span>
                  <span>${(cart!.totalAmount + (cart!.totalAmount >= 50 ? 0 : 4.99)).toFixed(2)}</span>
                </div>
              </div>
              <Button fullWidth size="lg" className="mt-6" onClick={() => navigate('/checkout')}>
                Proceed to Checkout
              </Button>
              <Link to="/products"
                className="block text-center text-sm text-primary-600 hover:underline mt-3">
                Continue Shopping
              </Link>
            </div>
          </div>
        )}
      </div>

      <ConfirmDialog
        isOpen={showClearConfirm}
        onClose={() => setShowClearConfirm(false)}
        onConfirm={handleClearCart}
        title="Clear Cart"
        message="Are you sure you want to remove all items from your cart?"
        confirmText="Clear Cart"
        isLoading={isClearing}
      />
    </Layout>
  )
}
