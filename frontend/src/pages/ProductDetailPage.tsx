import { useState, useEffect } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import { ShoppingCart, ChevronRight, Minus, Plus, Package } from 'lucide-react'
import toast from 'react-hot-toast'
import Layout from '../components/layout/Layout'
import LoadingSpinner from '../components/ui/LoadingSpinner'
import Button from '../components/ui/Button'
import type { Product } from '../types'
import { productsApi } from '../api/products'
import { useAuth } from '../context/AuthContext'
import { useCart } from '../context/CartContext'

export default function ProductDetailPage() {
  const { id } = useParams<{ id: string }>()
  const [product, setProduct] = useState<Product | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [quantity, setQuantity] = useState(1)
  const [isAdding, setIsAdding] = useState(false)
  const { isAuthenticated } = useAuth()
  const { addItem } = useCart()
  const navigate = useNavigate()

  useEffect(() => {
    if (!id) return
    productsApi.getById(Number(id))
      .then(r => setProduct(r.data))
      .catch(() => navigate('/products'))
      .finally(() => setIsLoading(false))
  }, [id])

  const handleAddToCart = async () => {
    if (!isAuthenticated) { navigate('/login'); return }
    if (!product) return
    try {
      setIsAdding(true)
      await addItem(product.id, quantity)
      toast.success('Added to cart!')
    } catch (err: any) {
      toast.error(err.response?.data?.message ?? 'Failed to add to cart')
    } finally {
      setIsAdding(false)
    }
  }

  if (isLoading) return <Layout><div className="flex justify-center py-20"><LoadingSpinner size="lg" /></div></Layout>
  if (!product) return null

  const inStock = product.stockQuantity > 0
  const maxQty = Math.min(product.stockQuantity, 10)

  return (
    <Layout>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Breadcrumb */}
        <nav className="flex items-center gap-2 text-sm text-gray-500 mb-6">
          <Link to="/" className="hover:text-primary-600">Home</Link>
          <ChevronRight className="h-4 w-4" />
          <Link to="/products" className="hover:text-primary-600">Products</Link>
          <ChevronRight className="h-4 w-4" />
          <span className="text-gray-900 font-medium truncate">{product.name}</span>
        </nav>

        <div className="grid md:grid-cols-2 gap-10">
          {/* Image */}
          <div className="aspect-square bg-gray-100 rounded-2xl overflow-hidden">
            {product.imageUrl ? (
              <img src={product.imageUrl} alt={product.name} className="w-full h-full object-cover" />
            ) : (
              <div className="w-full h-full flex items-center justify-center">
                <Package className="h-20 w-20 text-gray-300" />
              </div>
            )}
          </div>

          {/* Info */}
          <div className="flex flex-col">
            {product.category && (
              <Link to={`/products?categoryId=${product.category.id}`}
                className="text-sm text-primary-600 font-medium uppercase tracking-wide hover:underline mb-2">
                {product.category.name}
              </Link>
            )}
            <h1 className="text-2xl md:text-3xl font-bold text-gray-900 mb-2">{product.name}</h1>
            <p className="text-sm text-gray-500 mb-4">SKU: {product.sku}</p>
            <p className="text-3xl font-bold text-primary-700 mb-4">${product.price.toFixed(2)}</p>

            <div className="flex items-center gap-2 mb-6">
              <span className={`px-3 py-1 rounded-full text-sm font-medium ${inStock ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                {inStock ? `In Stock (${product.stockQuantity} available)` : 'Out of Stock'}
              </span>
            </div>

            {inStock && (
              <div className="flex items-center gap-4 mb-6">
                <span className="text-sm font-medium text-gray-700">Quantity:</span>
                <div className="flex items-center border border-gray-300 rounded-lg overflow-hidden">
                  <button onClick={() => setQuantity(q => Math.max(1, q - 1))} disabled={quantity <= 1}
                    className="p-2.5 hover:bg-gray-100 disabled:opacity-40 transition-colors">
                    <Minus className="h-4 w-4" />
                  </button>
                  <span className="px-4 py-2 font-medium min-w-[3rem] text-center">{quantity}</span>
                  <button onClick={() => setQuantity(q => Math.min(maxQty, q + 1))} disabled={quantity >= maxQty}
                    className="p-2.5 hover:bg-gray-100 disabled:opacity-40 transition-colors">
                    <Plus className="h-4 w-4" />
                  </button>
                </div>
              </div>
            )}

            <Button size="lg" onClick={handleAddToCart} disabled={!inStock} isLoading={isAdding} className="mb-4">
              <ShoppingCart className="h-5 w-5" />
              {!isAuthenticated ? 'Login to Add to Cart' : inStock ? 'Add to Cart' : 'Out of Stock'}
            </Button>

            {product.description && (
              <div className="mt-6 pt-6 border-t border-gray-100">
                <h3 className="font-semibold text-gray-900 mb-3">Description</h3>
                <p className="text-gray-600 leading-relaxed whitespace-pre-line">{product.description}</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </Layout>
  )
}
