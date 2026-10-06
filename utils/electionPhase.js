/**
 * Election.status (DRAFT/ACTIVE/CLOSED) is an admin-controlled lifecycle
 * flag — it does NOT mean voting is open right now. An ACTIVE election can
 * still be scheduled for the future (startTime hasn't arrived yet) or,
 * briefly, past its endTime before the backend's auto-close job catches up.
 *
 * This computes the actual, honest phase a voter should see, using the
 * same startTime/endTime data the backend already sends.
 */
export function getElectionPhase(election) {
  if (election.status === 'DRAFT') return 'DRAFT'
  if (election.status === 'CLOSED') return 'CLOSED'

  const now = new Date()
  const start = new Date(election.startTime)
  const end = new Date(election.endTime)

  if (now < start) return 'SCHEDULED'
  if (now > end) return 'CLOSED'
  return 'OPEN'
}

export const PHASE_LABEL = {
  DRAFT: 'DRAFT',
  SCHEDULED: 'SCHEDULED',
  OPEN: 'ACTIVE',
  CLOSED: 'CLOSED',
}

export const PHASE_STYLE = {
  DRAFT: 'text-paper-dim',
  SCHEDULED: 'text-brass',
  OPEN: 'text-teal',
  CLOSED: 'text-paper-dim/50',
}