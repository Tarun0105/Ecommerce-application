interface Props { size?: 'sm' | 'md' | 'lg'; fullPage?: boolean }

export default function LoadingSpinner({ size = 'md', fullPage = false }: Props) {
  const sizes = { sm: 'h-4 w-4', md: 'h-8 w-8', lg: 'h-12 w-12' }
  const spinner = (
    <div className={`${sizes[size]} animate-spin rounded-full border-2 border-gray-200 border-t-primary-600`} />
  )
  if (fullPage) {
    return (
      <div className="fixed inset-0 flex items-center justify-center bg-white bg-opacity-75 z-50">
        {spinner}
      </div>
    )
  }
  return spinner
}
