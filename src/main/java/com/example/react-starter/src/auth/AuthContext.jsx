import { useEffect, useState } from 'react'
import { AuthContext } from './authContext.js'
import { api, clearTokens, getRefreshToken, setSessionExpiredHandler, setTokens } from '../api/client.js'

const homeByRole = {
  ETUDIANT: '/app/etudiant',
  ADMIN_ETABLISSEMENT: '/app/etablissement',
  ADMIN_PLATEFORME: '/app/plateforme',
}
let bootstrapRequest = null

function toSession(data, profile) {
  const authorities = data.authorities || []
  const role = authorities.map((authority) => authority.replace(/^ROLE_/, '')).find((authority) => authority in homeByRole)
  if (!role) throw new Error('Le compte ne possède aucun rôle reconnu.')
  const tenantId = profile.tenantId || data.tenantId
  const displayName = [profile.prenom, profile.nom].filter(Boolean).join(' ').trim()
  return {
    username: data.username,
    displayName: displayName || data.username,
    tenantId,
    tenantLabel: tenantId?.replace(/^tenant-/i, '').toUpperCase(),
    role,
  }
}

async function loadCurrentSession() {
  const [{ data: session }, { data: profile }] = await Promise.all([
    api.get('/api/me'),
    api.get('/api/profil'),
  ])
  return toSession(session, profile)
}

async function restoreSession() {
  if (!bootstrapRequest) {
    bootstrapRequest = (async () => {
      const storedRefreshToken = getRefreshToken()
      if (!storedRefreshToken) return null
      const { data: tokens } = await api.post('/api/auth/refresh', { refreshToken: storedRefreshToken })
      setTokens(tokens)
      return loadCurrentSession()
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
      const currentUser = await loadCurrentSession()
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
