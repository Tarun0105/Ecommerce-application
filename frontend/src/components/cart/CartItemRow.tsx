import { Minus, Plus, Trash2 } from 'lucide-react'
import type { CartItem } from '../../types'

interface Props {
  item: CartItem
  onUpdateQuantity: (itemId: number, quantity: number) => void
  onRemove: (itemId: number) => void
  isLoading?: boolean
}

export default function CartItemRow({ item, onUpdateQuantity, onRemove, isLoading }: Props) {
  return (
    <div className="flex gap-4 py-4 border-b border-gray-100 last:border-0">
      <div className="h-20 w-20 bg-gray-100 rounded-lg overflow-hidden flex-shrink-0">
        {item.productImageUrl ? (
          <img src={item.productImageUrl} alt={item.productName} className="h-full w-full object-cover" />
        ) : (
          <div className="h-full w-full flex items-center justify-center text-gray-300 text-xs">No img</div>
        )}
      </div>
      <div className="flex-1 min-w-0">
        <p className="font-medium text-gray-900 truncate">{item.productName}</p>
        <p className="text-xs text-gray-500 mt-0.5">SKU: {item.productSku}</p>
        <p className="text-primary-600 font-semibold mt-1">${item.unitPrice.toFixed(2)} each</p>
      </div>
      <div className="flex flex-col items-end gap-2">
        <div className="flex items-center border border-gray-200 rounded-lg overflow-hidden">
          <button onClick={() => onUpdateQuantity(item.id, item.quantity - 1)} disabled={isLoading || item.quantity <= 1}
            className="p-1.5 hover:bg-gray-100 disabled:opacity-40 disabled:cursor-not-allowed transition-colors">
            <Minus className="h-3 w-3" />
          </button>
          <span className="px-3 py-1 text-sm font-medium min-w-[2rem] text-center">{item.quantity}</span>
          <button onClick={() => onUpdateQuantity(item.id, item.quantity + 1)} disabled={isLoading}
            className="p-1.5 hover:bg-gray-100 disabled:opacity-40 transition-colors">
            <Plus className="h-3 w-3" />
          </button>
        </div>
        <p className="font-bold text-gray-900">${item.subtotal.toFixed(2)}</p>
        <button onClick={() => onRemove(item.id)} disabled={isLoading}
          className="text-red-500 hover:text-red-700 disabled:opacity-40 transition-colors p-1">
          <Trash2 className="h-4 w-4" />
        </button>
      </div>
    </div>
  )
}
