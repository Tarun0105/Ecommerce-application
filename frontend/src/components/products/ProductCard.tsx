import { useState } from 'react'
import { Link } from 'react-router-dom'
import { ShoppingCart, ImageOff } from 'lucide-react'
import toast from 'react-hot-toast'
import type { Product } from '../../types'
import { useAuth } from '../../context/AuthContext'
import { useCart } from '../../context/CartContext'

export default function ProductCard({ product }: { product: Product }) {
  const { isAuthenticated } = useAuth()
  const { addItem } = useCart()
  const [isAdding, setIsAdding] = useState(false)
  const inStock = product.stockQuantity > 0

  const handleAddToCart = async (e: React.MouseEvent) => {
    e.preventDefault()
    if (!isAuthenticated) { toast.error('Please login to add items to cart'); return }
    if (!inStock) return
    try {
      setIsAdding(true)
      await addItem(product.id, 1)
      toast.success('Added to cart!')
    } catch {
      toast.error('Failed to add to cart')
    } finally {
      setIsAdding(false)
    }
  }

  return (
    <Link to={`/products/${product.id}`}
      className="bg-white rounded-xl border border-gray-100 shadow-sm hover:shadow-md transition-shadow overflow-hidden group">
      <div className="aspect-square overflow-hidden bg-gray-100 relative">
        {product.imageUrl ? (
          <img src={product.imageUrl} alt={product.name}
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300" />
        ) : (
          <div className="w-full h-full flex items-center justify-center">
            <ImageOff className="h-12 w-12 text-gray-300" />
          </div>
        )}
        {!inStock && (
          <div className="absolute inset-0 bg-black bg-opacity-40 flex items-center justify-center">
            <span className="bg-white text-gray-800 px-3 py-1 rounded-full text-sm font-medium">Out of Stock</span>
          </div>
        )}
      </div>
      <div className="p-4">
        {product.category && (
          <p className="text-xs text-primary-600 font-medium mb-1 uppercase tracking-wide">{product.category.name}</p>
        )}
        <h3 className="font-semibold text-gray-900 mb-1 line-clamp-2 text-sm">{product.name}</h3>
        <div className="flex items-center justify-between mt-3">
          <span className="text-lg font-bold text-gray-900">${product.price.toFixed(2)}</span>
          <button
            onClick={handleAddToCart}
            disabled={!inStock || isAdding}
            className="flex items-center gap-1.5 bg-primary-600 hover:bg-primary-700 text-white px-3 py-1.5 rounded-lg text-sm font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <ShoppingCart className="h-4 w-4" />
            {isAdding ? '...' : 'Add'}
          </button>
        </div>
      </div>
    </Link>
  )
}
