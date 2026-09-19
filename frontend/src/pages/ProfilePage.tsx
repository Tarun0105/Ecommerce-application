import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import toast from 'react-hot-toast'
import { User, KeyRound } from 'lucide-react'
import Layout from '../components/layout/Layout'
import Input from '../components/ui/Input'
import Button from '../components/ui/Button'
import { useAuth } from '../context/AuthContext'
import { usersApi } from '../api/users'

const profileSchema = z.object({
  firstName: z.string().min(1, 'First name is required').max(100),
  lastName: z.string().min(1, 'Last name is required').max(100),
})
type ProfileData = z.infer<typeof profileSchema>

const passwordSchema = z.object({
  currentPassword: z.string().min(1, 'Current password is required'),
  newPassword: z.string().min(8, 'New password must be at least 8 characters'),
  confirmPassword: z.string(),
}).refine(d => d.newPassword === d.confirmPassword, { message: "Passwords don't match", path: ['confirmPassword'] })
type PasswordData = z.infer<typeof passwordSchema>

export default function ProfilePage() {
  const { user, setUser } = useAuth()
  const [isSavingProfile, setIsSavingProfile] = useState(false)
  const [isSavingPassword, setIsSavingPassword] = useState(false)

  const profileForm = useForm<ProfileData>({
    resolver: zodResolver(profileSchema),
    defaultValues: { firstName: user?.firstName ?? '', lastName: user?.lastName ?? '' },
  })

  const passwordForm = useForm<PasswordData>({
    resolver: zodResolver(passwordSchema),
  })

  const onSaveProfile = async (data: ProfileData) => {
    try {
      setIsSavingProfile(true)
      const res = await usersApi.updateProfile(data)
      setUser(res.data)
      toast.success('Profile updated!')
    } catch (err: any) {
      toast.error(err.response?.data?.message ?? 'Failed to update profile')
    } finally {
      setIsSavingProfile(false)
    }
  }

  const onChangePassword = async ({ currentPassword, newPassword }: PasswordData) => {
    try {
      setIsSavingPassword(true)
      await usersApi.changePassword({ currentPassword, newPassword })
      toast.success('Password changed!')
      passwordForm.reset()
    } catch (err: any) {
      toast.error(err.response?.data?.message ?? 'Failed to change password')
    } finally {
      setIsSavingPassword(false)
    }
  }

  return (
    <Layout>
      <div className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
        <h1 className="text-2xl font-bold text-gray-900">My Profile</h1>

        {/* Profile Info */}
        <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
            <User className="h-5 w-5" /> Personal Information
          </h2>
          <form onSubmit={profileForm.handleSubmit(onSaveProfile)} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <Input label="First Name" {...profileForm.register('firstName')}
                error={profileForm.formState.errors.firstName?.message} />
              <Input label="Last Name" {...profileForm.register('lastName')}
                error={profileForm.formState.errors.lastName?.message} />
            </div>
            <Input label="Email Address" value={user?.email ?? ''} disabled
              helperText="Email cannot be changed" />
            <div className="flex justify-end">
              <Button type="submit" isLoading={isSavingProfile}>Save Changes</Button>
            </div>
          </form>
        </div>

        {/* Change Password */}
        <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
            <KeyRound className="h-5 w-5" /> Change Password
          </h2>
          <form onSubmit={passwordForm.handleSubmit(onChangePassword)} className="space-y-4">
            <Input label="Current Password" type="password"
              {...passwordForm.register('currentPassword')}
              error={passwordForm.formState.errors.currentPassword?.message} />
            <Input label="New Password" type="password"
              {...passwordForm.register('newPassword')}
              error={passwordForm.formState.errors.newPassword?.message}
              helperText="At least 8 characters" />
            <Input label="Confirm New Password" type="password"
              {...passwordForm.register('confirmPassword')}
              error={passwordForm.formState.errors.confirmPassword?.message} />
            <div className="flex justify-end">
              <Button type="submit" isLoading={isSavingPassword}>Update Password</Button>
            </div>
          </form>
        </div>

        {/* Account Info */}
        <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-3">Account Details</h2>
          <dl className="space-y-2 text-sm">
            <div className="flex justify-between">
              <dt className="text-gray-500">Role</dt>
              <dd className="font-medium text-gray-900">
                {user?.role === 'ROLE_ADMIN' ? 'Administrator' : 'Customer'}
              </dd>
            </div>
            <div className="flex justify-between">
              <dt className="text-gray-500">Member Since</dt>
              <dd className="font-medium text-gray-900">
                {user?.createdAt ? new Date(user.createdAt).toLocaleDateString() : '-'}
              </dd>
            </div>
          </dl>
        </div>
      </div>
    </Layout>
  )
}
