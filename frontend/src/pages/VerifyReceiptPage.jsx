import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import client from '../api/client';

async function sha256Hex(text) {
  const buf = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(text));
  return [...new Uint8Array(buf)].map((b) => b.toString(16).padStart(2, '0')).join('');
}

const short = (h) => (h && h.length > 20 ? `${h.slice(0, 10)}…${h.slice(-8)}` : h);

export default function VerifyReceiptPage() {
  const [params] = useSearchParams();
  const [electionId, setElectionId] = useState(params.get('election') || '');
  const [receipt, setReceipt] = useState(params.get('hash') || '');
  const [result, setResult] = useState(null);
  const [steps, setSteps] = useState([]);
  const [localMatch, setLocalMatch] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const verify = async (e) => {
    e?.preventDefault();
    setError(''); setResult(null); setSteps([]); setLocalMatch(null);
    setLoading(true);
    try {
      const { data } = await client.get(
        `/api/elections/${electionId.trim()}/verify/${receipt.trim()}`);
      setResult(data);

      if (data.status === 'CONFIRMED') {
        // Re-do the maths in the browser — don't just trust the server's "valid" flag
        let current = data.transactionHash;
        const trail = [{ label: 'Your receipt (leaf)', hash: current }];
        for (const step of data.proof) {
          current = await sha256Hex(
            step.position === 'LEFT' ? step.hash + current : current + step.hash);
          trail.push({
            label: `Combined with sibling on the ${step.position.toLowerCase()} (${short(step.hash)})`,
            hash: current,
          });
        }
        setSteps(trail);
        setLocalMatch(current === data.merkleRoot);
      }
    } catch (err) {
      setError(err?.response?.data?.message || 'Could not verify this receipt');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (electionId && receipt) verify();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div className="mx-auto max-w-3xl px-6 py-12">
      <Link to="/elections" className="text-sm opacity-70 hover:opacity-100">← Back to elections</Link>
      <h1 className="mt-6 font-serif text-4xl">Verify my vote</h1>
      <p className="mt-2 text-sm opacity-70">
        Paste your receipt to confirm your vote is recorded on the chain. This proves your vote
        was counted — it never reveals who you voted for.
      </p>

      <form onSubmit={verify} className="mt-8 space-y-4">
        <div>
          <label className="font-mono text-xs uppercase opacity-60">Election ID</label>
          <input value={electionId} onChange={(e) => setElectionId(e.target.value)}
            className="mt-1 w-full border border-white/20 bg-transparent p-3 font-mono text-sm" required />
        </div>
        <div>
          <label className="font-mono text-xs uppercase opacity-60">Receipt hash</label>
          <input value={receipt} onChange={(e) => setReceipt(e.target.value)}
            className="mt-1 w-full border border-white/20 bg-transparent p-3 font-mono text-sm" required />
        </div>
        <button disabled={loading} className="border border-white/30 px-6 py-3 hover:bg-white/10">
          {loading ? 'Verifying…' : 'Verify'}
        </button>
      </form>

      {error && <p className="mt-6 text-red-400">{error}</p>}

      {result?.status === 'PENDING' && (
        <div className="mt-8 border border-white/15 p-6">
          <p className="font-serif text-2xl">Received — not yet in a block</p>
          <p className="mt-2 text-sm opacity-70">
            Your vote is recorded and waiting to be mined. It is added to a block when enough
            votes arrive or when the election closes. Check back later.
          </p>
        </div>
      )}

      {result?.status === 'CONFIRMED' && (
        <div className="mt-8">
          <div className={`border p-6 ${localMatch ? 'border-teal-500/60' : 'border-red-500/60'}`}>
            <p className="font-serif text-2xl">
              {localMatch
                ? `Your vote is in block #${result.blockIndex} ✓`
                : 'Proof did not match — chain may be tampered'}
            </p>
            <p className="mt-2 font-mono text-xs opacity-70">
              block hash {short(result.blockHash)} · {result.votesInBlock} vote(s) in block ·
              server check {result.proofValid ? 'passed' : 'failed'} · browser check{' '}
              {localMatch ? 'passed' : 'failed'}
            </p>
          </div>

          {result.signatureValid !== null && result.signatureValid !== undefined && (
            <p className="mt-4 font-mono text-xs opacity-80">
              {result.signatureValid
                ? `Digital signature valid ✓ — signed with key ${result.signerFingerprint}`
                : 'Digital signature INVALID — this vote was altered after it was cast'}
            </p>
          )}

          <h2 className="mt-8 font-serif text-2xl">How the proof works</h2>
          <ol className="mt-4 space-y-3">
            {steps.map((s, i) => (
              <li key={i} className="border-l border-white/20 pl-4">
                <p className="text-sm opacity-70">{i + 1}. {s.label}</p>
                <p className="font-mono text-sm">{short(s.hash)}</p>
              </li>
            ))}
            <li className="border-l border-yellow-600/60 pl-4">
              <p className="text-sm opacity-70">Block's merkle root (stored on chain)</p>
              <p className="font-mono text-sm">{short(result.merkleRoot)}</p>
              <p className="mt-1 text-xs opacity-60">
                {localMatch ? 'Matches the last step above.' : 'Does NOT match.'}
              </p>
            </li>
          </ol>
        </div>
      )}
    </div>
  );
}