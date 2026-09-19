import { useState, useEffect } from 'react'
import { Plus, Pencil, Trash2 } from 'lucide-react'
import toast from 'react-hot-toast'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import AdminLayout from '../../components/layout/AdminLayout'
import Modal from '../../components/ui/Modal'
import ConfirmDialog from '../../components/ui/ConfirmDialog'
import Button from '../../components/ui/Button'
import Input from '../../components/ui/Input'
import LoadingSpinner from '../../components/ui/LoadingSpinner'
import type { Category } from '../../types'
import { categoriesApi } from '../../api/categories'

const schema = z.object({
  name: z.string().min(1, 'Name is required').max(100),
  description: z.string().optional(),
})
type FormData = z.infer<typeof schema>

export default function AdminCategoriesPage() {
  const [categories, setCategories] = useState<Category[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<Category | null>(null)
  const [deleteTarget, setDeleteTarget] = useState<Category | null>(null)
  const [isDeleting, setIsDeleting] = useState(false)

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
  })

  const load = () => {
    categoriesApi.getAll().then(r => setCategories(r.data)).finally(() => setIsLoading(false))
  }
  useEffect(load, [])

  const openAdd = () => { setEditing(null); reset({ name: '', description: '' }); setModalOpen(true) }
  const openEdit = (cat: Category) => { setEditing(cat); reset({ name: cat.name, description: cat.description ?? '' }); setModalOpen(true) }

  const onSubmit = async (data: FormData) => {
    try {
      if (editing) await categoriesApi.update(editing.id, data)
      else await categoriesApi.create(data)
      toast.success(editing ? 'Category updated!' : 'Category created!')
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
      await categoriesApi.delete(deleteTarget.id)
      toast.success('Category deleted')
      setDeleteTarget(null)
      load()
    } catch (err: any) {
      toast.error(err.response?.data?.message ?? 'Failed to delete category')
    } finally {
      setIsDeleting(false)
    }
  }

  return (
    <AdminLayout>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Categories</h1>
        <Button onClick={openAdd}><Plus className="h-4 w-4" /> Add Category</Button>
      </div>

      {isLoading ? (
        <div className="flex justify-center py-12"><LoadingSpinner size="lg" /></div>
      ) : (
        <div className="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 border-b border-gray-100">
              <tr>
                <th className="text-left px-6 py-3 text-xs font-medium text-gray-500 uppercase">Name</th>
                <th className="text-left px-6 py-3 text-xs font-medium text-gray-500 uppercase">Description</th>
                <th className="text-left px-6 py-3 text-xs font-medium text-gray-500 uppercase">Created</th>
                <th className="px-6 py-3"></th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {categories.map(cat => (
                <tr key={cat.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-6 py-4 font-medium text-gray-900">{cat.name}</td>
                  <td className="px-6 py-4 text-gray-500 max-w-xs truncate">{cat.description ?? '—'}</td>
                  <td className="px-6 py-4 text-gray-500">{new Date(cat.createdAt).toLocaleDateString()}</td>
                  <td className="px-6 py-4">
                    <div className="flex gap-2 justify-end">
                      <button onClick={() => openEdit(cat)} className="p-1.5 text-gray-500 hover:text-primary-600 hover:bg-primary-50 rounded-lg transition-colors">
                        <Pencil className="h-4 w-4" />
                      </button>
                      <button onClick={() => setDeleteTarget(cat)} className="p-1.5 text-gray-500 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors">
                        <Trash2 className="h-4 w-4" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {categories.length === 0 && <p className="text-center text-gray-500 py-12">No categories yet.</p>}
        </div>
      )}

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Edit Category' : 'Add Category'}>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Input label="Name" {...register('name')} error={errors.name?.message} />
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
            <textarea {...register('description')} rows={3}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400 resize-none" />
          </div>
          <div className="flex gap-3 justify-end pt-2">
            <Button variant="outline" type="button" onClick={() => setModalOpen(false)}>Cancel</Button>
            <Button type="submit" isLoading={isSubmitting}>{editing ? 'Update' : 'Create'}</Button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog isOpen={!!deleteTarget} onClose={() => setDeleteTarget(null)}
        onConfirm={handleDelete} isLoading={isDeleting}
        title="Delete Category" message={`Delete "${deleteTarget?.name}"? This cannot be undone.`}
        confirmText="Delete" />
    </AdminLayout>
  )
}
