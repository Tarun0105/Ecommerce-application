import Button from './Button'
import { ChevronLeft, ChevronRight } from 'lucide-react'

interface Props { currentPage: number; totalPages: number; onPageChange: (page: number) => void }

export default function Pagination({ currentPage, totalPages, onPageChange }: Props) {
  if (totalPages <= 1) return null
  const pages = Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
    const start = Math.max(0, Math.min(currentPage - 2, totalPages - 5))
    return start + i
  })
  return (
    <div className="flex items-center justify-center gap-2 mt-6">
      <Button variant="outline" size="sm" onClick={() => onPageChange(currentPage - 1)} disabled={currentPage === 0}>
        <ChevronLeft className="h-4 w-4" />
      </Button>
      {pages.map(p => (
        <button key={p} onClick={() => onPageChange(p)}
          className={`h-8 w-8 rounded-lg text-sm font-medium transition-colors ${
            p === currentPage ? 'bg-primary-600 text-white' : 'hover:bg-gray-100 text-gray-700'
          }`}>
          {p + 1}
        </button>
      ))}
      <Button variant="outline" size="sm" onClick={() => onPageChange(currentPage + 1)} disabled={currentPage >= totalPages - 1}>
        <ChevronRight className="h-4 w-4" />
      </Button>
    </div>
  )
}
