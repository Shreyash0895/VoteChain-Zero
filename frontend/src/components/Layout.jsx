import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

/**
 * Shared page shell. The header reads like a ledger's title plate: brand
 * mark on the left, a single ruled line beneath the whole header (not a
 * boxed nav bar), and the voter's status on the right.
 */
export default function Layout({ children }) {
  const { voter, isAuthenticated, logout } = useAuth()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/login')
  }

  return (
    <div className="min-h-screen flex flex-col">
      <header className="ledger-rule border-b">
        <div className="max-w-4xl mx-auto px-6 py-5 flex items-center justify-between">
          <Link to="/" className="flex items-baseline gap-2">
            <span className="font-serif text-xl font-semibold tracking-tight">VoteChain</span>
            <span className="font-mono text-xs text-brass">ZERO</span>
          </Link>

          {isAuthenticated ? (
            <div className="flex items-center gap-4 text-sm">
              <span className="text-paper-dim">
                {voter?.fullName} <span className="text-paper-dim/60">· {voter?.role}</span>
              </span>
              <button onClick={handleLogout} className="text-paper-dim hover:text-paper transition-colors">
                Log out
              </button>
            </div>
          ) : (
            <div className="flex items-center gap-4 text-sm">
              <Link to="/login" className="text-paper-dim hover:text-paper transition-colors">
                Log in
              </Link>
              <Link to="/register" className="btn-primary text-sm py-2 px-4">
                Register
              </Link>
            </div>
          )}
        </div>
      </header>

      <main className="flex-1 max-w-4xl mx-auto w-full px-6 py-12">{children}</main>

      <footer className="border-t border-rule">
        <div className="max-w-4xl mx-auto px-6 py-6 text-xs text-paper-dim/60 font-mono">
          Every vote is a signed entry on an append-only chain.
        </div>
      </footer>
    </div>
  )
}