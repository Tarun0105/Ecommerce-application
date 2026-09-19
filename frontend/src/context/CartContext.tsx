import React, { createContext, useContext, useState, useEffect, useCallback } from 'react'
import type { Cart } from '../types'
import { cartApi } from '../api/cart'
import { useAuth } from './AuthContext'

interface CartContextType {
  cart: Cart | null
  isLoading: boolean
  cartItemCount: number
  fetchCart: () => Promise<void>
  addItem: (productId: number, quantity: number) => Promise<void>
  updateItem: (itemId: number, quantity: number) => Promise<void>
  removeItem: (itemId: number) => Promise<void>
  clearCart: () => Promise<void>
}

const CartContext = createContext<CartContextType | undefined>(undefined)

export function CartProvider({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuth()
  const [cart, setCart] = useState<Cart | null>(null)
  const [isLoading, setIsLoading] = useState(false)

  const fetchCart = useCallback(async () => {
    if (!isAuthenticated) { setCart(null); return }
    try {
      setIsLoading(true)
      const { data } = await cartApi.getCart()
      setCart(data)
    } catch {
      setCart(null)
    } finally {
      setIsLoading(false)
    }
  }, [isAuthenticated])

  useEffect(() => { fetchCart() }, [fetchCart])

  const addItem = async (productId: number, quantity: number) => {
    const { data } = await cartApi.addItem({ productId, quantity })
    setCart(data)
  }

  const updateItem = async (itemId: number, quantity: number) => {
    const { data } = await cartApi.updateItem(itemId, { quantity })
    setCart(data)
  }

  const removeItem = async (itemId: number) => {
    const { data } = await cartApi.removeItem(itemId)
    setCart(data)
  }

  const clearCart = async () => {
    await cartApi.clearCart()
    setCart(null)
  }

  return (
    <CartContext.Provider value={{
      cart, isLoading,
      cartItemCount: cart?.totalItems ?? 0,
      fetchCart, addItem, updateItem, removeItem, clearCart,
    }}>
      {children}
    </CartContext.Provider>
  )
}

export function useCart() {
  const context = useContext(CartContext)
  if (!context) throw new Error('useCart must be used within CartProvider')
  return context
}
