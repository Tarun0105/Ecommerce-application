import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import toast from 'react-hot-toast'
import { MapPin } from 'lucide-react'
import Layout from '../components/layout/Layout'
import Input from '../components/ui/Input'
import Button from '../components/ui/Button'
import EmptyState from '../components/ui/EmptyState'
import { useCart } from '../context/CartContext'
import { ordersApi } from '../api/orders'
import type { ShippingAddress } from '../types'
import { ShoppingBag } from 'lucide-react'

const schema = z.object({
  firstName: z.string().min(1, 'Required'),
  lastName: z.string().min(1, 'Required'),
  street: z.string().min(1, 'Required'),
  city: z.string().min(1, 'Required'),
  state: z.string().min(1, 'Required'),
  zipCode: z.string().min(1, 'Required'),
  country: z.string().min(1, 'Required'),
  notes: z.string().optional(),
})
type FormData = z.infer<typeof schema>

export default function CheckoutPage() {
  const { cart, clearCart } = useCart()
  const navigate = useNavigate()
  const [isSubmitting, setIsSubmitting] = useState(false)

  const { register, handleSubmit, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { country: 'US' },
  })

  const isEmpty = !cart || cart.items.length === 0

  const onSubmit = async (data: FormData) => {
    const { notes, ...address } = data
    try {
      setIsSubmitting(true)
      const res = await ordersApi.checkout({ shippingAddress: address as ShippingAddress, notes: notes || undefined })
      await clearCart()
      toast.success('Order placed successfully!')
      navigate(`/order-confirmation/${res.data.id}`)
    } catch (err: any) {
      toast.error(err.response?.data?.message ?? 'Failed to place order')
    } finally {
      setIsSubmitting(false)
    }
  }

  if (isEmpty) {
    return (
      <Layout>
        <div className="max-w-3xl mx-auto px-4 py-16">
          <EmptyState icon={ShoppingBag} title="Your cart is empty"
            description="Add items to your cart before checking out."
            actionLabel="Browse Products" onAction={() => navigate('/products')} />
        </div>
      </Layout>
    )
  }

  return (
    <Layout>
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <h1 className="text-2xl font-bold text-gray-900 mb-6 flex items-center gap-2">
          <MapPin className="h-6 w-6" /> Checkout
        </h1>
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Shipping Form */}
          <form onSubmit={handleSubmit(onSubmit)} className="lg:col-span-2 bg-white rounded-xl border border-gray-100 shadow-sm p-6 space-y-5">
            <h2 className="text-lg font-semibold text-gray-900">Shipping Address</h2>
            <div className="grid grid-cols-2 gap-4">
              <Input label="First Name" {...register('firstName')} error={errors.firstName?.message} />
              <Input label="Last Name" {...register('lastName')} error={errors.lastName?.message} />
            </div>
            <Input label="Street Address" {...register('street')} error={errors.street?.message} />
            <div className="grid grid-cols-2 gap-4">
              <Input label="City" {...register('city')} error={errors.city?.message} />
              <Input label="State / Province" {...register('state')} error={errors.state?.message} />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <Input label="ZIP / Postal Code" {...register('zipCode')} error={errors.zipCode?.message} />
              <Input label="Country" {...register('country')} error={errors.country?.message} />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Order Notes (optional)</label>
              <textarea {...register('notes')} rows={3}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
                placeholder="Any special instructions..." />
            </div>
            <Button type="submit" fullWidth size="lg" isLoading={isSubmitting}>
              Place Order
            </Button>
          </form>

          {/* Order Summary */}
          <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-6 h-fit">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Order Summary</h2>
            <div className="space-y-2 mb-4">
              {cart!.items.map(item => (
                <div key={item.id} className="flex justify-between text-sm text-gray-600">
                  <span className="truncate mr-2">{item.productName} x{item.quantity}</span>
                  <span className="flex-shrink-0">${item.subtotal.toFixed(2)}</span>
                </div>
              ))}
            </div>
            <div className="border-t border-gray-100 pt-3 space-y-2 text-sm">
              <div className="flex justify-between text-gray-600">
                <span>Shipping</span>
                <span className="text-green-600">{cart!.totalAmount >= 50 ? 'Free' : '$4.99'}</span>
              </div>
              <div className="flex justify-between font-bold text-gray-900 text-base pt-1">
                <span>Total</span>
                <span>${(cart!.totalAmount + (cart!.totalAmount >= 50 ? 0 : 4.99)).toFixed(2)}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Layout>
  )
}
