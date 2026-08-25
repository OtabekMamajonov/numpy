const BASE = import.meta.env.VITE_API_BASE_URL || ''

/** Telegram imzolagan initData — server shu orqali foydalanuvchini aniqlaydi */
function authHeaders() {
  const initData = window.Telegram?.WebApp?.initData || ''
  return { Authorization: `tma ${initData}` }
}

async function request(path, options = {}) {
  const res = await fetch(`${BASE}${path}`, {
    ...options,
    headers: { ...authHeaders(), ...(options.headers || {}) },
  })
  if (!res.ok) {
    let detail = ''
    try {
      detail = (await res.json()).detail || ''
    } catch {
      /* javob JSON emas */
    }
    throw new Error(detail || `So'rov bajarilmadi (${res.status})`)
  }
  return res.json()
}

export function fetchMe() {
  return request('/api/me')
}

export function register(data) {
  return request('/api/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })
}

export function fetchCategories() {
  return request('/api/categories')
}

export function fetchServices({ categoryId, sort }) {
  const params = new URLSearchParams({ sort })
  if (categoryId != null) params.set('category_id', categoryId)
  return request(`/api/services?${params}`)
}
