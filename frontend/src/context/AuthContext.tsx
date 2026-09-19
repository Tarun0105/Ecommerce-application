import React, { createContext, useContext, useState, useEffect, useCallback } from 'react'
import type { User, AuthResponse } from '../types'

interface AuthContextType {
  user: User | null
  token: string | null
  isLoading: boolean
  isAuthenticated: boolean
  isAdmin: boolean
  login: (response: AuthResponse) => void
  logout: () => void
  setUser: (user: User) => void
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUserState] = useState<User | null>(null)
  const [token, setToken] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const savedToken = localStorage.getItem('shopverse_token')
    const savedUser = localStorage.getItem('shopverse_user')
    if (savedToken && savedUser) {
      try {
        setToken(savedToken)
        setUserState(JSON.parse(savedUser))
      } catch {
        localStorage.removeItem('shopverse_token')
        localStorage.removeItem('shopverse_user')
      }
    }
    setIsLoading(false)
  }, [])

  const login = useCallback((response: AuthResponse) => {
    const userData: User = {
      id: 0,
      firstName: response.firstName,
      lastName: response.lastName,
      email: response.email,
      role: response.role as 'ROLE_ADMIN' | 'ROLE_USER',
      enabled: true,
      createdAt: new Date().toISOString(),
    }
    localStorage.setItem('shopverse_token', response.token)
    localStorage.setItem('shopverse_user', JSON.stringify(userData))
    setToken(response.token)
    setUserState(userData)
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem('shopverse_token')
    localStorage.removeItem('shopverse_user')
    setToken(null)
    setUserState(null)
  }, [])

  const setUser = useCallback((updatedUser: User) => {
    localStorage.setItem('shopverse_user', JSON.stringify(updatedUser))
    setUserState(updatedUser)
  }, [])

  return (
    <AuthContext.Provider value={{
      user, token, isLoading,
      isAuthenticated: !!token && !!user,
      isAdmin: user?.role === 'ROLE_ADMIN',
      login, logout, setUser,
    }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used within AuthProvider')
  return context
}
