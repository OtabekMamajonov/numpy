import { useState } from 'react'
import { register } from './api'

const CITIES = [
  'Toshkent',
  'Toshkent viloyati',
  'Samarqand',
  'Buxoro',
  'Andijon',
  "Farg'ona",
  'Namangan',
  'Qashqadaryo',
  'Surxondaryo',
  'Xorazm',
  'Navoiy',
  'Jizzax',
  'Sirdaryo',
  "Qoraqalpog'iston",
]

const tgUser = window.Telegram?.WebApp?.initDataUnsafe?.user

export default function Registration({ onDone }) {
  const [fullName, setFullName] = useState(
    [tgUser?.first_name, tgUser?.last_name].filter(Boolean).join(' ')
  )
  const [phone, setPhone] = useState('+998')
  const [city, setCity] = useState('')
  const [district, setDistrict] = useState('')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState(null)

  const valid =
    fullName.trim().length >= 2 &&
    phone.trim().length >= 5 &&
    city.trim().length >= 2 &&
    district.trim().length >= 2

  const submit = async () => {
    if (!valid || saving) return
    setSaving(true)
    setError(null)
    try {
      const me = await register({
        full_name: fullName.trim(),
        phone: phone.trim(),
        city: city.trim(),
        district: district.trim(),
      })
      onDone(me)
    } catch (e) {
      setError(e.message)
      setSaving(false)
    }
  }

  return (
    <div className="app">
      <header className="header">
        <h1>Xush kelibsiz!</h1>
        <p className="subtitle">
          Buyurtma berishdan oldin qisqa ma'lumotlaringizni qoldiring — usta siz bilan shu
          orqali bog'lanadi.
        </p>
      </header>

      <label className="label">
        Ism va familiya
        <input
          className="input"
          value={fullName}
          onChange={(e) => setFullName(e.target.value)}
          placeholder="Ali Valiyev"
        />
      </label>

      <label className="label">
        Telefon raqam
        <input
          className="input"
          type="tel"
          inputMode="tel"
          value={phone}
          onChange={(e) => setPhone(e.target.value)}
          placeholder="+998901234567"
        />
      </label>

      <label className="label">
        Shahar / viloyat
        <div className="select-wrap">
          <select className="input select" value={city} onChange={(e) => setCity(e.target.value)}>
            <option value="">Tanlang</option>
            {CITIES.map((name) => (
              <option key={name} value={name}>
                {name}
              </option>
            ))}
          </select>
        </div>
      </label>

      <label className="label">
        Tuman / hudud
        <input
          className="input"
          value={district}
          onChange={(e) => setDistrict(e.target.value)}
          placeholder="Chilonzor tumani"
        />
      </label>

      {error && <p className="form-error">{error}</p>}

      <button className="submit" onClick={submit} disabled={!valid || saving}>
        {saving ? 'Saqlanmoqda...' : 'Davom etish'}
      </button>
    </div>
  )
}
