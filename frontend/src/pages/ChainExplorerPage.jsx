import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import client, { extractErrorMessage } from '../api/client'

function truncateHash(hash) {
  if (!hash) return ''
  return `${hash.slice(0, 10)}…${hash.slice(-8)}`
}

/**
 * The chain, made visible. Each block is a numbered ledger entry — the
 * numbering here is genuine sequence data (blockIndex), not decorative
 * step-counting. Hashes render in mono type, truncated with the full
 * value available on hover via the title attribute, since a 64-character
 * hex string has no value being fully readable at a glance.
 */
export default function ChainExplorerPage() {
  const { electionId } = useParams()
  const [chain, setChain] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    client
      .get(`/api/elections/${electionId}/chain`)
      .then((res) => setChain(res.data))
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false))
  }, [electionId])

  if (loading) return <p className="text-paper-dim font-mono text-sm">Reading chain…</p>
  if (error) return <p className="text-signal text-sm">{error}</p>
  if (!chain) return null

  return (
    <div>
      <Link to={`/elections/${electionId}`} className="text-sm text-paper-dim hover:text-paper mb-6 inline-block">
        ← Back to ballot
      </Link>

      <div className="flex items-center justify-between mb-2">
        <h1 className="text-3xl font-semibold">Blockchain explorer</h1>
        {chain.valid ? (
          <span className="font-mono text-xs text-teal border border-teal/40 rounded-sm px-3 py-1">
            chain verified ✓
          </span>
        ) : (
          <span className="font-mono text-xs text-signal border border-signal/40 rounded-sm px-3 py-1">
            {chain.errors.length} issue{chain.errors.length === 1 ? '' : 's'} detected
          </span>
        )}
      </div>
      <p className="text-paper-dim mb-10 font-mono text-xs">
        {chain.blocks.length} block{chain.blocks.length === 1 ? '' : 's'} · every hash recomputed and cross-checked on this load
      </p>

      {!chain.valid && (
        <div className="border border-signal/40 rounded-sm p-5 mb-10 bg-signal/5">
          <p className="font-mono text-xs text-signal mb-2">integrity errors</p>
          <ul className="text-sm text-paper-dim space-y-1">
            {chain.errors.map((err, i) => (
              <li key={i}>· {err}</li>
            ))}
          </ul>
        </div>
      )}

      <div className="border-t border-rule">
        {chain.blocks.map((block) => (
          <div key={block.blockIndex} className="py-6 border-b border-rule">
            <div className="flex items-baseline justify-between mb-3">
              <p className="font-serif text-lg">
                Block <span className="text-brass">#{block.blockIndex}</span>
              </p>
              <p className="font-mono text-xs text-paper-dim">
                {new Date(block.timestamp).toLocaleString()}
              </p>
            </div>

            <div className="grid grid-cols-[100px_1fr] gap-y-1.5 text-xs font-mono">
              <span className="text-paper-dim">hash</span>
              <span title={block.hash}>{truncateHash(block.hash)}</span>

              <span className="text-paper-dim">prev hash</span>
              <span title={block.previousHash}>{truncateHash(block.previousHash)}</span>

              <span className="text-paper-dim">merkle root</span>
              <span title={block.merkleRoot}>{truncateHash(block.merkleRoot)}</span>

              <span className="text-paper-dim">nonce</span>
              <span>{block.nonce}</span>

              <span className="text-paper-dim">validator</span>
              <span>{block.validatorId}</span>

              <span className="text-paper-dim">votes</span>
              <span>{block.transactionCount}</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}