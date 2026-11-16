const API_BASE = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/+$/, '')
const SESSION_KEY = 'gradox.session'
const SESSION_EVENT = 'gradox:session'

export class ApiError extends Error {
  constructor(message, { status = 0, code = 'UNKNOWN' } = {}) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.code = code
  }
}

export const sessionStore = {
  read() {
    try {
      const raw = window.localStorage.getItem(SESSION_KEY)
      return raw ? JSON.parse(raw) : null
    } catch {
      return null
    }
  },
  write(session) {
    if (!session?.token) {
      this.clear()
      return
    }
    window.localStorage.setItem(SESSION_KEY, JSON.stringify(session))
    window.dispatchEvent(new CustomEvent(SESSION_EVENT, { detail: session }))
  },
  clear() {
    window.localStorage.removeItem(SESSION_KEY)
    window.dispatchEvent(new CustomEvent(SESSION_EVENT, { detail: null }))
  },
}

export const onSessionChange = (handler) => {
  const listener = (event) => handler(event.detail)
  window.addEventListener(SESSION_EVENT, listener)
  return () => window.removeEventListener(SESSION_EVENT, listener)
}

let refreshPromise = null

async function refreshSession() {
  const session = sessionStore.read()
  if (!session?.refreshToken) return null

  if (!refreshPromise) {
    refreshPromise = fetch(`${API_BASE}/api/auth/token/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken: session.refreshToken }),
    })
      .then(async (response) => {
        if (!response.ok) throw new Error('No se pudo renovar la sesión')
        const data = await response.json()
        const next = {
          token: data.token,
          refreshToken: data.refreshToken,
          username: data.username,
          role: data.role,
        }
        sessionStore.write(next)
        return next
      })
      .catch(() => {
        sessionStore.clear()
        return null
      })
      .finally(() => {
        refreshPromise = null
      })
  }

  return refreshPromise
}

async function parseResponse(response) {
  if (response.status === 204) return null
  const contentType = response.headers.get('content-type') || ''
  if (contentType.includes('application/json')) {
    return response.json()
  }
  const text = await response.text()
  return text || null
}

function messageFromPayload(payload, fallback) {
  if (typeof payload === 'string' && payload.trim()) return payload
  return payload?.errorMessage || payload?.message || fallback
}

async function request(
  path,
  { method = 'GET', body, form = false, auth = true, retry = true, signal } = {},
) {
  const session = sessionStore.read()
  const headers = { Accept: 'application/json' }

  if (auth && session?.token) {
    headers.Authorization = `Bearer ${session.token}`
  }

  let requestBody
  if (body !== undefined && body !== null) {
    if (form) {
      requestBody = body
    } else {
      headers['Content-Type'] = 'application/json'
      requestBody = JSON.stringify(body)
    }
  }

  let response
  try {
    response = await fetch(`${API_BASE}/${path.replace(/^\/+/, '')}`, {
      method,
      headers,
      body: requestBody,
      signal,
    })
  } catch (error) {
    if (error.name === 'AbortError') throw error
    throw new ApiError(
      'No se pudo conectar con Gradox. Comprueba que la API esté disponible e inténtalo de nuevo.',
      { code: 'NETWORK_ERROR' },
    )
  }

  if (response.status === 401 && auth && retry) {
    const refreshed = await refreshSession()
    if (refreshed) {
      return request(path, { method, body, form, auth, retry: false, signal })
    }
  }

  const payload = await parseResponse(response)

  if (!response.ok) {
    const fallback =
      response.status === 401
        ? 'Tu sesión ha caducado. Inicia sesión de nuevo.'
        : response.status === 403
          ? 'No tienes permisos para realizar esta acción.'
          : response.status === 429
            ? 'Demasiados intentos. Espera un momento antes de volver a probar.'
            : 'La operación no se pudo completar.'
    throw new ApiError(messageFromPayload(payload, fallback), {
      status: response.status,
      code: payload?.errorCode || `HTTP_${response.status}`,
    })
  }

  return payload
}

const qs = (params = {}) => {
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      search.set(key, String(value))
    }
  })
  const value = search.toString()
  return value ? `?${value}` : ''
}

export const api = {
  auth: {
    login: (payload) =>
      request('api/auth/login', { method: 'POST', body: payload, auth: false }),
    register: (payload) =>
      request('api/auth/register', { method: 'POST', body: payload, auth: false }),
    verify: (token) =>
      request(`api/auth/verify${qs({ token })}`, { auth: false }),
    logout: (refreshToken) =>
      request('api/auth/logout', { method: 'POST', body: { refreshToken }, auth: false }),
    requestPasswordReset: (email) =>
      request('api/auth/password/reset-request', {
        method: 'POST',
        body: { email },
        auth: false,
      }),
    resetPassword: (payload) =>
      request('api/auth/password/reset', {
        method: 'POST',
        body: payload,
        auth: false,
      }),
  },

  users: {
    me: () => request('users/me'),
    updateMe: (payload) => request('users/me', { method: 'PUT', body: payload }),
    uploadPhoto: (file) => {
      const data = new FormData()
      data.append('file', file)
      return request('users/me/profile-picture', { method: 'PUT', body: data, form: true })
    },
    deletePhoto: () => request('users/me/profile-picture', { method: 'DELETE' }),
    publicProfile: (id) => request(`users/${id}`),
  },

  subjects: {
    list: () => request('subjects'),
    get: (id) => request(`subjects/${id}`),
  },

  courses: {
    list: () => request('courses'),
    get: (id) => request(`courses/${id}`),
  },

  files: {
    list: (signal) => request('files/all', { signal }),
    get: (id, signal) => request(`files/${id}`, { signal }),
    download: (id) => request(`files/${id}/download`),
    vote: (id, upvote) =>
      request(`files/${id}/vote/${upvote}`, { method: 'POST' }),
    retractVote: (id) => request(`files/${id}/vote`, { method: 'DELETE' }),
    requestDeletion: (id) => request(`files/${id}`, { method: 'DELETE' }),
    updateVisibility: (id, visibilityLevel) =>
      request(`files/${id}/visibility`, { method: 'PUT', body: visibilityLevel }),
  },

  forum: {
    list: (fileId, page = 0, size = 20) =>
      request(`files/${fileId}/comments${qs({ page, size })}`),
    create: (fileId, payload) =>
      request(`files/${fileId}/comments`, { method: 'POST', body: payload }),
    edit: (fileId, commentId, content) =>
      request(`files/${fileId}/comments/${commentId}`, {
        method: 'PUT',
        body: { content },
      }),
    remove: (fileId, commentId) =>
      request(`files/${fileId}/comments/${commentId}`, { method: 'DELETE' }),
    lock: (fileId) =>
      request(`files/${fileId}/thread/lock`, { method: 'POST' }),
    unlock: (fileId) =>
      request(`files/${fileId}/thread/lock`, { method: 'DELETE' }),
  },

  proposals: {
    list: () => request('uploadProposal'),
    get: (id) => request(`uploadProposal/${id}`),
    create: (formData) =>
      request('uploadProposal/upload', { method: 'POST', body: formData, form: true }),
    remove: (id) => request(`uploadProposal/${id}`, { method: 'DELETE' }),
    download: (id) => request(`uploadProposal/${id}/download`),
  },

  votes: {
    mine: (proposalId) => request(`vote/${proposalId}`),
    results: (proposalId) => request(`vote/${proposalId}/results`),
    vote: (proposalId, upvote) =>
      request(`vote/${proposalId}/${upvote}`, { method: 'POST' }),
    retract: (proposalId) => request(`vote/${proposalId}`, { method: 'DELETE' }),
  },

  promotions: {
    request: () => request('promoteProposal/request', { method: 'POST' }),
    cancel: () => request('promoteProposal/delete', { method: 'DELETE' }),
    pending: () => request('promoteProposal/pending'),
    get: (id) => request(`promoteProposal/${id}`),
    demote: (userId) => request(`promoteProposal/demote/${userId}`, { method: 'POST' }),
  },

  stats: {
    get: (signal) => request('stats', { signal }),
  },

  badges: {
    list: () => request('badges'),
  },

  config: {
    get: () => request('vote-config'),
    propose: (payload) => request('vote-config', { method: 'PUT', body: payload }),
  },

  admin: {
    proposals: (page = 0, size = 20) =>
      request(`admin/proposals${qs({ page, size })}`),
    banned: (page = 0, size = 20) =>
      request(`admin/users/banned${qs({ page, size })}`),
    ban: (userId, reason) =>
      request(`admin/users/${userId}/ban${qs({ reason })}`, { method: 'PUT' }),
    unban: (userId) =>
      request(`admin/users/${userId}/unban`, { method: 'PUT' }),
    audit: (params = {}) => request(`admin/audit${qs(params)}`),
  },
}

export { API_BASE }
