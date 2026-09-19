import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const STEPS = [
  { n: '01', title: 'Register & verify', body: 'Your government ID is hashed once, at signup — never stored raw.' },
  { n: '02', title: 'Cast your vote', body: 'One vote per person, enforced by the chain itself, not just a UI rule.' },
  { n: '03', title: 'Verify on-chain', body: 'Use your receipt hash to confirm your vote is genuinely recorded, any time.' },
]

export default function HomePage() {
  const { voter, isAuthenticated, isAdmin } = useAuth()

  if (!isAuthenticated) {
    return (
      <div className="grid md:grid-cols-2 gap-16 items-start">
        <div>
          <h1 className="font-serif text-5xl font-semibold leading-tight mb-5">VoteChain Zero</h1>
          <p className="text-paper-dim max-w-md leading-relaxed mb-10">
            A blockchain-backed voting system where every vote is a signed, tamper-evident
            entry on an append-only ledger.
          </p>
          <div className="flex gap-3">
            <Link to="/register" className="btn-primary">Register to vote</Link>
            <Link to="/login" className="btn-secondary">Log in</Link>
          </div>
        </div>

        <div className="border-l border-rule pl-12">
          <p className="font-mono text-xs text-brass mb-6">/ how it works</p>
          {STEPS.map((step) => (
            <div key={step.n} className="flex gap-5 py-5 border-b border-rule last:border-b-0">
              <span className="font-mono text-sm text-paper-dim/40">{step.n}</span>
              <div>
                <p className="font-serif text-lg mb-1">{step.title}</p>
                <p className="text-sm text-paper-dim leading-relaxed">{step.body}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    )
  }

  return (
    <div className="grid md:grid-cols-2 gap-16 items-start">
      <div>
        <h1 className="text-4xl font-semibold mb-2">Welcome, {voter?.fullName}</h1>
        <p className="text-paper-dim mb-10 font-mono text-sm">Role: {voter?.role}</p>
        <div className="flex gap-3">
          <Link to="/elections" className="btn-primary">View elections</Link>
          {isAdmin && <Link to="/admin/elections" className="btn-secondary">Manage elections</Link>}
        </div>
      </div>

      <div className="border-l border-rule pl-12">
        <p className="font-mono text-xs text-brass mb-6">/ quick links</p>
        <Link to="/elections" className="flex items-center justify-between py-5 border-b border-rule hover:text-brass transition-colors">
          <span className="font-serif text-lg">Browse elections</span>
          <span className="font-mono text-xs text-paper-dim">→</span>
        </Link>
        {isAdmin && (
          <Link to="/admin/elections/new" className="flex items-center justify-between py-5 border-b border-rule hover:text-brass transition-colors">
            <span className="font-serif text-lg">Create an election</span>
            <span className="font-mono text-xs text-paper-dim">→</span>
          </Link>
        )}
      </div>
    </div>
  )
}