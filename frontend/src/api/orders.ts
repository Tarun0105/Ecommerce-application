import api from './axios'
import type { Order, PageResponse, ShippingAddress } from '../types'

export const ordersApi = {
  checkout: (data: { shippingAddress: ShippingAddress; notes?: string }) =>
    api.post<Order>('/orders/checkout', data),
  getMyOrders: (params: { page?: number; size?: number } = {}) =>
    api.get<PageResponse<Order>>('/orders', { params }),
  getOrderById: (id: number) => api.get<Order>(`/orders/${id}`),
  cancelOrder: (id: number) => api.put<Order>(`/orders/${id}/cancel`),
}
