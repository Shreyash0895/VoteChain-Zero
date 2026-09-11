import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import client, { extractErrorMessage } from '../api/client'

/**
 * The admin's working page for a single election: staff it with candidates
 * while DRAFT, then activate (mints the genesis block on the backend) or
 * close it. Vote counts shown here use the same mined/pending/total split
 * as the voter ballot — transparency for the admin too, not just voters.
 */
export default function ManageElectionPage() {
  const { electionId } = useParams()
  const [election, setElection] = useState(null)
  const [candidateForm, setCandidateForm] = useState({ name: '', party: '', symbolUrl: '' })
  const [error, setError] = useState('')
  const [actionError, setActionError] = useState('')
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)

  function loadElection() {
    return client
      .get(`/api/elections/${electionId}`)
      .then((res) => setElection(res.data))
      .catch((err) => setError(extractErrorMessage(err)))
  }

  useEffect(() => {
    loadElection().finally(() => setLoading(false))
  }, [electionId])

  function updateCandidateForm(field) {
    return (e) => setCandidateForm((f) => ({ ...f, [field]: e.target.value }))
  }

  async function handleAddCandidate(e) {
    e.preventDefault()
    setActionError('')
    setBusy(true)
    try {
      await client.post(`/api/elections/${electionId}/candidates`, candidateForm)
      setCandidateForm({ name: '', party: '', symbolUrl: '' })
      await loadElection()
    } catch (err) {
      setActionError(extractErrorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  async function handleActivate() {
    setActionError('')
    setBusy(true)
    try {
      await client.post(`/api/elections/${electionId}/activate`)
      await loadElection()
    } catch (err) {
      setActionError(extractErrorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  async function handleClose() {
    setActionError('')
    setBusy(true)
    try {
      await client.post(`/api/elections/${electionId}/close`)
      await loadElection()
    } catch (err) {
      setActionError(extractErrorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  if (loading) return <p className="text-paper-dim font-mono text-sm">Loading…</p>
  if (!election) return <p className="text-signal text-sm">{error || 'Election not found.'}</p>

  const isDraft = election.status === 'DRAFT'
  const isActive = election.status === 'ACTIVE'
  const canActivate = isDraft && election.candidates.length >= 2

  return (
    <div>
      <Link to="/admin/elections" className="text-sm text-paper-dim hover:text-paper mb-6 inline-block">
        ← Back to elections
      </Link>

      <div className="flex items-center justify-between mb-2">
        <h1 className="text-3xl font-semibold">{election.title}</h1>
        <span className="font-mono text-xs uppercase text-paper-dim">{election.status}</span>
      </div>
      {election.description && <p className="text-paper-dim mb-10">{election.description}</p>}

      {actionError && <p className="text-signal text-sm mb-6">{actionError}</p>}

      {/* Candidate ledger */}
      <h2 className="font-serif text-xl mb-4">Candidates</h2>
      <div className="border-t border-rule mb-8">
        {election.candidates.length === 0 && (
          <p className="text-paper-dim text-sm py-4">No candidates yet — add at least 2 to activate.</p>
        )}
        {election.candidates.map((c) => (
          <div key={c.id} className="flex items-center justify-between py-4 border-b border-rule">
            <div>
              <p className="font-serif">{c.name}</p>
              <p className="text-sm text-paper-dim">{c.party}</p>
            </div>
            <span className="font-mono text-sm text-paper-dim">
              {c.totalVotes} votes {c.pendingVotes > 0 && `(${c.pendingVotes} pending)`}
            </span>
          </div>
        ))}
      </div>

      {/* Add candidate form — only while DRAFT */}
      {isDraft && (
        <form onSubmit={handleAddCandidate} className="space-y-4 mb-10 max-w-sm">
          <h3 className="font-serif text-lg">Add a candidate</h3>
          <div>
            <label className="field-label" htmlFor="name">Name</label>
            <input id="name" className="field-input" required value={candidateForm.name} onChange={updateCandidateForm('name')} />
          </div>
          <div>
            <label className="field-label" htmlFor="party">Party</label>
            <input id="party" className="field-input" required value={candidateForm.party} onChange={updateCandidateForm('party')} />
          </div>
          <div>
            <label className="field-label" htmlFor="symbolUrl">Symbol URL (optional)</label>
            <input id="symbolUrl" className="field-input" value={candidateForm.symbolUrl} onChange={updateCandidateForm('symbolUrl')} />
          </div>
          <button type="submit" disabled={busy} className="btn-secondary">
            {busy ? 'Adding…' : 'Add candidate'}
          </button>
        </form>
      )}

      {/* Lifecycle actions */}
      <div className="border-t border-rule pt-6 flex gap-3">
        {isDraft && (
          <button onClick={handleActivate} disabled={!canActivate || busy} className="btn-primary">
            {canActivate ? 'Activate election' : 'Add 2+ candidates to activate'}
          </button>
        )}
        {isActive && (
          <button onClick={handleClose} disabled={busy} className="btn-secondary">
            Close election
          </button>
        )}
      </div>
    </div>
  )
}