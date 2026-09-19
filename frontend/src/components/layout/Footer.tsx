import { Link } from 'react-router-dom'
import { Package } from 'lucide-react'

export default function Footer() {
  return (
    <footer className="bg-gray-900 text-gray-300 mt-16">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div>
            <div className="flex items-center gap-2 mb-4">
              <Package className="h-6 w-6 text-primary-400" />
              <span className="text-white font-bold text-lg">ShopVerse</span>
            </div>
            <p className="text-sm text-gray-400">Your one-stop destination for quality products at great prices.</p>
          </div>
          <div>
            <h4 className="text-white font-semibold mb-4">Quick Links</h4>
            <ul className="space-y-2 text-sm">
              <li><Link to="/" className="hover:text-primary-400 transition-colors">Home</Link></li>
              <li><Link to="/products" className="hover:text-primary-400 transition-colors">Products</Link></li>
              <li><Link to="/register" className="hover:text-primary-400 transition-colors">Create Account</Link></li>
            </ul>
          </div>
          <div>
            <h4 className="text-white font-semibold mb-4">Contact</h4>
            <ul className="space-y-2 text-sm text-gray-400">
              <li>support@shopverse.com</li>
              <li>1-800-SHOPVERSE</li>
              <li>Mon–Fri, 9am–6pm EST</li>
            </ul>
          </div>
        </div>
        <div className="border-t border-gray-800 mt-8 pt-8 text-center text-sm text-gray-500">
          © {new Date().getFullYear()} ShopVerse. All rights reserved.
        </div>
      </div>
    </footer>
  )
}
