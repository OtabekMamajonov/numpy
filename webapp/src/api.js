const BASE = import.meta.env.VITE_API_BASE_URL || ''

export async function fetchCategories() {
  const res = await fetch(`${BASE}/api/categories`)
  if (!res.ok) throw new Error('Kategoriyalarni yuklab bo`lmadi')
  return res.json()
}

export async function fetchServices({ categoryId, sort }) {
  const params = new URLSearchParams({ sort })
  if (categoryId != null) params.set('category_id', categoryId)
  const res = await fetch(`${BASE}/api/services?${params}`)
  if (!res.ok) throw new Error('Xizmatlarni yuklab bo`lmadi')
  return res.json()
}
