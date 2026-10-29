import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import client, { extractErrorMessage } from '../api/client'
import ElectionResults from '../components/ElectionResults'

const POLL_MS = 5000

// Admin-only live results (route is wrapped in RequireAdmin; the backend only
// sends counts to admins while the election is still open).
export default function ResultsPage() {
  const { electionId } = useParams()
  const [election, setElection] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    let alive = true
    const load = () =>
      client
        .get(`/api/elections/${electionId}`)
        .then((res) => { if (alive) { setElection(res.data); setError('') } })
        .catch((err) => { if (alive) setError(extractErrorMessage(err)) })
    load()
    const t = setInterval(load, POLL_MS)
    return () => { alive = false; clearInterval(t) }
  }, [electionId])

  if (error && !election) return <p className="text-signal text-sm">{error}</p>
  if (!election) return <p className="text-paper-dim font-mono text-sm">Loading results…</p>

  return (
    <div>
      <Link to={`/admin/elections/${electionId}`} className="text-sm text-paper-dim hover:text-paper">
        ← Back to election
      </Link>
      <div className="mt-6 mb-8 flex items-start justify-between">
        <div>
          <h1 className="text-3xl font-semibold">{election.title}</h1>
          <p className="mt-1 text-sm text-paper-dim">Admin analytics · refreshes every 5s</p>
        </div>
        <span className="font-mono text-xs uppercase text-paper-dim">{election.status}</span>
      </div>
      <ElectionResults election={election} />
    </div>
  )
}