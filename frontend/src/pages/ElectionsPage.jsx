import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import client, { extractErrorMessage } from '../api/client'

const STATUS_STYLE = {
  DRAFT: 'text-paper-dim',
  ACTIVE: 'text-teal',
  CLOSED: 'text-paper-dim/50',
}

/**
 * The register's table of contents — every election as a single ledger
 * row, ruled dividers between entries, status shown as plain text rather
 * than a decorative badge (a register doesn't need chrome to say what a
 * row's status is, the word itself is enough).
 */
export default function ElectionsPage() {
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
      <h1 className="text-3xl font-semibold mb-1">Elections</h1>
      <p className="text-paper-dim mb-10">Every entry below is a live chain — pick one to view or vote.</p>

      {loading && <p className="text-paper-dim font-mono text-sm">Loading register…</p>}
      {error && <p className="text-signal text-sm">{error}</p>}

      {!loading && !error && elections.length === 0 && (
        <p className="text-paper-dim">No elections yet. Check back once an admin creates one.</p>
      )}

      <div className="border-t border-rule">
        {elections.map((election) => (
          <Link
            key={election.id}
            to={`/elections/${election.id}`}
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