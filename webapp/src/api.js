const BASE = import.meta.env.VITE_API_BASE_URL || ''

export async function fetchCatalog() {
  const res = await fetch(`${BASE}/api/catalog`)
  if (!res.ok) throw new Error('Katalogni yuklab bo`lmadi')
  return res.json()
}
