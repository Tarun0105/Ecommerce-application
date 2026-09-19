import api from './axios'
import type { AuthResponse } from '../types'

export const authApi = {
  register: (data: { firstName: string; lastName: string; email: string; password: string }) =>
    api.post<AuthResponse>('/auth/register', data),
  login: (data: { email: string; password: string }) =>
    api.post<AuthResponse>('/auth/login', data),
}
