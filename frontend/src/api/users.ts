import api from './axios'
import type { User } from '../types'

export const usersApi = {
  getMe: () => api.get<User>('/users/me'),
  updateProfile: (data: { firstName: string; lastName: string }) =>
    api.put<User>('/users/me', data),
  changePassword: (data: { currentPassword: string; newPassword: string }) =>
    api.put('/users/me/password', data),
}
