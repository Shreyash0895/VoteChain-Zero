import { useState } from 'react'
import { useNavigate, useLocation, Link } from 'react-router-dom'
import client, { extractErrorMessage } from '../api/client'
import { useAuth } from '../context/AuthContext'

export default function VerifyOtpPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { login } = useAuth()

  const emailFromNav = location.state?.email || ''
  const mode = location.state?.mode || 'login' // 'register' | 'login' — copy only, both hit the same endpoint

  const [email, setEmail] = useState(emailFromNav)
  const [otp, setOtp] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      // One endpoint, two purposes — see AuthService.verifyOtp() javadoc:
      // if this is the voter's first verification it completes registration
      // AND logs them in; if they were already verified, it's just 2FA.
      const res = await client.post('/api/auth/verify-otp', { email, otp })
      login(res.data)
      navigate('/')
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="max-w-md mx-auto">
      <h1 className="text-3xl font-semibold mb-2">Enter your code</h1>
      <p className="text-paper-dim mb-8">
        {mode === 'register'
          ? 'We emailed a 6-digit code to finish registration.'
          : 'We emailed a 6-digit code to confirm it\u2019s you.'}
      </p>

      <form onSubmit={handleSubmit} className="space-y-5">
        <div>
          <label className="field-label" htmlFor="email">Email</label>
          <input id="email" type="email" className="field-input" required value={email} onChange={(e) => setEmail(e.target.value)} />
        </div>

        <div>
          <label className="field-label" htmlFor="otp">6-digit code</label>
          <input
            id="otp"
            className="field-input font-mono tracking-[0.3em] text-center text-lg"
            required
            maxLength={6}
            inputMode="numeric"
            value={otp}
            onChange={(e) => setOtp(e.target.value.replace(/\D/g, ''))}
          />
        </div>

        {error && <p className="text-signal text-sm">{error}</p>}

        <button type="submit" disabled={loading || otp.length !== 6} className="btn-primary w-full">
          {loading ? 'Verifying…' : 'Verify'}
        </button>
      </form>

      <p className="text-sm text-paper-dim mt-6">
        Wrong email? <Link to="/login" className="text-brass hover:underline">Start over</Link>
      </p>
    </div>
  )
}