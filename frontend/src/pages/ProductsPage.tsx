import { useState, useEffect } from 'react'
import { useSearchParams } from 'react-router-dom'
import Layout from '../components/layout/Layout'
import ProductGrid from '../components/products/ProductGrid'
import SearchFilter from '../components/products/SearchFilter'
import Pagination from '../components/ui/Pagination'
import EmptyState from '../components/ui/EmptyState'
import { Package } from 'lucide-react'
import type { Category, Product, ProductFilters, PageResponse } from '../types'
import { categoriesApi } from '../api/categories'
import { productsApi } from '../api/products'

export default function ProductsPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [categories, setCategories] = useState<Category[]>([])
  const [response, setResponse] = useState<PageResponse<Product> | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const filters: ProductFilters = {
    page: Number(searchParams.get('page') ?? 0),
    size: 12,
    search: searchParams.get('search') ?? undefined,
    categoryId: searchParams.get('categoryId') ? Number(searchParams.get('categoryId')) : undefined,
    minPrice: searchParams.get('minPrice') ? Number(searchParams.get('minPrice')) : undefined,
    maxPrice: searchParams.get('maxPrice') ? Number(searchParams.get('maxPrice')) : undefined,
    sort: searchParams.get('sort') ?? 'createdAt_desc',
  }

  const updateFilters = (newFilters: ProductFilters) => {
    const params: Record<string, string> = {}
    if (newFilters.search) params.search = newFilters.search
    if (newFilters.categoryId) params.categoryId = String(newFilters.categoryId)
    if (newFilters.minPrice) params.minPrice = String(newFilters.minPrice)
    if (newFilters.maxPrice) params.maxPrice = String(newFilters.maxPrice)
    if (newFilters.sort) params.sort = newFilters.sort
    if (newFilters.page) params.page = String(newFilters.page)
    setSearchParams(params)
  }

  useEffect(() => { categoriesApi.getAll().then(r => setCategories(r.data)) }, [])

  useEffect(() => {
    setIsLoading(true)
    productsApi.getAll(filters)
      .then(r => setResponse(r.data))
      .finally(() => setIsLoading(false))
  }, [searchParams.toString()])

  return (
    <Layout>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <h1 className="text-2xl font-bold text-gray-900 mb-6">All Products</h1>
        <div className="flex gap-6">
          <aside className="hidden lg:block w-64 flex-shrink-0">
            <SearchFilter categories={categories} filters={filters} onFilterChange={updateFilters} />
          </aside>
          <div className="flex-1 min-w-0">
            {response && !isLoading && (
              <p className="text-sm text-gray-500 mb-4">
                {response.totalElements} product{response.totalElements !== 1 ? 's' : ''} found
              </p>
            )}
            {!isLoading && response?.content.length === 0 ? (
              <EmptyState icon={Package} title="No products found"
                description="Try adjusting your search or filters."
                actionLabel="Clear Filters" onAction={() => updateFilters({ page: 0, size: 12 })} />
            ) : (
              <>
                <ProductGrid products={response?.content ?? []} isLoading={isLoading} />
                {response && (
                  <Pagination currentPage={response.page} totalPages={response.totalPages}
                    onPageChange={p => updateFilters({ ...filters, page: p })} />
                )}
              </>
            )}
          </div>
        </div>
      </div>
    </Layout>
  )
}
