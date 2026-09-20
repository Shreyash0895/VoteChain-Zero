import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import client, { extractErrorMessage } from '../api/client'
import { useAuth } from '../context/AuthContext'
import BrandPanel from '../components/BrandPanel'

export default function RegisterPage() {
  const navigate = useNavigate()
  const { login } = useAuth()
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
      // Backend now returns a JWT directly — registering logs you straight in.
      const res = await client.post('/api/auth/register', form)
      login(res.data)
      navigate('/')
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="grid md:grid-cols-2 gap-16 items-start">
      <BrandPanel
        eyebrow="/ register"
        title="One person, one vote, one entry."
        description="Your government ID is hashed the instant you submit it. Nobody — not even a database admin — can reverse it back to your real identity."
      />

      <div>
        <h1 className="text-3xl font-semibold mb-2">Register to vote</h1>
        <p className="text-paper-dim mb-8">Takes about a minute.</p>

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
    </div>
  )
}