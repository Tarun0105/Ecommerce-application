import api from './axios'
import type { Product, PageResponse, ProductFilters } from '../types'

export const productsApi = {
  getAll: (filters: ProductFilters = {}) =>
    api.get<PageResponse<Product>>('/products', { params: filters }),
  getById: (id: number) => api.get<Product>(`/products/${id}`),
  create: (data: Partial<Product> & { categoryId?: number | null }) =>
    api.post<Product>('/products', data),
  update: (id: number, data: Partial<Product> & { categoryId?: number | null }) =>
    api.put<Product>(`/products/${id}`, data),
  delete: (id: number) => api.delete(`/products/${id}`),
}
