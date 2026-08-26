import { useState } from 'react'
import { placeOrder } from './api'
import { ArrowLeftIcon, CheckIcon, ToolIcon } from './icons'

const tg = window.Telegram?.WebApp

export default function ServiceDetail({ service, onBack }) {
  const [comment, setComment] = useState('')
  const [address, setAddress] = useState('')
  const [sending, setSending] = useState(false)
  const [placed, setPlaced] = useState(null)
  const [error, setError] = useState(null)

  const submit = async () => {
    if (sending) return
    setSending(true)
    setError(null)
    try {
      const order = await placeOrder({
        serviceId: service.id,
        comment: comment.trim() || null,
        address: address.trim() || null,
      })
      setPlaced(order)
      tg?.HapticFeedback?.notificationOccurred?.('success')
    } catch (e) {
      setError(e.message)
      setSending(false)
    }
  }

  if (placed) {
    return (
      <div className="app success">
        <div className="success-badge">
          <CheckIcon />
        </div>
        <h2 className="detail-title">Buyurtma qabul qilindi</h2>
        <p className="detail-desc">
          Raqami: <b>#{placed.id}</b>
          <br />
          {placed.service_name} — {placed.price}
          <br />
          <br />
          Tez orada operatorimiz siz bilan bog'lanadi. Buyurtma holatini botdagi
          «📋 Mening buyurtmalarim» bo'limidan kuzatib borasiz.
        </p>
        <button className="submit" onClick={() => (tg?.close ? tg.close() : onBack())}>
          Yopish
        </button>
        <button className="link-button" onClick={onBack}>
          Katalogga qaytish
        </button>
      </div>
    )
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

      {error && <p className="form-error">{error}</p>}

      <button className="submit" onClick={submit} disabled={sending}>
        {sending ? 'Yuborilmoqda...' : 'Buyurtma berish'}
      </button>
    </div>
  )
}
