export interface User {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: 'ROLE_ADMIN' | 'ROLE_USER';
  enabled: boolean;
  createdAt: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
}

export interface Category {
  id: number;
  name: string;
  description: string | null;
  createdAt: string;
}

export interface Product {
  id: number;
  name: string;
  description: string | null;
  price: number;
  category: Category | null;
  stockQuantity: number;
  sku: string;
  imageUrl: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CartItem {
  id: number;
  productId: number;
  productName: string;
  productSku: string;
  productImageUrl: string | null;
  unitPrice: number;
  quantity: number;
  subtotal: number;
}

export interface Cart {
  id: number;
  items: CartItem[];
  totalItems: number;
  totalAmount: number;
}

export interface ShippingAddress {
  firstName: string;
  lastName: string;
  street: string;
  city: string;
  state: string;
  zipCode: string;
  country: string;
}

export interface OrderItem {
  id: number;
  productId: number | null;
  productName: string;
  productSku: string;
  unitPrice: number;
  quantity: number;
  subtotal: number;
}

export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'PROCESSING' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';

export interface Order {
  id: number;
  userId: number;
  userEmail: string;
  items: OrderItem[];
  shippingAddress: ShippingAddress;
  status: OrderStatus;
  totalAmount: number;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface DashboardStats {
  totalUsers: number;
  totalProducts: number;
  totalOrders: number;
  pendingOrders: number;
  totalRevenue: number;
}

export interface ProductFilters {
  search?: string;
  categoryId?: number;
  minPrice?: number;
  maxPrice?: number;
  sort?: string;
  page?: number;
  size?: number;
}

export interface ApiError {
  message: string;
  status: number;
  error: string;
}
