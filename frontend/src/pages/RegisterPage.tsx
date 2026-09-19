import { useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
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
  firstName: z.string().min(1, 'First name is required').max(100),
  lastName: z.string().min(1, 'Last name is required').max(100),
  email: z.string().email('Invalid email address'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
  confirmPassword: z.string(),
}).refine(d => d.password === d.confirmPassword, { message: "Passwords don't match", path: ['confirmPassword'] })
type FormData = z.infer<typeof schema>

export default function RegisterPage() {
  const { login, isAuthenticated } = useAuth()
  const navigate = useNavigate()

  useEffect(() => { if (isAuthenticated) navigate('/') }, [isAuthenticated])

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
  })

  const onSubmit = async ({ confirmPassword: _, ...data }: FormData) => {
    try {
      const res = await authApi.register(data)
      login(res.data)
      toast.success('Account created! Welcome to ShopVerse!')
      navigate('/')
    } catch (err: any) {
      toast.error(err.response?.data?.message ?? 'Registration failed. Please try again.')
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4 py-8">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <Link to="/" className="inline-flex items-center gap-2 justify-center">
            <Package className="h-8 w-8 text-primary-600" />
            <span className="text-2xl font-bold text-primary-700">ShopVerse</span>
          </Link>
          <h2 className="mt-4 text-2xl font-bold text-gray-900">Create your account</h2>
        </div>
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <Input label="First Name" {...register('firstName')} error={errors.firstName?.message} />
              <Input label="Last Name" {...register('lastName')} error={errors.lastName?.message} />
            </div>
            <Input label="Email address" type="email" {...register('email')} error={errors.email?.message} />
            <Input label="Password" type="password" {...register('password')} error={errors.password?.message}
              helperText="At least 8 characters" />
            <Input label="Confirm Password" type="password" {...register('confirmPassword')} error={errors.confirmPassword?.message} />
            <Button type="submit" isLoading={isSubmitting} fullWidth size="lg" className="mt-2">Create Account</Button>
          </form>
          <p className="mt-6 text-center text-sm text-gray-600">
            Already have an account?{' '}
            <Link to="/login" className="text-primary-600 hover:underline font-medium">Sign in</Link>
          </p>
        </div>
      </div>
    </div>
  )
}
