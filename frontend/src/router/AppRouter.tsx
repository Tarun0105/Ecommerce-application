import { Routes, Route } from 'react-router-dom'
import PrivateRoute from './PrivateRoute'
import AdminRoute from './AdminRoute'

import HomePage from '../pages/HomePage'
import ProductsPage from '../pages/ProductsPage'
import ProductDetailPage from '../pages/ProductDetailPage'
import LoginPage from '../pages/LoginPage'
import RegisterPage from '../pages/RegisterPage'
import CartPage from '../pages/CartPage'
import CheckoutPage from '../pages/CheckoutPage'
import OrderConfirmationPage from '../pages/OrderConfirmationPage'
import OrderHistoryPage from '../pages/OrderHistoryPage'
import OrderDetailPage from '../pages/OrderDetailPage'
import ProfilePage from '../pages/ProfilePage'
import NotFoundPage from '../pages/NotFoundPage'

import AdminDashboardPage from '../pages/admin/AdminDashboardPage'
import AdminProductsPage from '../pages/admin/AdminProductsPage'
import AdminCategoriesPage from '../pages/admin/AdminCategoriesPage'
import AdminUsersPage from '../pages/admin/AdminUsersPage'
import AdminOrdersPage from '../pages/admin/AdminOrdersPage'

export default function AppRouter() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/products" element={<ProductsPage />} />
      <Route path="/products/:id" element={<ProductDetailPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/cart" element={<PrivateRoute><CartPage /></PrivateRoute>} />
      <Route path="/checkout" element={<PrivateRoute><CheckoutPage /></PrivateRoute>} />
      <Route path="/order-confirmation/:id" element={<PrivateRoute><OrderConfirmationPage /></PrivateRoute>} />
      <Route path="/orders" element={<PrivateRoute><OrderHistoryPage /></PrivateRoute>} />
      <Route path="/orders/:id" element={<PrivateRoute><OrderDetailPage /></PrivateRoute>} />
      <Route path="/profile" element={<PrivateRoute><ProfilePage /></PrivateRoute>} />
      <Route path="/admin" element={<AdminRoute><AdminDashboardPage /></AdminRoute>} />
      <Route path="/admin/products" element={<AdminRoute><AdminProductsPage /></AdminRoute>} />
      <Route path="/admin/categories" element={<AdminRoute><AdminCategoriesPage /></AdminRoute>} />
      <Route path="/admin/users" element={<AdminRoute><AdminUsersPage /></AdminRoute>} />
      <Route path="/admin/orders" element={<AdminRoute><AdminOrdersPage /></AdminRoute>} />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  )
}
