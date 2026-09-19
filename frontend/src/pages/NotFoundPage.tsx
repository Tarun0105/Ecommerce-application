import { Link } from 'react-router-dom'
import Layout from '../components/layout/Layout'
import Button from '../components/ui/Button'

export default function NotFoundPage() {
  return (
    <Layout>
      <div className="flex flex-col items-center justify-center py-24 text-center px-4">
        <h1 className="text-8xl font-bold text-primary-200 mb-4">404</h1>
        <h2 className="text-2xl font-bold text-gray-900 mb-3">Page Not Found</h2>
        <p className="text-gray-500 mb-8">The page you're looking for doesn't exist or has been moved.</p>
        <Link to="/"><Button>Return to Home</Button></Link>
      </div>
    </Layout>
  )
}
