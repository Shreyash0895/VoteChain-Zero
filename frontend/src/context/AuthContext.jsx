import { createContext, useContext, useState, useCallback } from 'react'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('votechain_token'))
  const [voter, setVoter] = useState(() => {
    const stored = localStorage.getItem('votechain_voter')
    return stored ? JSON.parse(stored) : null
  })

  const login = useCallback((authResponse) => {
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