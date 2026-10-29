import {
  Bar, BarChart, CartesianGrid, Legend, ResponsiveContainer, Tooltip, XAxis, YAxis,
} from 'recharts'

// Shared results view: winner banner, stat tiles, bar chart, standings.
// Used by the admin results page (live) and the voter ballot page (after close).
export default function ElectionResults({ election }) {
  const candidates = (election.candidates || []).map((c) => ({
    ...c,
    confirmed: c.minedVotes ?? 0,
    pending: c.pendingVotes ?? 0,
    total: c.totalVotes ?? 0,
  }))
  const sorted = [...candidates].sort((a, b) => b.total - a.total)
  const grandTotal = sorted.reduce((s, c) => s + c.total, 0)
  const confirmed = sorted.reduce((s, c) => s + c.confirmed, 0)
  const pending = sorted.reduce((s, c) => s + c.pending, 0)
  const top = sorted[0]
  const second = sorted[1]
  const tied = top ? sorted.filter((c) => c.total === top.total) : []
  const margin = second ? top.total - second.total : top?.total ?? 0
  const finalised = election.status === 'CLOSED' && pending === 0
  const pct = (n) => (grandTotal ? ((n / grandTotal) * 100).toFixed(1) : '0.0')
  const plural = (n) => `${n} vote${n === 1 ? '' : 's'}`

  let label
  let headline
  let detail = null
  if (grandTotal === 0) {
    label = finalised ? 'Result' : 'Status'
    headline = finalised ? 'No votes were cast' : 'No votes cast yet'
  } else if (tied.length > 1) {
    label = finalised ? 'Result' : 'Currently'
    headline = `${finalised ? 'Tie' : 'Tied'} between ${tied.map((c) => c.name).join(' & ')}`
    detail = `${plural(top.total)} each`
  } else {
    label = finalised ? 'Winner' : 'Currently leading'
    headline = finalised
      ? `${top.name} (${top.party}) has won the election`
      : `${top.name} (${top.party})`
    detail = `${plural(top.total)} · ${pct(top.total)}% share` +
      (second ? ` · ahead of ${second.name} by ${plural(margin)}` : '')
  }

  const showPending = pending > 0
  const chartData = sorted.map((c) => ({
    name: c.name,
    Confirmed: c.confirmed,
    ...(showPending ? { Pending: c.pending } : {}),
  }))

  return (
    <div>
      <div className={`border rounded-sm p-6 ${finalised ? 'border-brass' : 'border-rule'} bg-surface`}>
        <p className="font-mono text-xs uppercase tracking-widest text-paper-dim">{label}</p>
        <p className="mt-2 font-serif text-3xl">{headline}</p>
        {detail && <p className="mt-2 text-sm text-paper-dim">{detail}</p>}
        {!finalised && grandTotal > 0 && (
          <p className="mt-2 text-xs text-paper-dim/60">
            Provisional — the winner is declared once the election is closed and all votes are confirmed.
          </p>
        )}
      </div>

      <div className="mt-6 grid grid-cols-2 sm:grid-cols-4 gap-px bg-rule border border-rule">
        {[
          ['Total votes', grandTotal],
          ['Confirmed on chain', confirmed],
          ['Pending', pending],
          ['Winning margin', margin],
        ].map(([k, v]) => (
          <div key={k} className="bg-ink p-5">
            <p className="font-mono text-xs uppercase text-paper-dim">{k}</p>
            <p className="mt-1 font-serif text-3xl">{v}</p>
          </div>
        ))}
      </div>

      <h2 className="mt-10 font-serif text-2xl">Votes by candidate</h2>
      <div className="mt-4 h-72">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" stroke="#ffffff20" />
            <XAxis dataKey="name" stroke="#9ca3af" />
            <YAxis allowDecimals={false} stroke="#9ca3af" />
            <Tooltip contentStyle={{ background: '#0f131a', border: '1px solid #ffffff30' }} />
            {showPending && <Legend />}
            <Bar dataKey="Confirmed" stackId="a" fill="#c9a24b" />
            {showPending && <Bar dataKey="Pending" stackId="a" fill="#2aa198" />}
          </BarChart>
        </ResponsiveContainer>
      </div>

      <h2 className="mt-10 font-serif text-2xl">Standings</h2>
      <ul className="mt-4 border-t border-rule">
        {sorted.map((c, i) => (
          <li key={c.id} className="py-4 border-b border-rule">
            <div className="flex items-baseline justify-between">
              <span>
                <span className="font-mono text-sm text-paper-dim">#{i + 1}</span>{' '}
                <span className="font-serif text-lg">{c.name}</span>{' '}
                <span className="text-sm text-paper-dim">{c.party}</span>
                {finalised && i === 0 && tied.length === 1 && (
                  <span className="ml-2 font-mono text-xs text-brass">WINNER</span>
                )}
              </span>
              <span className="font-mono text-sm">
                {plural(c.total)} · {pct(c.total)}%
                {c.pending > 0 && <span className="text-paper-dim"> ({c.pending} pending)</span>}
              </span>
            </div>
            <div className="mt-2 h-1.5 bg-rule">
              <div className="h-full bg-brass" style={{ width: `${pct(c.total)}%` }} />
            </div>
          </li>
        ))}
      </ul>
    </div>
  )
}