import api from './axios'
import type { User, Order, PageResponse, DashboardStats, Product } from '../types'

export const adminApi = {
  getDashboardStats: () => api.get<DashboardStats>('/admin/dashboard'),
  getUsers: (params: { page?: number; size?: number; search?: string } = {}) =>
    api.get<PageResponse<User>>('/admin/users', { params }),
  getUserById: (id: number) => api.get<User>(`/admin/users/${id}`),
  toggleUserStatus: (id: number) => api.put<User>(`/admin/users/${id}/toggle-status`),
  getAllOrders: (params: { page?: number; size?: number; status?: string } = {}) =>
    api.get<PageResponse<Order>>('/admin/orders', { params }),
  updateOrderStatus: (id: number, status: string) =>
    api.put<Order>(`/admin/orders/${id}/status`, { status }),
  getAllProducts: (params: { page?: number; size?: number; search?: string } = {}) =>
    api.get<PageResponse<Product>>('/admin/products', { params }),
}
