import { useState, useEffect, useCallback } from 'react'
import { Plus, Pencil, Trash2, Search } from 'lucide-react'
import toast from 'react-hot-toast'
import { useForm, Controller } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import AdminLayout from '../../components/layout/AdminLayout'
import Modal from '../../components/ui/Modal'
import ConfirmDialog from '../../components/ui/ConfirmDialog'
import Button from '../../components/ui/Button'
import Input from '../../components/ui/Input'
import Pagination from '../../components/ui/Pagination'
import LoadingSpinner from '../../components/ui/LoadingSpinner'
import type { Product, Category, PageResponse } from '../../types'
import { adminApi } from '../../api/admin'
import { categoriesApi } from '../../api/categories'
import { productsApi } from '../../api/products'

const schema = z.object({
  name: z.string().min(1, 'Required'),
  description: z.string().optional(),
  price: z.coerce.number().min(0.01, 'Must be > 0'),
  categoryId: z.coerce.number().optional().nullable(),
  stockQuantity: z.coerce.number().min(0, 'Cannot be negative').int(),
  sku: z.string().min(1, 'Required'),
  imageUrl: z.string().optional(),
  active: z.boolean(),
})
type FormData = z.infer<typeof schema>

export default function AdminProductsPage() {
  const [response, setResponse] = useState<PageResponse<Product> | null>(null)
  const [categories, setCategories] = useState<Category[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<Product | null>(null)
  const [deleteTarget, setDeleteTarget] = useState<Product | null>(null)
  const [isDeleting, setIsDeleting] = useState(false)

  const { register, handleSubmit, reset, control, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { active: true, stockQuantity: 0 },
  })

  const load = useCallback(() => {
    setIsLoading(true)
    adminApi.getAllProducts({ page, size: 20, search }).then(r => setResponse(r.data)).finally(() => setIsLoading(false))
  }, [page, search])

  useEffect(load, [load])
  useEffect(() => { categoriesApi.getAll().then(r => setCategories(r.data)) }, [])

  const openAdd = () => {
    setEditing(null)
    reset({ name: '', description: '', price: 0, categoryId: null, stockQuantity: 0, sku: '', imageUrl: '', active: true })
    setModalOpen(true)
  }
  const openEdit = (p: Product) => {
    setEditing(p)
    reset({ name: p.name, description: p.description ?? '', price: p.price, categoryId: p.category?.id ?? null, stockQuantity: p.stockQuantity, sku: p.sku, imageUrl: p.imageUrl ?? '', active: p.active })
    setModalOpen(true)
  }

  const onSubmit = async (data: FormData) => {
    try {
      if (editing) await productsApi.update(editing.id, data)
      else await productsApi.create(data)
      toast.success(editing ? 'Product updated!' : 'Product created!')
      setModalOpen(false)
      load()
    } catch (err: any) {
      toast.error(err.response?.data?.message ?? 'Operation failed')
    }
  }

  const handleDelete = async () => {
    if (!deleteTarget) return
    try {
      setIsDeleting(true)
      await productsApi.delete(deleteTarget.id)
      toast.success('Product deactivated')
      setDeleteTarget(null)
      load()
    } catch (err: any) {
      toast.error(err.response?.data?.message ?? 'Failed')
    } finally {
      setIsDeleting(false)
    }
  }

  return (
    <AdminLayout>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Products</h1>
        <Button onClick={openAdd}><Plus className="h-4 w-4" /> Add Product</Button>
      </div>

      <div className="mb-4 relative">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
        <input value={search} onChange={e => { setSearch(e.target.value); setPage(0) }} placeholder="Search products..."
          className="w-full sm:w-80 pl-9 pr-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary-400" />
      </div>

      {isLoading ? (
        <div className="flex justify-center py-12"><LoadingSpinner size="lg" /></div>
      ) : (
        <div className="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 border-b border-gray-100">
                <tr>
                  {['Name', 'SKU', 'Category', 'Price', 'Stock', 'Status', ''].map(h => (
                    <th key={h} className="text-left px-4 py-3 text-xs font-medium text-gray-500 uppercase whitespace-nowrap">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {response?.content.map(p => (
                  <tr key={p.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3">
                      <div className="flex items-center gap-3">
                        {p.imageUrl && <img src={p.imageUrl} alt="" className="h-10 w-10 rounded-lg object-cover flex-shrink-0" />}
                        <span className="font-medium text-gray-900 max-w-[180px] truncate">{p.name}</span>
                      </div>
                    </td>
                    <td className="px-4 py-3 text-gray-500 font-mono text-xs">{p.sku}</td>
                    <td className="px-4 py-3 text-gray-500">{p.category?.name ?? '—'}</td>
                    <td className="px-4 py-3 font-semibold text-gray-900">${p.price.toFixed(2)}</td>
                    <td className="px-4 py-3">
                      <span className={`font-medium ${p.stockQuantity === 0 ? 'text-red-600' : p.stockQuantity < 10 ? 'text-yellow-600' : 'text-green-600'}`}>
                        {p.stockQuantity}
                      </span>
                    </td>
                    <td className="px-4 py-3">
                      <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${p.active ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                        {p.active ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex gap-1 justify-end">
                        <button onClick={() => openEdit(p)} className="p-1.5 text-gray-400 hover:text-primary-600 hover:bg-primary-50 rounded transition-colors">
                          <Pencil className="h-4 w-4" />
                        </button>
                        <button onClick={() => setDeleteTarget(p)} className="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded transition-colors">
                          <Trash2 className="h-4 w-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            {!response?.content.length && <p className="text-center text-gray-500 py-12">No products found.</p>}
          </div>
        </div>
      )}
      {response && <Pagination currentPage={response.page} totalPages={response.totalPages} onPageChange={p => { setPage(p) }} />}

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Edit Product' : 'Add Product'} size="lg">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <Input label="Name" {...register('name')} error={errors.name?.message} />
            <Input label="SKU" {...register('sku')} error={errors.sku?.message} />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
            <textarea {...register('description')} rows={3}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400 resize-none" />
          </div>
          <div className="grid grid-cols-3 gap-4">
            <Input label="Price ($)" type="number" step="0.01" {...register('price')} error={errors.price?.message} />
            <Input label="Stock Qty" type="number" {...register('stockQuantity')} error={errors.stockQuantity?.message} />
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Category</label>
              <select {...register('categoryId')}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400">
                <option value="">None</option>
                {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
              </select>
            </div>
          </div>
          <Input label="Image URL" {...register('imageUrl')} />
          <label className="flex items-center gap-2 cursor-pointer">
            <Controller name="active" control={control} render={({ field }) => (
              <input type="checkbox" className="h-4 w-4 text-primary-600 rounded" checked={field.value} onChange={field.onChange} />
            )} />
            <span className="text-sm font-medium text-gray-700">Active (visible to customers)</span>
          </label>
          <div className="flex gap-3 justify-end pt-2">
            <Button variant="outline" type="button" onClick={() => setModalOpen(false)}>Cancel</Button>
            <Button type="submit" isLoading={isSubmitting}>{editing ? 'Update' : 'Create'}</Button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog isOpen={!!deleteTarget} onClose={() => setDeleteTarget(null)} onConfirm={handleDelete}
        isLoading={isDeleting} title="Deactivate Product"
        message={`Deactivate "${deleteTarget?.name}"? It will be hidden from customers.`} confirmText="Deactivate" />
    </AdminLayout>
  )
}
