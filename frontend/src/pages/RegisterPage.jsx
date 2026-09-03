import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import client, { extractErrorMessage } from '../api/client'

export default function RegisterPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ fullName: '', email: '', password: '', governmentId: '' })
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
      // Matches POST /api/auth/register — backend hashes governmentId
      // immediately and never stores it raw. See RegisterRequest.java.
      await client.post('/api/auth/register', form)
      navigate('/verify-otp', { state: { email: form.email, mode: 'register' } })
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="max-w-md mx-auto">
      <h1 className="text-3xl font-semibold mb-2">Register to vote</h1>
      <p className="text-paper-dim mb-8">
        Your ID is hashed the moment you submit it — it's never stored in readable form.
      </p>

      <form onSubmit={handleSubmit} className="space-y-5">
        <div>
          <label className="field-label" htmlFor="fullName">Full name</label>
          <input id="fullName" className="field-input" required value={form.fullName} onChange={update('fullName')} />
        </div>

        <div>
          <label className="field-label" htmlFor="email">Email</label>
          <input id="email" type="email" className="field-input" required value={form.email} onChange={update('email')} />
        </div>

        <div>
          <label className="field-label" htmlFor="password">Password</label>
          <input id="password" type="password" className="field-input" required minLength={8} value={form.password} onChange={update('password')} />
        </div>

        <div>
          <label className="field-label" htmlFor="governmentId">Government ID</label>
          <input id="governmentId" className="field-input" required value={form.governmentId} onChange={update('governmentId')} />
        </div>

        {error && <p className="text-signal text-sm">{error}</p>}

        <button type="submit" disabled={loading} className="btn-primary w-full">
          {loading ? 'Registering…' : 'Register'}
        </button>
      </form>

      <p className="text-sm text-paper-dim mt-6">
        Already registered? <Link to="/login" className="text-brass hover:underline">Log in</Link>
      </p>
    </div>
  )
}