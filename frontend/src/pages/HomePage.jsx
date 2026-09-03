import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function HomePage() {
  const { voter, isAuthenticated } = useAuth()

  if (!isAuthenticated) {
    return (
      <div>
        <h1 className="text-4xl font-semibold mb-4">VoteChain Zero</h1>
        <p className="text-paper-dim max-w-md">
          A blockchain-backed voting system where every vote is a signed, tamper-evident
          entry on an append-only ledger. Register or log in to continue.
        </p>
      </div>
    )
  }

  return (
    <div>
      <h1 className="text-4xl font-semibold mb-2">Welcome, {voter?.fullName}</h1>
      <p className="text-paper-dim mb-8 font-mono text-sm">Role: {voter?.role}</p>
      <div className="border-t border-rule pt-6">
        <Link to="/elections" className="btn-primary inline-block">
          View elections
        </Link>
      </div>
    </div>
  )
}