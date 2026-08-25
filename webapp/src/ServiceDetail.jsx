import { useState } from 'react'
import { ArrowLeftIcon, ToolIcon } from './icons'

const tg = window.Telegram?.WebApp

export default function ServiceDetail({ service, onBack }) {
  const [comment, setComment] = useState('')
  const [address, setAddress] = useState('')

  const submit = () => {
    const payload = JSON.stringify({
      service_id: service.id,
      comment: comment.trim() || null,
      address: address.trim() || null,
    })
    if (tg?.sendData) {
      tg.sendData(payload)
    } else {
      alert('Bu sahifani Telegram ilovasi ichida oching.')
    }
  }

  return (
    <div className="app">
      <button className="back" onClick={onBack} aria-label="Orqaga">
        <ArrowLeftIcon />
      </button>

      {service.image_url ? (
        <img src={service.image_url} alt={service.name} className="hero" />
      ) : (
        <div className="hero hero-placeholder">
          <ToolIcon size={44} />
        </div>
      )}

      <h2 className="detail-title">{service.name}</h2>
      <span className="price-tag">{service.price}</span>
      {service.description && <p className="detail-desc">{service.description}</p>}

      <label className="label">
        Manzil (ixtiyoriy)
        <input
          className="input"
          value={address}
          onChange={(e) => setAddress(e.target.value)}
          placeholder="Ko'cha, uy raqami"
        />
      </label>

      <label className="label">
        Izoh (ixtiyoriy)
        <textarea
          className="input"
          rows={3}
          value={comment}
          onChange={(e) => setComment(e.target.value)}
          placeholder="Ish hajmi, qo'shimcha talablar..."
        />
      </label>

      <button className="submit" onClick={submit}>
        Buyurtma berish
      </button>
    </div>
  )
}
