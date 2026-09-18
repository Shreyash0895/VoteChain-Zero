import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import client, { extractErrorMessage } from '../api/client'

export default function CreateElectionPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ title: '', description: '', startTime: '', endTime: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  function update(field) {
    return (e) => setForm((f) => ({ ...f, [field]: e.target.value }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const res = await client.post('/api/elections', form)
      navigate(`/admin/elections/${res.data.id}`)
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="max-w-md mx-auto">
      <h1 className="text-3xl font-semibold mb-8">New election</h1>

      <form onSubmit={handleSubmit} className="space-y-5">
        <div>
          <label className="field-label" htmlFor="title">Title</label>
          <input id="title" className="field-input" required value={form.title} onChange={update('title')} />
        </div>

        <div>
          <label className="field-label" htmlFor="description">Description</label>
          <textarea id="description" className="field-input" rows={3} value={form.description} onChange={update('description')} />
        </div>

        <div>
          <label className="field-label" htmlFor="startTime">Start time</label>
          <input id="startTime" type="datetime-local" className="field-input" required value={form.startTime} onChange={update('startTime')} />
        </div>

        <div>
          <label className="field-label" htmlFor="endTime">End time</label>
          <input id="endTime" type="datetime-local" className="field-input" required value={form.endTime} onChange={update('endTime')} />
        </div>

        {error && <p className="text-signal text-sm">{error}</p>}

        <button type="submit" disabled={loading} className="btn-primary w-full">
          {loading ? 'Creating…' : 'Create election'}
        </button>
      </form>
    </div>
  )
}