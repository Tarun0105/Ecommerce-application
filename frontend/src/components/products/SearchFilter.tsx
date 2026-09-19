import { useState, useEffect } from 'react'
import { Search, X, SlidersHorizontal } from 'lucide-react'
import type { Category, ProductFilters } from '../../types'

interface Props {
  categories: Category[]
  filters: ProductFilters
  onFilterChange: (filters: ProductFilters) => void
}

export default function SearchFilter({ categories, filters, onFilterChange }: Props) {
  const [localSearch, setLocalSearch] = useState(filters.search ?? '')

  useEffect(() => {
    const timer = setTimeout(() => {
      if (localSearch !== filters.search) {
        onFilterChange({ ...filters, search: localSearch || undefined, page: 0 })
      }
    }, 400)
    return () => clearTimeout(timer)
  }, [localSearch])

  const clear = () => {
    setLocalSearch('')
    onFilterChange({ page: 0, size: 12 })
  }

  const hasFilters = filters.categoryId || filters.minPrice || filters.maxPrice || filters.search

  return (
    <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-5 space-y-5">
      <div className="flex items-center gap-2 text-gray-700 font-semibold">
        <SlidersHorizontal className="h-4 w-4" />
        Filters
        {hasFilters && (
          <button onClick={clear} className="ml-auto text-xs text-primary-600 hover:underline flex items-center gap-1">
            <X className="h-3 w-3" /> Clear all
          </button>
        )}
      </div>

      <div className="relative">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
        <input
          value={localSearch}
          onChange={e => setLocalSearch(e.target.value)}
          placeholder="Search products..."
          className="w-full pl-9 pr-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
        />
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">Category</label>
        <select
          value={filters.categoryId ?? ''}
          onChange={e => onFilterChange({ ...filters, categoryId: e.target.value ? Number(e.target.value) : undefined, page: 0 })}
          className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
        >
          <option value="">All Categories</option>
          {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">Price Range</label>
        <div className="flex gap-2">
          <input type="number" min="0" placeholder="Min"
            value={filters.minPrice ?? ''}
            onChange={e => onFilterChange({ ...filters, minPrice: e.target.value ? Number(e.target.value) : undefined, page: 0 })}
            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
          />
          <input type="number" min="0" placeholder="Max"
            value={filters.maxPrice ?? ''}
            onChange={e => onFilterChange({ ...filters, maxPrice: e.target.value ? Number(e.target.value) : undefined, page: 0 })}
            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
          />
        </div>
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">Sort By</label>
        <select
          value={filters.sort ?? 'createdAt_desc'}
          onChange={e => onFilterChange({ ...filters, sort: e.target.value, page: 0 })}
          className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
        >
          <option value="createdAt_desc">Newest First</option>
          <option value="price_asc">Price: Low to High</option>
          <option value="price_desc">Price: High to Low</option>
          <option value="name_asc">Name: A-Z</option>
          <option value="name_desc">Name: Z-A</option>
        </select>
      </div>
    </div>
  )
}
