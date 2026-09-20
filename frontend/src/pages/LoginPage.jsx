import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import client, { extractErrorMessage } from '../api/client'
import { useAuth } from '../context/AuthContext'
import BrandPanel from '../components/BrandPanel'

export default function LoginPage() {
  const navigate = useNavigate()
  const { login } = useAuth()
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
      const res = await client.post('/api/auth/login', form)
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
        eyebrow="/ log in"
        title="Welcome back."
        description="Every vote you cast is a signed, tamper-evident entry — verifiable by you, any time, in the blockchain explorer."
      />

      <div>
        <h1 className="text-3xl font-semibold mb-2">Log in</h1>
        <p className="text-paper-dim mb-8">Enter your email and password to continue.</p>

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
            {loading ? 'Logging in…' : 'Log in'}
          </button>
        </form>

        <p className="text-sm text-paper-dim mt-6">
          New here? <Link to="/register" className="text-brass hover:underline">Register</Link>
        </p>
      </div>
    </div>
  )
}