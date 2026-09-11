import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
} from 'recharts'
import client, { extractErrorMessage } from '../api/client'

const BRASS = '#C9A227'
const TEAL = '#3EC9B0'
const RULE = '#2B323D'
const PAPER_DIM = '#9AA0AC'

/** Renders the exact mined+pending split each candidate carries — see
 * CandidateResponse.java — as a stacked bar, so the chart itself shows
 * the batching behavior (confirmed vs still-in-mempool) rather than
 * hiding it behind a single misleading number. */
function CustomTooltip({ active, payload, label }) {
  if (!active || !payload?.length) return null
  const mined = payload.find((p) => p.dataKey === 'minedVotes')?.value ?? 0
  const pending = payload.find((p) => p.dataKey === 'pendingVotes')?.value ?? 0
  return (
    <div className="bg-surface border border-rule rounded-sm px-4 py-3 font-mono text-xs">
      <p className="font-sans text-paper mb-1">{label}</p>
      <p className="text-brass">confirmed: {mined}</p>
      <p className="text-teal">pending: {pending}</p>
    </div>
  )
}

export default function ResultsPage() {
  const { electionId } = useParams()
  const [election, setElection] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  function loadElection() {
    return client
      .get(`/api/elections/${electionId}`)
      .then((res) => setElection(res.data))
      .catch((err) => setError(extractErrorMessage(err)))
  }

  useEffect(() => {
    loadElection().finally(() => setLoading(false))
    // Poll for live updates — votes get mined in batches on the backend,
    // so a fresh fetch periodically is what makes this feel "live".
    const interval = setInterval(loadElection, 5000)
    return () => clearInterval(interval)
  }, [electionId])

  if (loading) return <p className="text-paper-dim font-mono text-sm">Loading results…</p>
  if (!election) return <p className="text-signal text-sm">{error || 'Election not found.'}</p>

  const candidates = [...election.candidates].sort((a, b) => b.totalVotes - a.totalVotes)
  const totalAllVotes = candidates.reduce((sum, c) => sum + c.totalVotes, 0)
  const leader = candidates[0]

  const chartData = candidates.map((c) => ({
    name: c.name,
    minedVotes: c.minedVotes,
    pendingVotes: c.pendingVotes,
  }))

  return (
    <div>
      <Link to={`/elections/${electionId}`} className="text-sm text-paper-dim hover:text-paper mb-6 inline-block">
        ← Back to ballot
      </Link>

      <div className="flex items-center justify-between mb-1">
        <h1 className="text-3xl font-semibold">{election.title}</h1>
        <span className="font-mono text-xs uppercase text-paper-dim">{election.status}</span>
      </div>
      <p className="text-paper-dim mb-10 font-mono text-xs">
        {totalAllVotes} total vote{totalAllVotes === 1 ? '' : 's'} · refreshes every 5s
      </p>

      {totalAllVotes === 0 ? (
        <p className="text-paper-dim">No votes cast yet.</p>
      ) : (
        <>
          {leader && (
            <div className="border border-rule rounded-sm p-6 bg-surface mb-10">
              <p className="font-mono text-xs text-brass mb-1">currently leading</p>
              <p className="font-serif text-2xl">{leader.name}</p>
              <p className="text-paper-dim text-sm">
                {leader.totalVotes} votes · {((leader.totalVotes / totalAllVotes) * 100).toFixed(1)}%
              </p>
            </div>
          )}

          <div className="h-72 mb-10">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData} layout="vertical" margin={{ left: 20 }}>
                <CartesianGrid horizontal={false} stroke={RULE} />
                <XAxis type="number" stroke={PAPER_DIM} tick={{ fill: PAPER_DIM, fontSize: 12 }} allowDecimals={false} />
                <YAxis
                  type="category"
                  dataKey="name"
                  stroke={PAPER_DIM}
                  tick={{ fill: PAPER_DIM, fontSize: 13, fontFamily: 'IBM Plex Sans' }}
                  width={110}
                />
                <Tooltip content={<CustomTooltip />} cursor={{ fill: RULE, opacity: 0.3 }} />
                <Bar dataKey="minedVotes" stackId="votes" fill={BRASS} radius={[0, 0, 0, 0]} />
                <Bar dataKey="pendingVotes" stackId="votes" fill={TEAL} radius={[0, 2, 2, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>

          <div className="flex gap-6 text-xs font-mono text-paper-dim mb-10">
            <span><span className="inline-block w-2.5 h-2.5 bg-brass mr-2 align-middle" />confirmed on-chain</span>
            <span><span className="inline-block w-2.5 h-2.5 bg-teal mr-2 align-middle" />pending next block</span>
          </div>

          <div className="border-t border-rule">
            {candidates.map((c) => (
              <div key={c.id} className="flex items-center justify-between py-4 border-b border-rule">
                <div>
                  <p className="font-serif">{c.name}</p>
                  <p className="text-sm text-paper-dim">{c.party}</p>
                </div>
                <span className="font-mono text-sm text-paper-dim">
                  {c.totalVotes} · {totalAllVotes > 0 ? ((c.totalVotes / totalAllVotes) * 100).toFixed(1) : '0.0'}%
                </span>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  )
}