import api from './axios'
import type { Category } from '../types'

export const categoriesApi = {
  getAll: () => api.get<Category[]>('/categories'),
  getById: (id: number) => api.get<Category>(`/categories/${id}`),
  create: (data: { name: string; description?: string }) =>
    api.post<Category>('/categories', data),
  update: (id: number, data: { name: string; description?: string }) =>
    api.put<Category>(`/categories/${id}`, data),
  delete: (id: number) => api.delete(`/categories/${id}`),
}
