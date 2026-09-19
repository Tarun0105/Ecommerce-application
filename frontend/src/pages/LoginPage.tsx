import { useEffect } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import toast from 'react-hot-toast'
import { Package } from 'lucide-react'
import { authApi } from '../api/auth'
import { useAuth } from '../context/AuthContext'
import Input from '../components/ui/Input'
import Button from '../components/ui/Button'

const schema = z.object({
  email: z.string().email('Invalid email'),
  password: z.string().min(1, 'Password is required'),
})
type FormData = z.infer<typeof schema>

export default function LoginPage() {
  const { login, isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const from = (location.state as any)?.from?.pathname ?? '/'

  useEffect(() => { if (isAuthenticated) navigate(from, { replace: true }) }, [isAuthenticated])

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
  })

  const onSubmit = async (data: FormData) => {
    try {
      const res = await authApi.login(data)
      login(res.data)
      toast.success(`Welcome back, ${res.data.firstName}!`)
      navigate(from, { replace: true })
    } catch (err: any) {
      toast.error(err.response?.data?.message ?? 'Login failed. Check your credentials.')
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <Link to="/" className="inline-flex items-center gap-2 justify-center">
            <Package className="h-8 w-8 text-primary-600" />
            <span className="text-2xl font-bold text-primary-700">ShopVerse</span>
          </Link>
          <h2 className="mt-4 text-2xl font-bold text-gray-900">Sign in to your account</h2>
        </div>
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
            <Input label="Email address" type="email" autoComplete="email"
              {...register('email')} error={errors.email?.message} />
            <Input label="Password" type="password" autoComplete="current-password"
              {...register('password')} error={errors.password?.message} />
            <Button type="submit" isLoading={isSubmitting} fullWidth size="lg">Sign In</Button>
          </form>
          <p className="mt-6 text-center text-sm text-gray-600">
            Don't have an account?{' '}
            <Link to="/register" className="text-primary-600 hover:underline font-medium">Create one</Link>
          </p>
        </div>
      </div>
    </div>
  )
}
