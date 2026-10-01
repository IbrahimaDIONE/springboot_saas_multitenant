import axios from 'axios'

const baseURL = import.meta.env.VITE_API_BASE_URL || ''
const refreshStorageKey = 'bibliouniv.refreshToken'
let accessToken = null
let refreshToken = sessionStorage.getItem(refreshStorageKey)
let refreshRequest = null
let onSessionExpired = () => {}

export const api = axios.create({ baseURL })

export function setTokens(tokens) {
  accessToken = tokens.accessToken
  refreshToken = tokens.refreshToken
  sessionStorage.setItem(refreshStorageKey, refreshToken)
}

export function clearTokens() {
  accessToken = null
  refreshToken = null
  sessionStorage.removeItem(refreshStorageKey)
}

export function getRefreshToken() {
  return refreshToken
}

export function setSessionExpiredHandler(handler) {
  onSessionExpired = handler || (() => {})
}

async function renewTokens() {
  if (!refreshRequest) {
    refreshRequest = axios.post(`${baseURL}/api/auth/refresh`, { refreshToken })
      .then(({ data }) => {
        setTokens(data)
        return data
      })
      .finally(() => {
        refreshRequest = null
      })
  }
  return refreshRequest
}

api.interceptors.request.use((config) => {
  if (accessToken) config.headers.Authorization = `Bearer ${accessToken}`
  return config
})

api.interceptors.response.use((response) => response, async (error) => {
  const request = error.config
  const status = error.response?.status
  const isAuthRequest = request?.url?.includes('/api/auth/')

  if (status === 403 && typeof window !== 'undefined') {
    window.dispatchEvent(new Event('bibliouniv:forbidden'))
  }

  if (status !== 401 || !request || isAuthRequest) return Promise.reject(error)

  if (request._retried) {
    clearTokens()
    onSessionExpired()
    return Promise.reject(error)
  }

  if (!refreshToken) {
    clearTokens()
    onSessionExpired()
    return Promise.reject(error)
  }

  request._retried = true
  try {
    const tokens = await renewTokens()
    request.headers.Authorization = `Bearer ${tokens.accessToken}`
    return api(request)
  } catch (refreshError) {
    clearTokens()
    onSessionExpired()
    return Promise.reject(refreshError)
  }
})
