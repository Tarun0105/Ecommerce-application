import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('shopverse_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const isAuthEndpoint = error.config?.url?.includes('/auth/')
      if (!isAuthEndpoint) {
        localStorage.removeItem('shopverse_token')
        localStorage.removeItem('shopverse_user')
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default api
