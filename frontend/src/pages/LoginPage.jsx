import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import client, { extractErrorMessage } from '../api/client'
import BrandPanel from '../components/BrandPanel'

export default function LoginPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ email: '', password: '' })
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
      await client.post('/api/auth/login', form)
      navigate('/verify-otp', { state: { email: form.email, mode: 'login' } })
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="grid md:grid-cols-2 gap-16 items-start">
      <BrandPanel
        eyebrow="/ log in"
        title="Two factors, every time."
        description="Your password alone never issues a session. A fresh one-time code is required on every login — no exceptions, even for admins."
      />

      <div>
        <h1 className="text-3xl font-semibold mb-2">Log in</h1>
        <p className="text-paper-dim mb-8">We'll email you a one-time code as a second factor.</p>

        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label className="field-label" htmlFor="email">Email</label>
            <input id="email" type="email" className="field-input" required value={form.email} onChange={update('email')} />
          </div>

          <div>
            <label className="field-label" htmlFor="password">Password</label>
            <input id="password" type="password" className="field-input" required value={form.password} onChange={update('password')} />
          </div>

          {error && <p className="text-signal text-sm">{error}</p>}

          <button type="submit" disabled={loading} className="btn-primary w-full">
            {loading ? 'Checking…' : 'Continue'}
          </button>
        </form>

        <p className="text-sm text-paper-dim mt-6">
          New here? <Link to="/register" className="text-brass hover:underline">Register</Link>
        </p>
      </div>
    </div>
  )
}