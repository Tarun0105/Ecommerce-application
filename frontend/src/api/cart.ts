import api from './axios'
import type { Cart } from '../types'

export const cartApi = {
  getCart: () => api.get<Cart>('/cart'),
  addItem: (data: { productId: number; quantity: number }) =>
    api.post<Cart>('/cart/items', data),
  updateItem: (itemId: number, data: { quantity: number }) =>
    api.put<Cart>(`/cart/items/${itemId}`, data),
  removeItem: (itemId: number) => api.delete<Cart>(`/cart/items/${itemId}`),
  clearCart: () => api.delete('/cart'),
}
