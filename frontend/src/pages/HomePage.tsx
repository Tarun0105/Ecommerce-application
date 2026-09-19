import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { ArrowRight, ShieldCheck, Truck, RefreshCw } from 'lucide-react'
import Layout from '../components/layout/Layout'
import ProductGrid from '../components/products/ProductGrid'
import type { Category, Product } from '../types'
import { categoriesApi } from '../api/categories'
import { productsApi } from '../api/products'

export default function HomePage() {
  const [categories, setCategories] = useState<Category[]>([])
  const [featuredProducts, setFeaturedProducts] = useState<Product[]>([])
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    Promise.all([categoriesApi.getAll(), productsApi.getAll({ size: 8, sort: 'createdAt_desc' })])
      .then(([cats, prods]) => {
        setCategories(cats.data)
        setFeaturedProducts(prods.data.content)
      })
      .finally(() => setIsLoading(false))
  }, [])

  return (
    <Layout>
      {/* Hero */}
      <section className="bg-gradient-to-br from-primary-700 via-primary-600 to-primary-500 text-white py-20">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h1 className="text-4xl md:text-5xl font-bold mb-4 leading-tight">
            Discover Amazing Products<br />at Unbeatable Prices
          </h1>
          <p className="text-primary-100 text-lg mb-8 max-w-2xl mx-auto">
            Thousands of products across hundreds of categories. Fast shipping, easy returns, and world-class support.
          </p>
          <Link to="/products"
            className="inline-flex items-center gap-2 bg-white text-primary-700 hover:bg-primary-50 font-semibold px-8 py-3 rounded-xl transition-colors text-lg">
            Shop Now <ArrowRight className="h-5 w-5" />
          </Link>
        </div>
      </section>

      {/* Features */}
      <section className="py-10 bg-white border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 text-center">
            {[
              { icon: Truck, title: 'Free Shipping', desc: 'On all orders over $50' },
              { icon: RefreshCw, title: 'Easy Returns', desc: '30-day return policy' },
              { icon: ShieldCheck, title: 'Secure Payments', desc: 'Your data is protected' },
            ].map(({ icon: Icon, title, desc }) => (
              <div key={title} className="flex flex-col items-center gap-2">
                <Icon className="h-8 w-8 text-primary-600" />
                <h3 className="font-semibold text-gray-900">{title}</h3>
                <p className="text-sm text-gray-500">{desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Categories */}
      {categories.length > 0 && (
        <section className="py-12 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <h2 className="text-2xl font-bold text-gray-900 mb-6">Shop by Category</h2>
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-6 gap-3">
            {categories.map(cat => (
              <Link key={cat.id} to={`/products?categoryId=${cat.id}`}
                className="bg-white border border-gray-100 rounded-xl p-4 text-center hover:border-primary-300 hover:shadow-md transition-all group">
                <p className="font-medium text-gray-800 group-hover:text-primary-600 text-sm">{cat.name}</p>
              </Link>
            ))}
          </div>
        </section>
      )}

      {/* Featured Products */}
      <section className="py-8 pb-16 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-2xl font-bold text-gray-900">New Arrivals</h2>
          <Link to="/products" className="text-primary-600 hover:underline text-sm font-medium flex items-center gap-1">
            View all <ArrowRight className="h-4 w-4" />
          </Link>
        </div>
        <ProductGrid products={featuredProducts} isLoading={isLoading} />
      </section>
    </Layout>
  )
}
