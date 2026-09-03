import { createContext, useContext, useState, useCallback } from 'react'

const AuthContext = createContext(null)

/**
 * Holds the logged-in voter's JWT + basic profile in memory and localStorage.
 * localStorage keeps the session alive across page refreshes — this is a
 * real deployed app (not a sandboxed artifact), so browser storage is fine
 * here, unlike in Claude's in-chat preview environment.
 */
export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('votechain_token'))
  const [voter, setVoter] = useState(() => {
    const stored = localStorage.getItem('votechain_voter')
    return stored ? JSON.parse(stored) : null
  })

  const login = useCallback((authResponse) => {
    // authResponse shape matches AuthResponse.java: { token, voterId, fullName, role }
    localStorage.setItem('votechain_token', authResponse.token)
    localStorage.setItem(
      'votechain_voter',
      JSON.stringify({
        id: authResponse.voterId,
        fullName: authResponse.fullName,
        role: authResponse.role,
      })
    )
    setToken(authResponse.token)
    setVoter({ id: authResponse.voterId, fullName: authResponse.fullName, role: authResponse.role })
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem('votechain_token')
    localStorage.removeItem('votechain_voter')
    setToken(null)
    setVoter(null)
  }, [])

  const isAdmin = voter?.role === 'ADMIN'

  return (
    <AuthContext.Provider value={{ token, voter, isAdmin, login, logout, isAuthenticated: !!token }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider')
  return ctx
}