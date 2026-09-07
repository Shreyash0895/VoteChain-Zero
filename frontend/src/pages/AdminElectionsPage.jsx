import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import client, { extractErrorMessage } from '../api/client'

const STATUS_STYLE = {
  DRAFT: 'text-paper-dim',
  ACTIVE: 'text-teal',
  CLOSED: 'text-paper-dim/50',
}

/** Admin's register — same ledger-row pattern as the voter list, but each
 * row links to management (add candidates / activate / close) instead of
 * a ballot. */
export default function AdminElectionsPage() {
  const [elections, setElections] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    client
      .get('/api/elections')
      .then((res) => setElections(res.data))
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false))
  }, [])

  return (
    <div>
      <div className="flex items-center justify-between mb-10">
        <div>
          <h1 className="text-3xl font-semibold mb-1">Manage elections</h1>
          <p className="text-paper-dim">Create, staff, and activate elections here.</p>
        </div>
        <Link to="/admin/elections/new" className="btn-primary">
          New election
        </Link>
      </div>

      {loading && <p className="text-paper-dim font-mono text-sm">Loading register…</p>}
      {error && <p className="text-signal text-sm">{error}</p>}

      {!loading && !error && elections.length === 0 && (
        <p className="text-paper-dim">No elections yet — create the first one.</p>
      )}

      <div className="border-t border-rule">
        {elections.map((election) => (
          <Link
            key={election.id}
            to={`/admin/elections/${election.id}`}
            className="flex items-center justify-between py-5 border-b border-rule hover:bg-surface/50 transition-colors px-2 -mx-2"
          >
            <div>
              <p className="font-serif text-lg">{election.title}</p>
              <p className="text-sm text-paper-dim mt-0.5">
                {election.candidates?.length || 0} candidate{election.candidates?.length === 1 ? '' : 's'}
              </p>
            </div>
            <span className={`font-mono text-xs uppercase ${STATUS_STYLE[election.status] || 'text-paper-dim'}`}>
              {election.status}
            </span>
          </Link>
        ))}
      </div>
    </div>
  )
}