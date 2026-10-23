import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import {
  Bar, BarChart, CartesianGrid, Legend, ResponsiveContainer, Tooltip, XAxis, YAxis,
} from 'recharts';
import client from '../api/client';

const POLL_MS = 5000;

export default function ResultsPage() {
  const { electionId } = useParams();
  const [election, setElection] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    let alive = true;
    const load = async () => {
      try {
        const { data } = await client.get(`/api/elections/${electionId}`);
        if (alive) { setElection(data); setError(''); }
      } catch (e) {
        if (alive) setError(e?.response?.data?.message || 'Could not load results');
      }
    };
    load();
    const t = setInterval(load, POLL_MS);
    return () => { alive = false; clearInterval(t); };
  }, [electionId]);

  if (error && !election) return <p className="mx-auto max-w-5xl px-6 py-16 text-red-400">{error}</p>;
  if (!election) return <p className="mx-auto max-w-5xl px-6 py-16 opacity-70">Loading results…</p>;

  const candidates = (election.candidates || []).map((c) => ({
    ...c,
    confirmed: c.minedVotes ?? 0,
    pending: c.pendingVotes ?? 0,
    total: c.totalVotes ?? 0,
  }));
  const sorted = [...candidates].sort((a, b) => b.total - a.total);
  const grandTotal = sorted.reduce((s, c) => s + c.total, 0);
  const confirmed = sorted.reduce((s, c) => s + c.confirmed, 0);
  const pending = sorted.reduce((s, c) => s + c.pending, 0);
  const top = sorted[0];
  const tied = top ? sorted.filter((c) => c.total === top.total) : [];
  const margin = sorted.length > 1 ? sorted[0].total - sorted[1].total : sorted[0]?.total ?? 0;
  const closed = election.status === 'CLOSED';
  const finalised = closed && pending === 0;
  const pct = (n) => (grandTotal ? ((n / grandTotal) * 100).toFixed(1) : '0.0');

  let banner;
  if (grandTotal === 0) {
    banner = { label: finalised ? 'Result' : 'Status', text: finalised ? 'No votes were cast' : 'No votes cast yet' };
  } else if (tied.length > 1) {
    banner = {
      label: finalised ? 'Result' : 'Currently',
      text: `${finalised ? 'Tie' : 'Tied'} between ${tied.map((c) => c.name).join(' & ')}`,
    };
  } else {
    banner = {
      label: finalised ? 'Winner' : 'Currently leading',
      text: finalised ? `${top.name} (${top.party}) has won the election` : `${top.name} (${top.party})`,
    };
  }

  const chartData = sorted.map((c) => ({ name: c.name, Confirmed: c.confirmed, Pending: c.pending }));

  return (
    <div className="mx-auto max-w-5xl px-6 py-12">
      <Link to={`/admin/elections/${electionId}`} className="text-sm opacity-70 hover:opacity-100">
        ← Back to election
      </Link>

      <div className="mt-6 flex items-start justify-between">
        <div>
          <h1 className="font-serif text-4xl">{election.title}</h1>
          <p className="mt-1 text-sm opacity-60">Admin analytics · refreshes every 5s</p>
        </div>
        <span className="font-mono text-xs uppercase tracking-wider opacity-70">{election.status}</span>
      </div>

      <div className={`mt-8 border p-6 ${finalised ? 'border-yellow-600/60' : 'border-white/15'}`}>
        <p className="font-mono text-xs uppercase tracking-widest opacity-60">{banner.label}</p>
        <p className="mt-2 font-serif text-3xl">{banner.text}</p>
        {grandTotal > 0 && tied.length === 1 && (
          <p className="mt-2 text-sm opacity-70">
            {top.total} votes · {pct(top.total)}% share · margin of {margin} vote{margin === 1 ? '' : 's'}
          </p>
        )}
        {!finalised && grandTotal > 0 && (
          <p className="mt-2 text-xs opacity-50">
            Provisional — the winner is declared once the election is closed and all votes are confirmed.
          </p>
        )}
      </div>

      <div className="mt-8 grid grid-cols-2 gap-px bg-white/10 sm:grid-cols-4">
        {[
          ['Total votes', grandTotal],
          ['Confirmed on chain', confirmed],
          ['Pending', pending],
          ['Lead margin', margin],
        ].map(([k, v]) => (
          <div key={k} className="bg-[#0f131a] p-5">
            <p className="font-mono text-xs uppercase opacity-60">{k}</p>
            <p className="mt-1 font-serif text-3xl">{v}</p>
          </div>
        ))}
      </div>

      <h2 className="mt-12 font-serif text-2xl">Votes by candidate</h2>
      <div className="mt-4 h-72">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" stroke="#ffffff20" />
            <XAxis dataKey="name" stroke="#9ca3af" />
            <YAxis allowDecimals={false} stroke="#9ca3af" />
            <Tooltip contentStyle={{ background: '#0f131a', border: '1px solid #ffffff30' }} />
            <Legend />
            <Bar dataKey="Confirmed" stackId="a" fill="#c9a24b" />
            <Bar dataKey="Pending" stackId="a" fill="#2aa198" />
          </BarChart>
        </ResponsiveContainer>
      </div>

      <h2 className="mt-12 font-serif text-2xl">Standings</h2>
      <ul className="mt-4 divide-y divide-white/10 border-y border-white/10">
        {sorted.map((c, i) => (
          <li key={c.id} className="py-4">
            <div className="flex items-baseline justify-between">
              <span>
                <span className="font-mono text-sm opacity-50">#{i + 1}</span>{' '}
                <span className="font-serif text-lg">{c.name}</span>{' '}
                <span className="text-sm opacity-60">{c.party}</span>
                {finalised && i === 0 && tied.length === 1 && (
                  <span className="ml-2 font-mono text-xs text-yellow-500">WINNER</span>
                )}
              </span>
              <span className="font-mono text-sm">
                {c.total} votes · {pct(c.total)}%
                {c.pending > 0 && <span className="opacity-60"> ({c.pending} pending)</span>}
              </span>
            </div>
            <div className="mt-2 h-1.5 bg-white/10">
              <div className="h-full bg-[#c9a24b]" style={{ width: `${pct(c.total)}%` }} />
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
}