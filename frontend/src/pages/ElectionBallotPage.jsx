import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import client, { extractErrorMessage } from '../api/client'
import CandidateAvatar from '../components/CandidateAvatar'

export default function ElectionBallotPage() {
  const { electionId } = useParams()
  const [election, setElection] = useState(null)
  const [selectedCandidateId, setSelectedCandidateId] = useState(null)
  const [receipt, setReceipt] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)

  function loadElection() {
    return client
      .get(`/api/elections/${electionId}`)
      .then((res) => setElection(res.data))
      .catch((err) => setError(extractErrorMessage(err)))
  }

  useEffect(() => {
    loadElection().finally(() => setLoading(false))
  }, [electionId])

  async function handleVote() {
    setError('')
    setSubmitting(true)
    try {
      const res = await client.post(`/api/elections/${electionId}/votes`, {
        candidateId: selectedCandidateId,
      })
      setReceipt(res.data)
      await loadElection()
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) return <p className="text-paper-dim font-mono text-sm">Loading ballot…</p>
  if (!election) return <p className="text-signal text-sm">{error || 'Election not found.'}</p>

  const votingOpen = election.status === 'ACTIVE'

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <Link to="/elections" className="text-sm text-paper-dim hover:text-paper">
          ← Back to elections
        </Link>
        <div className="flex gap-5">
          <Link to={`/elections/${electionId}/chain`} className="text-sm text-paper-dim hover:text-paper">
            View chain
          </Link>
          <Link to={`/elections/${electionId}/results`} className="text-sm text-brass hover:underline">
            View live results →
          </Link>
        </div>
      </div>

      <h1 className="text-3xl font-semibold mb-1">{election.title}</h1>
      {election.description && <p className="text-paper-dim mb-2">{election.description}</p>}
      <p className="font-mono text-xs uppercase text-paper-dim/60 mb-10">{election.status}</p>

      {receipt ? (
        <div className="border border-brass rounded-sm p-6 bg-surface">
          <p className="font-mono text-xs text-brass mb-2">vote recorded</p>
          <p className="text-paper-dim text-sm mb-4">
            Save this receipt — it's the only way to independently verify your vote was counted.
          </p>
          <p className="font-mono text-sm break-all bg-ink border border-rule rounded-sm px-4 py-3">
            {receipt.transactionHash}
          </p>
          <p className="text-xs text-paper-dim mt-3">
            {receipt.mined ? 'Already confirmed on-chain.' : 'Pending — will confirm once the next block mines.'}
          </p>
        </div>
      ) : (
        <>
          {!votingOpen && (
            <p className="text-paper-dim text-sm mb-6">
              Voting isn't open for this election right now.
            </p>
          )}

          <div className="border-t border-rule mb-6">
            {election.candidates.map((candidate) => {
              const selected = selectedCandidateId === candidate.id
              return (
                <button
                  key={candidate.id}
                  disabled={!votingOpen}
                  onClick={() => setSelectedCandidateId(candidate.id)}
                  className={`w-full flex items-center justify-between py-5 border-b border-rule px-2 -mx-2 text-left transition-colors
                    ${selected ? 'bg-surface' : 'hover:bg-surface/40'}
                    disabled:cursor-not-allowed`}
                >
                  <div className="flex items-center gap-4">
                    <span className={`w-4 h-4 rounded-full border flex-shrink-0 ${selected ? 'bg-brass border-brass' : 'border-rule'}`} />
                    <CandidateAvatar name={candidate.name} symbolUrl={candidate.symbolUrl} />
                    <div>
                      <p className="font-serif text-lg">{candidate.name}</p>
                      <p className="text-sm text-paper-dim">{candidate.party}</p>
                    </div>
                  </div>
                  <span className="font-mono text-sm text-paper-dim">{candidate.totalVotes} votes</span>
                </button>
              )
            })}
          </div>

          {error && <p className="text-signal text-sm mb-4">{error}</p>}

          {votingOpen && (
            <button
              onClick={handleVote}
              disabled={!selectedCandidateId || submitting}
              className="btn-primary"
            >
              {submitting ? 'Casting vote…' : 'Cast vote'}
            </button>
          )}
        </>
      )}
    </div>
  )
}