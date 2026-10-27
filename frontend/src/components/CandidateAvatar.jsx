import { useState } from 'react'


export default function CandidateAvatar({ name, symbolUrl, size = 44 }) {
  const [failed, setFailed] = useState(false)

  const initials = name
    .split(' ')
    .map((w) => w[0])
    .slice(0, 2)
    .join('')
    .toUpperCase()

  const dimension = `${size}px`

  if (symbolUrl && !failed) {
    return (
      <img
        src={symbolUrl}
        alt={`${name} symbol`}
        onError={() => setFailed(true)}
        style={{ width: dimension, height: dimension }}
        className="rounded-full object-cover border border-rule flex-shrink-0"
      />
    )
  }

  return (
    <div
      style={{ width: dimension, height: dimension }}
      className="rounded-full border border-rule bg-surface flex items-center justify-center flex-shrink-0"
    >
      <span className="font-serif text-sm text-brass">{initials}</span>
    </div>
  )
}