import { useEffect, useState } from 'react'
import { AuthContext } from './authContext.js'
import { api, clearTokens, getRefreshToken, setSessionExpiredHandler, setTokens } from '../api/client.js'

const homeByRole = {
  ETUDIANT: '/app/etudiant',
  ADMIN_ETABLISSEMENT: '/app/etablissement',
  ADMIN_PLATEFORME: '/app/plateforme',
}
let bootstrapRequest = null

function toSession(data) {
  const authorities = data.authorities || []
  const role = authorities.map((authority) => authority.replace(/^ROLE_/, '')).find((authority) => authority in homeByRole)
  if (!role) throw new Error('Le compte ne possède aucun rôle reconnu.')
  return { username: data.username, tenantId: data.tenantId, role }
}

async function restoreSession() {
  if (!bootstrapRequest) {
    bootstrapRequest = (async () => {
      const storedRefreshToken = getRefreshToken()
      if (!storedRefreshToken) return null
      const { data: tokens } = await api.post('/api/auth/refresh', { refreshToken: storedRefreshToken })
      setTokens(tokens)
      const { data: session } = await api.get('/api/me')
      return toSession(session)
    })().finally(() => {
      bootstrapRequest = null
    })
  }
  return bootstrapRequest
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let active = true
    setSessionExpiredHandler(() => {
      if (active) setUser(null)
    })

    restoreSession()
      .then((session) => {
        if (active) setUser(session)
      })
      .catch(() => {
        clearTokens()
        if (active) setUser(null)
      })
      .finally(() => {
        if (active) setLoading(false)
      })

    return () => {
      active = false
      setSessionExpiredHandler(null)
    }
  }, [])

  async function login(username, password) {
    const { data: tokens } = await api.post('/api/auth/login', { username, password })
    setTokens(tokens)
    try {
      const { data: session } = await api.get('/api/me')
      const currentUser = toSession(session)
      setUser(currentUser)
      return currentUser
    } catch (error) {
      clearTokens()
      throw error
    }
  }

  async function logout() {
    const currentRefreshToken = getRefreshToken()
    try {
      if (currentRefreshToken) await api.post('/api/auth/logout', { refreshToken: currentRefreshToken })
    } finally {
      clearTokens()
      setUser(null)
    }
  }

  return <AuthContext.Provider value={{ user, loading, login, logout, homeByRole }}>{children}</AuthContext.Provider>
}
