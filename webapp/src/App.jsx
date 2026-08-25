import { useEffect, useState } from 'react'
import { fetchCategories, fetchServices } from './api'
import ServiceDetail from './ServiceDetail'
import { ToolIcon } from './icons'

const SORTS = [
  { key: 'popular', label: 'Ommabop' },
  { key: 'expensive', label: 'Qimmat' },
  { key: 'cheap', label: 'Arzon' },
  { key: 'new', label: 'Yangi' },
]

export default function App() {
  const [categories, setCategories] = useState([])
  const [services, setServices] = useState([])
  const [activeCategory, setActiveCategory] = useState(null)
  const [sort, setSort] = useState('popular')
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState(null)
  const [selectedService, setSelectedService] = useState(null)

  useEffect(() => {
    fetchCategories()
      .then(setCategories)
      .catch((e) => setError(e.message))
  }, [])

  useEffect(() => {
    let cancelled = false
    setBusy(true)
    fetchServices({ categoryId: activeCategory, sort })
      .then((data) => {
        if (!cancelled) setServices(data)
      })
      .catch((e) => {
        if (!cancelled) setError(e.message)
      })
      .finally(() => {
        if (!cancelled) {
          setBusy(false)
          setLoading(false)
        }
      })
    return () => {
      cancelled = true
    }
  }, [activeCategory, sort])

  if (loading) return <div className="state">Yuklanmoqda...</div>
  if (error) return <div className="state error">{error}</div>

  if (selectedService) {
    return <ServiceDetail service={selectedService} onBack={() => setSelectedService(null)} />
  }

  return (
    <div className="app">
      <header className="header">
        <h1>Qurilish xizmatlari</h1>
        <p className="subtitle">Kerakli xizmatni tanlang va buyurtma bering</p>
      </header>

      <nav className="tabs">
        <button
          className={activeCategory === null ? 'tab active' : 'tab'}
          onClick={() => setActiveCategory(null)}
        >
          Hammasi
        </button>
        {categories.map((category) => (
          <button
            key={category.id}
            className={category.id === activeCategory ? 'tab active' : 'tab'}
            onClick={() => setActiveCategory(category.id)}
          >
            {category.name}
          </button>
        ))}
      </nav>

      <nav className="tabs sorts">
        {SORTS.map((option) => (
          <button
            key={option.key}
            className={option.key === sort ? 'sort active' : 'sort'}
            onClick={() => setSort(option.key)}
          >
            {option.label}
          </button>
        ))}
      </nav>

      <div className={busy ? 'list busy' : 'list'}>
        {services.map((service) => (
          <button key={service.id} className="card" onClick={() => setSelectedService(service)}>
            {service.image_url ? (
              <img src={service.image_url} alt={service.name} className="thumb" />
            ) : (
              <div className="thumb thumb-placeholder">
                <ToolIcon />
              </div>
            )}
            <div className="card-body">
              <h3>{service.name}</h3>
              {service.description && <p className="desc">{service.description}</p>}
              <span className="price">{service.price}</span>
            </div>
          </button>
        ))}
        {services.length === 0 && <div className="state">Bu bo'limda xizmatlar yo'q.</div>}
      </div>
    </div>
  )
}
