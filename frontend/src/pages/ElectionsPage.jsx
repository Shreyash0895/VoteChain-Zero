import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import client, { extractErrorMessage } from '../api/client'
import { getElectionPhase, PHASE_LABEL, PHASE_STYLE } from '../utils/electionPhase'

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
        {elections.map((election) => {
          const phase = getElectionPhase(election)
          return (
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
              <span className={`font-mono text-xs uppercase ${PHASE_STYLE[phase]}`}>
                {PHASE_LABEL[phase]}
              </span>
            </Link>
          )
        })}
      </div>
    </div>
  )
}