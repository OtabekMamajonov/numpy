import { useEffect, useState } from 'react'
import { fetchCatalog } from './api'
import ServiceDetail from './ServiceDetail'

const tg = window.Telegram?.WebApp

export default function App() {
  const [catalog, setCatalog] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [activeCategory, setActiveCategory] = useState(null)
  const [selectedService, setSelectedService] = useState(null)

  useEffect(() => {
    fetchCatalog()
      .then((data) => {
        setCatalog(data)
        setActiveCategory(data[0]?.id ?? null)
      })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <div className="state">Yuklanmoqda...</div>
  if (error) return <div className="state error">{error}</div>
  if (catalog.length === 0) return <div className="state">Katalog hozircha bo'sh.</div>

  if (selectedService) {
    return <ServiceDetail service={selectedService} onBack={() => setSelectedService(null)} />
  }

  const current = catalog.find((c) => c.id === activeCategory)

  return (
    <div className="app">
      <header className="header">
        <h1>Qurilish xizmatlari</h1>
        <p className="subtitle">Kerakli xizmatni tanlang va buyurtma bering</p>
      </header>

      <nav className="tabs">
        {catalog.map((category) => (
          <button
            key={category.id}
            className={category.id === activeCategory ? 'tab active' : 'tab'}
            onClick={() => setActiveCategory(category.id)}
          >
            {category.name}
          </button>
        ))}
      </nav>

      <div className="list">
        {current?.services.map((service) => (
          <button key={service.id} className="card" onClick={() => setSelectedService(service)}>
            {service.image_url && <img src={service.image_url} alt={service.name} className="thumb" />}
            <div className="card-body">
              <h3>{service.name}</h3>
              {service.description && <p className="desc">{service.description}</p>}
              <span className="price">{service.price}</span>
            </div>
          </button>
        ))}
        {current?.services.length === 0 && <div className="state">Bu bo'limda xizmatlar yo'q.</div>}
      </div>
    </div>
  )
}
